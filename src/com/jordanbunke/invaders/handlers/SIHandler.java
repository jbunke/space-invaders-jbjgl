package com.jordanbunke.invaders.handlers;

import com.jordanbunke.invaders.io.Settings;
import com.jordanbunke.invaders.logic.entity.EntityFactory;
import com.jordanbunke.invaders.logic.entity.EntityUtils;
import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.invaders.logic.components.*;
import com.jordanbunke.invaders.logic.systems.Collision;
import com.jordanbunke.invaders.math.SIMath;
import com.jordanbunke.invaders.menus.Menus;
import com.jordanbunke.jbjgl.events.EventHandler;
import com.jordanbunke.jbjgl.events.GameEvent;
import com.jordanbunke.jbjgl.events.GameKeyEvent;
import com.jordanbunke.jbjgl.game.LogicHandler;
import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.GameEntity;
import com.jordanbunke.jbjgl.io.InputEventLogger;

import java.util.*;
import java.util.stream.Collectors;

public final class SIHandler implements LogicHandler, EventHandler {
    private static final SIHandler INSTANCE = new SIHandler();

    // fields
    private GameEntity<Vector2D> player;
    private final Set<GameEntity<Vector2D>> entities;
    private Set<GameEntity<Vector2D>> toAdd;

    private int direction, tickCounter, tickCycle, score, highScore, lives, wave;
    private boolean paused;
    private State state;

    // enums
    public enum PlayerAction {
        PAUSE_PLAY, SHOOT, MOVE_LEFT, MOVE_RIGHT, STOP_MOVING_LEFT, STOP_MOVING_RIGHT
    }

    public enum State {
        PLAYING(true),
        LIFE_LOST(true),
        GAME_OVER(false),
        BETWEEN_WAVES(false),
        BEAT_THE_GAME(false),
        GAME_START_UP(false),
        NOT_STARTED(false);

        public final boolean canBePaused;

        State(final boolean canBePaused) {
            this.canBePaused = canBePaused;
        }
    }

    // constructor
    private SIHandler() {
        highScore = Settings.getSavedHighScore();

        state = State.GAME_START_UP;
        entities = new HashSet<>();
    }

    public static SIHandler get() {
        return INSTANCE;
    }

    @Override
    public void process(final InputEventLogger eventLogger) {
        Menus.resolve().process(eventLogger);

        final List<GameEvent> events = eventLogger.getUnprocessedEvents();

        for (GameEvent event : events) {
            if (event instanceof GameKeyEvent keyEvent) {
                if (keyEvent.action == GameKeyEvent.Action.PRESS) {
                    switch (keyEvent.key) {
                        case ESCAPE -> processAction(PlayerAction.PAUSE_PLAY);
                        case SPACE -> processAction(PlayerAction.SHOOT);
                        case A, LEFT_ARROW -> processAction(PlayerAction.MOVE_LEFT);
                        case D, RIGHT_ARROW -> processAction(PlayerAction.MOVE_RIGHT);
                    }
                } else if (keyEvent.action == GameKeyEvent.Action.RELEASE) {
                    switch (keyEvent.key) {
                        case A, LEFT_ARROW -> processAction(PlayerAction.STOP_MOVING_LEFT);
                        case D, RIGHT_ARROW -> processAction(PlayerAction.STOP_MOVING_RIGHT);
                    }
                }
            }
        }
    }

    @Override
    public void update(final double deltaTime) {
        Menus.resolve().update(deltaTime);

        if (!isPaused()) {
            switch (state) {
                case PLAYING -> {
                    updateTickCounter();
                    trySpawnUFO();
                    updateEntities(deltaTime);
                    checkCollision();
                    checkDirectionChange();
                    cleanUp();
                    checkEndgame();
                }
                case BETWEEN_WAVES -> updateBetweenLevels();
                case LIFE_LOST -> updateImpendingPlayerRespawn();
                default -> {}
            }
        }
    }

    /* STATE CHANGE FUNCTIONS */
    private void start() {
        if (wave >= GameConstants.WAVES) {
            beatTheGame();
            return;
        }

        direction = GameConstants.RIGHT;
        tickCounter = 0;
        tickCycle = GameConstants.INITIAL_TICK_CYCLE;

        paused = false;

        playerSpawn(false);

        addEnemies();

        state = State.PLAYING;
    }

    private void reset() {
        state = State.NOT_STARTED;

        removeEverythingExceptBunkers();
    }

    public void restartGame() {
        paused = false;
        entities.removeAll(new HashSet<>(entities));

        tickCounter = 0;
        state = State.GAME_START_UP;
    }

    private void startGame() {
        addBunkers();

        score = 0;
        wave = 0;
        lives = 3;

        start();
    }

    private void lostLife() {
        lives--;

        if (lives <= 0)
            gameOver();
        else
            prepPlayerRespawn();
    }

    private void playerRespawn() {
        playerSpawn(true);

        state = State.PLAYING;
    }

    private void beatTheGame() {
        Menus.regenerate();
        state = State.BEAT_THE_GAME;
    }

    private void gameOver() {
        Menus.regenerate();
        state = State.GAME_OVER;
    }

    private void levelComplete() {
        lives++;
        wave++;

        reset();
        prepNextLevel();
    }

    private void prepNextLevel() {
        state = State.BETWEEN_WAVES;

        tickCounter = 0;
    }

    private void prepPlayerRespawn() {
        state = State.LIFE_LOST;

        // remove active projectiles
        final Set<GameEntity<Vector2D>> toRemove = new HashSet<>();
        entities.stream().filter(x -> x.hasComponent(ProjectileComponent.class)).forEach(toRemove::add);
        entities.removeAll(toRemove);

        tickCounter = 0;
    }

    /* ENTITY SPAWNERS */
    private void playerSpawn(final boolean remove) {
        if (remove)
            entities.remove(player);

        player = EntityFactory.createPlayer();
        entities.add(player);
    }

    private void addEnemies() {
        final EnemyLogicComponent.Type[] ROWS = new EnemyLogicComponent.Type[] {
                EnemyLogicComponent.Type.SQUID,
                EnemyLogicComponent.Type.CRAB,
                EnemyLogicComponent.Type.CRAB,
                EnemyLogicComponent.Type.OCTOPUS,
                EnemyLogicComponent.Type.OCTOPUS
        };

        final int columns = GameConstants.ENEMY_COLUMNS +
                Math.min(wave / 2, GameConstants.MAX_ADDITIONAL_COLUMNS);

        for (int r = 0; r < ROWS.length; r++) {
            for (int c = 0; c < columns; c++) {
                final int x = GameConstants.ENEMY_MIDDLE_X + ((c - (columns / 2)) * GameConstants.ENEMY_SPACE_BETWEEN_X),
                        y = GameConstants.ENEMY_TOP_Y + (r * GameConstants.ENEMY_SPACE_BETWEEN_Y);
                final GameEntity<Vector2D> enemy = EntityFactory.createEnemy(new Vector2D(x, y), ROWS[r]);
                entities.add(enemy);
            }
        }
    }

    private void addBunkers() {
        for (int b = 0; b < GameConstants.NUM_BUNKERS; b++) {
            final int bunkerX = (int)(GameConstants.GW_WIDTH * ((b + 1) / (double)(GameConstants.NUM_BUNKERS + 1)));
            final int MIDDLE_X = 2;

            for (int x = 0; x < GameConstants.NUM_BUNKER_PARTS_X; x++) {
                for (int y = 0; y < GameConstants.NUM_BUNKER_PARTS_Y; y++) {
                    final BunkerPartComponent.Type type = getBunkerPartTypeIDFromCoordinate(x, y);

                    if (type == null)
                        continue;

                    final int xOffset = (x - MIDDLE_X) * GameConstants.BUNKER_PART_WIDTH,
                            yOffset = y * GameConstants.BUNKER_PART_DEPTH;
                    final GameEntity<Vector2D> bunkerPart = EntityFactory
                            .createBunkerPart(new Vector2D(bunkerX + xOffset,
                                    GameConstants.BUNKER_TOP + yOffset), type);
                    entities.add(bunkerPart);
                }
            }
        }
    }

    private BunkerPartComponent.Type getBunkerPartTypeIDFromCoordinate(final int x, final int y) {
        if (x == 0 && y == 0)
            return BunkerPartComponent.Type.BUNKER_TOP_LEFT;
        else if (x == GameConstants.NUM_BUNKER_PARTS_X - 1 && y == 0)
            return BunkerPartComponent.Type.BUNKER_TOP_RIGHT;
        else if (x == 1 && y == GameConstants.NUM_BUNKER_PARTS_Y - 2)
            return BunkerPartComponent.Type.BUNKER_UNDER_LEFT;
        else if (x == GameConstants.NUM_BUNKER_PARTS_X - 2 && y == GameConstants.NUM_BUNKER_PARTS_Y - 2)
            return BunkerPartComponent.Type.BUNKER_UNDER_RIGHT;
        else if (x > 0 && x < GameConstants.NUM_BUNKER_PARTS_X - 1 && y == GameConstants.NUM_BUNKER_PARTS_Y - 1)
            return null;

        return BunkerPartComponent.Type.BUNKER_FULL;
    }

    private void removeEverythingExceptBunkers() {
        final Set<GameEntity<Vector2D>> toRemove = new HashSet<>();

        entities.stream().filter(x -> !x.hasComponent(BunkerPartComponent.class)).forEach(toRemove::add);

        entities.removeAll(toRemove);
    }

    private void processAction(final PlayerAction action) {
        final PlayerControllerComponent controller = player == null
                ? null : player.getComponent(PlayerControllerComponent.class);
        final boolean playerActionsValid = controller != null && state == State.PLAYING;

        if (action == null)
            return;

        if (action == PlayerAction.PAUSE_PLAY && state.canBePaused)
            pausePlay();
        else if (playerActionsValid) {
            switch (action) {
                case SHOOT -> controller.attemptShot();
                case MOVE_LEFT -> controller.setMovingLeft(true);
                case MOVE_RIGHT -> controller.setMovingRight(true);
                case STOP_MOVING_LEFT -> controller.setMovingLeft(false);
                case STOP_MOVING_RIGHT -> controller.setMovingRight(false);
            }
        }
        else if (action == PlayerAction.SHOOT) {
            switch (state) {
                case GAME_OVER, BEAT_THE_GAME -> restartGame();
                case GAME_START_UP -> startGame();
            }
        }
    }

    public void pausePlay() {
        paused = !paused;
    }

    /* UPDATE LOOP HELPERS */
    private void updateTickCounter() {
        tickCounter++;

        if (tickCounter >= tickCycle)
            tickCounter = 0;
    }

    private void trySpawnUFO() {
        if (tickCounter == 0 && entities.stream()
                .filter(x -> x.hasComponent(UFOLogicComponent.class))
                .collect(Collectors.toSet()).isEmpty()) {
            if (SIMath.prob(GameConstants.UFO_SPAWN_PROBABILITY))
                entities.add(EntityFactory.createUFO());
        }
    }

    private void updateEntities(final double deltaTime) {
        toAdd = new HashSet<>();
        entities.forEach(x -> x.update(deltaTime));
        entities.addAll(toAdd);
    }

    private void checkCollision() {
        final Set<GameEntity<Vector2D>> targets = new HashSet<>();
        final Set<GameEntity<Vector2D>> projectiles = new HashSet<>();

        entities.stream().filter(x -> {
            final DestructibleComponent dc = x.getComponent(DestructibleComponent.class);
            return dc!= null && !dc.isDestroyed();
        }).forEach(targets::add);
        entities.stream().filter(x -> {
            final ProjectileComponent pc = x.getComponent(ProjectileComponent.class);
            return pc != null && !pc.isSpent();
        }).forEach(projectiles::add);

        for (GameEntity<Vector2D> projectile : projectiles)
            for (GameEntity<Vector2D> target : targets)
                if (Collision.checkCollision(target, projectile)) {
                    final HasPointsComponent hpc = target.getComponent(HasPointsComponent.class);
                    if (hpc != null) {
                        score += hpc.points;
                        highScore = Math.max(score, highScore);
                    }
                }
    }

    private void checkDirectionChange() {
        final boolean changeDirection;
        if (direction == GameConstants.LEFT) {
            final Optional<GameEntity<Vector2D>> leftest = entities.stream()
                    .filter(x -> EntityUtils.isEnemy(x, false))
                    .min(Comparator.comparing(x -> x.getPosition().x));

            changeDirection = leftest.isPresent() &&
                    leftest.get().getPosition().x < GameConstants.GW_BORDER;
        } else {
            final Optional<GameEntity<Vector2D>> rightest = entities.stream()
                    .filter(x -> EntityUtils.isEnemy(x, false))
                    .max(Comparator.comparing(x -> x.getPosition().x));

            changeDirection = rightest.isPresent() &&
                    rightest.get().getPosition().x > GameConstants.GW_WIDTH - GameConstants.GW_BORDER;
        }

        if (changeDirection) {
            direction = -direction;
            entities.stream().filter(EntityUtils::isEnemy)
                    .forEach(x -> x.move(new Vector2D(0, GameConstants.DOWN_STEP_SIZE)));
            if (tickCycle > GameConstants.MIN_TICK_CYCLE)
                tickCycle -= GameConstants.TICK_CYCLE_DECREMENT;
        }
    }

    // Remove spent projectiles and dead/destroyed enemies
    private void cleanUp() {
        final Set<GameEntity<Vector2D>> toRemove = new HashSet<>(), toAdd = new HashSet<>();

        // remove destroyed bunker parts
        entities.stream().filter(EntityUtils::isDestroyedBunkerPart).forEach(toRemove::add);

        // remove spent projectiles or projectiles out of range
        entities.stream().filter(x -> {
            final ProjectileComponent pc = x.getComponent(ProjectileComponent.class);
            return pc!= null && (pc.isSpent() || EntityUtils.outOfBounds(x));
        }).forEach(toRemove::add);

        // remove destroyed enemies and replace them with explosions
        entities.stream().filter(x -> EntityUtils.isEnemy(x, true)).forEach(x -> {
            toAdd.add(EntityFactory.createEffect(x.getPosition(),
                    "explosion", GameConstants.ENEMY_EXPLOSION_LIFESPAN));
            toRemove.add(x);
        });

        // remove destroyed UFOs and replace them with explosions that signal how many points they were worth
        // remove UFOs that escaped the player successfully
        entities.stream().filter(x -> x.hasComponent(UFOLogicComponent.class)).forEach(x -> {
            final UFOLogicComponent ufo = x.getComponent(UFOLogicComponent.class);
            final DestructibleComponent dc = x.getComponent(DestructibleComponent.class);
            final HasPointsComponent hpc = x.getComponent(HasPointsComponent.class);

            if (ufo == null || dc == null || hpc == null)
                return;

            if (dc.isDestroyed()) {
                toAdd.add(EntityFactory.createEffect(x.getPosition(),
                        String.valueOf(hpc.points), GameConstants.UFO_EXPLOSION_LIFESPAN));
                toRemove.add(x);
            } else if (ufo.hasEscaped())
                toRemove.add(x);
        });

        // remove expired effects
        entities.stream().filter(x -> {
            final EffectComponent ec = x.getComponent(EffectComponent.class);
            return ec != null && ec.isExpired();
        }).forEach(toRemove::add);

        entities.removeAll(toRemove);
        entities.addAll(toAdd);
    }

    // endgame checks: game over and level complete
    private void checkEndgame() {
        final Optional<GameEntity<Vector2D>> lowestEnemy = entities.stream()
                .filter(x -> EntityUtils.isEnemy(x, false))
                .max(Comparator.comparingDouble(x -> x.getPosition().y));
        final boolean enemiesReachedBottom = lowestEnemy.isPresent() &&
                lowestEnemy.get().getPosition().y > GameConstants.ENEMY_GAME_OVER_Y_THRESHOLD;

        final DestructibleComponent dc = player.getComponent(DestructibleComponent.class);
        final boolean playerCannonDestroyed = dc != null && dc.isDestroyed();

        if (playerCannonDestroyed) {
            lostLife();
            return;
        }

        if (enemiesReachedBottom) {
            gameOver();
            return;
        }

        // check level complete condition: no remaining enemies that haven't been destroyed
        if (entities.stream().filter(x -> EntityUtils.isEnemy(x, false))
                .collect(Collectors.toSet()).isEmpty())
            levelComplete();
    }

    /* LASER QUEUES */
    public void queueEnemyLaser(final GameEntity<Vector2D> launcher) {
        final Vector2D position = launcher.getPosition().displace(0d,
                GameConstants.PROJECTILE_LAUNCHER_Y_DISTANCE);

        toAdd.add(EntityFactory.createEnemyLaser(position));
    }

    public void queuePlayerLaser() {
        final Vector2D position = player.getPosition().displace(0d,
                -GameConstants.PROJECTILE_LAUNCHER_Y_DISTANCE);

        toAdd.add(EntityFactory.createPlayerLaser(position));
    }


    /* TIMEOUT FUNCTIONS */
    private void updateImpendingPlayerRespawn() {
        tickCounter++;

        if (tickCounter >= GameConstants.TICKS_FOR_RESPAWN)
            playerRespawn();
    }

    private void updateBetweenLevels() {
        tickCounter++;

        if (tickCounter >= GameConstants.TICKS_TO_NEXT_LEVEL)
            start();
    }

    /* LOGICAL GETTERS */
    public int getDirection() {
        return direction;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getWave() {
        return wave;
    }

    /* VISUALIZATION GETTERS */
    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public State getState() {
        return state;
    }

    public Set<GameEntity<Vector2D>> getEntities() {
        return entities;
    }

    public boolean isPaused() {
        return paused && state.canBePaused;
    }
}

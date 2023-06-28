package com.jordanbunke.invaders.handlers;

import com.jordanbunke.invaders.io.AssetFetcher;
import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.invaders.logic.components.*;
import com.jordanbunke.invaders.logic.entity.EntityUtils;
import com.jordanbunke.invaders.menus.Menus;
import com.jordanbunke.invaders.menus.RenderHelper;
import com.jordanbunke.delta_time.debug.GameDebugger;
import com.jordanbunke.delta_time.game.Renderer;
import com.jordanbunke.delta_time.game_world.ecs.GameEntity;
import com.jordanbunke.delta_time.game_world.physics.vector.Vector2D;
import com.jordanbunke.delta_time.image.GameImage;
import com.jordanbunke.delta_time.io.ResourceLoader;
import com.jordanbunke.delta_time.utility.Coord2D;

import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class SIRenderer implements Renderer {
    private static final SIRenderer INSTANCE = new SIRenderer();

    private static final int
            UI_STRIP_HEIGHT = 10, UI_ELEMENT_SPACE_BETWEEN = 2,
            UI_STRIP_Y = GameConstants.GW_DEPTH;
    public static final int
            CANVAS_WIDTH = GameConstants.GW_WIDTH,
            CANVAS_HEIGHT = UI_STRIP_Y + UI_STRIP_HEIGHT,
            WINDOW_SCALE_UP = calculateWindowScaleUp(),
            RENDER_TICK_COUNTER_MAX = 10;

    private enum Sprite {
        BUNKER_FULL, BUNKER_TOP_LEFT, BUNKER_TOP_RIGHT, BUNKER_UNDER_LEFT, BUNKER_UNDER_RIGHT,
        CANNON, CANNON_DESTROYED_1, CANNON_DESTROYED_2,
        CRAB_1, CRAB_2, OCTOPUS_1, OCTOPUS_2, SQUID_1, SQUID_2,
        DAMAGE_1, DAMAGE_2, DAMAGE_3,
        EXPLOSION,
        CROSS_1, CROSS_2, PLAYER_LASER,
        UFO
    }

    private final Map<Sprite, GameImage> spriteMap = new HashMap<>();
    private final Map<GameEntity<Vector2D>, GameImage> effectSpriteMap = new HashMap<>();
    private final Map<GameEntity<Vector2D>, GameImage> bunkerPartSpriteMap = new HashMap<>();
    private final Map<GameEntity<Vector2D>, Integer> bunkerPartDamageStatusMap = new HashMap<>();

    private boolean spriteStateA, spriteStateB;
    private int renderTickCounter;

    private static int calculateWindowScaleUp() {
        final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height,
                APPROX_TOOLBAR_HEIGHT_PIXELS = 50;

        final int roundedDownQuotient = SCREEN_HEIGHT / CANVAS_HEIGHT;

        if (SCREEN_HEIGHT - (CANVAS_HEIGHT * roundedDownQuotient) <
                APPROX_TOOLBAR_HEIGHT_PIXELS && roundedDownQuotient > 1)
            return roundedDownQuotient - 1;
        else
            return roundedDownQuotient;
    }

    private SIRenderer() {
        spriteStateA = false;
        spriteStateB = false;
        renderTickCounter = 0;

        populateSpriteMap();
    }

    public static SIRenderer get() {
        return INSTANCE;
    }

    private void populateSpriteMap() {
        Arrays.stream(Sprite.values()).forEach(x -> spriteMap.put(x, AssetFetcher.sprite(x.name().toLowerCase())));
    }

    @Override
    public void render(final GameImage canvas) {
        renderBackground(canvas);

        final SIHandler.State state = SIHandler.get().getState();

        switch (state) {
            case PLAYING, LIFE_LOST, NOT_STARTED,
                    GAME_OVER, BETWEEN_WAVES, BEAT_THE_GAME -> {
                renderGameWorld(canvas);
                renderUI(canvas);
            }
            case GAME_START_UP -> renderStartUpOverlay(canvas);
        }

        if (state == SIHandler.State.BETWEEN_WAVES)
            renderWaveCompleteOverlay(canvas);

        Menus.resolve().render(canvas);
    }

    /* BACKGROUND */
    private void renderBackground(final GameImage canvas) {
        canvas.fillRectangle(RenderHelper.BLACK, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    /* UI */
    private void renderWaveCompleteOverlay(final GameImage canvas) {
        renderOverlay(canvas, "COMPLETED WAVE " + SIHandler.get().getWave() + " OF " + GameConstants.WAVES);
    }

    private void renderStartUpOverlay(final GameImage canvas) {
        final GameImage logo = ResourceLoader.loadImageResource(Path.of("img", "logo.png"));
        final GameImage points = ResourceLoader.loadImageResource(Path.of("img", "points.png"));

        canvas.draw(logo, (CANVAS_WIDTH - logo.getWidth()) / 2, 10);
        canvas.draw(points, (CANVAS_WIDTH - points.getWidth()) / 2, (int)(CANVAS_HEIGHT * 0.34));

        final String mainMenuText = """
                
                
                
                
                
                
                MOVE - [A/D]              SHOOT - [SPACE]
                Reprogrammed by Flinker Flitzer
                
                """;

        renderOverlay(canvas, mainMenuText + (spriteStateB ? "[SPACE] to start" : " "));
    }

    private void renderOverlay(final GameImage canvas, final String text) {
        final GameImage overlay = RenderHelper.drawText(text);

        final int x = (CANVAS_WIDTH - overlay.getWidth()) / 2,
                y = (CANVAS_HEIGHT - overlay.getHeight()) / 2;

        canvas.draw(overlay, x, y);
    }

    private void renderUI(final GameImage canvas) {
        canvas.fillRectangle(RenderHelper.BLACK, 0, UI_STRIP_Y, CANVAS_WIDTH, UI_STRIP_HEIGHT);
        canvas.fillRectangle(RenderHelper.GREEN, 0, UI_STRIP_Y, CANVAS_WIDTH, 1);

        final GameImage score = RenderHelper.drawText("Score: " + SIHandler.get().getScore()),
                wave = RenderHelper.drawText("Wave: " + (SIHandler.get().getWave() + 1) + "/" + GameConstants.WAVES),
                highScore = RenderHelper.drawText("HS: " + (SIHandler.get().getHighScore())),
                lives = RenderHelper.drawText("Lives: "),
                livesValue = RenderHelper.drawText(" " + SIHandler.get().getLives()),
                playerIcon = spriteMap.get(Sprite.CANNON);

        final int y = UI_STRIP_Y + 1 + UI_ELEMENT_SPACE_BETWEEN;

        canvas.draw(score, UI_ELEMENT_SPACE_BETWEEN, y - AssetFetcher.FONT_OFFSET);
        canvas.draw(highScore, (int)(CANVAS_WIDTH * 0.35) - (highScore.getWidth() / 2), y - AssetFetcher.FONT_OFFSET);
        canvas.draw(wave, (int)(CANVAS_WIDTH * 0.65) - (wave.getWidth() / 2), y - AssetFetcher.FONT_OFFSET);

        int livesX = CANVAS_WIDTH - (UI_ELEMENT_SPACE_BETWEEN + livesValue.getWidth());

        canvas.draw(livesValue, livesX, y - AssetFetcher.FONT_OFFSET);
        livesX -= UI_ELEMENT_SPACE_BETWEEN + playerIcon.getWidth();
        canvas.draw(playerIcon, livesX, y);
        livesX -= UI_ELEMENT_SPACE_BETWEEN + lives.getWidth();
        canvas.draw(lives, livesX, y - AssetFetcher.FONT_OFFSET);
    }

    /* GAME WORLD */
    private void renderGameWorld(final GameImage canvas) {
        final Set<GameEntity<Vector2D>> entities = SIHandler.get().getEntities();

        final Optional<GameEntity<Vector2D>>
                player = entities.stream().filter(
                x -> x.hasComponent(PlayerControllerComponent.class)).findAny(),
                ufo = entities.stream().filter(
                        x -> x.hasComponent(UFOLogicComponent.class)).findAny();
        final Set<GameEntity<Vector2D>>
                enemies = entities.stream().filter(EntityUtils::isEnemy).collect(Collectors.toSet()),
                bunkerParts = entities.stream().filter(
                        x -> x.hasComponent(BunkerPartComponent.class)).collect(Collectors.toSet()),
                projectiles = entities.stream().filter(
                        x -> x.hasComponent(ProjectileComponent.class)).collect(Collectors.toSet()),
                effects = entities.stream().filter(
                        x -> x.hasComponent(EffectComponent.class)).collect(Collectors.toSet());

        // Render order (back to front): projectiles, bunker parts, UFO, enemies, player, effects
        renderProjectiles(canvas, projectiles);
        renderBunkerParts(canvas, bunkerParts);
        ufo.ifPresent(u -> renderUFO(canvas, u));
        renderEnemies(canvas, enemies);
        player.ifPresent(p -> renderPlayer(canvas, p));
        renderEffects(canvas, effects);
    }

    private void renderEffects(final GameImage canvas, final Set<GameEntity<Vector2D>> effects) {
        renderEntityIterator(canvas, effects, this::renderEffect);
    }

    private void renderEnemies(final GameImage canvas, final Set<GameEntity<Vector2D>> enemies) {
        renderEntityIterator(canvas, enemies, this::renderEnemy);
    }

    private void renderBunkerParts(final GameImage canvas, final Set<GameEntity<Vector2D>> bunkerParts) {
        renderEntityIterator(canvas, bunkerParts, this::renderBunkerPart);
    }

    private void renderProjectiles(final GameImage canvas, final Set<GameEntity<Vector2D>> projectiles) {
        renderEntityIterator(canvas, projectiles, this::renderProjectile);
    }

    private void renderEntityIterator(
            final GameImage canvas, final Set<GameEntity<Vector2D>> entities,
            final BiConsumer<GameImage, GameEntity<Vector2D>> f
    ) {
        entities.forEach(x -> f.accept(canvas, x));
    }

    /* INDIVIDUAL ENTITIES */
    private void renderPlayer(final GameImage canvas, final GameEntity<Vector2D> player) {
        player.executeIfComponentPresent(DestructibleComponent.class, dc -> {
            final GameImage sprite = dc.isDestroyed()
                    ? (spriteStateB
                    ? spriteMap.get(Sprite.CANNON_DESTROYED_2)
                    : spriteMap.get(Sprite.CANNON_DESTROYED_1))
                    : spriteMap.get(Sprite.CANNON);

            renderEntity(canvas, sprite, player);
        });
    }

    private void renderUFO(final GameImage canvas, final GameEntity<Vector2D> ufo) {
        renderEntity(canvas, spriteMap.get(Sprite.UFO), ufo);
    }

    private void renderEffect(final GameImage canvas, final GameEntity<Vector2D> effect) {
        effect.executeIfComponentPresent(EffectComponent.class, ec -> {
            final GameImage sprite;
            final String id = ec.id;

            if (id.equals("explosion") ||
                    ec.getAgeAsLifespanRatio() < GameConstants.UFO_EXPLOSION_LONGEVITY_SHOW_POINT_THRESHOLD)
                sprite = spriteMap.get(Sprite.EXPLOSION);
            else if (effectSpriteMap.containsKey(effect))
                sprite = effectSpriteMap.get(effect);
            else {
                sprite = RenderHelper.drawText(id);
                effectSpriteMap.put(effect, sprite);
            }

            renderEntity(canvas, sprite, effect);
        });
    }

    private void renderEnemy(final GameImage canvas, final GameEntity<Vector2D> enemy) {
        enemy.executeIfComponentPresent(EnemyLogicComponent.class, elc -> {
            final String suffix = spriteStateA ? "_2" : "_1";
            final GameImage sprite = spriteMap.get(Sprite.valueOf(elc.type + suffix));

            renderEntity(canvas, sprite, enemy);
        });
    }

    private void renderBunkerPart(final GameImage canvas, final GameEntity<Vector2D> part) {
        final BunkerPartComponent bpc = part.getComponent(BunkerPartComponent.class);
        final DestructibleComponent dc = part.getComponent(DestructibleComponent.class);

        if (bpc == null || dc == null)
            return;

        final GameImage sprite;

        if (bunkerPartDamageStatusMap.containsKey(part) && dc.getHP() == bunkerPartDamageStatusMap.get(part))
            sprite = bunkerPartSpriteMap.get(part);
        else {
            final GameImage baseSprite = spriteMap.get(Sprite.valueOf(bpc.type.name()));

            final GameImage damage = switch (dc.getHP()) {
                case 3 -> spriteMap.get(Sprite.DAMAGE_3);
                case 2 -> spriteMap.get(Sprite.DAMAGE_2);
                case 1 -> spriteMap.get(Sprite.DAMAGE_1);
                default -> new GameImage(baseSprite.getWidth(), baseSprite.getHeight());
            };

            sprite = RenderHelper.mask(RenderHelper.composeOverNonTransparentPixels(baseSprite, damage));
            bunkerPartSpriteMap.put(part, sprite);
            bunkerPartDamageStatusMap.put(part, dc.getHP());
        }

        renderEntity(canvas, sprite, part);
    }

    private void renderProjectile(final GameImage canvas, final GameEntity<Vector2D> projectile) {
        projectile.executeIfComponentPresent(ProjectileComponent.class, ppc -> {
            final GameImage sprite = switch (ppc.type) {
                case PLAYER -> spriteMap.get(Sprite.PLAYER_LASER);
                case ENEMY -> {
                    if (spriteStateA)
                        yield spriteMap.get(Sprite.CROSS_2);

                    yield spriteMap.get(Sprite.CROSS_1);
                }
            };

            renderEntity(canvas, sprite, projectile);
        });
    }

    private void renderEntity(final GameImage canvas, final GameImage sprite, final GameEntity<Vector2D> entity) {
        final Coord2D renderPosition = gameWorldToRenderPosition(entity.getPosition());

        canvas.draw(sprite, renderPosition.x - (sprite.getWidth() / 2),
                renderPosition.y - (sprite.getHeight() / 2));
    }

    private Coord2D gameWorldToRenderPosition(final Vector2D gwPosition) {
        return new Coord2D((int) gwPosition.x, (int) gwPosition.y);
    }

    /* DEBUG RENDER */
    @Override
    public void debugRender(final GameImage canvas, final GameDebugger debugger) {
        final GameImage fps = RenderHelper.drawText(debugger.getFPS() + " fps");
        canvas.draw(fps, CANVAS_WIDTH - (fps.getWidth() + 2), 2 - AssetFetcher.FONT_OFFSET);

        Menus.resolve().debugRender(canvas, debugger);
    }

    public void update() {
        final boolean paused = SIHandler.get().isPaused();
        final SIHandler.State state = SIHandler.get().getState();

        if (SIHandler.get().getTickCounter() == 0 && !paused && state.canBePaused)
            spriteStateA = !spriteStateA;

        if (!paused || !state.canBePaused) {
            renderTickCounter++;

            if (renderTickCounter >= RENDER_TICK_COUNTER_MAX) {
                spriteStateB = !spriteStateB;
                renderTickCounter = 0;
            }
        }

        final Set<GameEntity<Vector2D>> entities = SIHandler.get().getEntities();

        // Manage effect map size by removing effects no longer in Game
        final Set<GameEntity<Vector2D>> trackedEffects = new HashSet<>(effectSpriteMap.keySet());
        trackedEffects.stream().filter(x -> !entities.contains(x))
                .forEach(effectSpriteMap::remove);
    }
}

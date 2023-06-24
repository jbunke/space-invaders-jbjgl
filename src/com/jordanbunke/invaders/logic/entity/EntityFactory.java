package com.jordanbunke.invaders.logic.entity;

import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.invaders.logic.components.*;
import com.jordanbunke.invaders.math.SIMath;
import com.jordanbunke.jbjgl.game_world.ecs.GameEntity;
import com.jordanbunke.jbjgl.game_world.physics.vector.Vector2D;

public final class EntityFactory {
    public static GameEntity<Vector2D> createEnemy(
            final Vector2D position, final EnemyLogicComponent.Type type
    ) {
        final GameEntity<Vector2D> enemy = new GameEntity<>(position);

        enemy.addComponent(new DestructibleComponent());
        enemy.addComponent(new EnemyLogicComponent(type));
        enemy.addComponent(new HasPointsComponent(type.points));
        enemy.addComponent(new ColliderComponent(type.collisionWidth,
                GameConstants.MOST_ENTITY_DEPTH));

        return enemy;
    }

    public static GameEntity<Vector2D> createPlayer() {
        final GameEntity<Vector2D> player = new GameEntity<>(
                new Vector2D(GameConstants.GW_WIDTH / 2d, GameConstants.PLAYER_Y)
        );

        player.addComponent(new DestructibleComponent());
        player.addComponent(new PlayerControllerComponent());
        player.addComponent(new ColliderComponent(GameConstants.PLAYER_WIDTH,
                GameConstants.MOST_ENTITY_DEPTH));

        return player;
    }

    public static GameEntity<Vector2D> createUFO() {
        final GameEntity<Vector2D> ufo = new GameEntity<>(new Vector2D());

        ufo.addComponent(new DestructibleComponent());
        ufo.addComponent(new UFOLogicComponent());
        ufo.addComponent(new HasPointsComponent(SIMath.randomInRange(1, 7) *
                GameConstants.UFO_POINTS_MULTIPLIER));
        ufo.addComponent(new ColliderComponent(GameConstants.UFO_WIDTH,
                GameConstants.MOST_ENTITY_DEPTH));

        ufo.start();

        return ufo;
    }

    public static GameEntity<Vector2D> createBunkerPart(
            final Vector2D position, final BunkerPartComponent.Type type
    ) {
        final GameEntity<Vector2D> bunkerPart = new GameEntity<>(position);

        bunkerPart.addComponent(new DestructibleComponent(GameConstants.BUNKER_PART_HP));
        bunkerPart.addComponent(new ColliderComponent(GameConstants.BUNKER_PART_WIDTH,
                GameConstants.BUNKER_PART_DEPTH));
        bunkerPart.addComponent(new BunkerPartComponent(type));

        return bunkerPart;
    }

    public static GameEntity<Vector2D> createPlayerLaser(final Vector2D position) {
        final GameEntity<Vector2D> projectile = new GameEntity<>(position);

        projectile.addComponent(new ProjectileComponent(ProjectileComponent.Type.PLAYER));
        projectile.addComponent(new ColliderComponent(GameConstants.PLAYER_LASER_WIDTH,
                GameConstants.PROJECTILE_DEPTH));

        return projectile;
    }

    public static GameEntity<Vector2D> createEnemyLaser(final Vector2D position) {
        final GameEntity<Vector2D> projectile = new GameEntity<>(position);

        projectile.addComponent(new ProjectileComponent(ProjectileComponent.Type.ENEMY));
        projectile.addComponent(new ColliderComponent(GameConstants.ENEMY_LASER_WIDTH,
                GameConstants.PROJECTILE_DEPTH));

        return projectile;
    }

    public static GameEntity<Vector2D> createEffect(
            final Vector2D position, final String id, final int lifespan
    ) {
        final GameEntity<Vector2D> effect = new GameEntity<>(position);

        effect.addComponent(new EffectComponent(id, lifespan));

        return effect;
    }
}

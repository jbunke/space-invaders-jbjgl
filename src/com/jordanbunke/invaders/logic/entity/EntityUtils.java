package com.jordanbunke.invaders.logic.entity;

import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.invaders.logic.components.*;
import com.jordanbunke.jbjgl.game_world.ecs.GameEntity;
import com.jordanbunke.jbjgl.game_world.physics.vector.Vector2D;

import java.util.Set;

public final class EntityUtils {
    public static boolean isPlayerLaser(final GameEntity<Vector2D> entity) {
        final ProjectileComponent pc =
                entity.getComponent(ProjectileComponent.class);
        return pc != null && pc.type == ProjectileComponent.Type.PLAYER;
    }

    public static boolean isEnemyLaser(final GameEntity<Vector2D> entity) {
        final ProjectileComponent pc =
                entity.getComponent(ProjectileComponent.class);
        return pc != null && pc.type == ProjectileComponent.Type.ENEMY;
    }

    public static boolean isEnemy(final GameEntity<Vector2D> entity, final EnemyLogicComponent.Type type) {
        final EnemyLogicComponent ec =
                entity.getComponent(EnemyLogicComponent.class);
        return entity.hasComponent(DestructibleComponent.class) &&
                ec!= null && ec.type == type;
    }

    public static boolean isEnemy(final GameEntity<Vector2D> entity, final boolean isDestroyed) {
        final DestructibleComponent dc =
                entity.getComponent(DestructibleComponent.class);
        return entity.hasComponent(EnemyLogicComponent.class) &&
                dc!= null && dc.isDestroyed() == isDestroyed;
    }

    public static boolean isEnemy(final GameEntity<Vector2D> entity) {
        return isEnemy(entity, true) || isEnemy(entity, false);
    }

    public static boolean isEffect(final GameEntity<Vector2D> entity, final Set<String> ids) {
        final EffectComponent ec = entity.getComponent(EffectComponent.class);
        return ec != null && ids.contains(ec.id);
    }

    public static boolean isDestroyedBunkerPart(final GameEntity<Vector2D> entity) {
        final BunkerPartComponent bpc = entity.getComponent(BunkerPartComponent.class);
        final DestructibleComponent dc = entity.getComponent(DestructibleComponent.class);

        return bpc != null && dc != null && dc.isDestroyed();
    }

    public static boolean outOfBounds(final GameEntity<Vector2D> entity) {
        final double y = entity.getPosition().y;
        return y < 0 || y > GameConstants.GW_DEPTH;
    }
}

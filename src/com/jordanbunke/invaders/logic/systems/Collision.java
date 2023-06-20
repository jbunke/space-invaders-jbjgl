package com.jordanbunke.invaders.logic.systems;

import com.jordanbunke.invaders.logic.components.*;
import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.GameEntity;

public final class Collision {
    public static boolean checkCollision(final GameEntity<Vector2D> target, final GameEntity<Vector2D> projectile) {
        final ProjectileComponent ppc = projectile.getComponent(ProjectileComponent.class);
        final ColliderComponent pcc = projectile.getComponent(ColliderComponent.class);
        final ColliderComponent tcc = target.getComponent(ColliderComponent.class);
        final DestructibleComponent tdc = target.getComponent(DestructibleComponent.class);

        // necessary components for valid collision
        if (ppc == null || pcc == null || tcc == null || tdc == null)
            return false;

        final boolean playerStrike = ppc.type == ProjectileComponent.Type.PLAYER &&
                (target.hasComponent(EnemyLogicComponent.class) ||
                        target.hasComponent(UFOLogicComponent.class) ||
                        target.hasComponent(BunkerPartComponent.class)),
                enemyStrike = ppc.type == ProjectileComponent.Type.ENEMY &&
                        (target.hasComponent(PlayerControllerComponent.class) ||
                                target.hasComponent(BunkerPartComponent.class)),
                validStrike = playerStrike || enemyStrike;

        if (!validStrike || tdc.isDestroyed() || ppc.isSpent())
            return false;

        final boolean collides = projectileCollidesWithTarget(projectile, target);

        if (collides) {
            tdc.damage();
            ppc.spend();
        }

        return collides;
    }

    private static boolean projectileCollidesWithTarget(
            final GameEntity<Vector2D> projectile, final GameEntity<Vector2D> target
    ) {
        return projectileCollidesWithTarget(projectile, target,
                projectile.getComponent(ProjectileComponent.class),
                projectile.getComponent(ColliderComponent.class),
                target.getComponent(ColliderComponent.class));
    }

    private static boolean projectileCollidesWithTarget(
            final GameEntity<Vector2D> projectile, final GameEntity<Vector2D> target,
            final ProjectileComponent ppc, final ColliderComponent pcc,
            final ColliderComponent tcc
    ) {
        if (ppc == null || !ppc.getEntity().equals(projectile) ||
                pcc == null || !pcc.getEntity().equals(projectile) ||
                tcc == null || !tcc.getEntity().equals(target))
            return false;

        final double tLeft = target.getPosition().x - (tcc.width / 2d), tRight = tLeft + tcc.width,
                tTop = target.getPosition().y - (tcc.depth / 2d), tBottom = tTop + tcc.depth,
                pLeft = projectile.getPosition().x - (pcc.width / 2d), pRight = pLeft + pcc.width,
                pTop = projectile.getPosition().y - (pcc.depth / 2d), pBottom = pTop + pcc.depth,
                pLastTop = pTop - ppc.type.velocity, pLastBottom = pBottom - ppc.type.velocity;

        final boolean withinXBounds = tLeft < pRight && tRight > pLeft,
                withinYBounds = tTop < pBottom && tBottom > pTop,
                wasWithinYBounds = tTop < pLastBottom && tBottom > pLastTop;

        return withinXBounds && (withinYBounds || wasWithinYBounds);
    }
}

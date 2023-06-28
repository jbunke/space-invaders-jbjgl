package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.delta_time.utility.RNG;
import com.jordanbunke.invaders.handlers.SIHandler;
import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.delta_time.game_world.ecs.basic_components.EntityComponent;
import com.jordanbunke.delta_time.game_world.physics.vector.Vector2D;

public final class EnemyLogicComponent extends EntityComponent<Vector2D> {
    public enum Type {
        SQUID(GameConstants.SQUID_WIDTH, GameConstants.SQUID_POINTS, GameConstants.SQUID_PROJ_PROB),
        CRAB(GameConstants.CRAB_WIDTH, GameConstants.CRAB_POINTS, GameConstants.CRAB_PROJ_PROB),
        OCTOPUS(GameConstants.OCTOPUS_WIDTH, GameConstants.OCTOPUS_POINTS, GameConstants.OCTOPUS_PROJ_PROB);

        public final int collisionWidth, points;
        public final double projectileProbability;

        Type(final int collisionWidth, final int points, final double projectileProbability) {
            this.collisionWidth = collisionWidth;
            this.points = points;
            this.projectileProbability = projectileProbability;
        }
    }

    public final Type type;

    public EnemyLogicComponent(final Type type) {
        this.type = type;
    }

    @Override
    public void update(double deltaTime) {
        final DestructibleComponent d = getEntity().getComponent(DestructibleComponent.class);

        if ((d != null && d.isDestroyed()) || SIHandler.get().getTickCounter() != 0)
            return;

        getEntity().move(new Vector2D(SIHandler.get().getDirection() * GameConstants.ENEMY_SPEED, 0));

        if (RNG.prob(boundedProbability())) {
            SIHandler.get().queueEnemyLaser(getEntity());
        }
    }

    private double boundedProbability() {
        final double probAugment = SIHandler.get().getWave() * GameConstants.PER_WAVE_PROJ_PROB;
        return Math.min(type.projectileProbability + probAugment, GameConstants.MAX_PROJ_PROB);
    }
}

package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.delta_time.game_world.ecs.basic_components.EntityComponent;
import com.jordanbunke.delta_time.game_world.physics.vector.Vector2D;

public final class ProjectileComponent extends EntityComponent<Vector2D> {
    public enum Type {
        PLAYER(GameConstants.PLAYER_LASER_VELOCITY),
        ENEMY(GameConstants.ENEMY_LASER_VELOCITY);

        public final int velocity;

        Type(final int velocity) {
            this.velocity = velocity;
        }
    }
    public final Type type;
    private boolean spent;

    public ProjectileComponent(final Type type) {
        this.type = type;
        spent = false;
    }

    @Override
    public void update(double deltaTime) {
        if (spent)
            return;

        getEntity().move(new Vector2D(0, type.velocity));
    }

    public boolean isSpent() {
        return spent;
    }

    public void spend() {
        spent = true;
    }
}

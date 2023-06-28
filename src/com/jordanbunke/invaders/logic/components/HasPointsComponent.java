package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.ecs.basic_components.EntityComponent;
import com.jordanbunke.jbjgl.game_world.physics.vector.Vector2D;

public final class HasPointsComponent extends EntityComponent<Vector2D> {
    public final int points;

    public HasPointsComponent(final int points) {
        this.points = points;
    }

    @Override
    public void update(double deltaTime) {

    }
}

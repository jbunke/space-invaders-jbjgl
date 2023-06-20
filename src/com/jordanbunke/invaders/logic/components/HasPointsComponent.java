package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;

public final class HasPointsComponent extends EntityComponent<Vector2D> {
    public final int points;

    public HasPointsComponent(final int points) {
        this.points = points;
    }

    @Override
    public void update(double deltaTime) {

    }
}

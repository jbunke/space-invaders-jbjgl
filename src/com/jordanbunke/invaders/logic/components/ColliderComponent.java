package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;

public final class ColliderComponent extends EntityComponent<Vector2D> {
    public final int width, depth;

    public ColliderComponent(final int width, final int depth) {
        this.width = width;
        this.depth = depth;
    }

    @Override
    public void update(final double deltaTIme) {

    }
}

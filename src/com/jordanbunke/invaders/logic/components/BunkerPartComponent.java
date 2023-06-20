package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;

public final class BunkerPartComponent extends EntityComponent<Vector2D> {
    public enum Type {
        BUNKER_FULL,
        BUNKER_UNDER_LEFT, BUNKER_UNDER_RIGHT,
        BUNKER_TOP_LEFT, BUNKER_TOP_RIGHT
    }

    public final Type type;

    public BunkerPartComponent(final Type type) {
        this.type = type;
    }

    @Override
    public void update(final double deltaTime) {

    }
}

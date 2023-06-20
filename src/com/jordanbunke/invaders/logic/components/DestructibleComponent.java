package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;

public final class DestructibleComponent extends EntityComponent<Vector2D> {
    private int hp;
    private boolean destroyed;

    public DestructibleComponent() {
        this.hp = 1;
    }

    @Override
    public void update(double deltaTime) {

    }

    public DestructibleComponent(final int hp) {
        this.hp = hp;
    }

    public void damage() {
        hp--;

        if (hp <= 0)
            destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int getHP() {
        return hp;
    }
}

package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.jbjgl.game_world.Vector2D;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;

public final class EffectComponent extends EntityComponent<Vector2D> {
    public final int lifespan;
    public final String id;

    private int age;

    public EffectComponent(final String id, final int lifespan) {
        this.id = id;
        this.lifespan = lifespan;

        this.age = 0;
    }

    // useful for animated effects
    public double getAgeAsLifespanRatio() {
        return age / (double)lifespan;
    }

    public boolean isExpired() {
        return age >= lifespan;
    }

    @Override
    public void update(double deltaTime) {
        if (age < lifespan)
            age++;
    }
}

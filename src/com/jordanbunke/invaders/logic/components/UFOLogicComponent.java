package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.invaders.math.SIMath;
import com.jordanbunke.jbjgl.game_world.ecs.EntityComponent;
import com.jordanbunke.jbjgl.game_world.physics.vector.Vector2D;

public final class UFOLogicComponent extends EntityComponent<Vector2D> {
    private static final int SPEED = 1;
    private static final int LEFT_SPAWN = -GameConstants.UFO_WIDTH,
            RIGHT_SPAWN = GameConstants.GW_WIDTH + GameConstants.UFO_WIDTH;
    public final boolean fromLeft;
    private boolean escaped;

    public UFOLogicComponent() {
        fromLeft = SIMath.flipCoin();
        escaped = false;
    }

    @Override
    public void start() {
        getEntity().setPosition(new Vector2D(fromLeft ? LEFT_SPAWN : RIGHT_SPAWN, GameConstants.UFO_Y));
    }

    @Override
    public void update(double deltaTime) {
        final DestructibleComponent d = getEntity().getComponent(DestructibleComponent.class);

        if ((d != null && d.isDestroyed()) || escaped)
            return;

        getEntity().move(new Vector2D(fromLeft ? SPEED : -SPEED, 0));

        final boolean offScreenLeft = getEntity().getPosition().x < LEFT_SPAWN,
                offScreenRight = getEntity().getPosition().x > RIGHT_SPAWN;

        if ((fromLeft && offScreenRight) || (!fromLeft && offScreenLeft))
            escaped = true;
    }

    public boolean hasEscaped() {
        return escaped;
    }
}

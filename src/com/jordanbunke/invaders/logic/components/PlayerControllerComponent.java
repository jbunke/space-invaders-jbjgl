package com.jordanbunke.invaders.logic.components;

import com.jordanbunke.invaders.handlers.SIHandler;
import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.delta_time.game_world.ecs.basic_components.EntityComponent;
import com.jordanbunke.delta_time.game_world.physics.vector.Vector2D;

public final class PlayerControllerComponent extends EntityComponent<Vector2D> {
    private boolean movingLeft, movingRight,attemptShotOnUpdate;
    private int shotCooldown;

    public PlayerControllerComponent() {
        movingLeft = false;
        movingRight = false;

        shotCooldown = 0;
    }

    @Override
    public void update(double deltaTime) {
        final DestructibleComponent d = getEntity().getComponent(DestructibleComponent.class);

        if (d != null && d.isDestroyed())
            return;

        if (shotCooldown > 0)
            shotCooldown--;

        if (attemptShotOnUpdate && shotCooldown == 0) {
            SIHandler.get().queuePlayerLaser();
            shotCooldown = GameConstants.PLAYER_COOLDOWN_TICKS;
        }
        attemptShotOnUpdate = false;

        final boolean roomToMoveLeft = getEntity().getPosition().x -
                GameConstants.PLAYER_SPEED >= GameConstants.GW_BORDER,
                roomToMoveRight = getEntity().getPosition().x +
                        GameConstants.PLAYER_SPEED <= GameConstants.GW_WIDTH -
                        GameConstants.GW_BORDER;

        if (movingLeft && roomToMoveLeft)
            getEntity().move(new Vector2D(-GameConstants.PLAYER_SPEED, 0));

        if (movingRight && roomToMoveRight)
            getEntity().move(new Vector2D(GameConstants.PLAYER_SPEED, 0));
    }

    public void setMovingLeft(final boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public void setMovingRight(final boolean movingRight) {
        this.movingRight = movingRight;
    }

    public void attemptShot() {
        attemptShotOnUpdate = true;
    }
}

package com.jordanbunke.invaders.logic;

public final class GameConstants {
    /* INFORMATION */
    public static final String TITLE = "Space Invaders";

    /* GAME WORLD CONSTANTS */
    public static final int GW_WIDTH = 320, GW_DEPTH = 240, GW_BORDER = 10;

    /* GENERAL MATHS */
    public static final double UPDATE_HZ = 50d, TARGET_FPS = 30d;
    public static final int WAVES = 15,
            PROJECTILE_LAUNCHER_Y_DISTANCE = 6, MOST_ENTITY_DEPTH = 8,
            LEFT = -1, RIGHT = 1,
            INITIAL_TICK_CYCLE = 24, MIN_TICK_CYCLE = 4, TICK_CYCLE_DECREMENT = 2;

    /* PLAYER CONSTANTS */
    public static final int
            PLAYER_WIDTH = 13, PLAYER_SPEED = 2, PLAYER_COOLDOWN_TICKS = 20,
            PLAYER_Y = GW_DEPTH - (MOST_ENTITY_DEPTH / 2);

    /* ENEMY CONSTANTS */
    public static final int ENEMY_GAME_OVER_Y_THRESHOLD = PLAYER_Y - MOST_ENTITY_DEPTH,
            ENEMY_COLUMNS = 11, MAX_ADDITIONAL_COLUMNS = 7,
            ENEMY_SPACE_BETWEEN_X = 16, ENEMY_SPACE_BETWEEN_Y = 16, ENEMY_TOP_Y = GW_DEPTH / 5,
            ENEMY_MIDDLE_X = GW_WIDTH / 2,
            DOWN_STEP_SIZE = 8, ENEMY_SPEED = 4,
            SQUID_WIDTH = 8, CRAB_WIDTH = 11, OCTOPUS_WIDTH = 12,
            SQUID_POINTS = 40, CRAB_POINTS = 20, OCTOPUS_POINTS = 10;
    public static final double
            SQUID_PROJ_PROB = 0.02, CRAB_PROJ_PROB = 0.01, OCTOPUS_PROJ_PROB = 0.005,
            MAX_PROJ_PROB = 0.08, PER_WAVE_PROJ_PROB = 0.005;

    /* UFO CONSTANTS */
    public static final int UFO_WIDTH = 16,
            UFO_Y = 10 + (MOST_ENTITY_DEPTH / 2),
            UFO_POINTS_MULTIPLIER = 50;
    public static final double UFO_SPAWN_PROBABILITY = 0.05;

    /* EXPLOSION CONSTANTS */
    public static final int ENEMY_EXPLOSION_LIFESPAN = 15, UFO_EXPLOSION_LIFESPAN = 25;
    public static final double UFO_EXPLOSION_LONGEVITY_SHOW_POINT_THRESHOLD = 0.4;

    /* PROJECTILE CONSTANTS */
    public static final int PLAYER_LASER_WIDTH = 1, ENEMY_LASER_WIDTH = 3, PROJECTILE_DEPTH = 5;
    public static final int PLAYER_LASER_VELOCITY = -5, ENEMY_LASER_VELOCITY = 2;

    /* BUNKER CONSTANTS */
    public static final int BUNKER_PART_HP = 4,
            BUNKER_PART_WIDTH = 6, BUNKER_PART_DEPTH = 6,
            NUM_BUNKER_PARTS_X = 4, NUM_BUNKER_PARTS_Y = 4,
            BUNKER_TOP = PLAYER_Y - (7 * BUNKER_PART_DEPTH), NUM_BUNKERS = 4;

    /* TIMING CONSTANTS */
    public static final int TICKS_TO_NEXT_LEVEL = 50, TICKS_FOR_RESPAWN = 40;
}

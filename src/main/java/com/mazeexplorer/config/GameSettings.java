package com.mazeexplorer.config;

/**
 * Global game constants and configuration parameters for Algorithmic 3D Maze Explorer.
 */
public final class GameSettings {

    private GameSettings() {}

    // Window & Display Settings
    public static final String WINDOW_TITLE = "Algorithmic 3D Maze Explorer";
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final boolean VSYNC_ENABLED = true;

    // Camera & View Settings
    public static final float CAMERA_FOV = 60.0f; // degrees
    public static final float CAMERA_NEAR = 0.1f;
    public static final float CAMERA_FAR = 1000.0f;
    public static final float MOUSE_SENSITIVITY = 1.5f;
    public static final boolean INVERT_Y = false; // Default: Natural vertical mouse look (mouse up = look up)

    // Player Physics & Movement
    public static final float PLAYER_WALK_SPEED = 6.0f;
    public static final float PLAYER_RUN_SPEED = 10.0f;
    public static final float PLAYER_HEIGHT = 1.8f;
    public static final float PLAYER_RADIUS = 0.4f;

    // Maze Dimensions & World Scale
    public static final float WALL_SIZE = 3.0f; // Each maze cell is 3x3 world units
    public static final float WALL_HEIGHT = 3.5f;

    // Gameplay Attributes
    public static final int MAX_PLAYER_HEALTH = 100;
    public static final int SCORE_PER_COIN = 100;
    public static final int SCORE_PER_GEM = 500;
}


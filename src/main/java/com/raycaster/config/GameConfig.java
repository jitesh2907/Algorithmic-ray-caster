package com.raycaster.config;

/**
 * Game configuration holding display, camera, movement, and ray-casting parameters.
 * Provides sensible defaults matching project specifications and allows runtime customization.
 */
public class GameConfig {

    // Display configuration
    public static final int DEFAULT_SCREEN_WIDTH = 80;
    public static final int DEFAULT_SCREEN_HEIGHT = 24;

    // Camera and Ray Casting configuration
    public static final double DEFAULT_FOV_DEGREES = 60.0;
    public static final double DEFAULT_MAX_RAY_DISTANCE = 20.0;

    // Movement configuration
    public static final double DEFAULT_MOVE_STEP = 0.25;
    public static final double DEFAULT_ROTATION_STEP_DEGREES = 10.0;
    public static final double DEFAULT_COLLISION_RADIUS = 0.20;

    // Map cell representations
    public static final int CELL_EMPTY = 0;
    public static final int CELL_WALL = 1;

    // Instance variables
    private int screenWidth;
    private int screenHeight;
    private double fovDegrees;
    private double maxRayDistance;
    private double moveStep;
    private double rotationStepDegrees;
    private double collisionRadius;
    private boolean miniMapEnabled;
    private boolean showFovOnMiniMap;

    public GameConfig() {
        this.screenWidth = DEFAULT_SCREEN_WIDTH;
        this.screenHeight = DEFAULT_SCREEN_HEIGHT;
        this.fovDegrees = DEFAULT_FOV_DEGREES;
        this.maxRayDistance = DEFAULT_MAX_RAY_DISTANCE;
        this.moveStep = DEFAULT_MOVE_STEP;
        this.rotationStepDegrees = DEFAULT_ROTATION_STEP_DEGREES;
        this.collisionRadius = DEFAULT_COLLISION_RADIUS;
        this.miniMapEnabled = true;
        this.showFovOnMiniMap = true;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        if (screenWidth >= 20 && screenWidth <= 240) {
            this.screenWidth = screenWidth;
        }
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        if (screenHeight >= 10 && screenHeight <= 100) {
            this.screenHeight = screenHeight;
        }
    }

    public double getFovDegrees() {
        return fovDegrees;
    }

    public double getFovRadians() {
        return Math.toRadians(fovDegrees);
    }

    public void setFovDegrees(double fovDegrees) {
        if (fovDegrees >= 30.0 && fovDegrees <= 120.0) {
            this.fovDegrees = fovDegrees;
        }
    }

    public double getMaxRayDistance() {
        return maxRayDistance;
    }

    public void setMaxRayDistance(double maxRayDistance) {
        if (maxRayDistance > 0) {
            this.maxRayDistance = maxRayDistance;
        }
    }

    public double getMoveStep() {
        return moveStep;
    }

    public void setMoveStep(double moveStep) {
        if (moveStep > 0 && moveStep < 1.0) {
            this.moveStep = moveStep;
        }
    }

    public double getRotationStepDegrees() {
        return rotationStepDegrees;
    }

    public double getRotationStepRadians() {
        return Math.toRadians(rotationStepDegrees);
    }

    public void setRotationStepDegrees(double rotationStepDegrees) {
        if (rotationStepDegrees > 0 && rotationStepDegrees <= 45.0) {
            this.rotationStepDegrees = rotationStepDegrees;
        }
    }

    public double getCollisionRadius() {
        return collisionRadius;
    }

    public void setCollisionRadius(double collisionRadius) {
        if (collisionRadius >= 0 && collisionRadius < 0.5) {
            this.collisionRadius = collisionRadius;
        }
    }

    public boolean isMiniMapEnabled() {
        return miniMapEnabled;
    }

    public void setMiniMapEnabled(boolean miniMapEnabled) {
        this.miniMapEnabled = miniMapEnabled;
    }

    public void toggleMiniMap() {
        this.miniMapEnabled = !this.miniMapEnabled;
    }

    public boolean isShowFovOnMiniMap() {
        return showFovOnMiniMap;
    }

    public void setShowFovOnMiniMap(boolean showFovOnMiniMap) {
        this.showFovOnMiniMap = showFovOnMiniMap;
    }
}

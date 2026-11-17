package com.mazeexplorer.player;

import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.stats.HealthSystem;
import com.mazeexplorer.stats.ScoreManager;

/**
 * Pure Java player model encapsulating position, orientation, velocity,
 * and associated gameplay state.
 */
public class Player {

    private float posX;
    private float posY;
    private float posZ;

    private float yaw;   // Rotation around Y axis (horizontal looking angle)
    private float pitch; // Vertical looking angle (clamped [-89, +89])

    private final float collisionRadius;
    private final float eyeHeight;

    private final HealthSystem health;
    private final ScoreManager score;

    public Player(float startX, float startY, float startZ) {
        this.posX = startX;
        this.posY = startY;
        this.posZ = startZ;
        this.yaw = 0f;
        this.pitch = 0f;
        this.collisionRadius = GameSettings.PLAYER_RADIUS;
        this.eyeHeight = GameSettings.PLAYER_HEIGHT;
        this.health = new HealthSystem();
        this.score = new ScoreManager();
    }

    public void setPosition(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        // Clamp pitch to prevent camera flip
        this.pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public float getEyeY() {
        return posY + eyeHeight;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getCollisionRadius() {
        return collisionRadius;
    }

    public HealthSystem getHealth() {
        return health;
    }

    public ScoreManager getScore() {
        return score;
    }
}


package com.mazeexplorer.entity;

/**
 * Exit doorway marking level completion.
 * Remains locked until the player collects all three keys.
 */
public class ExitDoor {

    public static final int REQUIRED_KEYS = 3;

    private final float worldX;
    private final float worldZ;
    private final float interactionRadius;
    private boolean unlocked;
    private boolean opened;

    public ExitDoor(float worldX, float worldZ) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.interactionRadius = 1.8f;
        this.unlocked = false;
        this.opened = false;
    }

    public boolean canUnlock(int keysHeld) {
        return keysHeld >= REQUIRED_KEYS;
    }

    public boolean tryUnlock(int keysHeld) {
        if (!unlocked && canUnlock(keysHeld)) {
            this.unlocked = true;
            this.opened = true;
            return true;
        }
        return false;
    }

    public boolean tryUnlock(boolean legacyHasKey) {
        return tryUnlock(legacyHasKey ? REQUIRED_KEYS : 0);
    }

    public boolean isPlayerAtDoor(float playerX, float playerZ) {
        float dx = playerX - worldX;
        float dz = playerZ - worldZ;
        return (dx * dx + dz * dz) <= (interactionRadius * interactionRadius);
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldZ() {
        return worldZ;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean isOpened() {
        return opened;
    }

    public int getRequiredKeys() {
        return REQUIRED_KEYS;
    }
}

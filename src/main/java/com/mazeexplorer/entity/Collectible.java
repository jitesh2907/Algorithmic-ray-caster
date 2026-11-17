package com.mazeexplorer.entity;

/**
 * Procedural in-world collectible item (Coin, Gem, Key).
 * Animates with vertical bobbing and rotational spin.
 */
public class Collectible {

    public enum Type {
        COIN(100),
        GEM(500),
        KEY(1000);

        private final int pointValue;

        Type(int pointValue) {
            this.pointValue = pointValue;
        }

        public int getPointValue() {
            return pointValue;
        }
    }

    private final Type type;
    private final float baseWorldX;
    private final float baseWorldY;
    private final float baseWorldZ;

    private float currentY;
    private float rotationAngle;
    private boolean collected;

    public Collectible(Type type, float x, float y, float z) {
        this.type = type;
        this.baseWorldX = x;
        this.baseWorldY = y;
        this.baseWorldZ = z;
        this.currentY = y;
        this.rotationAngle = 0f;
        this.collected = false;
    }

    public void update(float tpf) {
        if (!collected) {
            rotationAngle += 2.0f * tpf;
            // Harmonic levitation animation
            currentY = baseWorldY + 0.15f * (float) Math.sin(rotationAngle * 1.5f);
        }
    }

    public boolean checkPickup(float playerX, float playerZ, float pickupRadius) {
        if (collected) return false;
        float dx = playerX - baseWorldX;
        float dz = playerZ - baseWorldZ;
        if ((dx * dx + dz * dz) <= (pickupRadius * pickupRadius)) {
            collected = true;
            return true;
        }
        return false;
    }

    public Type getType() {
        return type;
    }

    public float getBaseWorldX() {
        return baseWorldX;
    }

    public float getBaseWorldZ() {
        return baseWorldZ;
    }

    public float getCurrentY() {
        return currentY;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public boolean isCollected() {
        return collected;
    }
}


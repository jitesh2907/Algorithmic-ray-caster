package com.mazeexplorer.raycast;

/**
 * Result data object for 3D/2D DDA ray cast queries.
 */
public class RayHit3D {

    private final boolean hit;
    private final float distance;
    private final float hitX;
    private final float hitZ;
    private final int gridX;
    private final int gridZ;
    private final int side; // 0 = hit X normal, 1 = hit Z normal

    public RayHit3D(boolean hit, float distance, float hitX, float hitZ, int gridX, int gridZ, int side) {
        this.hit = hit;
        this.distance = distance;
        this.hitX = hitX;
        this.hitZ = hitZ;
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.side = side;
    }

    public static RayHit3D miss(float maxDist) {
        return new RayHit3D(false, maxDist, 0f, 0f, -1, -1, 0);
    }

    public boolean isHit() {
        return hit;
    }

    public float getDistance() {
        return distance;
    }

    public float getHitX() {
        return hitX;
    }

    public float getHitZ() {
        return hitZ;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridZ() {
        return gridZ;
    }

    public int getSide() {
        return side;
    }
}


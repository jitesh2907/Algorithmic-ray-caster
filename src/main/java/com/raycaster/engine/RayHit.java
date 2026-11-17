package com.raycaster.engine;

/**
 * Stores the result of casting a single ray into the virtual maze.
 * Contains geometric hit details, perpendicular distance (fish-eye corrected),
 * Euclidean distance, hit grid coordinates, and surface orientation.
 */
public class RayHit {

    private final boolean hit;
    private final double perpDistance;
    private final double euclideanDistance;
    private final int mapX;
    private final int mapY;
    private final int side; // 0 = X-side (vertical wall line), 1 = Y-side (horizontal wall line)
    private final int wallType;
    private final double wallX;
    private final double rayAngle;

    public RayHit(boolean hit, double perpDistance, double euclideanDistance,
                  int mapX, int mapY, int side, int wallType, double wallX, double rayAngle) {
        this.hit = hit;
        this.perpDistance = perpDistance;
        this.euclideanDistance = euclideanDistance;
        this.mapX = mapX;
        this.mapY = mapY;
        this.side = side;
        this.wallType = wallType;
        this.wallX = wallX;
        this.rayAngle = rayAngle;
    }

    /**
     * Factory method for rays that did not hit any wall within maximum distance.
     */
    public static RayHit noHit(double maxDistance, double rayAngle) {
        return new RayHit(false, maxDistance, maxDistance, -1, -1, 0, 0, 0.0, rayAngle);
    }

    public boolean isHit() {
        return hit;
    }

    public double getPerpDistance() {
        return perpDistance;
    }

    public double getEuclideanDistance() {
        return euclideanDistance;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public int getSide() {
        return side;
    }

    public int getWallType() {
        return wallType;
    }

    public double getWallX() {
        return wallX;
    }

    public double getRayAngle() {
        return rayAngle;
    }
}

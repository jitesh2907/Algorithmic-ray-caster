package com.raycaster.core;

import com.raycaster.config.GameConfig;

/**
 * Represents the first-person player navigating the virtual maze.
 * Maintains player position (x, y), viewing angle, and handles movement,
 * camera rotation, and collision detection with sliding physics.
 */
public class Player {

    private double x;
    private double y;
    private double angle; // in radians, [0, 2*PI)
    private double collisionRadius;

    /**
     * Constructs a player at given coordinates and angle.
     *
     * @param x Initial X coordinate (column index in map units)
     * @param y Initial Y coordinate (row index in map units)
     * @param angle Initial viewing angle in radians
     */
    public Player(double x, double y, double angle) {
        this(x, y, angle, GameConfig.DEFAULT_COLLISION_RADIUS);
    }

    /**
     * Constructs a player with custom collision radius.
     */
    public Player(double x, double y, double angle, double collisionRadius) {
        this.x = x;
        this.y = y;
        this.angle = normalizeAngle(angle);
        this.collisionRadius = collisionRadius;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public double getAngleDegrees() {
        return Math.toDegrees(angle);
    }

    public void setAngle(double angle) {
        this.angle = normalizeAngle(angle);
    }

    public double getCollisionRadius() {
        return collisionRadius;
    }

    public void setCollisionRadius(double collisionRadius) {
        this.collisionRadius = collisionRadius;
    }

    /**
     * Normalizes an angle in radians into the range [0, 2*PI).
     */
    public static double normalizeAngle(double rad) {
        double twoPi = 2.0 * Math.PI;
        double normalized = rad % twoPi;
        if (normalized < 0.0) {
            normalized += twoPi;
        }
        return normalized;
    }

    /**
     * Moves the player forward along the current viewing angle.
     *
     * @param map The maze map for collision checks
     * @param step Step distance in map units
     * @return true if movement succeeded (full or sliding), false if completely blocked
     */
    public boolean moveForward(MazeMap map, double step) {
        double dx = Math.cos(angle) * step;
        double dy = Math.sin(angle) * step;
        return attemptMove(dx, dy, map);
    }

    /**
     * Moves the player backward opposite to the current viewing angle.
     */
    public boolean moveBackward(MazeMap map, double step) {
        double dx = -Math.cos(angle) * step;
        double dy = -Math.sin(angle) * step;
        return attemptMove(dx, dy, map);
    }

    /**
     * Strafes the player left perpendicular to the current viewing angle.
     */
    public boolean strafeLeft(MazeMap map, double step) {
        // -90 degrees from angle
        double strafeAngle = angle - Math.PI / 2.0;
        double dx = Math.cos(strafeAngle) * step;
        double dy = Math.sin(strafeAngle) * step;
        return attemptMove(dx, dy, map);
    }

    /**
     * Strafes the player right perpendicular to the current viewing angle.
     */
    public boolean strafeRight(MazeMap map, double step) {
        // +90 degrees from angle
        double strafeAngle = angle + Math.PI / 2.0;
        double dx = Math.cos(strafeAngle) * step;
        double dy = Math.sin(strafeAngle) * step;
        return attemptMove(dx, dy, map);
    }

    /**
     * Rotates camera to the left (counter-clockwise).
     *
     * @param deltaAngle Rotation step in radians
     */
    public void rotateLeft(double deltaAngle) {
        this.angle = normalizeAngle(this.angle - deltaAngle);
    }

    /**
     * Rotates camera to the right (clockwise).
     *
     * @param deltaAngle Rotation step in radians
     */
    public void rotateRight(double deltaAngle) {
        this.angle = normalizeAngle(this.angle + deltaAngle);
    }

    /**
     * Attempts to move the player by (dx, dy), supporting wall sliding.
     *
     * @return true if player moved (either full diagonal or sliding), false if blocked
     */
    public boolean attemptMove(double dx, double dy, MazeMap map) {
        double targetX = this.x + dx;
        double targetY = this.y + dy;

        // Try direct diagonal movement
        if (!isCollisionAt(targetX, targetY, map)) {
            this.x = targetX;
            this.y = targetY;
            return true;
        }

        // Try sliding along X only
        boolean moved = false;
        if (!isCollisionAt(targetX, this.y, map)) {
            this.x = targetX;
            moved = true;
        }

        // Try sliding along Y only
        if (!isCollisionAt(this.x, targetY, map)) {
            this.y = targetY;
            moved = true;
        }

        return moved;
    }

    /**
     * Checks if a circular bounding box with collisionRadius around (px, py)
     * intersects any solid wall or goes out of map bounds.
     */
    public boolean isCollisionAt(double px, double py, MazeMap map) {
        double r = this.collisionRadius;

        // Check center and 4 bounding box corners
        return map.isWallAt(px, py)
            || map.isWallAt(px - r, py - r)
            || map.isWallAt(px + r, py - r)
            || map.isWallAt(px - r, py + r)
            || map.isWallAt(px + r, py + r);
    }

    /**
     * Gets cardinal/intercardinal direction name (e.g. "East", "North-East").
     */
    public String getCompassDirection() {
        double deg = Math.toDegrees(angle);
        if (deg >= 337.5 || deg < 22.5) return "E (East)";
        if (deg >= 22.5 && deg < 67.5) return "SE (South-East)";
        if (deg >= 67.5 && deg < 112.5) return "S (South)";
        if (deg >= 112.5 && deg < 157.5) return "SW (South-West)";
        if (deg >= 157.5 && deg < 202.5) return "W (West)";
        if (deg >= 202.5 && deg < 247.5) return "NW (North-West)";
        if (deg >= 247.5 && deg < 292.5) return "N (North)";
        return "NE (North-East)";
    }

    /**
     * Returns an ASCII character representing the player's orientation on a mini-map.
     */
    public char getOrientationChar(boolean unicode) {
        double deg = Math.toDegrees(angle);
        if (deg >= 315 || deg < 45) {
            return unicode ? '►' : '>';
        } else if (deg >= 45 && deg < 135) {
            return unicode ? '▼' : 'v';
        } else if (deg >= 135 && deg < 225) {
            return unicode ? '◄' : '<';
        } else {
            return unicode ? '▲' : '^';
        }
    }
}

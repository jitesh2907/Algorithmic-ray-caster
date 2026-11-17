package com.raycaster.core;

/**
 * Computes camera direction and camera plane vectors based on player orientation and FOV.
 * Uses the standard camera plane formulation (as in Wolfenstein 3D) which establishes
 * planar perspective projection and naturally prevents fish-eye distortion.
 */
public class Camera {

    private double dirX;
    private double dirY;
    private double planeX;
    private double planeY;
    private double fovRadians;

    public Camera(double angle, double fovRadians) {
        this.fovRadians = fovRadians;
        update(angle, fovRadians);
    }

    /**
     * Updates camera direction and projection plane vectors based on player angle and FOV.
     *
     * @param angle Viewing direction angle in radians
     * @param fovRadians Field of view in radians
     */
    public void update(double angle, double fovRadians) {
        this.fovRadians = fovRadians;
        // Direction vector of camera
        this.dirX = Math.cos(angle);
        this.dirY = Math.sin(angle);

        // Plane magnitude = tan(FOV / 2)
        double planeLength = Math.tan(fovRadians / 2.0);

        // Plane vector is perpendicular to direction: (-dirY, dirX) * planeLength
        this.planeX = -this.dirY * planeLength;
        this.planeY = this.dirX * planeLength;
    }

    public double getDirX() {
        return dirX;
    }

    public double getDirY() {
        return dirY;
    }

    public double getPlaneX() {
        return planeX;
    }

    public double getPlaneY() {
        return planeY;
    }

    public double getFovRadians() {
        return fovRadians;
    }

    /**
     * Calculates the ray direction vector for a specific screen column x out of screenWidth.
     *
     * @param x Screen column index [0, screenWidth - 1]
     * @param screenWidth Total screen width in characters
     * @return 2D array [rayDirX, rayDirY]
     */
    public double[] getRayDirection(int x, int screenWidth) {
        // cameraX ranges from -1 (leftmost column) to +1 (rightmost column)
        double cameraX = 2.0 * x / (double) screenWidth - 1.0;
        double rayDirX = this.dirX + this.planeX * cameraX;
        double rayDirY = this.dirY + this.planeY * cameraX;
        return new double[] { rayDirX, rayDirY };
    }
}

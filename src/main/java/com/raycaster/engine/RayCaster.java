package com.raycaster.engine;

import com.raycaster.config.GameConfig;
import com.raycaster.core.Camera;
import com.raycaster.core.MazeMap;
import com.raycaster.core.Player;

/**
 * Implements the Digital Differential Analyzer (DDA) ray-casting algorithm.
 * Casts rays through the 2D grid map, detects wall collisions, and calculates
 * perpendicular distance with fish-eye distortion correction.
 */
public class RayCaster {

    /**
     * Casts rays across the entire horizontal field of view for all screen columns.
     *
     * @param player Current player state
     * @param camera Camera projection vectors
     * @param map Virtual maze map
     * @param screenWidth Number of horizontal columns
     * @param maxDist Maximum ray travel distance
     * @return Array of RayHit objects, one for each column
     */
    public RayHit[] castScreen(Player player, Camera camera, MazeMap map, int screenWidth, double maxDist) {
        RayHit[] hits = new RayHit[screenWidth];
        for (int x = 0; x < screenWidth; x++) {
            double[] rayDir = camera.getRayDirection(x, screenWidth);
            double rayDirX = rayDir[0];
            double rayDirY = rayDir[1];
            double rayAngle = Math.atan2(rayDirY, rayDirX);

            hits[x] = traceRayDDA(player.getX(), player.getY(), player.getAngle(), rayDirX, rayDirY, rayAngle, map, maxDist);
        }
        return hits;
    }

    /**
     * Casts a single ray at a specific geometric angle.
     *
     * @param player Current player
     * @param rayAngle Angle in radians
     * @param map Virtual maze map
     * @param maxDist Maximum ray travel distance
     * @return RayHit containing intersection details
     */
    public RayHit castRayByAngle(Player player, double rayAngle, MazeMap map, double maxDist) {
        double rayDirX = Math.cos(rayAngle);
        double rayDirY = Math.sin(rayAngle);
        return traceRayDDA(player.getX(), player.getY(), player.getAngle(), rayDirX, rayDirY, rayAngle, map, maxDist);
    }

    /**
     * Core DDA traversal routine.
     */
    private RayHit traceRayDDA(double posX, double posY, double playerAngle,
                               double rayDirX, double rayDirY, double rayAngle,
                               MazeMap map, double maxDist) {
        // Current map grid cell
        int mapX = (int) Math.floor(posX);
        int mapY = (int) Math.floor(posY);

        // Length of ray from one X or Y-side to next X or Y-side
        double deltaDistX = (rayDirX == 0.0) ? 1e30 : Math.abs(1.0 / rayDirX);
        double deltaDistY = (rayDirY == 0.0) ? 1e30 : Math.abs(1.0 / rayDirY);

        // Direction to step in (+1 or -1) and initial distance to first grid boundary
        int stepX;
        double sideDistX;
        if (rayDirX < 0.0) {
            stepX = -1;
            sideDistX = (posX - mapX) * deltaDistX;
        } else {
            stepX = 1;
            sideDistX = (mapX + 1.0 - posX) * deltaDistX;
        }

        int stepY;
        double sideDistY;
        if (rayDirY < 0.0) {
            stepY = -1;
            sideDistY = (posY - mapY) * deltaDistY;
        } else {
            stepY = 1;
            sideDistY = (mapY + 1.0 - posY) * deltaDistY;
        }

        boolean hit = false;
        int side = 0; // 0 = X-side (vertical wall line), 1 = Y-side (horizontal wall line)
        int wallType = 0;

        // Perform DDA grid stepping
        int maxSteps = (int) (maxDist * 4); // safety limit against infinite loops
        int stepsTaken = 0;

        while (!hit && stepsTaken++ < maxSteps) {
            // Jump to next map square in X or Y direction
            if (sideDistX < sideDistY) {
                sideDistX += deltaDistX;
                mapX += stepX;
                side = 0;
            } else {
                sideDistY += deltaDistY;
                mapY += stepY;
                side = 1;
            }

            // Check boundary or wall hit
            if (map.isOutOfBounds(mapX, mapY)) {
                hit = true;
                wallType = GameConfig.CELL_WALL;
                break;
            }

            wallType = map.getCell(mapX, mapY);
            if (wallType > 0) {
                hit = true;
            }
        }

        if (!hit) {
            return RayHit.noHit(maxDist, rayAngle);
        }

        // Calculate exact point on the wall where the ray hit (for wall texturing/shading)
        double t;
        if (side == 0) {
            t = (mapX - posX + (1.0 - stepX) / 2.0) / rayDirX;
        } else {
            t = (mapY - posY + (1.0 - stepY) / 2.0) / rayDirY;
        }

        double hitPointX = posX + t * rayDirX;
        double hitPointY = posY + t * rayDirY;

        double wallX;
        if (side == 0) {
            wallX = hitPointY;
        } else {
            wallX = hitPointX;
        }
        wallX -= Math.floor(wallX);

        // Euclidean distance from player to wall hit
        double euclideanDistance = Math.hypot(hitPointX - posX, hitPointY - posY);

        // Perpendicular distance projects euclidean distance onto player viewing angle to eliminate fish-eye distortion
        double perpWallDist = euclideanDistance * Math.cos(rayAngle - playerAngle);

        // Safeguard against negative or near-zero distance
        if (perpWallDist < 0.01) {
            perpWallDist = 0.01;
        }

        return new RayHit(true, perpWallDist, euclideanDistance, mapX, mapY, side, wallType, wallX, rayAngle);
    }
}

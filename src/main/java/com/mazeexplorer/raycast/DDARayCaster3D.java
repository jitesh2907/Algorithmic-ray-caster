package com.mazeexplorer.raycast;

import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.maze.MazeGrid;

/**
 * High-performance Digital Differential Analyzer (DDA) for ray casting on
 * discrete grid maps.
 * Used for AI line-of-sight checks, weapon hit detection, and spatial queries.
 */
public class DDARayCaster3D {

    private final MazeGrid mazeGrid;
    private final float cellSize;

    public DDARayCaster3D(MazeGrid mazeGrid) {
        this(mazeGrid, GameSettings.WALL_SIZE);
    }

    public DDARayCaster3D(MazeGrid mazeGrid, float cellSize) {
        this.mazeGrid = mazeGrid;
        this.cellSize = cellSize;
    }

    /**
     * Casts a ray from (startX, startZ) in direction (dirX, dirZ) up to maxDist.
     */
    public RayHit3D castRay(float startX, float startZ, float dirX, float dirZ, float maxDist) {
        float norm = (float) Math.hypot(dirX, dirZ);
        if (norm < 1e-6f) {
            return RayHit3D.miss(maxDist);
        }
        float rX = dirX / norm;
        float rZ = dirZ / norm;

        // Convert world to grid coordinates
        float originGridX = startX / cellSize;
        float originGridZ = startZ / cellSize;

        int mapX = (int) Math.floor(originGridX);
        int mapZ = (int) Math.floor(originGridZ);

        float deltaDistX = (rX == 0f) ? 1e30f : Math.abs(1.0f / rX);
        float deltaDistZ = (rZ == 0f) ? 1e30f : Math.abs(1.0f / rZ);

        int stepX = (rX < 0f) ? -1 : 1;
        float sideDistX = (rX < 0f) ? (originGridX - mapX) * deltaDistX : (mapX + 1.0f - originGridX) * deltaDistX;

        int stepZ = (rZ < 0f) ? -1 : 1;
        float sideDistZ = (rZ < 0f) ? (originGridZ - mapZ) * deltaDistZ : (mapZ + 1.0f - originGridZ) * deltaDistZ;

        boolean hit = false;
        int side = 0;
        float maxGridDist = maxDist / cellSize;

        int maxSteps = (int) (maxGridDist * 4) + 8;
        int steps = 0;

        while (!hit && steps++ < maxSteps) {
            if (sideDistX < sideDistZ) {
                sideDistX += deltaDistX;
                mapX += stepX;
                side = 0;
            } else {
                sideDistZ += deltaDistZ;
                mapZ += stepZ;
                side = 1;
            }

            float t = (side == 0)
                    ? (mapX - originGridX + (1.0f - stepX) / 2.0f) / rX
                    : (mapZ - originGridZ + (1.0f - stepZ) / 2.0f) / rZ;

            if (t > maxGridDist) {
                break;
            }

            if (mazeGrid.isWall(mapX, mapZ)) {
                hit = true;
                break;
            }
        }

        if (!hit) {
            return RayHit3D.miss(maxDist);
        }

        float t = (side == 0)
                ? (mapX - originGridX + (1.0f - stepX) / 2.0f) / rX
                : (mapZ - originGridZ + (1.0f - stepZ) / 2.0f) / rZ;

        float distance = t * cellSize;
        float hitX = startX + rX * distance;
        float hitZ = startZ + rZ * distance;

        return new RayHit3D(true, distance, hitX, hitZ, mapX, mapZ, side);
    }

    /**
     * Checks if line-of-sight between two world points is unobstructed by walls.
     */
    public boolean hasLineOfSight(float x1, float z1, float x2, float z2) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float dist = (float) Math.hypot(dx, dz);
        if (dist < 1e-4f)
            return true;

        RayHit3D hit = castRay(x1, z1, dx, dz, dist);
        return !hit.isHit() || hit.getDistance() >= (dist - 0.05f);
    }
}

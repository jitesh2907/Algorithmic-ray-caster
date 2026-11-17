package com.mazeexplorer.collision;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.mazeexplorer.builder.Maze3DBuilder;
import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.maze.MazeGrid;

/**
 * High-precision discrete-continuous collision detection and wall sliding system.
 * Evaluates entity bounding cylinders against maze boundary limits and solid cell AABBs.
 */
public class CollisionSystem {

    private final MazeGrid mazeGrid;
    private final float cellSize;
    private final float totalWidth;
    private final float totalDepth;

    public CollisionSystem(MazeGrid mazeGrid) {
        this(mazeGrid, Maze3DBuilder.CELL_SIZE);
    }

    public CollisionSystem(MazeGrid mazeGrid, float cellSize) {
        this.mazeGrid = mazeGrid;
        this.cellSize = cellSize;
        this.totalWidth = mazeGrid.getWidth() * cellSize;
        this.totalDepth = mazeGrid.getHeight() * cellSize;
    }

    public float getCellSize() {
        return cellSize;
    }

    /**
     * Checks if a cylinder bounding circle at (worldX, worldZ) with given radius intersects
     * any wall cell or maze boundary.
     *
     * @param worldX World X position
     * @param worldZ World Z position
     * @param radius Entity collision radius
     * @return true if overlapping a wall or out-of-bounds boundary
     */
    public boolean checkCollision(float worldX, float worldZ, float radius) {
        // 1. Boundary check: ensure entity does not escape perimeter boundaries
        if (worldX - radius < 0f || worldX + radius > totalWidth
                || worldZ - radius < 0f || worldZ + radius > totalDepth) {
            return true;
        }

        // 2. Identify grid cell range overlapping entity bounding box
        int minGridX = (int) Math.floor((worldX - radius) / cellSize);
        int maxGridX = (int) Math.floor((worldX + radius) / cellSize);
        int minGridZ = (int) Math.floor((worldZ - radius) / cellSize);
        int maxGridZ = (int) Math.floor((worldZ + radius) / cellSize);

        // Clamp to valid grid indices
        minGridX = Math.max(0, minGridX);
        maxGridX = Math.min(mazeGrid.getWidth() - 1, maxGridX);
        minGridZ = Math.max(0, minGridZ);
        maxGridZ = Math.min(mazeGrid.getHeight() - 1, maxGridZ);

        float radiusSq = radius * radius;

        // 3. Test intersection with each solid wall cell AABB
        for (int gz = minGridZ; gz <= maxGridZ; gz++) {
            for (int gx = minGridX; gx <= maxGridX; gx++) {
                if (mazeGrid.isWall(gx, gz)) {
                    float boxMinX = gx * cellSize;
                    float boxMaxX = (gx + 1) * cellSize;
                    float boxMinZ = gz * cellSize;
                    float boxMaxZ = (gz + 1) * cellSize;

                    float closestX = FastMath.clamp(worldX, boxMinX, boxMaxX);
                    float closestZ = FastMath.clamp(worldZ, boxMinZ, boxMaxZ);

                    float dx = worldX - closestX;
                    float dz = worldZ - closestZ;

                    if ((dx * dx + dz * dz) < radiusSq) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Resolves movement from current position with delta (dx, dz) supporting smooth wall sliding.
     *
     * @param currentX Current X coordinate
     * @param currentZ Current Z coordinate
     * @param dx Desired movement along X
     * @param dz Desired movement along Z
     * @param radius Collision radius
     * @return Resolved world position [X, 0, Z]
     */
    public Vector3f resolveMovement(float currentX, float currentZ, float dx, float dz, float radius) {
        float targetX = currentX + dx;
        float targetZ = currentZ + dz;

        // 1. Try full diagonal displacement
        if (!checkCollision(targetX, targetZ, radius)) {
            return new Vector3f(targetX, 0f, targetZ);
        }

        // 2. Try sliding along X axis only
        boolean canMoveX = (dx != 0f) && !checkCollision(targetX, currentZ, radius);

        // 3. Try sliding along Z axis only
        boolean canMoveZ = (dz != 0f) && !checkCollision(currentX, targetZ, radius);

        float resolvedX = canMoveX ? targetX : currentX;
        float resolvedZ = canMoveZ ? targetZ : currentZ;

        return new Vector3f(resolvedX, 0f, resolvedZ);
    }
}

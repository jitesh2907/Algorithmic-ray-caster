package com.mazeexplorer.pathfinding;

import com.mazeexplorer.maze.MazeGrid;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Breadth-First Search (BFS) pathfinder operating on discrete 2D maze grid connectivity.
 * Guarantees optimal (shortest) path traversal from enemy or entity to target location.
 */
public class BFSPathfinder {

    private static final int[][] CARDINAL_DIRS = {
        {0, -1}, // North
        {1, 0},  // East
        {0, 1},  // South
        {-1, 0}  // West
    };

    private final MazeGrid mazeGrid;

    public BFSPathfinder(MazeGrid mazeGrid) {
        this.mazeGrid = mazeGrid;
    }

    /**
     * Finds shortest path from (startX, startZ) to (goalX, goalZ).
     *
     * @return Ordered list of PathNodes from start to goal, or empty list if no path exists.
     */
    public List<PathNode> findPath(int startX, int startZ, int goalX, int goalZ) {
        if (!mazeGrid.isWalkable(startX, startZ) || !mazeGrid.isWalkable(goalX, goalZ)) {
            return Collections.emptyList();
        }

        PathNode start = new PathNode(startX, startZ);
        PathNode goal = new PathNode(goalX, goalZ);

        if (start.equals(goal)) {
            return List.of(start);
        }

        Queue<PathNode> queue = new ArrayDeque<>();
        Map<PathNode, PathNode> parentMap = new HashMap<>();
        boolean[][] visited = new boolean[mazeGrid.getHeight()][mazeGrid.getWidth()];

        queue.add(start);
        visited[startZ][startX] = true;
        boolean found = false;

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();

            if (current.equals(goal)) {
                found = true;
                break;
            }

            for (int[] dir : CARDINAL_DIRS) {
                int nextX = current.x + dir[0];
                int nextZ = current.z + dir[1];

                if (mazeGrid.isWalkable(nextX, nextZ) && !visited[nextZ][nextX]) {
                    visited[nextZ][nextX] = true;
                    PathNode neighbor = new PathNode(nextX, nextZ);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!found) {
            return Collections.emptyList();
        }

        // Reconstruct path from goal to start
        List<PathNode> path = new ArrayList<>();
        PathNode curr = goal;
        while (curr != null) {
            path.add(curr);
            curr = parentMap.get(curr);
        }
        Collections.reverse(path);
        return path;
    }
}


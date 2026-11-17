package com.mazeexplorer.pathfinding;

import java.util.Objects;

/**
 * Discrete grid node representing a waypoint in the maze graph.
 */
public class PathNode {

    public final int x;
    public final int z;

    public PathNode(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathNode)) return false;
        PathNode other = (PathNode) o;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }
}


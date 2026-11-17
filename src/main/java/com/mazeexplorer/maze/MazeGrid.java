package com.mazeexplorer.maze;

/**
 * 2D discrete cell representation of the maze environment.
 * Maps discrete coordinates (x, z) to tile cell types.
 */
public class MazeGrid {

    public static final int CELL_EMPTY = 0;
    public static final int CELL_WALL = 1;
    public static final int CELL_EXIT = 2;
    public static final int CELL_SPAWN = 3;
    public static final int CELL_KEY = 4;

    private final int width;
    private final int height;
    private final int[][] grid;

    public MazeGrid(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Maze dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isOutOfBounds(int x, int z) {
        return x < 0 || x >= width || z < 0 || z >= height;
    }

    public int getCell(int x, int z) {
        if (isOutOfBounds(x, z)) {
            return CELL_WALL; // Boundary safety
        }
        return grid[z][x];
    }

    public void setCell(int x, int z, int cellType) {
        if (!isOutOfBounds(x, z)) {
            grid[z][x] = cellType;
        }
    }

    public boolean isWall(int x, int z) {
        return getCell(x, z) == CELL_WALL;
    }

    public boolean isWalkable(int x, int z) {
        return !isOutOfBounds(x, z) && getCell(x, z) != CELL_WALL;
    }
}


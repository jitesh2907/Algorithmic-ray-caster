package com.raycaster.core;

import com.raycaster.config.GameConfig;

/**
 * Represents the 2D grid of the virtual environment.
 * Cells with value 1 (or >0) are solid walls, while 0 represents navigable empty corridors.
 */
public class MazeMap {

    private final int width;
    private final int height;
    private final int[][] grid;
    private final String name;

    /**
     * Constructs a map with a custom 2D grid.
     *
     * @param grid 2D array [row][column] representing [y][x]
     * @param name Descriptive name of the map
     */
    public MazeMap(int[][] grid, String name) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            throw new IllegalArgumentException("Grid must be non-null and have non-zero dimensions");
        }
        this.height = grid.length;
        this.width = grid[0].length;
        this.name = name != null ? name : "Custom Map";
        this.grid = new int[height][width];

        // Deep copy grid to ensure immutability
        for (int y = 0; y < height; y++) {
            if (grid[y].length != width) {
                throw new IllegalArgumentException("Inconsistent row width in map grid at row " + y);
            }
            System.arraycopy(grid[y], 0, this.grid[y], 0, width);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    /**
     * Checks if coordinates (x, y) fall outside the map boundary.
     */
    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    /**
     * Checks if coordinates (x, y) fall outside the map boundary (double coordinates).
     */
    public boolean isOutOfBounds(double x, double y) {
        return x < 0.0 || x >= width || y < 0.0 || y >= height;
    }

    /**
     * Returns the cell value at integer grid coordinates (x, y).
     * Out-of-bounds queries return WALL (1) to safeguard against ray escape.
     */
    public int getCell(int x, int y) {
        if (isOutOfBounds(x, y)) {
            return GameConfig.CELL_WALL;
        }
        return grid[y][x];
    }

    /**
     * Checks if the cell at integer coordinates (x, y) is solid wall.
     */
    public boolean isWall(int x, int y) {
        return getCell(x, y) != GameConfig.CELL_EMPTY;
    }

    /**
     * Checks if floating-point coordinates (x, y) fall inside a wall cell.
     */
    public boolean isWallAt(double x, double y) {
        int cellX = (int) Math.floor(x);
        int cellY = (int) Math.floor(y);
        return isWall(cellX, cellY);
    }

    /**
     * Preset 1: Default 10x8 Maze according to Project Specification (Section 7).
     * Dimensions: 10 columns by 8 rows.
     */
    public static MazeMap createDefaultMap() {
        int[][] defaultGrid = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 1, 1, 0, 0, 1, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        return new MazeMap(defaultGrid, "10x8 Classic Maze");
    }

    /**
     * Preset 2: The Pillars (10x8 open hall with symmetric architectural pillars).
     */
    public static MazeMap createPillarsMap() {
        int[][] pillarsGrid = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 1, 0, 0, 0, 1},
            {1, 0, 0, 0, 1, 1, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        return new MazeMap(pillarsGrid, "10x8 Pillar Hall");
    }

    /**
     * Preset 3: The Labyrinth (12x10 complex winding corridors).
     */
    public static MazeMap createLabyrinthMap() {
        int[][] labyrinthGrid = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        return new MazeMap(labyrinthGrid, "12x10 Winding Labyrinth");
    }
}

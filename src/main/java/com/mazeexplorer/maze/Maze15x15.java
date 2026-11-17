package com.mazeexplorer.maze;

/**
 * 15x15 Dungeon Maze data model.
 * Pure Java 2D array representation separating game logic and map layout from rendering.
 * 1 represents solid stone walls, 0 represents walkable corridors.
 */
public final class Maze15x15 {

    public static final int WIDTH = 15;
    public static final int HEIGHT = 15;

    public static final int START_X = 1;
    public static final int START_Z = 1;

    public static final int EXIT_X = 13;
    public static final int EXIT_Z = 13;

    private static final int[][] GRID_DATA = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1},
        {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1},
        {1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1},
        {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1},
        {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1},
        {1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
        {1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
        {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
        {1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1},
        {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    private Maze15x15() {}

    /**
     * Creates and initializes the 15x15 MazeGrid instance.
     */
    public static MazeGrid createGrid() {
        MazeGrid grid = new MazeGrid(WIDTH, HEIGHT);
        for (int z = 0; z < HEIGHT; z++) {
            for (int x = 0; x < WIDTH; x++) {
                grid.setCell(x, z, GRID_DATA[z][x]);
            }
        }
        grid.setCell(START_X, START_Z, MazeGrid.CELL_SPAWN);
        grid.setCell(EXIT_X, EXIT_Z, MazeGrid.CELL_EXIT);
        return grid;
    }

    public static int[][] getRawData() {
        int[][] copy = new int[HEIGHT][WIDTH];
        for (int z = 0; z < HEIGHT; z++) {
            System.arraycopy(GRID_DATA[z], 0, copy[z], 0, WIDTH);
        }
        return copy;
    }
}

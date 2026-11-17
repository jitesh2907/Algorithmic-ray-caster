package com.mazeexplorer;

import com.mazeexplorer.collision.CollisionSystem;
import com.mazeexplorer.entity.Collectible;
import com.mazeexplorer.entity.ExitDoor;
import com.mazeexplorer.maze.MazeGrid;
import com.mazeexplorer.pathfinding.BFSPathfinder;
import com.mazeexplorer.pathfinding.PathNode;
import com.mazeexplorer.raycast.DDARayCaster3D;
import com.mazeexplorer.raycast.RayHit3D;
import com.mazeexplorer.stats.HealthSystem;
import com.mazeexplorer.stats.ScoreManager;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Automated test suite verifying the core mathematical and algorithmic subsystems
 * of ALGORITHMIC 3D MAZE EXPLORER without requiring an active OpenGL window context.
 */
public class AlgorithmVerificationTest {

    private MazeGrid testMaze;

    @BeforeEach
    public void setup() {
        // Simple 5x5 test grid
        // 1 1 1 1 1
        // 1 0 0 0 1
        // 1 0 1 0 1
        // 1 0 0 0 1
        // 1 1 1 1 1
        testMaze = new MazeGrid(5, 5);
        for (int x = 0; x < 5; x++) {
            testMaze.setCell(x, 0, MazeGrid.CELL_WALL);
            testMaze.setCell(x, 4, MazeGrid.CELL_WALL);
            testMaze.setCell(0, x, MazeGrid.CELL_WALL);
            testMaze.setCell(4, x, MazeGrid.CELL_WALL);
        }
        testMaze.setCell(2, 2, MazeGrid.CELL_WALL); // Central pillar
    }

    @Test
    public void testMazeBoundaryProtection() {
        Assertions.assertTrue(testMaze.isWall(-1, 0), "Negative coordinates should return solid wall");
        Assertions.assertTrue(testMaze.isWall(5, 2), "Out of bounds coordinates should return solid wall");
        Assertions.assertTrue(testMaze.isWall(2, 2), "Center pillar should be a wall");
        Assertions.assertTrue(testMaze.isWalkable(1, 1), "Corridor cell should be walkable");
    }

    @Test
    public void testBFSPathfindingOptimalPath() {
        BFSPathfinder pathfinder = new BFSPathfinder(testMaze);
        // Path from (1, 1) around center pillar to (3, 3)
        List<PathNode> path = pathfinder.findPath(1, 1, 3, 3);

        Assertions.assertFalse(path.isEmpty(), "A valid path must exist around the pillar");
        Assertions.assertEquals(new PathNode(1, 1), path.get(0), "Path must start at (1, 1)");
        Assertions.assertEquals(new PathNode(3, 3), path.get(path.size() - 1), "Path must end at (3, 3)");
        Assertions.assertFalse(path.contains(new PathNode(2, 2)), "Path must not traverse center wall");
    }

    @Test
    public void testDDARayCasterLineOfSight() {
        DDARayCaster3D rayCaster = new DDARayCaster3D(testMaze, 2.0f);

        // Clear line of sight along corridor row 1 (from grid cell (1, 1) to (3, 1))
        // World coordinates: cell (1,1) center is (3.0, 3.0), cell (3,1) center is (7.0, 3.0)
        boolean clearSight = rayCaster.hasLineOfSight(3.0f, 3.0f, 7.0f, 3.0f);
        Assertions.assertTrue(clearSight, "Corridor line-of-sight must be clear");

        // Obstructed line of sight across center pillar (from (3.0, 5.0) to (7.0, 5.0) across (2, 2))
        boolean blockedSight = rayCaster.hasLineOfSight(3.0f, 5.0f, 7.0f, 5.0f);
        Assertions.assertFalse(blockedSight, "Line of sight across center wall must be blocked");
    }

    @Test
    public void testCollisionDetectionAgainstWalls() {
        CollisionSystem collision = new CollisionSystem(testMaze, 2.0f);

        // Center of cell (1,1) is (3.0, 3.0), well away from walls
        Assertions.assertFalse(collision.checkCollision(3.0f, 3.0f, 0.4f), "Corridor center should not collide");

        // Edge near wall x=0 (wall extends [0.0, 2.0]), checking at x=2.1 with radius 0.4 intersects wall at 2.0
        Assertions.assertTrue(collision.checkCollision(2.1f, 3.0f, 0.4f), "Entity overlapping wall edge should collide");
    }

    @Test
    public void testHealthSystemLifecycle() {
        HealthSystem health = new HealthSystem(100, 0.5f);

        boolean damaged = health.applyDamage(30);
        Assertions.assertTrue(damaged);
        Assertions.assertEquals(70, health.getCurrentHealth());

        // During invulnerability cooldown, further damage is ignored
        boolean blocked = health.applyDamage(20);
        Assertions.assertFalse(blocked);
        Assertions.assertEquals(70, health.getCurrentHealth());

        // Cooldown expiration
        health.update(0.6f);
        health.applyDamage(70);
        Assertions.assertEquals(0, health.getCurrentHealth());
        Assertions.assertTrue(health.isDead());
    }

    @Test
    public void testScoreAndCollectibleProgression() {
        ScoreManager score = new ScoreManager();
        Collectible gem = new Collectible(Collectible.Type.GEM, 5.0f, 1.0f, 5.0f);

        Assertions.assertTrue(gem.checkPickup(5.1f, 5.0f, 0.5f), "Within pickup radius should collect gem");
        score.collectGem(gem.getType().getPointValue());
        Assertions.assertEquals(500, score.getScore());
        Assertions.assertEquals(1, score.getGemsCollected());

        // Exit door key requirement
        ExitDoor door = new ExitDoor(9.0f, 9.0f);
        Assertions.assertFalse(door.tryUnlock(false), "Door cannot open without key");

        score.collectKey();
        Assertions.assertTrue(score.useKey(), "Key must be consumable");
        Assertions.assertTrue(door.tryUnlock(true), "Door should open with valid key");
    }
}


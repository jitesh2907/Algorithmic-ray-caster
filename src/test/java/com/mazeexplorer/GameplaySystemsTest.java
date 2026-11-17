package com.mazeexplorer;

import com.mazeexplorer.ai.Enemy;
import com.mazeexplorer.ai.EnemyController;
import com.mazeexplorer.entity.Collectible;
import com.mazeexplorer.entity.ExitDoor;
import com.mazeexplorer.maze.Maze15x15;
import com.mazeexplorer.maze.MazeGrid;
import com.mazeexplorer.pathfinding.BFSPathfinder;
import com.mazeexplorer.pathfinding.PathNode;
import com.mazeexplorer.player.Player;
import com.mazeexplorer.raycast.DDARayCaster3D;
import com.mazeexplorer.raycast.RayHit3D;
import com.mazeexplorer.stats.ScoreManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Independent verification test suite for Milestone 4 & 5 algorithmic gameplay systems:
 * 1. DDA Ray Casting
 * 2. BFS Pathfinding
 * 3. Enemy AI (Patrol, LOS Detection, Chase, Reset, Attack)
 * 4. Collectibles (3 Keys, 5 Coins, Pickup, Scoring)
 * 5. Exit Door (Locked, Unlocked at 3 keys, Victory Condition)
 */
public class GameplaySystemsTest {

    private MazeGrid mazeGrid;
    private static final float CELL_SIZE = 3.0f;

    @BeforeEach
    public void setup() {
        this.mazeGrid = Maze15x15.createGrid();
    }

    @Test
    public void testDDARayCasterHitInfoAndDistance() {
        DDARayCaster3D rayCaster = new DDARayCaster3D(mazeGrid, CELL_SIZE);

        // Cell (1, 1) world center is (4.5, 4.5)
        // Row 1 layout: {1, 0, 0, 0, 1, ...} -> col 4 is wall at x = 12.0
        float originX = 4.5f;
        float originZ = 4.5f;

        // Cast East (+X)
        RayHit3D hitEast = rayCaster.castRay(originX, originZ, 1.0f, 0.0f, 50.0f);
        Assertions.assertTrue(hitEast.isHit(), "East ray must hit wall at col 4");
        Assertions.assertEquals(4, hitEast.getGridX(), "Hit wall gridX must be 4");
        Assertions.assertEquals(1, hitEast.getGridZ(), "Hit wall gridZ must be 1");
        Assertions.assertEquals(0, hitEast.getSide(), "Side 0 represents vertical/X-axis wall face");
        Assertions.assertEquals(7.5f, hitEast.getDistance(), 0.01f, "Distance from 4.5 to 12.0 must be 7.5m");

        // Cast West (-X) -> hits wall at col 0 at x = 3.0
        RayHit3D hitWest = rayCaster.castRay(originX, originZ, -1.0f, 0.0f, 50.0f);
        Assertions.assertTrue(hitWest.isHit(), "West ray must hit wall at col 0");
        Assertions.assertEquals(0, hitWest.getGridX(), "Hit wall gridX must be 0");
        Assertions.assertEquals(1.5f, hitWest.getDistance(), 0.01f, "Distance from 4.5 to 3.0 must be 1.5m");

        // Cast North (-Z) -> hits wall at row 0 at z = 3.0
        RayHit3D hitNorth = rayCaster.castRay(originX, originZ, 0.0f, -1.0f, 50.0f);
        Assertions.assertTrue(hitNorth.isHit(), "North ray must hit boundary wall at row 0");
        Assertions.assertEquals(1, hitNorth.getSide(), "Side 1 represents horizontal/Z-axis wall face");
        Assertions.assertEquals(1.5f, hitNorth.getDistance(), 0.01f);
    }

    @Test
    public void testDDARayCasterLineOfSight() {
        DDARayCaster3D rayCaster = new DDARayCaster3D(mazeGrid, CELL_SIZE);

        // Corridor between (1,1) [4.5, 4.5] and (3,1) [10.5, 4.5] is open
        boolean openLOS = rayCaster.hasLineOfSight(4.5f, 4.5f, 10.5f, 4.5f);
        Assertions.assertTrue(openLOS, "Direct corridor should have unobstructed line of sight");

        // Cell (2,2) is a solid wall: Line of sight between (1,2) [4.5, 7.5] and (3,2) [10.5, 7.5] is blocked
        boolean blockedLOS = rayCaster.hasLineOfSight(4.5f, 7.5f, 10.5f, 7.5f);
        Assertions.assertFalse(blockedLOS, "Wall at cell (2, 2) must block line of sight");
    }

    @Test
    public void testBFSPathfinderShortestPathValidity() {
        BFSPathfinder pathfinder = new BFSPathfinder(mazeGrid);

        // Path from Start (1, 1) to Exit (13, 13)
        List<PathNode> path = pathfinder.findPath(1, 1, 13, 13);

        Assertions.assertFalse(path.isEmpty(), "A valid traversable path must exist from Start to Exit");
        Assertions.assertEquals(new PathNode(1, 1), path.get(0), "Path must originate at Start (1, 1)");
        Assertions.assertEquals(new PathNode(13, 13), path.get(path.size() - 1), "Path must conclude at Exit (13, 13)");

        // Verify every single step is non-wall and contiguous
        for (int i = 0; i < path.size(); i++) {
            PathNode node = path.get(i);
            Assertions.assertTrue(mazeGrid.isWalkable(node.x, node.z),
                "Path step [" + node.x + ", " + node.z + "] must be a walkable corridor");

            if (i > 0) {
                PathNode prev = path.get(i - 1);
                int manhattanDist = Math.abs(node.x - prev.x) + Math.abs(node.z - prev.z);
                Assertions.assertEquals(1, manhattanDist, "Adjacent path nodes must be orthogonal neighbors");
            }
        }
    }

    @Test
    public void testEnemyPatrolMovement() {
        List<PathNode> patrolWaypoints = List.of(
            new PathNode(9, 7),
            new PathNode(11, 7),
            new PathNode(11, 11),
            new PathNode(7, 11)
        );

        float spawnX = 9.5f * CELL_SIZE; // (28.5)
        float spawnZ = 7.5f * CELL_SIZE; // (22.5)
        Enemy enemy = new Enemy(spawnX, 1.2f, spawnZ);
        EnemyController controller = new EnemyController(enemy, mazeGrid, CELL_SIZE, patrolWaypoints);

        Assertions.assertEquals(Enemy.State.PATROL, enemy.getState());

        Player player = new Player(1.5f * CELL_SIZE, 0f, 1.5f * CELL_SIZE); // Far away at (1, 1)

        // Advance simulation by 0.5s
        controller.update(0.5f, player);

        // Enemy must remain in PATROL because player is far beyond detection radius
        Assertions.assertEquals(Enemy.State.PATROL, enemy.getState());
        Assertions.assertFalse(controller.isPlayerInSight());
    }

    @Test
    public void testEnemyLineOfSightDetectionAndBFSPursuit() {
        List<PathNode> patrolWaypoints = List.of(new PathNode(9, 7), new PathNode(11, 7));

        // Enemy at (9, 7) world center
        float enemyX = 9.5f * CELL_SIZE;
        float enemyZ = 7.5f * CELL_SIZE;
        Enemy enemy = new Enemy(enemyX, 1.2f, enemyZ);
        EnemyController controller = new EnemyController(enemy, mazeGrid, CELL_SIZE, patrolWaypoints);

        // In Maze15x15 row 7: cells (7,7), (8,7), (9,7) form an open horizontal corridor
        // Place player at (7, 7) world center (dist = 6.0m <= 12m, LOS is clear)
        Player player = new Player(7.5f * CELL_SIZE, 0f, 7.5f * CELL_SIZE);

        controller.update(0.1f, player);

        Assertions.assertTrue(controller.isPlayerInSight(), "Enemy must have line of sight to player");
        Assertions.assertEquals(Enemy.State.CHASE, enemy.getState(), "Enemy must transition to CHASE state");
        Assertions.assertNotNull(enemy.getNextWaypoint(), "Enemy must have generated BFS pursuit path");
    }

    @Test
    public void testEnemyObstructedLOSDoesNotTriggerChase() {
        List<PathNode> patrolWaypoints = List.of(new PathNode(1, 7));

        // Enemy at (1, 7) world center
        float enemyX = 1.5f * CELL_SIZE; // 4.5
        float enemyZ = 7.5f * CELL_SIZE; // 22.5
        Enemy enemy = new Enemy(enemyX, 1.2f, enemyZ);
        EnemyController controller = new EnemyController(enemy, mazeGrid, CELL_SIZE, patrolWaypoints);

        // Place player at (1, 5) [4.5, 16.5] -> cell (1, 6) is a solid wall directly in between
        Player player = new Player(1.5f * CELL_SIZE, 0f, 5.5f * CELL_SIZE);

        controller.update(0.1f, player);

        Assertions.assertFalse(controller.isPlayerInSight(), "Wall at (1, 6) must obstruct line of sight");
        Assertions.assertEquals(Enemy.State.PATROL, enemy.getState(), "Enemy must remain in PATROL when player is hidden");
    }

    @Test
    public void testEnemyEscapeAndResetToPatrol() {
        List<PathNode> patrolWaypoints = List.of(new PathNode(9, 7), new PathNode(11, 7));
        Enemy enemy = new Enemy(9.5f * CELL_SIZE, 1.2f, 7.5f * CELL_SIZE);
        EnemyController controller = new EnemyController(enemy, mazeGrid, CELL_SIZE, patrolWaypoints);

        // Put player in line-of-sight to trigger chase
        Player player = new Player(7.5f * CELL_SIZE, 0f, 7.5f * CELL_SIZE);
        controller.update(0.1f, player);
        Assertions.assertEquals(Enemy.State.CHASE, enemy.getState());

        // Move player beyond LOSE_INTEREST_RADIUS (e.g. 25m away)
        player.setPosition(1.5f * CELL_SIZE, 0f, 1.5f * CELL_SIZE);

        controller.update(0.1f, player);
        Assertions.assertEquals(Enemy.State.PATROL, enemy.getState(), "Enemy must reset to PATROL when player escapes");
    }

    @Test
    public void testEnemyMeleeAttackInflictsDamage() {
        Enemy enemy = new Enemy(10.0f, 1.2f, 10.0f);
        EnemyController controller = new EnemyController(enemy, mazeGrid, CELL_SIZE, List.of(new PathNode(3, 3)));

        // Place player in melee range (dist = 0.5m < ATTACK_RADIUS)
        Player player = new Player(10.5f, 0f, 10.0f);
        int initialHp = player.getHealth().getCurrentHealth();

        controller.update(0.05f, player);

        Assertions.assertEquals(initialHp - EnemyController.ATTACK_DAMAGE, player.getHealth().getCurrentHealth(),
            "Enemy melee strike must deduct attack damage from player");
        Assertions.assertFalse(enemy.canAttack(), "Attack cooldown must be active immediately after strike");
    }

    @Test
    public void testThreeKeysAndFiveCoinsGameplayMechanics() {
        ScoreManager score = new ScoreManager();

        // 3 Keys
        int[][] keyPositions = {{13, 1}, {7, 3}, {1, 13}};
        List<Collectible> keys = new ArrayList<>();
        for (int[] pos : keyPositions) {
            Assertions.assertTrue(mazeGrid.isWalkable(pos[0], pos[1]), "Key must be in walkable cell");
            keys.add(new Collectible(Collectible.Type.KEY, (pos[0] + 0.5f) * CELL_SIZE, 1.0f, (pos[1] + 0.5f) * CELL_SIZE));
        }
        Assertions.assertEquals(3, keys.size(), "Must instantiate exactly 3 keys");

        // 5 Coins
        int[][] coinPositions = {{3, 1}, {5, 5}, {11, 3}, {3, 11}, {9, 9}};
        List<Collectible> coins = new ArrayList<>();
        for (int[] pos : coinPositions) {
            Assertions.assertTrue(mazeGrid.isWalkable(pos[0], pos[1]), "Coin must be in walkable cell");
            coins.add(new Collectible(Collectible.Type.COIN, (pos[0] + 0.5f) * CELL_SIZE, 0.8f, (pos[1] + 0.5f) * CELL_SIZE));
        }
        Assertions.assertEquals(5, coins.size(), "Must instantiate exactly 5 coins");

        // Collect all 5 coins
        for (Collectible coin : coins) {
            boolean picked = coin.checkPickup(coin.getBaseWorldX(), coin.getBaseWorldZ(), 1.0f);
            Assertions.assertTrue(picked, "Player at coin coordinate must trigger pickup");
            score.collectCoin(coin.getType().getPointValue());
        }
        Assertions.assertEquals(5, score.getCoinsCollected(), "Must record 5 collected coins");
        Assertions.assertEquals(500, score.getScore(), "5 coins @ 100pts = 500pts");

        // Collect all 3 keys
        for (Collectible key : keys) {
            boolean picked = key.checkPickup(key.getBaseWorldX(), key.getBaseWorldZ(), 1.0f);
            Assertions.assertTrue(picked, "Player at key coordinate must trigger pickup");
            score.collectKey();
        }
        Assertions.assertEquals(3, score.getKeysCollected(), "Must record 3 collected keys");
        Assertions.assertEquals(3500, score.getScore(), "500 coin pts + 3000 key pts = 3500pts");

        // Verify duplicate pickup cannot occur
        Assertions.assertFalse(keys.get(0).checkPickup(keys.get(0).getBaseWorldX(), keys.get(0).getBaseWorldZ(), 1.0f),
            "Already collected key cannot be collected again");
    }

    @Test
    public void testExitDoorLockingAndVictoryUnlock() {
        float exitWorldX = (Maze15x15.EXIT_X + 0.5f) * CELL_SIZE;
        float exitWorldZ = (Maze15x15.EXIT_Z + 0.5f) * CELL_SIZE;
        ExitDoor exitDoor = new ExitDoor(exitWorldX, exitWorldZ);

        Assertions.assertFalse(exitDoor.isUnlocked(), "Exit door must start locked");

        // Attempt unlock with 0 keys
        Assertions.assertFalse(exitDoor.canUnlock(0));
        Assertions.assertFalse(exitDoor.tryUnlock(0));
        Assertions.assertFalse(exitDoor.isUnlocked());

        // Attempt unlock with 1 key
        Assertions.assertFalse(exitDoor.canUnlock(1));
        Assertions.assertFalse(exitDoor.tryUnlock(1));
        Assertions.assertFalse(exitDoor.isUnlocked());

        // Attempt unlock with 2 keys
        Assertions.assertFalse(exitDoor.canUnlock(2));
        Assertions.assertFalse(exitDoor.tryUnlock(2));
        Assertions.assertFalse(exitDoor.isUnlocked());

        // Unlock with all 3 keys
        Assertions.assertTrue(exitDoor.canUnlock(3));
        Assertions.assertTrue(exitDoor.tryUnlock(3));
        Assertions.assertTrue(exitDoor.isUnlocked(), "Exit door must unlock when 3 keys are acquired");

        // Player at exit door triggers completion
        Assertions.assertTrue(exitDoor.isPlayerAtDoor(exitWorldX, exitWorldZ),
            "Player at exit portal should trigger completion zone");
        Assertions.assertFalse(exitDoor.isPlayerAtDoor(4.5f, 4.5f),
            "Player far away should not trigger exit door");
    }
}

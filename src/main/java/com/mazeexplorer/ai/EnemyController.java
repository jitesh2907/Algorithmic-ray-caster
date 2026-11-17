package com.mazeexplorer.ai;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.mazeexplorer.maze.MazeGrid;
import com.mazeexplorer.pathfinding.BFSPathfinder;
import com.mazeexplorer.pathfinding.PathNode;
import com.mazeexplorer.player.Player;
import com.mazeexplorer.raycast.DDARayCaster3D;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates AI Enemy navigation, state transitions, line-of-sight DDA checks,
 * BFS path calculation, player chasing, and melee attacks.
 * Designed to operate independently of rendering for automated testing,
 * with optional Spatial binding for 3D graphics.
 */
public class EnemyController {

    public static final float PATROL_SPEED = 2.4f;
    public static final float CHASE_SPEED = 3.3f;
    public static final float DETECTION_RADIUS = 12.0f;
    public static final float LOSE_INTEREST_RADIUS = 18.0f;
    public static final float ATTACK_RADIUS = 1.3f;
    public static final int ATTACK_DAMAGE = 20;
    public static final float ATTACK_COOLDOWN = 1.2f;
    public static final float REPATH_INTERVAL = 0.35f;
    public static final float MAX_LOST_LOS_DURATION = 3.0f;

    private final Enemy enemy;
    private final MazeGrid mazeGrid;
    private final float cellSize;
    private final BFSPathfinder pathfinder;
    private final DDARayCaster3D rayCaster;

    private final List<PathNode> patrolWaypoints;
    private int currentPatrolIndex;

    private float repathTimer;
    private float lostLOSTimer;
    private boolean playerInSight;

    // Optional 3D Spatial visual representation
    private Spatial visualSpatial;

    public EnemyController(Enemy enemy, MazeGrid mazeGrid, float cellSize, List<PathNode> patrolWaypoints) {
        this.enemy = enemy;
        this.mazeGrid = mazeGrid;
        this.cellSize = cellSize;
        this.pathfinder = new BFSPathfinder(mazeGrid);
        this.rayCaster = new DDARayCaster3D(mazeGrid, cellSize);
        this.patrolWaypoints = patrolWaypoints != null ? new ArrayList<>(patrolWaypoints) : new ArrayList<>();
        this.currentPatrolIndex = 0;
        this.repathTimer = 0f;
        this.lostLOSTimer = 0f;
        this.playerInSight = false;

        // Initialize first patrol route if waypoints available
        initPatrolRoute();
    }

    private void initPatrolRoute() {
        if (!patrolWaypoints.isEmpty()) {
            PathNode target = patrolWaypoints.get(currentPatrolIndex);
            int currentGridX = (int) Math.floor(enemy.getPosX() / cellSize);
            int currentGridZ = (int) Math.floor(enemy.getPosZ() / cellSize);
            List<PathNode> path = pathfinder.findPath(currentGridX, currentGridZ, target.x, target.z);
            enemy.setPath(path);
        }
    }

    public void update(float tpf, Player player) {
        if (enemy == null || player == null) {
            return;
        }

        // Update internal enemy attack cooldowns
        enemy.update(tpf);
        if (repathTimer > 0f) {
            repathTimer -= tpf;
        }

        float pX = player.getPosX();
        float pZ = player.getPosZ();
        float eX = enemy.getPosX();
        float eZ = enemy.getPosZ();

        float dx = pX - eX;
        float dz = pZ - eZ;
        float distToPlayer = (float) Math.hypot(dx, dz);

        // Check Line-of-Sight via DDA Ray Caster
        boolean hasLOS = false;
        if (distToPlayer <= DETECTION_RADIUS) {
            hasLOS = rayCaster.hasLineOfSight(eX, eZ, pX, pZ);
        }
        this.playerInSight = hasLOS;

        // State Machine
        switch (enemy.getState()) {
            case PATROL:
                handlePatrolState(tpf, distToPlayer, hasLOS, pX, pZ, player);
                break;

            case CHASE:
                handleChaseState(tpf, distToPlayer, hasLOS, pX, pZ, player);
                break;

            case ATTACK:
                handleAttackState(tpf, distToPlayer, player);
                break;
        }

        // Synchronize 3D Spatial transform if present
        syncVisualSpatial();
    }

    private void handlePatrolState(float tpf, float distToPlayer, boolean hasLOS, float pX, float pZ, Player player) {
        // Player spotted with direct line of sight within detection radius!
        if (hasLOS && distToPlayer <= DETECTION_RADIUS) {
            triggerChase(pX, pZ);
            if (distToPlayer <= ATTACK_RADIUS) {
                enemy.setState(Enemy.State.ATTACK);
                performAttack(player);
            }
            return;
        }

        // Follow patrol waypoints
        moveAlongPath(tpf, PATROL_SPEED);

        // If path completed, advance to next patrol waypoint
        if (enemy.getNextWaypoint() == null && !patrolWaypoints.isEmpty()) {
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolWaypoints.size();
            PathNode nextGoal = patrolWaypoints.get(currentPatrolIndex);
            int egX = (int) Math.floor(enemy.getPosX() / cellSize);
            int egZ = (int) Math.floor(enemy.getPosZ() / cellSize);
            List<PathNode> path = pathfinder.findPath(egX, egZ, nextGoal.x, nextGoal.z);
            enemy.setPath(path);
        }
    }

    private void handleChaseState(float tpf, float distToPlayer, boolean hasLOS, float pX, float pZ, Player player) {
        // Lost interest if player got too far
        if (distToPlayer > LOSE_INTEREST_RADIUS) {
            resetToPatrol();
            return;
        }

        // Track line-of-sight loss duration
        if (!hasLOS) {
            lostLOSTimer += tpf;
            if (lostLOSTimer > MAX_LOST_LOS_DURATION) {
                resetToPatrol();
                return;
            }
        } else {
            lostLOSTimer = 0f;
        }

        // Re-calculate BFS path towards player periodically
        if (repathTimer <= 0f) {
            repathTimer = REPATH_INTERVAL;
            int egX = (int) Math.floor(enemy.getPosX() / cellSize);
            int egZ = (int) Math.floor(enemy.getPosZ() / cellSize);
            int pgX = (int) Math.floor(pX / cellSize);
            int pgZ = (int) Math.floor(pZ / cellSize);

            List<PathNode> path = pathfinder.findPath(egX, egZ, pgX, pgZ);
            if (!path.isEmpty()) {
                enemy.setPath(path);
            }
        }

        // If within melee reach, attack!
        if (distToPlayer <= ATTACK_RADIUS) {
            enemy.setState(Enemy.State.ATTACK);
            performAttack(player);
            return;
        }

        // Pursue player at chase speed
        moveAlongPath(tpf, CHASE_SPEED);
    }

    private void handleAttackState(float tpf, float distToPlayer, Player player) {
        if (distToPlayer > ATTACK_RADIUS * 1.5f) {
            enemy.setState(Enemy.State.CHASE);
            return;
        }

        if (enemy.canAttack()) {
            performAttack(player);
        }
    }

    private void performAttack(Player player) {
        if (enemy.canAttack() && player != null) {
            player.getHealth().applyDamage(ATTACK_DAMAGE);
            enemy.triggerAttackCooldown(ATTACK_COOLDOWN);
        }
    }

    public void triggerChase(float playerX, float playerZ) {
        enemy.setState(Enemy.State.CHASE);
        lostLOSTimer = 0f;
        repathTimer = REPATH_INTERVAL;

        int egX = (int) Math.floor(enemy.getPosX() / cellSize);
        int egZ = (int) Math.floor(enemy.getPosZ() / cellSize);
        int pgX = (int) Math.floor(playerX / cellSize);
        int pgZ = (int) Math.floor(playerZ / cellSize);

        List<PathNode> path = pathfinder.findPath(egX, egZ, pgX, pgZ);
        enemy.setPath(path);
    }

    public void resetToPatrol() {
        enemy.setState(Enemy.State.PATROL);
        lostLOSTimer = 0f;
        repathTimer = 0f;

        if (!patrolWaypoints.isEmpty()) {
            // Find closest patrol waypoint
            int bestIndex = 0;
            float bestDistSq = Float.MAX_VALUE;
            float ex = enemy.getPosX();
            float ez = enemy.getPosZ();

            for (int i = 0; i < patrolWaypoints.size(); i++) {
                PathNode node = patrolWaypoints.get(i);
                float wx = (node.x + 0.5f) * cellSize;
                float wz = (node.z + 0.5f) * cellSize;
                float distSq = (wx - ex) * (wx - ex) + (wz - ez) * (wz - ez);
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestIndex = i;
                }
            }

            currentPatrolIndex = bestIndex;
            PathNode target = patrolWaypoints.get(currentPatrolIndex);
            int egX = (int) Math.floor(enemy.getPosX() / cellSize);
            int egZ = (int) Math.floor(enemy.getPosZ() / cellSize);
            List<PathNode> path = pathfinder.findPath(egX, egZ, target.x, target.z);
            enemy.setPath(path);
        }
    }

    private void moveAlongPath(float tpf, float speed) {
        PathNode waypoint = enemy.getNextWaypoint();
        if (waypoint == null) {
            return;
        }

        float targetX = (waypoint.x + 0.5f) * cellSize;
        float targetZ = (waypoint.z + 0.5f) * cellSize;

        float dx = targetX - enemy.getPosX();
        float dz = targetZ - enemy.getPosZ();
        float dist = (float) Math.hypot(dx, dz);

        float step = speed * tpf;

        if (dist <= step || dist < 0.15f) {
            enemy.setPosX(targetX);
            enemy.setPosZ(targetZ);
            enemy.advanceWaypoint();
        } else {
            enemy.setPosX(enemy.getPosX() + (dx / dist) * step);
            enemy.setPosZ(enemy.getPosZ() + (dz / dist) * step);
        }
    }

    private void syncVisualSpatial() {
        if (visualSpatial != null) {
            // Height floats at ~1.2m above floor
            float floatY = 1.2f + 0.1f * (float) Math.sin(System.currentTimeMillis() * 0.005);
            visualSpatial.setLocalTranslation(enemy.getPosX(), floatY, enemy.getPosZ());

            // Orient facing towards next waypoint or player
            PathNode nextNode = enemy.getNextWaypoint();
            if (nextNode != null) {
                float tx = (nextNode.x + 0.5f) * cellSize;
                float tz = (nextNode.z + 0.5f) * cellSize;
                float fwdX = tx - enemy.getPosX();
                float fwdZ = tz - enemy.getPosZ();
                if (fwdX * fwdX + fwdZ * fwdZ > 0.001f) {
                    float angle = (float) Math.atan2(fwdX, fwdZ);
                    Quaternion rot = new Quaternion();
                    rot.fromAngles(0f, angle, 0f);
                    visualSpatial.setLocalRotation(rot);
                }
            }
        }
    }

    public void setVisualSpatial(Spatial spatial) {
        this.visualSpatial = spatial;
    }

    public Spatial getVisualSpatial() {
        return visualSpatial;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public boolean isPlayerInSight() {
        return playerInSight;
    }

    public List<PathNode> getPatrolWaypoints() {
        return patrolWaypoints;
    }

    public int getCurrentPatrolIndex() {
        return currentPatrolIndex;
    }
}

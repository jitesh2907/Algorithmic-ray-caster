package com.mazeexplorer.ai;

import com.mazeexplorer.pathfinding.PathNode;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Enemy entity navigating the maze with state-machine behaviors
 * (PATROL, CHASE, ATTACK) and BFS waypoint following.
 */
public class Enemy {

    public enum State {
        PATROL,
        CHASE,
        ATTACK
    }

    private float posX;
    private float posY;
    private float posZ;
    private float moveSpeed;

    private State state;
    private final float detectionRadius;
    private final float attackRadius;
    private final int attackDamage;
    private float attackCooldown;

    private List<PathNode> currentPath;
    private int currentPathIndex;

    public Enemy(float startX, float startY, float startZ) {
        this.posX = startX;
        this.posY = startY;
        this.posZ = startZ;
        this.moveSpeed = 3.5f;
        this.state = State.PATROL;
        this.detectionRadius = 12.0f;
        this.attackRadius = 1.2f;
        this.attackDamage = 20;
        this.attackCooldown = 0f;
        this.currentPath = new ArrayList<>();
        this.currentPathIndex = 0;
    }

    public void update(float tpf) {
        if (attackCooldown > 0f) {
            attackCooldown -= tpf;
            if (attackCooldown < 0f) {
                attackCooldown = 0f;
            }
        }
    }

    public void setPath(List<PathNode> path) {
        this.currentPath = path != null ? new ArrayList<>(path) : new ArrayList<>();
        this.currentPathIndex = 0;
    }

    public PathNode getNextWaypoint() {
        if (currentPath == null || currentPathIndex >= currentPath.size()) {
            return null;
        }
        return currentPath.get(currentPathIndex);
    }

    public void advanceWaypoint() {
        if (currentPathIndex < currentPath.size()) {
            currentPathIndex++;
        }
    }

    public boolean canAttack() {
        return attackCooldown <= 0f;
    }

    public void triggerAttackCooldown(float cooldownSeconds) {
        this.attackCooldown = cooldownSeconds;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public void setPosZ(float posZ) {
        this.posZ = posZ;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getDetectionRadius() {
        return detectionRadius;
    }

    public float getAttackRadius() {
        return attackRadius;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public List<PathNode> getCurrentPath() {
        return currentPath;
    }
}


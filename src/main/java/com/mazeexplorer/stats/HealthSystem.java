package com.mazeexplorer.stats;

import com.mazeexplorer.config.GameSettings;

/**
 * Manages player or entity health, damage cooldowns, and death state.
 */
public class HealthSystem {

    private final int maxHealth;
    private int currentHealth;
    private float invulnerabilityTimer;
    private final float invulnerabilityCooldown;

    public HealthSystem() {
        this(GameSettings.MAX_PLAYER_HEALTH, 1.0f);
    }

    public HealthSystem(int maxHealth, float invulnerabilityCooldown) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.invulnerabilityCooldown = invulnerabilityCooldown;
        this.invulnerabilityTimer = 0f;
    }

    public void update(float tpf) {
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= tpf;
            if (invulnerabilityTimer < 0f) {
                invulnerabilityTimer = 0f;
            }
        }
    }

    public boolean applyDamage(int damage) {
        if (isInvulnerable() || isDead()) {
            return false;
        }
        currentHealth = Math.max(0, currentHealth - damage);
        invulnerabilityTimer = invulnerabilityCooldown;
        return true;
    }

    public void heal(int amount) {
        if (!isDead()) {
            currentHealth = Math.min(maxHealth, currentHealth + amount);
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercentage() {
        return (float) currentHealth / (float) maxHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0f;
    }

    public void reset() {
        currentHealth = maxHealth;
        invulnerabilityTimer = 0f;
    }
}


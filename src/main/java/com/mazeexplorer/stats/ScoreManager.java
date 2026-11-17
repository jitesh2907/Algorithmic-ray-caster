package com.mazeexplorer.stats;

/**
 * Tracks score, collected coins, gems, keys, and elapsed time.
 */
public class ScoreManager {

    private int score;
    private int coinsCollected;
    private int gemsCollected;
    private int keysCollected;
    private float elapsedTime;

    public ScoreManager() {
        reset();
    }

    public void update(float tpf) {
        elapsedTime += tpf;
    }

    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    public void collectCoin(int value) {
        coinsCollected++;
        addScore(value);
    }

    public void collectGem(int value) {
        gemsCollected++;
        addScore(value);
    }

    public void collectKey() {
        keysCollected++;
        addScore(1000);
    }

    public boolean useKey() {
        if (keysCollected > 0) {
            keysCollected--;
            return true;
        }
        return false;
    }

    public int getScore() {
        return score;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public int getGemsCollected() {
        return gemsCollected;
    }

    public int getKeysCollected() {
        return keysCollected;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void reset() {
        this.score = 0;
        this.coinsCollected = 0;
        this.gemsCollected = 0;
        this.keysCollected = 0;
        this.elapsedTime = 0f;
    }
}


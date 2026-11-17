package com.mazeexplorer;

import com.mazeexplorer.entity.ExitDoor;
import com.mazeexplorer.state.GameStateManager;
import com.mazeexplorer.stats.HealthSystem;
import com.mazeexplorer.stats.ScoreManager;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Automated test suite verifying the complete Game State Machine,
 * screen transitions, timer formatting, health thresholds, scoring tallies,
 * and expedition reset mechanics.
 */
public class GameStateAndUITest {

    private GameStateManager stateManager;

    @BeforeEach
    public void setup() {
        this.stateManager = new GameStateManager();
    }

    @Test
    public void testInitialStateIsMainMenu() {
        Assertions.assertEquals(GameStateManager.State.MAIN_MENU, stateManager.getCurrentState());
        Assertions.assertTrue(stateManager.isMainMenu());
        Assertions.assertFalse(stateManager.isPlaying());
        Assertions.assertFalse(stateManager.isPaused());
    }

    @Test
    public void testStateTransitionsAndListeners() {
        AtomicInteger transitions = new AtomicInteger(0);
        stateManager.addListener((oldState, newState) -> transitions.incrementAndGet());

        // MAIN_MENU -> PLAYING
        stateManager.transitionTo(GameStateManager.State.PLAYING);
        Assertions.assertTrue(stateManager.isPlaying());
        Assertions.assertEquals(1, transitions.get());

        // PLAYING -> PAUSED
        stateManager.transitionTo(GameStateManager.State.PAUSED);
        Assertions.assertTrue(stateManager.isPaused());
        Assertions.assertEquals(2, transitions.get());

        // PAUSED -> PLAYING
        stateManager.transitionTo(GameStateManager.State.PLAYING);
        Assertions.assertTrue(stateManager.isPlaying());
        Assertions.assertEquals(3, transitions.get());

        // PLAYING -> VICTORY
        stateManager.transitionTo(GameStateManager.State.VICTORY);
        Assertions.assertTrue(stateManager.isVictory());
        Assertions.assertEquals(4, transitions.get());

        // VICTORY -> GAME_OVER
        stateManager.transitionTo(GameStateManager.State.GAME_OVER);
        Assertions.assertTrue(stateManager.isGameOver());
        Assertions.assertEquals(5, transitions.get());
    }

    @Test
    public void testInstructionsScreenReturnFlow() {
        // From MAIN_MENU to INSTRUCTIONS and back
        stateManager.transitionTo(GameStateManager.State.INSTRUCTIONS);
        Assertions.assertTrue(stateManager.isInstructions());
        Assertions.assertEquals(GameStateManager.State.MAIN_MENU, stateManager.getPreviousState());

        stateManager.returnFromInstructions();
        Assertions.assertTrue(stateManager.isMainMenu());

        // From PAUSED to INSTRUCTIONS and back
        stateManager.transitionTo(GameStateManager.State.PLAYING);
        stateManager.transitionTo(GameStateManager.State.PAUSED);
        stateManager.transitionTo(GameStateManager.State.INSTRUCTIONS);
        Assertions.assertEquals(GameStateManager.State.PAUSED, stateManager.getPreviousState());

        stateManager.returnFromInstructions();
        Assertions.assertTrue(stateManager.isPaused());
    }

    @Test
    public void testScoreTimerAndRankEvaluation() {
        ScoreManager score = new ScoreManager();

        // Simulate 75.4 seconds of gameplay
        score.update(75.4f);
        Assertions.assertEquals(75.4f, score.getElapsedTime(), 0.05f);

        int minutes = (int) (score.getElapsedTime() / 60);
        float seconds = score.getElapsedTime() % 60;
        String formattedTime = String.format("%02d:%04.1f", minutes, seconds);
        Assertions.assertEquals("01:15.4", formattedTime, "Timer must format cleanly as MM:SS.S");

        // Collect all 5 coins and 3 keys
        for (int i = 0; i < 5; i++) {
            score.collectCoin(100);
        }
        for (int i = 0; i < 3; i++) {
            score.collectKey();
        }

        Assertions.assertEquals(5, score.getCoinsCollected());
        Assertions.assertEquals(3, score.getKeysCollected());
        Assertions.assertEquals(3500, score.getScore());
    }

    @Test
    public void testHealthThresholdCategories() {
        HealthSystem health = new HealthSystem(100, 0.5f);

        // Healthy: 100%
        Assertions.assertEquals(1.0f, health.getHealthPercentage(), 0.001f);
        Assertions.assertTrue(health.getHealthPercentage() > 0.5f);

        // Damaged to Caution: 40% (apply 60 damage)
        health.applyDamage(60);
        Assertions.assertEquals(40, health.getCurrentHealth());
        Assertions.assertEquals(0.40f, health.getHealthPercentage(), 0.001f);
        Assertions.assertTrue(health.getHealthPercentage() > 0.25f && health.getHealthPercentage() <= 0.5f);

        // Damaged to Critical: 20% (expire cooldown, apply 20 damage)
        health.update(0.6f);
        health.applyDamage(20);
        Assertions.assertEquals(20, health.getCurrentHealth());
        Assertions.assertTrue(health.getHealthPercentage() <= 0.25f);
        Assertions.assertFalse(health.isDead());

        // Fatal blow: 0%
        health.update(0.6f);
        health.applyDamage(20);
        Assertions.assertEquals(0, health.getCurrentHealth());
        Assertions.assertTrue(health.isDead());
    }

    @Test
    public void testExpeditionResetMechanics() {
        ScoreManager score = new ScoreManager();
        HealthSystem health = new HealthSystem(100, 1.0f);
        ExitDoor exitDoor = new ExitDoor(40.5f, 40.5f);

        // Progress gameplay
        score.collectKey();
        score.collectKey();
        score.collectKey();
        score.collectCoin(100);
        score.update(120.0f);
        health.applyDamage(80);
        exitDoor.tryUnlock(3);

        Assertions.assertEquals(3, score.getKeysCollected());
        Assertions.assertEquals(1, score.getCoinsCollected());
        Assertions.assertEquals(3100, score.getScore());
        Assertions.assertEquals(20, health.getCurrentHealth());
        Assertions.assertTrue(exitDoor.isUnlocked());

        // Perform Reset
        score.reset();
        health.reset();
        ExitDoor newDoor = new ExitDoor(40.5f, 40.5f);

        Assertions.assertEquals(0, score.getKeysCollected());
        Assertions.assertEquals(0, score.getCoinsCollected());
        Assertions.assertEquals(0, score.getScore());
        Assertions.assertEquals(0f, score.getElapsedTime(), 0.001f);
        Assertions.assertEquals(100, health.getCurrentHealth());
        Assertions.assertFalse(newDoor.isUnlocked(), "Exit door must be locked upon expedition reset");
    }
}


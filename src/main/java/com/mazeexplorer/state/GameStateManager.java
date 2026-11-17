package com.mazeexplorer.state;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level game state manager orchestrating screen transitions and flow.
 * Supports:
 * - MAIN_MENU: Title screen, Start Game, Instructions, Quit
 * - INSTRUCTIONS: Game controls, lore, mission objectives reference
 * - PLAYING: Active 3D gameplay with HUD and first-person control
 * - PAUSED: In-game pause overlay with Resume, Instructions, Restart, Menu
 * options
 * - GAME_OVER: Defeat screen with Respawn / Restart, Menu options
 * - VICTORY: Level escape screen with mission debriefing, Restart, Menu options
 */
public class GameStateManager {

    public enum State {
        MAIN_MENU,
        INSTRUCTIONS,
        PLAYING,
        PAUSED,
        GAME_OVER,
        VICTORY
    }

    public interface StateChangeListener {
        void onStateChange(State oldState, State newState);
    }

    private State currentState;
    private State previousState;
    private final List<StateChangeListener> listeners;

    public GameStateManager() {
        this.currentState = State.MAIN_MENU;
        this.previousState = State.MAIN_MENU;
        this.listeners = new ArrayList<>();
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getPreviousState() {
        return previousState;
    }

    public void addListener(StateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    public void transitionTo(State newState) {
        if (newState != null && newState != this.currentState) {
            State old = this.currentState;
            this.previousState = old;
            this.currentState = newState;
            for (StateChangeListener l : listeners) {
                l.onStateChange(old, newState);
            }
        }
    }

    public void returnFromInstructions() {
        if (currentState == State.INSTRUCTIONS) {
            transitionTo(previousState != null ? previousState : State.MAIN_MENU);
        }
    }

    public boolean isMainMenu() {
        return currentState == State.MAIN_MENU;
    }

    public boolean isInstructions() {
        return currentState == State.INSTRUCTIONS;
    }

    public boolean isPlaying() {
        return currentState == State.PLAYING;
    }

    public boolean isPaused() {
        return currentState == State.PAUSED;
    }

    public boolean isGameOver() {
        return currentState == State.GAME_OVER;
    }

    public boolean isVictory() {
        return currentState == State.VICTORY;
    }
}

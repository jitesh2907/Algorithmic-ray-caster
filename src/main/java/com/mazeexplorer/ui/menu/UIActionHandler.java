package com.mazeexplorer.ui.menu;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.mazeexplorer.state.GameStateManager;

/**
 * Handles keyboard navigation and menu actions across all application states.
 * Fully decouples menu actions from first-person player controls.
 */
public class UIActionHandler implements ActionListener {

    public static final String UI_KEY_1 = "UI_OPTION_1";
    public static final String UI_KEY_2 = "UI_OPTION_2";
    public static final String UI_KEY_3 = "UI_OPTION_3";
    public static final String UI_KEY_4 = "UI_OPTION_4";
    public static final String UI_CONFIRM = "UI_CONFIRM";
    public static final String UI_INSTRUCTIONS = "UI_INSTRUCTIONS";
    public static final String UI_MAIN_MENU = "UI_MAIN_MENU";
    public static final String UI_RESTART = "UI_RESTART";
    public static final String UI_QUIT = "UI_QUIT";

    public interface UIEventListener {
        void onStartGame();
        void onOpenInstructions();
        void onResumeGame();
        void onRestartGame();
        void onReturnToMainMenu();
        void onQuitToDesktop();
        void onReturnFromInstructions();
    }

    private final GameStateManager stateManager;
    private final InputManager inputManager;
    private UIEventListener listener;

    public UIActionHandler(GameStateManager stateManager, InputManager inputManager) {
        this.stateManager = stateManager;
        this.inputManager = inputManager;
        registerInputMappings();
    }

    public void setEventListener(UIEventListener listener) {
        this.listener = listener;
    }

    private void registerInputMappings() {
        inputManager.addMapping(UI_KEY_1, new KeyTrigger(KeyInput.KEY_1), new KeyTrigger(KeyInput.KEY_NUMPAD1));
        inputManager.addMapping(UI_KEY_2, new KeyTrigger(KeyInput.KEY_2), new KeyTrigger(KeyInput.KEY_NUMPAD2));
        inputManager.addMapping(UI_KEY_3, new KeyTrigger(KeyInput.KEY_3), new KeyTrigger(KeyInput.KEY_NUMPAD3));
        inputManager.addMapping(UI_KEY_4, new KeyTrigger(KeyInput.KEY_4), new KeyTrigger(KeyInput.KEY_NUMPAD4));

        inputManager.addMapping(UI_CONFIRM, new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_NUMPADENTER), new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(UI_INSTRUCTIONS, new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping(UI_MAIN_MENU, new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping(UI_RESTART, new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping(UI_QUIT, new KeyTrigger(KeyInput.KEY_Q));

        inputManager.addListener(this,
            UI_KEY_1, UI_KEY_2, UI_KEY_3, UI_KEY_4,
            UI_CONFIRM, UI_INSTRUCTIONS, UI_MAIN_MENU, UI_RESTART, UI_QUIT
        );
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed || listener == null) {
            return;
        }

        GameStateManager.State state = stateManager.getCurrentState();

        switch (state) {
            case MAIN_MENU:
                handleMainMenuInput(name);
                break;

            case INSTRUCTIONS:
                handleInstructionsInput(name);
                break;

            case PAUSED:
                handlePausedInput(name);
                break;

            case VICTORY:
            case GAME_OVER:
                handleVictoryOrGameOverInput(name);
                break;

            case PLAYING:
                // Playing state inputs are handled by PlayerController
                break;
        }
    }

    private void handleMainMenuInput(String name) {
        if (name.equals(UI_KEY_1) || name.equals(UI_CONFIRM)) {
            listener.onStartGame();
        } else if (name.equals(UI_KEY_2) || name.equals(UI_INSTRUCTIONS)) {
            listener.onOpenInstructions();
        } else if (name.equals(UI_KEY_3) || name.equals(UI_QUIT)) {
            listener.onQuitToDesktop();
        }
    }

    private void handleInstructionsInput(String name) {
        if (name.equals(UI_CONFIRM) || name.equals(UI_INSTRUCTIONS)) {
            listener.onReturnFromInstructions();
        }
    }

    private void handlePausedInput(String name) {
        if (name.equals(UI_KEY_1) || name.equals(UI_CONFIRM)) {
            listener.onResumeGame();
        } else if (name.equals(UI_KEY_2) || name.equals(UI_INSTRUCTIONS)) {
            listener.onOpenInstructions();
        } else if (name.equals(UI_KEY_3) || name.equals(UI_RESTART)) {
            listener.onRestartGame();
        } else if (name.equals(UI_KEY_4) || name.equals(UI_MAIN_MENU)) {
            listener.onReturnToMainMenu();
        } else if (name.equals(UI_QUIT)) {
            listener.onQuitToDesktop();
        }
    }

    private void handleVictoryOrGameOverInput(String name) {
        if (name.equals(UI_RESTART) || name.equals(UI_KEY_1) || name.equals(UI_CONFIRM)) {
            listener.onRestartGame();
        } else if (name.equals(UI_MAIN_MENU) || name.equals(UI_KEY_2)) {
            listener.onReturnToMainMenu();
        } else if (name.equals(UI_QUIT) || name.equals(UI_KEY_3)) {
            listener.onQuitToDesktop();
        }
    }
}


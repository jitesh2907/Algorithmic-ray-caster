package com.mazeexplorer.ui.menu;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.mazeexplorer.state.GameStateManager;
import com.mazeexplorer.stats.ScoreManager;

/**
 * Procedural Menu Renderer for ALGORITHMIC 3D MAZE EXPLORER.
 * Implements professional, responsive 2D screen layouts on jME3 guiNode:
 * 1. Main Menu (Title, Start Expedition, Instructions, Quit)
 * 2. Instructions Screen (Dungeon Lore, Mission Objectives, Complete Controls Guide)
 * 3. Pause Menu (Resume, Instructions, Restart, Main Menu, Quit)
 * 4. Victory Screen (Mission Statistics, Rank Calculation, Play Again, Main Menu)
 * 5. Game Over Screen (Defeat Message, Survival Stats, Respawn, Main Menu)
 */
public class MenuRenderer {

    private final Node guiNode;
    private final AssetManager assetManager;
    private final AppSettings settings;
    private final BitmapFont font;

    // Menu Nodes
    private Node mainMenuNode;
    private Node instructionsNode;
    private Node pauseMenuNode;
    private Node victoryNode;
    private Node gameOverNode;

    // Dynamic Texts
    private BitmapText victoryStatsText;
    private BitmapText victoryRankText;
    private BitmapText gameOverStatsText;

    public MenuRenderer(Node guiNode, AssetManager assetManager, AppSettings settings) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.settings = settings;
        this.font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        buildMainMenu();
        buildInstructionsScreen();
        buildPauseMenu();
        buildVictoryScreen();
        buildGameOverScreen();
    }

    private void buildMainMenu() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        mainMenuNode = new Node("MainMenuNode");

        // Translucent dark vignette backdrop
        Geometry bg = createQuadBackdrop("MainMenuBg", sw, sh, new ColorRGBA(0.03f, 0.03f, 0.05f, 0.88f));
        mainMenuNode.attachChild(bg);

        // Header Title
        BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 2.4f);
        title.setColor(new ColorRGBA(1.0f, 0.85f, 0.25f, 1.0f)); // Gold
        title.setText("ALGORITHMIC 3D MAZE EXPLORER");
        title.setLocalTranslation(sw / 2.0f - title.getLineWidth() / 2.0f, sh * 0.78f, 1f);
        mainMenuNode.attachChild(title);

        // Subtitle
        BitmapText subtitle = new BitmapText(font);
        subtitle.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        subtitle.setColor(new ColorRGBA(0.8f, 0.75f, 0.65f, 1.0f)); // Parchment silver
        subtitle.setText("A First-Person Procedural 3D Dungeon Simulation");
        subtitle.setLocalTranslation(sw / 2.0f - subtitle.getLineWidth() / 2.0f, sh * 0.70f, 1f);
        mainMenuNode.attachChild(subtitle);

        // Decorative Divider
        BitmapText divider = new BitmapText(font);
        divider.setSize(font.getCharSet().getRenderedSize() * 1.0f);
        divider.setColor(new ColorRGBA(0.6f, 0.5f, 0.3f, 0.8f));
        divider.setText("================================================================================");
        divider.setLocalTranslation(sw / 2.0f - divider.getLineWidth() / 2.0f, sh * 0.64f, 1f);
        mainMenuNode.attachChild(divider);

        // Menu Options Box
        float optionStartY = sh * 0.55f;
        float spacing = 45f;

        addMenuOption(mainMenuNode, "[1] / [ENTER]  -  START EXPEDITION",
                new ColorRGBA(0.3f, 1.0f, 0.45f, 1.0f), sw / 2.0f, optionStartY);
        addMenuOption(mainMenuNode, "[2] / [I]      -  HOW TO PLAY & LORE",
                new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f), sw / 2.0f, optionStartY - spacing);
        addMenuOption(mainMenuNode, "[3] / [ESC]    -  QUIT TO DESKTOP",
                new ColorRGBA(0.95f, 0.4f, 0.4f, 1.0f), sw / 2.0f, optionStartY - spacing * 2);

        // Footer info
        BitmapText footer = new BitmapText(font);
        footer.setSize(font.getCharSet().getRenderedSize() * 0.85f);
        footer.setColor(new ColorRGBA(0.6f, 0.6f, 0.7f, 0.9f));
        footer.setText("jMonkeyEngine 3.9.0-stable | LWJGL3 OpenGL 3D Engine | Antigravity IDE");
        footer.setLocalTranslation(sw / 2.0f - footer.getLineWidth() / 2.0f, 35f, 1f);
        mainMenuNode.attachChild(footer);

        guiNode.attachChild(mainMenuNode);
    }

    private void buildInstructionsScreen() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        instructionsNode = new Node("InstructionsNode");

        Geometry bg = createQuadBackdrop("InstructionsBg", sw, sh, new ColorRGBA(0.04f, 0.04f, 0.06f, 0.95f));
        instructionsNode.attachChild(bg);

        // Title
        BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 2.0f);
        title.setColor(new ColorRGBA(1.0f, 0.85f, 0.25f, 1.0f));
        title.setText("*** EXPEDITION BRIEFING & SURVIVAL GUIDE ***");
        title.setLocalTranslation(sw / 2.0f - title.getLineWidth() / 2.0f, sh * 0.88f, 1f);
        instructionsNode.attachChild(title);

        // Lore Section
        float startX = sw * 0.12f;
        float currentY = sh * 0.78f;

        BitmapText loreHeader = new BitmapText(font);
        loreHeader.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        loreHeader.setColor(new ColorRGBA(1.0f, 0.6f, 0.2f, 1.0f));
        loreHeader.setText("[ DUNGEON LORE ]");
        loreHeader.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(loreHeader);

        currentY -= 28f;
        BitmapText loreBody = new BitmapText(font);
        loreBody.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        loreBody.setColor(new ColorRGBA(0.85f, 0.85f, 0.85f, 1.0f));
        loreBody.setText(
            "You are trapped deep inside an ancient algorithmic crypt. The sealed golden portal at [13, 13]\n" +
            "is the only escape, but it is locked by arcane seals requiring 3 Sacred Golden Keys."
        );
        loreBody.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(loreBody);

        // Objectives Section
        currentY -= 60f;
        BitmapText objHeader = new BitmapText(font);
        objHeader.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        objHeader.setColor(new ColorRGBA(0.3f, 1.0f, 0.5f, 1.0f));
        objHeader.setText("[ MISSION OBJECTIVES ]");
        objHeader.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(objHeader);

        currentY -= 28f;
        BitmapText objBody = new BitmapText(font);
        objBody.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        objBody.setColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        objBody.setText(
            "1. EXPLORE: Navigate the 15x15 labyrinth corridors and follow torch sconces.\n" +
            "2. RECOVER 3 KEYS: Hidden in the East Wing [13, 1], Central Alcove [7, 3], and South Wing [1, 13].\n" +
            "3. COLLECT COINS: 5 Ancient Coins (+100 pts each) are scattered in key corridors.\n" +
            "4. EVADE THE STALKER: A lethal beast patrols the dungeon. If it catches direct line-of-sight (DDA),\n" +
            "   it computes the optimal shortest path (BFS) to pursue and strike you (-20 HP)!\n" +
            "5. ESCAPE: Reach the glowing Exit Portal at [13, 13] once all 3 keys are acquired to win!"
        );
        objBody.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(objBody);

        // Controls Section
        currentY -= 125f;
        BitmapText ctrlHeader = new BitmapText(font);
        ctrlHeader.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        ctrlHeader.setColor(new ColorRGBA(0.4f, 0.8f, 1.0f, 1.0f));
        ctrlHeader.setText("[ CONTROLS GUIDE ]");
        ctrlHeader.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(ctrlHeader);

        currentY -= 28f;
        BitmapText ctrlBody = new BitmapText(font);
        ctrlBody.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        ctrlBody.setColor(new ColorRGBA(0.9f, 0.9f, 0.95f, 1.0f));
        ctrlBody.setText(
            "- [W / A / S / D]  : First-Person Movement & Smooth Wall-Sliding\n" +
            "- [Left Shift]    : Sprint (Increases movement speed from 4.0 to 7.0 m/s)\n" +
            "- [Mouse]         : First-Person Camera Look (Clamped pitch +/- 89 degrees)\n" +
            "- [ESC]           : Pause Game & Free Cursor / Return from Menus\n" +
            "- [R]             : Quick Respawn / Restart Expedition"
        );
        ctrlBody.setLocalTranslation(startX, currentY, 1f);
        instructionsNode.attachChild(ctrlBody);

        // Return Prompt
        BitmapText prompt = new BitmapText(font);
        prompt.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        prompt.setColor(new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f));
        prompt.setText(">>> Press [ENTER], [SPACE], or [ESC] to Return <<<");
        prompt.setLocalTranslation(sw / 2.0f - prompt.getLineWidth() / 2.0f, 50f, 1f);
        instructionsNode.attachChild(prompt);

        instructionsNode.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(instructionsNode);
    }

    private void buildPauseMenu() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        pauseMenuNode = new Node("PauseMenuNode");

        Geometry bg = createQuadBackdrop("PauseMenuBg", sw, sh, new ColorRGBA(0.04f, 0.04f, 0.06f, 0.90f));
        pauseMenuNode.attachChild(bg);

        BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 2.2f);
        title.setColor(new ColorRGBA(1.0f, 0.35f, 0.35f, 1.0f));
        title.setText("*** EXPEDITION PAUSED ***");
        title.setLocalTranslation(sw / 2.0f - title.getLineWidth() / 2.0f, sh * 0.75f, 1f);
        pauseMenuNode.attachChild(title);

        BitmapText subtitle = new BitmapText(font);
        subtitle.setSize(font.getCharSet().getRenderedSize() * 1.1f);
        subtitle.setColor(new ColorRGBA(0.8f, 0.8f, 0.85f, 1.0f));
        subtitle.setText("The labyrinth stands still...");
        subtitle.setLocalTranslation(sw / 2.0f - subtitle.getLineWidth() / 2.0f, sh * 0.68f, 1f);
        pauseMenuNode.attachChild(subtitle);

        float optionStartY = sh * 0.55f;
        float spacing = 42f;

        addMenuOption(pauseMenuNode, "[1] / [ESC]  -  RESUME EXPEDITION",
                new ColorRGBA(0.3f, 1.0f, 0.5f, 1.0f), sw / 2.0f, optionStartY);
        addMenuOption(pauseMenuNode, "[2] / [I]    -  INSTRUCTIONS & CONTROLS",
                new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f), sw / 2.0f, optionStartY - spacing);
        addMenuOption(pauseMenuNode, "[3] / [R]    -  RESTART EXPEDITION",
                new ColorRGBA(0.4f, 0.8f, 1.0f, 1.0f), sw / 2.0f, optionStartY - spacing * 2);
        addMenuOption(pauseMenuNode, "[4] / [M]    -  RETURN TO MAIN MENU",
                new ColorRGBA(0.9f, 0.7f, 0.2f, 1.0f), sw / 2.0f, optionStartY - spacing * 3);
        addMenuOption(pauseMenuNode, "[Q]          -  QUIT TO DESKTOP",
                new ColorRGBA(0.95f, 0.35f, 0.35f, 1.0f), sw / 2.0f, optionStartY - spacing * 4);

        pauseMenuNode.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(pauseMenuNode);
    }

    private void buildVictoryScreen() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        victoryNode = new Node("VictoryNode");

        Geometry bg = createQuadBackdrop("VictoryBg", sw, sh, new ColorRGBA(0.02f, 0.06f, 0.03f, 0.94f));
        victoryNode.attachChild(bg);

        BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 2.3f);
        title.setColor(new ColorRGBA(0.2f, 1.0f, 0.45f, 1.0f)); // Radiant Emerald
        title.setText("*** DUNGEON ESCAPED - VICTORY! ***");
        title.setLocalTranslation(sw / 2.0f - title.getLineWidth() / 2.0f, sh * 0.82f, 1f);
        victoryNode.attachChild(title);

        BitmapText subtitle = new BitmapText(font);
        subtitle.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        subtitle.setColor(new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f));
        subtitle.setText("All 3 Golden Keys United - The Ancient Portal Sealed Behind You!");
        subtitle.setLocalTranslation(sw / 2.0f - subtitle.getLineWidth() / 2.0f, sh * 0.75f, 1f);
        victoryNode.attachChild(subtitle);

        // Stats Display Box
        victoryStatsText = new BitmapText(font);
        victoryStatsText.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        victoryStatsText.setColor(ColorRGBA.White);
        victoryStatsText.setText("");
        victoryStatsText.setLocalTranslation(sw / 2.0f - 180f, sh * 0.65f, 1f);
        victoryNode.attachChild(victoryStatsText);

        // Explorer Rank Award
        victoryRankText = new BitmapText(font);
        victoryRankText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        victoryRankText.setColor(new ColorRGBA(1.0f, 0.9f, 0.2f, 1.0f));
        victoryRankText.setText("EXPLORER RANK: S-TIER LEGEND");
        victoryRankText.setLocalTranslation(sw / 2.0f - 180f, sh * 0.38f, 1f);
        victoryNode.attachChild(victoryRankText);

        // Options
        float optY = sh * 0.24f;
        addMenuOption(victoryNode, "[R]  -  PLAY AGAIN (Restart)",
                new ColorRGBA(0.3f, 1.0f, 0.5f, 1.0f), sw / 2.0f, optY);
        addMenuOption(victoryNode, "[M]  -  RETURN TO MAIN MENU",
                new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f), sw / 2.0f, optY - 35f);
        addMenuOption(victoryNode, "[ESC] - QUIT TO DESKTOP",
                new ColorRGBA(0.9f, 0.4f, 0.4f, 1.0f), sw / 2.0f, optY - 70f);

        victoryNode.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(victoryNode);
    }

    private void buildGameOverScreen() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        gameOverNode = new Node("GameOverNode");

        Geometry bg = createQuadBackdrop("GameOverBg", sw, sh, new ColorRGBA(0.08f, 0.01f, 0.01f, 0.95f));
        gameOverNode.attachChild(bg);

        BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 2.4f);
        title.setColor(new ColorRGBA(1.0f, 0.15f, 0.15f, 1.0f)); // Blood Crimson
        title.setText("*** YOU PERISHED ***");
        title.setLocalTranslation(sw / 2.0f - title.getLineWidth() / 2.0f, sh * 0.80f, 1f);
        gameOverNode.attachChild(title);

        BitmapText subtitle = new BitmapText(font);
        subtitle.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        subtitle.setColor(new ColorRGBA(0.9f, 0.6f, 0.6f, 1.0f));
        subtitle.setText("The Maze Stalker tracked your scent through the darkness...");
        subtitle.setLocalTranslation(sw / 2.0f - subtitle.getLineWidth() / 2.0f, sh * 0.73f, 1f);
        gameOverNode.attachChild(subtitle);

        gameOverStatsText = new BitmapText(font);
        gameOverStatsText.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        gameOverStatsText.setColor(ColorRGBA.White);
        gameOverStatsText.setText("");
        gameOverStatsText.setLocalTranslation(sw / 2.0f - 180f, sh * 0.60f, 1f);
        gameOverNode.attachChild(gameOverStatsText);

        float optY = sh * 0.28f;
        addMenuOption(gameOverNode, "[R]  -  RESPAWN / RESTART EXPEDITION",
                new ColorRGBA(0.3f, 1.0f, 0.5f, 1.0f), sw / 2.0f, optY);
        addMenuOption(gameOverNode, "[M]  -  RETURN TO MAIN MENU",
                new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f), sw / 2.0f, optY - 35f);
        addMenuOption(gameOverNode, "[ESC] - QUIT TO DESKTOP",
                new ColorRGBA(0.9f, 0.4f, 0.4f, 1.0f), sw / 2.0f, optY - 70f);

        gameOverNode.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(gameOverNode);
    }

    private void addMenuOption(Node parent, String text, ColorRGBA color, float centerX, float y) {
        BitmapText option = new BitmapText(font);
        option.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        option.setColor(color);
        option.setText(text);
        option.setLocalTranslation(centerX - option.getLineWidth() / 2.0f, y, 1f);
        parent.attachChild(option);
    }

    private Geometry createQuadBackdrop(String name, float width, float height, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        Geometry geo = new Geometry(name, new Quad(width, height));
        geo.setMaterial(mat);
        geo.setLocalTranslation(0f, 0f, 0f);
        return geo;
    }

    /**
     * Updates visibility of all menu layers according to the active GameState.
     */
    public void updateView(GameStateManager.State state, ScoreManager score, int health, int maxHealth) {
        mainMenuNode.setCullHint(state == GameStateManager.State.MAIN_MENU ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        instructionsNode.setCullHint(state == GameStateManager.State.INSTRUCTIONS ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        pauseMenuNode.setCullHint(state == GameStateManager.State.PAUSED ? Spatial.CullHint.Never : Spatial.CullHint.Always);

        if (state == GameStateManager.State.VICTORY) {
            victoryNode.setCullHint(Spatial.CullHint.Never);
            updateVictoryStats(score, health, maxHealth);
        } else {
            victoryNode.setCullHint(Spatial.CullHint.Always);
        }

        if (state == GameStateManager.State.GAME_OVER) {
            gameOverNode.setCullHint(Spatial.CullHint.Never);
            updateGameOverStats(score);
        } else {
            gameOverNode.setCullHint(Spatial.CullHint.Always);
        }
    }

    private void updateVictoryStats(ScoreManager score, int health, int maxHealth) {
        int finalScore = score != null ? score.getScore() : 0;
        float elapsed = score != null ? score.getElapsedTime() : 0f;
        int coins = score != null ? score.getCoinsCollected() : 0;

        int minutes = (int) (elapsed / 60);
        float seconds = elapsed % 60;
        String timeStr = String.format("%02d:%04.1f", minutes, seconds);

        victoryStatsText.setText(String.format(
            "Final Score:       %,d Points\n\n" +
            "Completion Time:   %s\n\n" +
            "Golden Keys:       3 / 3 Found\n\n" +
            "Ancient Coins:     %d / 5 Recovered\n\n" +
            "Remaining Health:  %d / %d HP",
            finalScore, timeStr, coins, health, maxHealth
        ));

        // Rank determination
        String rank;
        ColorRGBA rankColor;
        if (finalScore >= 3500 && elapsed < 120f) {
            rank = "RANK: S-TIER (MASTER DUNGEON SPEEDRUNNER)";
            rankColor = new ColorRGBA(1.0f, 0.85f, 0.2f, 1.0f);
        } else if (finalScore >= 3000) {
            rank = "RANK: A-TIER (VETERAN LABYRINTH EXPLORER)";
            rankColor = new ColorRGBA(0.3f, 1.0f, 0.5f, 1.0f);
        } else if (finalScore >= 2000) {
            rank = "RANK: B-TIER (SURVIVOR OF THE CRYPT)";
            rankColor = new ColorRGBA(0.4f, 0.8f, 1.0f, 1.0f);
        } else {
            rank = "RANK: C-TIER (NOVICE ADVENTURER)";
            rankColor = new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f);
        }
        victoryRankText.setText(rank);
        victoryRankText.setColor(rankColor);
    }

    private void updateGameOverStats(ScoreManager score) {
        int sc = score != null ? score.getScore() : 0;
        float elapsed = score != null ? score.getElapsedTime() : 0f;
        int keys = score != null ? score.getKeysCollected() : 0;
        int coins = score != null ? score.getCoinsCollected() : 0;

        int minutes = (int) (elapsed / 60);
        float seconds = elapsed % 60;
        String timeStr = String.format("%02d:%04.1f", minutes, seconds);

        gameOverStatsText.setText(String.format(
            "Score Acquired:    %,d Points\n\n" +
            "Time Survived:     %s\n\n" +
            "Keys Recovered:    %d / 3\n\n" +
            "Coins Gathered:    %d / 5",
            sc, timeStr, keys, coins
        ));
    }
}


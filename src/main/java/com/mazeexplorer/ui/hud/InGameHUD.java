package com.mazeexplorer.ui.hud;

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
import com.mazeexplorer.ai.Enemy;
import com.mazeexplorer.ai.EnemyController;
import com.mazeexplorer.entity.ExitDoor;
import com.mazeexplorer.player.Player;

/**
 * High-performance In-Game HUD overlay rendering system on jME3 guiNode.
 * Renders cleanly during active gameplay:
 * 1. Health Bar & HP Counter (Dynamic green/yellow/red color shift)
 * 2. Mission Objective Display (Dynamic tracking from Key recovery to Exit escape)
 * 3. Score Counter & Formatted Level Timer (MM:SS.S)
 * 4. Golden Key slots ([KEY 1] [KEY 2] [KEY 3])
 * 5. Ancient Coin counter (X / 5)
 * 6. Enemy Threat Radar (PATROLLING vs PURSUING alert)
 * 7. Minimalist Center Crosshair
 * 8. Notification Toast Banner with auto-fade
 * 9. Real-time Player Telemetry (Coordinates, Grid Cell, Compass Direction)
 */
public class InGameHUD {

    private final Node guiNode;
    private final Node hudRootNode;
    private final AssetManager assetManager;
    private final AppSettings settings;
    private final BitmapFont font;

    // HUD Elements
    private BitmapText titleText;
    private BitmapText healthText;
    private Geometry healthBarBg;
    private Geometry healthBarFg;

    private BitmapText objectiveText;
    private BitmapText scoreText;
    private BitmapText timerText;
    private BitmapText keysText;
    private BitmapText coinsText;

    private BitmapText enemyStatusText;
    private BitmapText crosshair;
    private BitmapText telemetryText;

    private BitmapText notificationText;
    private float notificationTimer;

    private static final float BAR_WIDTH = 200f;
    private static final float BAR_HEIGHT = 18f;

    public InGameHUD(Node guiNode, AssetManager assetManager, AppSettings settings) {
        this.guiNode = guiNode;
        this.hudRootNode = new Node("InGameHUDRoot");
        this.assetManager = assetManager;
        this.settings = settings;
        this.font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        this.notificationTimer = 0f;

        buildHUD();
        guiNode.attachChild(hudRootNode);
    }

    private void buildHUD() {
        float sw = settings.getWidth();
        float sh = settings.getHeight();

        // 1. Title Banner (Top Left)
        titleText = new BitmapText(font);
        titleText.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        titleText.setColor(new ColorRGBA(1.0f, 0.85f, 0.25f, 1.0f)); // Gold
        titleText.setText("ALGORITHMIC 3D MAZE EXPLORER");
        titleText.setLocalTranslation(20f, sh - 18f, 1f);
        hudRootNode.attachChild(titleText);

        // 2. Health Bar Background & Foreground
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.12f, 0.12f, 0.15f, 0.90f));
        healthBarBg = new Geometry("HealthBarBg", new Quad(BAR_WIDTH + 4f, BAR_HEIGHT + 4f));
        healthBarBg.setMaterial(bgMat);
        healthBarBg.setLocalTranslation(18f, sh - 68f, 0f);
        hudRootNode.attachChild(healthBarBg);

        Material fgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fgMat.setColor("Color", new ColorRGBA(0.2f, 0.9f, 0.35f, 1.0f));
        healthBarFg = new Geometry("HealthBarFg", new Quad(BAR_WIDTH, BAR_HEIGHT));
        healthBarFg.setMaterial(fgMat);
        healthBarFg.setLocalTranslation(20f, sh - 66f, 1f);
        hudRootNode.attachChild(healthBarFg);

        healthText = new BitmapText(font);
        healthText.setSize(font.getCharSet().getRenderedSize() * 0.95f);
        healthText.setColor(ColorRGBA.White);
        healthText.setText("HP: 100 / 100 (100%)");
        healthText.setLocalTranslation(26f, sh - 51f, 2f);
        hudRootNode.attachChild(healthText);

        // 3. Mission Objective Display (Under Health Bar)
        objectiveText = new BitmapText(font);
        objectiveText.setSize(font.getCharSet().getRenderedSize() * 1.05f);
        objectiveText.setColor(new ColorRGBA(1.0f, 0.9f, 0.3f, 1.0f));
        objectiveText.setText("OBJECTIVE: Find 3 Golden Keys to Unseal Exit (0 / 3)");
        objectiveText.setLocalTranslation(20f, sh - 82f, 1f);
        hudRootNode.attachChild(objectiveText);

        // 4. Score & Level Timer
        scoreText = new BitmapText(font);
        scoreText.setSize(font.getCharSet().getRenderedSize() * 0.95f);
        scoreText.setColor(new ColorRGBA(0.85f, 0.9f, 1.0f, 1.0f));
        scoreText.setText("SCORE: 0 PTS");
        scoreText.setLocalTranslation(20f, sh - 106f, 1f);
        hudRootNode.attachChild(scoreText);

        timerText = new BitmapText(font);
        timerText.setSize(font.getCharSet().getRenderedSize() * 0.95f);
        timerText.setColor(new ColorRGBA(0.8f, 0.95f, 0.8f, 1.0f));
        timerText.setText("TIME: 00:00.0");
        timerText.setLocalTranslation(160f, sh - 106f, 1f);
        hudRootNode.attachChild(timerText);

        // 5. Keys & Coins
        keysText = new BitmapText(font);
        keysText.setSize(font.getCharSet().getRenderedSize() * 0.95f);
        keysText.setColor(new ColorRGBA(1.0f, 0.85f, 0.25f, 1.0f)); // Gold
        keysText.setText("KEYS: [---] [---] [---] (0 / 3)");
        keysText.setLocalTranslation(20f, sh - 128f, 1f);
        hudRootNode.attachChild(keysText);

        coinsText = new BitmapText(font);
        coinsText.setSize(font.getCharSet().getRenderedSize() * 0.95f);
        coinsText.setColor(new ColorRGBA(0.95f, 0.80f, 0.2f, 1.0f));
        coinsText.setText("COINS: 0 / 5");
        coinsText.setLocalTranslation(20f, sh - 148f, 1f);
        hudRootNode.attachChild(coinsText);

        // 6. Threat Radar (Top Right)
        enemyStatusText = new BitmapText(font);
        enemyStatusText.setSize(font.getCharSet().getRenderedSize() * 1.05f);
        enemyStatusText.setColor(new ColorRGBA(0.35f, 0.95f, 0.45f, 1.0f));
        enemyStatusText.setText("MAZE STALKER: PATROLLING (Unaware)");
        enemyStatusText.setLocalTranslation(sw - 330f, sh - 20f, 1f);
        hudRootNode.attachChild(enemyStatusText);

        // 7. Center Crosshair
        crosshair = new BitmapText(font);
        crosshair.setSize(font.getCharSet().getRenderedSize() * 1.4f);
        crosshair.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.70f));
        crosshair.setText("+");
        crosshair.setLocalTranslation(
            sw / 2.0f - crosshair.getLineWidth() / 2.0f,
            sh / 2.0f + crosshair.getLineHeight() / 2.0f,
            1f
        );
        hudRootNode.attachChild(crosshair);

        // 8. Notification Toast Banner (Lower Center)
        notificationText = new BitmapText(font);
        notificationText.setSize(font.getCharSet().getRenderedSize() * 1.15f);
        notificationText.setColor(new ColorRGBA(1.0f, 0.95f, 0.4f, 1.0f));
        notificationText.setText("");
        notificationText.setCullHint(Spatial.CullHint.Always);
        hudRootNode.attachChild(notificationText);

        // 9. Telemetry (Bottom Left)
        telemetryText = new BitmapText(font);
        telemetryText.setSize(font.getCharSet().getRenderedSize() * 0.85f);
        telemetryText.setColor(new ColorRGBA(0.7f, 0.75f, 0.85f, 1.0f));
        telemetryText.setText("Pos: (X: 0.0, Z: 0.0) | Cell: [1, 1] | Facing: EAST | [ESC] Pause Menu");
        telemetryText.setLocalTranslation(20f, 25f, 1f);
        hudRootNode.attachChild(telemetryText);
    }

    public void update(float tpf, Player player, EnemyController enemyController, ExitDoor exitDoor, String compassHeading) {
        if (player == null) return;

        // 1. Update Health Bar
        int hp = player.getHealth().getCurrentHealth();
        int maxHp = player.getHealth().getMaxHealth();
        float hpPercent = player.getHealth().getHealthPercentage();
        healthBarFg.setLocalScale(Math.max(0.001f, hpPercent), 1f, 1f);
        healthText.setText(String.format("HP: %d / %d (%d%%)", hp, maxHp, (int) (hpPercent * 100)));

        Material fgMat = healthBarFg.getMaterial();
        if (hpPercent > 0.5f) {
            fgMat.setColor("Color", new ColorRGBA(0.2f, 0.9f, 0.35f, 1.0f));
        } else if (hpPercent > 0.25f) {
            fgMat.setColor("Color", new ColorRGBA(0.95f, 0.8f, 0.15f, 1.0f));
        } else {
            fgMat.setColor("Color", new ColorRGBA(0.95f, 0.2f, 0.2f, 1.0f));
        }

        // 2. Mission Objective & Keys
        int keys = player.getScore().getKeysCollected();
        if (exitDoor != null && exitDoor.isUnlocked()) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(System.currentTimeMillis() * 0.008);
            objectiveText.setColor(new ColorRGBA(0.2f, 1.0f, 0.6f, pulse));
            objectiveText.setText("OBJECTIVE: PORTAL UNSEALED! Reach the Exit Gateway at [13, 13]!");
        } else {
            objectiveText.setColor(new ColorRGBA(1.0f, 0.9f, 0.3f, 1.0f));
            objectiveText.setText(String.format("OBJECTIVE: Find 3 Golden Keys to Unseal Exit (%d / 3)", keys));
        }

        String k1 = (keys >= 1) ? "[KEY 1]" : "[---]";
        String k2 = (keys >= 2) ? "[KEY 2]" : "[---]";
        String k3 = (keys >= 3) ? "[KEY 3]" : "[---]";
        keysText.setText(String.format("KEYS: %s %s %s (%d / 3)", k1, k2, k3, keys));

        // 3. Score & Formatted Timer
        scoreText.setText(String.format("SCORE: %,d PTS", player.getScore().getScore()));

        float elapsed = player.getScore().getElapsedTime();
        int minutes = (int) (elapsed / 60);
        float seconds = elapsed % 60;
        timerText.setText(String.format("TIME: %02d:%04.1f", minutes, seconds));

        // 4. Coins
        coinsText.setText(String.format("COINS: %d / 5", player.getScore().getCoinsCollected()));

        // 5. Enemy Radar Alert
        if (enemyController != null && enemyController.getEnemy() != null) {
            Enemy.State eState = enemyController.getEnemy().getState();
            if (eState == Enemy.State.CHASE) {
                float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() * 0.012);
                enemyStatusText.setColor(new ColorRGBA(1.0f, 0.15f, 0.15f, pulse));
                enemyStatusText.setText("! WARNING: STALKER PURSUING !");
            } else if (eState == Enemy.State.ATTACK) {
                enemyStatusText.setColor(new ColorRGBA(1.0f, 0.95f, 0.1f, 1.0f));
                enemyStatusText.setText("! DANGER: STALKER ATTACKING !");
            } else {
                enemyStatusText.setColor(new ColorRGBA(0.35f, 0.95f, 0.45f, 1.0f));
                enemyStatusText.setText("MAZE STALKER: PATROLLING (Unaware)");
            }
        }

        // 6. Notification Toast Timer
        if (notificationTimer > 0f) {
            notificationTimer -= tpf;
            if (notificationTimer <= 0f) {
                notificationText.setCullHint(Spatial.CullHint.Always);
            }
        }

        // 7. Telemetry Update with Live FPS Meter
        int gridX = (int) Math.floor(player.getPosX() / 3.0f);
        int gridZ = (int) Math.floor(player.getPosZ() / 3.0f);
        int fps = Math.round(1.0f / Math.max(tpf, 0.0001f));
        telemetryText.setText(String.format(
            "FPS: %d | Pos: (%.1f, %.1f) | Cell: [%d, %d] | Facing: %s | [WASD] Move | [Shift] Sprint | [ESC] Pause",
            fps, player.getPosX(), player.getPosZ(), gridX, gridZ, compassHeading != null ? compassHeading : "N/A"
        ));
    }

    public void showNotification(String message, float durationSeconds) {
        notificationText.setText(message);
        float sw = settings.getWidth();
        float sh = settings.getHeight();
        notificationText.setLocalTranslation(
            sw / 2.0f - notificationText.getLineWidth() / 2.0f,
            sh * 0.28f,
            5f
        );
        notificationText.setCullHint(Spatial.CullHint.Never);
        this.notificationTimer = durationSeconds;
    }

    public void setVisible(boolean visible) {
        hudRootNode.setCullHint(visible ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }
}

package com.mazeexplorer;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.mazeexplorer.ai.Enemy;
import com.mazeexplorer.ai.EnemyController;
import com.mazeexplorer.builder.Maze3DBuilder;
import com.mazeexplorer.camera.FirstPersonCamera;
import com.mazeexplorer.collision.CollisionSystem;
import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.entity.Collectible;
import com.mazeexplorer.entity.ExitDoor;
import com.mazeexplorer.maze.Maze15x15;
import com.mazeexplorer.maze.MazeGrid;
import com.mazeexplorer.pathfinding.PathNode;
import com.mazeexplorer.player.Player;
import com.mazeexplorer.player.PlayerController;
import com.mazeexplorer.state.GameStateManager;
import com.mazeexplorer.ui.hud.InGameHUD;
import com.mazeexplorer.ui.menu.MenuRenderer;
import com.mazeexplorer.ui.menu.UIActionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main application for ALGORITHMIC 3D MAZE EXPLORER.
 * Features a comprehensive UI & Presentation System:
 * - Professional Main Menu with start, instructions, and quit options
 * - Instructions screen detailing dungeon lore, mission objectives, and controls guide
 * - In-Game HUD: Dynamic Health bar, Mission Objective, Score, Timer, Key slots, Coin counter, Threat radar
 * - Responsive Pause Menu with resume, instructions, restart, and main menu options
 * - Victory Screen with mission performance debriefing and rank calculation
 * - Game Over Screen with survival statistics and respawn/restart support
 * - True 3D hardware-accelerated rendering with procedural geometry, lighting, and torches
 */
public class MazeExplorerApp extends SimpleApplication implements UIActionHandler.UIEventListener {

    // Subsystems
    private GameStateManager gameStateManager;
    private MenuRenderer menuRenderer;
    private InGameHUD hud;
    private UIActionHandler uiActionHandler;

    // 3D Scene & World
    private MazeGrid mazeGrid;
    private Maze3DBuilder mazeBuilder;
    private Node mazeSceneNode;

    // Player & Controls
    private Player player;
    private FirstPersonCamera cameraRig;
    private CollisionSystem collisionSystem;
    private PlayerController playerController;

    // Exit Gateway
    private ExitDoor exitDoor;
    private Spatial exitPortalCore;

    // Collectibles Management
    private static class ActiveCollectible {
        final Collectible model;
        final Node spatial;

        ActiveCollectible(Collectible model, Node spatial) {
            this.model = model;
            this.spatial = spatial;
        }
    }
    private final List<ActiveCollectible> activeCollectibles = new ArrayList<>();
    private Node collectiblesNode;

    // Enemy AI
    private Enemy enemy;
    private EnemyController enemyController;
    private Node enemySpatial;

    private float spawnWorldX;
    private float spawnWorldZ;
    private float enemySpawnX;
    private float enemySpawnZ;

    public static void main(String[] args) {
        MazeExplorerApp app = new MazeExplorerApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle(GameSettings.WINDOW_TITLE);
        settings.setWidth(GameSettings.WINDOW_WIDTH);
        settings.setHeight(GameSettings.WINDOW_HEIGHT);
        settings.setVSync(GameSettings.VSYNC_ENABLED);
        settings.setSamples(4); // 4x MSAA antialiasing
        settings.setResizable(true);

        app.setSettings(settings);
        app.setShowSettings(false); // Direct launch
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // 1. Dark crypt background atmospheric fog color
        viewPort.setBackgroundColor(new ColorRGBA(0.035f, 0.035f, 0.055f, 1.0f));

        // 2. Initialize 15x15 Maze Grid Data Model
        this.mazeGrid = Maze15x15.createGrid();

        // 3. Build 3D Dungeon Scene Graph (Walls, Floor, Ceiling, Torches, Gateways)
        this.mazeBuilder = new Maze3DBuilder(assetManager, mazeGrid);
        this.mazeSceneNode = mazeBuilder.buildMazeScene();
        rootNode.attachChild(mazeSceneNode);

        // Find Exit Portal Core spatial to update appearance on unlock
        this.exitPortalCore = mazeSceneNode.getChild("ExitPortalCore");

        // 4. Disable and unregister default flyCam to grant exclusive control to custom camera rig
        if (flyCam != null) {
            flyCam.setEnabled(false);
            flyCam.unregisterInput();
        }

        // 5. Initialize Player at Start Plinth [1, 1]
        this.spawnWorldX = Maze15x15.START_X * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        this.spawnWorldZ = Maze15x15.START_Z * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        float eyeY = GameSettings.PLAYER_HEIGHT;

        this.player = new Player(spawnWorldX, 0f, spawnWorldZ);

        // 6. Setup First-Person Camera Rig
        this.cameraRig = new FirstPersonCamera(cam);
        this.cameraRig.setPosition(spawnWorldX, eyeY, spawnWorldZ);
        this.cameraRig.lookAt(new Vector3f(spawnWorldX, eyeY, spawnWorldZ),
                              new Vector3f(spawnWorldX + 10.0f, eyeY, spawnWorldZ));

        // 7. Setup Collision Detection System
        this.collisionSystem = new CollisionSystem(mazeGrid, Maze3DBuilder.CELL_SIZE);

        // 8. Setup Player Controller (WASD, Mouse Look, Sliding, Pause)
        this.playerController = new PlayerController(player, cameraRig, collisionSystem, inputManager);
        this.playerController.setOnPauseCallback(this::handlePauseTrigger);
        this.playerController.setOnRespawnCallback(this::onRestartGame);

        // 9. Setup Exit Door at [13, 13]
        float exitWorldX = Maze15x15.EXIT_X * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        float exitWorldZ = Maze15x15.EXIT_Z * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        this.exitDoor = new ExitDoor(exitWorldX, exitWorldZ);

        // 10. Spawn Collectibles (3 Keys & 5 Coins)
        setupCollectibles();

        // 11. Spawn Maze Stalker Enemy AI
        setupEnemyAI();

        // 12. Setup UI Presentation Systems (State Manager, Menus, HUD, Input Handler)
        this.gameStateManager = new GameStateManager();
        this.menuRenderer = new MenuRenderer(guiNode, assetManager, settings);
        this.hud = new InGameHUD(guiNode, assetManager, settings);
        this.uiActionHandler = new UIActionHandler(gameStateManager, inputManager);
        this.uiActionHandler.setEventListener(this);

        // Start at MAIN_MENU state with mouse free and player frozen
        transitionToState(GameStateManager.State.MAIN_MENU);
    }

    private void setupCollectibles() {
        if (collectiblesNode == null) {
            this.collectiblesNode = new Node("CollectiblesGroup");
            rootNode.attachChild(collectiblesNode);
        } else {
            collectiblesNode.detachAllChildren();
            activeCollectibles.clear();
        }

        // 3 Keys at strategic reachable dead-ends / alcoves
        int[][] keyPositions = {
            {13, 1}, // East wing dead-end
            {7, 3},  // Central alcove
            {1, 13}  // South-west wing
        };

        for (int i = 0; i < keyPositions.length; i++) {
            int gx = keyPositions[i][0];
            int gz = keyPositions[i][1];
            float wx = gx * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
            float wz = gz * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;

            Collectible keyModel = new Collectible(Collectible.Type.KEY, wx, 1.0f, wz);
            Node keySpatial = mazeBuilder.buildKeySpatial(i + 1);
            keySpatial.setLocalTranslation(wx, 1.0f, wz);
            collectiblesNode.attachChild(keySpatial);

            activeCollectibles.add(new ActiveCollectible(keyModel, keySpatial));
        }

        // 5 Coins scattered in connecting corridors
        int[][] coinPositions = {
            {3, 1},
            {5, 5},
            {11, 3},
            {3, 11},
            {9, 9}
        };

        for (int i = 0; i < coinPositions.length; i++) {
            int gx = coinPositions[i][0];
            int gz = coinPositions[i][1];
            float wx = gx * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
            float wz = gz * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;

            Collectible coinModel = new Collectible(Collectible.Type.COIN, wx, 0.75f, wz);
            Node coinSpatial = mazeBuilder.buildCoinSpatial(i + 1);
            coinSpatial.setLocalTranslation(wx, 0.75f, wz);
            collectiblesNode.attachChild(coinSpatial);

            activeCollectibles.add(new ActiveCollectible(coinModel, coinSpatial));
        }
    }

    private void setupEnemyAI() {
        // Spawn Enemy at cell (9, 7)
        this.enemySpawnX = 9 * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        this.enemySpawnZ = 7 * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;

        this.enemy = new Enemy(enemySpawnX, 1.2f, enemySpawnZ);

        // Patrol circuit in central and eastern dungeon corridors
        List<PathNode> patrolWaypoints = List.of(
            new PathNode(9, 7),
            new PathNode(11, 7),
            new PathNode(11, 11),
            new PathNode(7, 11),
            new PathNode(7, 7)
        );

        this.enemyController = new EnemyController(enemy, mazeGrid, Maze3DBuilder.CELL_SIZE, patrolWaypoints);

        // Build procedural 3D Spatial and bind to controller
        this.enemySpatial = mazeBuilder.buildEnemySpatial();
        this.enemySpatial.setLocalTranslation(enemySpawnX, 1.2f, enemySpawnZ);
        rootNode.attachChild(enemySpatial);

        this.enemyController.setVisualSpatial(enemySpatial);
    }

    public void transitionToState(GameStateManager.State newState) {
        gameStateManager.transitionTo(newState);

        if (newState == GameStateManager.State.PLAYING) {
            playerController.setFrozen(false);
            playerController.captureMouse();
            hud.setVisible(true);
        } else {
            playerController.setFrozen(true);
            playerController.releaseMouse();
            hud.setVisible(false);
        }

        menuRenderer.updateView(
            newState,
            player != null ? player.getScore() : null,
            player != null ? player.getHealth().getCurrentHealth() : 100,
            player != null ? player.getHealth().getMaxHealth() : 100
        );
    }

    // --- UI Action Event Listeners ---

    @Override
    public void onStartGame() {
        if (gameStateManager.isMainMenu()) {
            restartExpedition();
            hud.showNotification("Expedition Begun! Recover 3 Keys to unseal the exit!", 5.0f);
        }
    }

    @Override
    public void onOpenInstructions() {
        transitionToState(GameStateManager.State.INSTRUCTIONS);
    }

    @Override
    public void onReturnFromInstructions() {
        GameStateManager.State prev = gameStateManager.getPreviousState();
        transitionToState(prev != null ? prev : GameStateManager.State.MAIN_MENU);
    }

    @Override
    public void onResumeGame() {
        if (gameStateManager.isPaused()) {
            transitionToState(GameStateManager.State.PLAYING);
            hud.showNotification("Expedition Resumed.", 2.0f);
        }
    }

    @Override
    public void onRestartGame() {
        restartExpedition();
        hud.showNotification("Expedition Restarted! Beware the Maze Stalker!", 4.0f);
    }

    @Override
    public void onReturnToMainMenu() {
        restartExpedition();
        transitionToState(GameStateManager.State.MAIN_MENU);
    }

    @Override
    public void onQuitToDesktop() {
        stop();
    }

    private void handlePauseTrigger() {
        if (gameStateManager.isPlaying()) {
            transitionToState(GameStateManager.State.PAUSED);
        } else if (gameStateManager.isPaused()) {
            transitionToState(GameStateManager.State.PLAYING);
        }
    }

    public void restartExpedition() {
        // Reset player coordinates, camera, health, and score
        player.setPosition(spawnWorldX, 0f, spawnWorldZ);
        player.getHealth().reset();
        player.getScore().reset();
        cameraRig.setPosition(spawnWorldX, GameSettings.PLAYER_HEIGHT, spawnWorldZ);
        cameraRig.lookAt(new Vector3f(spawnWorldX, GameSettings.PLAYER_HEIGHT, spawnWorldZ),
                         new Vector3f(spawnWorldX + 10.0f, GameSettings.PLAYER_HEIGHT, spawnWorldZ));

        // Reset enemy coordinates and state
        enemy.setPosX(enemySpawnX);
        enemy.setPosZ(enemySpawnZ);
        enemyController.resetToPatrol();

        // Reset Exit Door & Portal Core appearance
        float exitWorldX = Maze15x15.EXIT_X * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        float exitWorldZ = Maze15x15.EXIT_Z * Maze3DBuilder.CELL_SIZE + Maze3DBuilder.CELL_SIZE / 2.0f;
        this.exitDoor = new ExitDoor(exitWorldX, exitWorldZ);
        mazeBuilder.updateExitPortalCore(exitPortalCore, false);

        // Respawn all 3 Keys & 5 Coins
        setupCollectibles();

        // Switch to PLAYING
        transitionToState(GameStateManager.State.PLAYING);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Update dynamic dungeon atmosphere (torch flicker, crystal pulse, portal energy rings)
        if (mazeBuilder != null) {
            mazeBuilder.updateAtmosphere(tpf);
        }

        GameStateManager.State currentState = gameStateManager.getCurrentState();

        // When in Main Menu: provide smooth slow 3D dungeon camera panning in background
        if (currentState == GameStateManager.State.MAIN_MENU) {
            cameraRig.rotate(0.08f * tpf, 0f);
            return;
        }

        // When paused or reading instructions: freeze game world
        if (currentState == GameStateManager.State.PAUSED || currentState == GameStateManager.State.INSTRUCTIONS) {
            return;
        }

        // When victory or game-over screen active: frozen
        if (currentState == GameStateManager.State.VICTORY || currentState == GameStateManager.State.GAME_OVER) {
            return;
        }

        // Active PLAYING loop:
        // 1. Update Game Timers
        player.getScore().update(tpf);
        player.getHealth().update(tpf);

        // 2. Update First-Person Movement & Wall Sliding
        playerController.update(tpf);

        // 3. Update Collectibles & Proximity Pickups
        updateCollectibles(tpf);

        // 4. Update Enemy AI (DDA LOS, BFS Pursuit, Melee Damage)
        enemyController.update(tpf, player);

        // Check if player died from enemy attacks
        if (player.getHealth().isDead()) {
            transitionToState(GameStateManager.State.GAME_OVER);
            return;
        }

        // 5. Check Exit Portal Trigger
        checkExitDoorTrigger();

        // 6. Update HUD Overlay
        hud.update(tpf, player, enemyController, exitDoor, cameraRig.getCompassHeading());
    }

    private void updateCollectibles(float tpf) {
        float px = player.getPosX();
        float pz = player.getPosZ();
        float pickupRadius = 1.3f;

        Iterator<ActiveCollectible> iter = activeCollectibles.iterator();
        while (iter.hasNext()) {
            ActiveCollectible ac = iter.next();

            if (ac.model.checkPickup(px, pz, pickupRadius)) {
                // Item picked up!
                if (ac.model.getType() == Collectible.Type.KEY) {
                    player.getScore().collectKey();
                    int keys = player.getScore().getKeysCollected();
                    hud.showNotification(String.format("FOUND GOLDEN KEY! (%d / 3)", keys), 3.5f);

                    if (keys >= ExitDoor.REQUIRED_KEYS) {
                        exitDoor.tryUnlock(keys);
                        mazeBuilder.updateExitPortalCore(exitPortalCore, true);
                        hud.showNotification("ALL 3 KEYS FOUND! THE EXIT PORTAL IS UNLOCKED!", 5.0f);
                    }
                } else if (ac.model.getType() == Collectible.Type.COIN) {
                    player.getScore().collectCoin(ac.model.getType().getPointValue());
                    hud.showNotification(String.format("+100 PTS! Ancient Coin Recovered (%d / 5)",
                            player.getScore().getCoinsCollected()), 2.5f);
                }

                collectiblesNode.detachChild(ac.spatial);
                iter.remove();
            } else {
                ac.model.update(tpf);
                ac.spatial.setLocalTranslation(ac.model.getBaseWorldX(), ac.model.getCurrentY(), ac.model.getBaseWorldZ());
                ac.spatial.rotate(0f, 2.2f * tpf, 0f);
            }
        }
    }

    private void checkExitDoorTrigger() {
        float px = player.getPosX();
        float pz = player.getPosZ();

        if (exitDoor.isPlayerAtDoor(px, pz)) {
            if (exitDoor.isUnlocked()) {
                // VICTORY!
                transitionToState(GameStateManager.State.VICTORY);
            } else {
                hud.showNotification(String.format(
                    "Exit Portal Locked! Collect all 3 Golden Keys (Found: %d / 3)",
                    player.getScore().getKeysCollected()
                ), 1.5f);
            }
        }
    }

    // Getters for subsystems
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public Player getPlayer() {
        return player;
    }

    public EnemyController getEnemyController() {
        return enemyController;
    }

    public ExitDoor getExitDoor() {
        return exitDoor;
    }

    public List<ActiveCollectible> getActiveCollectibles() {
        return activeCollectibles;
    }
}

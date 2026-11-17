package com.raycaster;

import com.raycaster.config.GameConfig;
import com.raycaster.core.Camera;
import com.raycaster.core.MazeMap;
import com.raycaster.core.Player;
import com.raycaster.engine.RayCaster;
import com.raycaster.engine.Renderer;
import com.raycaster.ui.ShadingPalette.PaletteType;
import com.raycaster.ui.TerminalUI;
import java.util.Scanner;

/**
 * Entry point for the Algorithmic ASCII-Art Ray Caster application.
 * Manages the main interactive game loop, command processing, and application lifecycle.
 */
public class Main {

    public static void main(String[] args) {
        // Configure standard output encoding for UTF-8 support
        try {
            System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        // Check command-line arguments for initial settings
        boolean useUnicode = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--unicode") || arg.equalsIgnoreCase("-u")) {
                useUnicode = true;
            }
        }

        // Initialize core game components
        GameConfig config = new GameConfig();
        MazeMap map = MazeMap.createDefaultMap();

        // Initial player position: corridor cell (1.5, 1.5) facing East (angle = 0.0)
        Player player = new Player(1.5, 1.5, 0.0, config.getCollisionRadius());
        Camera camera = new Camera(player.getAngle(), config.getFovRadians());
        RayCaster rayCaster = new RayCaster();
        Renderer renderer = new Renderer(config.getScreenWidth(), config.getScreenHeight(),
                useUnicode ? PaletteType.UNICODE_BLOCKS : PaletteType.CLASSIC_ASCII);
        TerminalUI ui = new TerminalUI(config, rayCaster, renderer);

        Scanner scanner = new Scanner(System.in);

        // Display welcome banner on initial launch
        ui.displayWelcome();
        try {
            scanner.nextLine();
        } catch (Exception e) {
            // Ignore in non-interactive piping environments
        }

        boolean running = true;
        String statusNotification = "Welcome! Use W/S to move, A/D to rotate, H for help.";

        while (running) {
            // Synchronize camera with current player orientation
            camera.update(player.getAngle(), config.getFovRadians());

            // Display current screen frame
            ui.displayFrame(player, camera, map, statusNotification);
            statusNotification = "";

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            // Check full-word commands
            String lowerInput = input.toLowerCase();
            if (lowerInput.equals("x") || lowerInput.equals("exit") || lowerInput.equals("quit")) {
                running = false;
                break;
            }

            if (lowerInput.equals("help") || lowerInput.equals("h")) {
                ui.displayHelp();
                try {
                    scanner.nextLine();
                } catch (Exception ignored) {}
                continue;
            }

            // Process command characters (supports multi-command chaining like 'wwad')
            StringBuilder feedback = new StringBuilder();
            boolean collisionOccurred = false;

            for (int i = 0; i < input.length(); i++) {
                char cmd = Character.toUpperCase(input.charAt(i));

                switch (cmd) {
                    case 'W':
                        boolean movedFwd = player.moveForward(map, config.getMoveStep());
                        if (!movedFwd) collisionOccurred = true;
                        break;

                    case 'S':
                        boolean movedBack = player.moveBackward(map, config.getMoveStep());
                        if (!movedBack) collisionOccurred = true;
                        break;

                    case 'A':
                        player.rotateLeft(config.getRotationStepRadians());
                        break;

                    case 'D':
                        player.rotateRight(config.getRotationStepRadians());
                        break;

                    case 'Q':
                        boolean movedLeft = player.strafeLeft(map, config.getMoveStep());
                        if (!movedLeft) collisionOccurred = true;
                        break;

                    case 'E':
                        boolean movedRight = player.strafeRight(map, config.getMoveStep());
                        if (!movedRight) collisionOccurred = true;
                        break;

                    case 'M':
                        config.toggleMiniMap();
                        feedback.append("Mini-map toggled ").append(config.isMiniMapEnabled() ? "ON. " : "OFF. ");
                        break;

                    case 'P':
                        renderer.cyclePalette();
                        feedback.append("Palette switched to: ").append(renderer.getPalette().getDisplayName()).append(". ");
                        break;

                    case '1':
                        map = MazeMap.createDefaultMap();
                        player.setX(2.5);
                        player.setY(2.5);
                        player.setAngle(0.0);
                        feedback.append("Switched to Map 1: 10x8 Classic Maze. ");
                        break;

                    case '2':
                        map = MazeMap.createPillarsMap();
                        player.setX(1.5);
                        player.setY(1.5);
                        player.setAngle(0.0);
                        feedback.append("Switched to Map 2: 10x8 Pillar Hall. ");
                        break;

                    case '3':
                        map = MazeMap.createLabyrinthMap();
                        player.setX(1.5);
                        player.setY(1.5);
                        player.setAngle(0.0);
                        feedback.append("Switched to Map 3: 12x10 Winding Labyrinth. ");
                        break;

                    case 'H':
                        ui.displayHelp();
                        try {
                            scanner.nextLine();
                        } catch (Exception ignored) {}
                        break;

                    default:
                        feedback.append("Unknown command '").append(cmd).append("'. ");
                        break;
                }
            }

            if (collisionOccurred) {
                feedback.append("Wall collision detected! Movement blocked or adjusted. ");
            }

            statusNotification = feedback.toString().trim();
        }

        ui.displayExit();
        scanner.close();
    }
}

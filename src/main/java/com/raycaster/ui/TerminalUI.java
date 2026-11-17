package com.raycaster.ui;

import com.raycaster.config.GameConfig;
import com.raycaster.core.Camera;
import com.raycaster.core.MazeMap;
import com.raycaster.core.Player;
import com.raycaster.engine.RayCaster;
import com.raycaster.engine.RayHit;
import com.raycaster.engine.Renderer;
import com.raycaster.ui.ShadingPalette.PaletteType;
import java.util.List;

/**
 * Manages terminal output formatting, user interface layout, status bar,
 * mini-map placement, control menus, and help displays.
 */
public class TerminalUI {

    private final GameConfig config;
    private final RayCaster rayCaster;
    private final Renderer renderer;
    private final MiniMap miniMapUnicode;
    private final MiniMap miniMapAscii;

    public TerminalUI(GameConfig config, RayCaster rayCaster, Renderer renderer) {
        this.config = config;
        this.rayCaster = rayCaster;
        this.renderer = renderer;
        this.miniMapUnicode = new MiniMap(true);
        this.miniMapAscii = new MiniMap(false);
    }

    /**
     * Clears terminal screen using ANSI escape sequences.
     */
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Renders and displays the complete interactive frame:
     * Header, 3D viewport, status bar, mini-map, and controls.
     */
    public void displayFrame(Player player, Camera camera, MazeMap map, String statusNotification) {
        clearScreen();

        // 1. Ray cast screen columns
        RayHit[] hits = rayCaster.castScreen(player, camera, map, config.getScreenWidth(), config.getMaxRayDistance());

        // 2. Render 3D view buffer
        char[][] frameBuffer = renderer.renderFrame(hits);
        String[] viewportLines = renderer.formatFrame(frameBuffer, true);

        // 3. Print Header Banner
        System.out.println("================================================================================");
        System.out.println("                   ALGORITHMIC ASCII-ART RAY CASTER (3D)                        ");
        System.out.println("================================================================================");

        // 4. Print 3D Viewport
        for (String line : viewportLines) {
            System.out.println(line);
        }

        // 5. Print HUD Status Bar
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Pos: (X: %.2f, Y: %.2f) | Angle: %.1f° [%s] | Map: %s%n",
                player.getX(), player.getY(), player.getAngleDegrees(),
                player.getCompassDirection(), map.getName());
        System.out.printf(" FOV: %.0f° | Shading: %s | MiniMap: [%s]%n",
                config.getFovDegrees(), renderer.getPalette().getDisplayName(),
                config.isMiniMapEnabled() ? "ON" : "OFF");
        System.out.println("--------------------------------------------------------------------------------");

        // 6. Print Mini-Map if enabled
        if (config.isMiniMapEnabled()) {
            boolean isUnicode = renderer.getPalette() == PaletteType.UNICODE_BLOCKS;
            MiniMap activeMiniMap = isUnicode ? miniMapUnicode : miniMapAscii;
            char playerIcon = player.getOrientationChar(isUnicode);
            String wallIcon = isUnicode ? "██" : "##";

            System.out.println(">> TOP-DOWN MINIMAP (Player: '" + playerIcon + "' | Wall: '" + wallIcon + "' | Open: '  '):");
            List<String> mapLines = activeMiniMap.renderMiniMap(map, player);
            for (String mapLine : mapLines) {
                System.out.println("   " + mapLine);
            }
            System.out.println("--------------------------------------------------------------------------------");
        }

        // 7. Notification message (if any action occurred)
        if (statusNotification != null && !statusNotification.isEmpty()) {
            System.out.println(">> " + statusNotification);
            System.out.println("--------------------------------------------------------------------------------");
        }

        // 8. Command Reference
        System.out.println(" CONTROLS: [W] Forward   [S] Backward    [A] Turn Left   [D] Turn Right");
        System.out.println("           [Q/E] Strafe  [M] MiniMap     [P] Palette     [1-3] Select Map");
        System.out.println("           [H] Help      [X] Exit Game   (Supports multi-keys, e.g. 'wwad')");
        System.out.print(" Command > ");
    }

    /**
     * Displays detailed help manual explaining controls and ray casting principles.
     */
    public void displayHelp() {
        clearScreen();
        System.out.println("================================================================================");
        System.out.println("                  ALGORITHMIC RAY CASTER - HELP & MANUAL                        ");
        System.out.println("================================================================================");
        System.out.println(" 1. NAVIGATION CONTROLS:");
        System.out.println("    - W : Move forward along viewing angle by " + config.getMoveStep() + " units");
        System.out.println("    - S : Move backward along viewing angle by " + config.getMoveStep() + " units");
        System.out.println("    - A : Rotate camera left by " + config.getRotationStepDegrees() + " degrees");
        System.out.println("    - D : Rotate camera right by " + config.getRotationStepDegrees() + " degrees");
        System.out.println("    - Q : Strafe left perpendicular to viewing angle");
        System.out.println("    - E : Strafe right perpendicular to viewing angle");
        System.out.println("    - Tip: You can chain commands! Entering 'wwad' moves forward twice,");
        System.out.println("           turns left, and turns right in one execution.");
        System.out.println();
        System.out.println(" 2. DISPLAY & ENVIRONMENT SETTINGS:");
        System.out.println("    - M : Toggle Top-Down Mini-Map on / off");
        System.out.println("    - P : Cycle ASCII Shading palette (Unicode Blocks / Classic ASCII / Contrast)");
        System.out.println("    - 1 : Switch to Map Preset 1 (10x8 Classic Maze)");
        System.out.println("    - 2 : Switch to Map Preset 2 (10x8 Pillar Hall)");
        System.out.println("    - 3 : Switch to Map Preset 3 (12x10 Winding Labyrinth)");
        System.out.println("    - H : Show this help manual");
        System.out.println("    - X / QUIT : Exit the application");
        System.out.println();
        System.out.println(" 3. HOW THE RAY CASTER WORKS:");
        System.out.println("    - Digital Differential Analyzer (DDA) traverses the 2D grid cell-by-cell.");
        System.out.println("    - Calculates perpendicular distance to avoid fish-eye lens curvature.");
        System.out.println("    - Apparent wall height = ScreenHeight / PerpendicularDistance.");
        System.out.println("    - Characters are shaded based on depth and surface orientation (X vs Y hits).");
        System.out.println("    - Player has an active collision radius preventing wall clipping.");
        System.out.println("================================================================================");
        System.out.print(" Press [Enter] to return to the simulation... ");
    }

    /**
     * Prints welcome splash screen on startup.
     */
    public void displayWelcome() {
        clearScreen();
        System.out.println("================================================================================");
        System.out.println("      _    _                 _ _   _               _        ____  ____  ");
        System.out.println("     / \\  | | __ _  ___ _ __(_) |_| |__  _ __ ___ (_) ___  |___ \\|  _ \\ ");
        System.out.println("    / _ \\ | |/ _` |/ _ \\ '__| | __| '_ \\| '_ ` _ \\| |/ __|   __) | | | |");
        System.out.println("   / ___ \\| | (_| |  __/ |  | | |_| | | | | | | | | | (__   / __/| |_| |");
        System.out.println("  /_/   \\_\\_|\\__, |\\___|_|  |_|\\__|_| |_|_| |_| |_|_|\\___| |_____|____/ ");
        System.out.println("             |___/                                                      ");
        System.out.println("                    ASCII-ART 3D RAY CASTING SIMULATION                 ");
        System.out.println("================================================================================");
        System.out.println(" Welcome to the Algorithmic ASCII-Art Ray Caster!");
        System.out.println(" This application renders a pseudo-3D first-person perspective inside your");
        System.out.println(" terminal using pure mathematics, DDA ray traversal, and ASCII shading.");
        System.out.println();
        System.out.println(" - 2D Grid Maze representation");
        System.out.println(" - DDA Ray Tracing with fish-eye distortion correction");
        System.out.println(" - Real-time collision detection and wall sliding");
        System.out.println(" - Interactive top-down mini-map with orientation heading");
        System.out.println("================================================================================");
        System.out.print(" Press [Enter] to enter the maze... ");
    }

    /**
     * Prints goodbye message on exit.
     */
    public void displayExit() {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println(" Thank you for exploring the Algorithmic ASCII-Art Ray Caster!");
        System.out.println(" Simulation terminated safely. Goodbye!");
        System.out.println("================================================================================");
    }
}

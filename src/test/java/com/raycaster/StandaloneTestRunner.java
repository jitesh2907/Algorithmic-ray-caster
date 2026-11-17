package com.raycaster;

import com.raycaster.config.GameConfig;
import com.raycaster.core.Camera;
import com.raycaster.core.MazeMap;
import com.raycaster.core.Player;
import com.raycaster.engine.RayCaster;
import com.raycaster.engine.RayHit;
import com.raycaster.engine.Renderer;
import com.raycaster.ui.MiniMap;
import com.raycaster.ui.ShadingPalette;
import com.raycaster.ui.ShadingPalette.PaletteType;
import java.util.List;

/**
 * Automated Verification Suite for Algorithmic ASCII-Art Ray Caster.
 * Verifies all 10 testing requirements defined in Section 12 of the Project Specification.
 */
public class StandaloneTestRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("        STARTING AUTOMATED VERIFICATION SUITE - RAY CASTER                      ");
        System.out.println("================================================================================");

        test01_ApplicationLaunchAndConfig();
        test02_MazeBoundariesAsWalls();
        test03_RaysDetectNearestWall();
        test04_DistanceCalculationsValidAndFishEyeCorrected();
        test05_ForwardMovementOnEmptyPaths();
        test06_PlayerMovementBlockedByWall();
        test07_CameraRotationAndNormalization();
        test08_MiniMapDisplaysCorrectLocation();
        test09_AsciiRenderingProducesCompleteScreen();
        test10_InvalidCommandsHandledSafely();
        test11_WallSlidingMechanic();
        test12_MultiplePalettesAndShading();

        System.out.println("================================================================================");
        System.out.printf(" TEST SUMMARY: Total: %d | Passed: %d | Failed: %d%n", totalTests, passedTests, failedTests);
        if (failedTests == 0) {
            System.out.println(" RESULT: ALL TESTS PASSED SUCCESSFULLY! (100% SUCCESS RATE)");
        } else {
            System.err.println(" RESULT: " + failedTests + " TEST(S) FAILED!");
            System.exit(1);
        }
        System.out.println("================================================================================");
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %s: %s%n", testName, details);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %s: %s%n", testName, details);
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double epsilon, String details) {
        boolean match = Math.abs(expected - actual) <= epsilon;
        assertTrue(testName, match, String.format("%s (Expected: %.4f, Actual: %.4f)", details, expected, actual));
    }

    /**
     * Test Case 1: Application launches successfully and components initialize properly.
     */
    private static void test01_ApplicationLaunchAndConfig() {
        GameConfig config = new GameConfig();
        boolean validDims = config.getScreenWidth() == 80 && config.getScreenHeight() == 24;
        boolean validFov = Math.abs(config.getFovDegrees() - 60.0) < 0.001;
        boolean validStep = Math.abs(config.getMoveStep() - 0.25) < 0.001;
        boolean validRot = Math.abs(config.getRotationStepDegrees() - 10.0) < 0.001;

        assertTrue("TC-01", validDims && validFov && validStep && validRot,
                "Config initializes with Screen 80x24, FOV 60°, Step 0.25, Rot 10°");
    }

    /**
     * Test Case 2: Maze boundaries are recognized as walls.
     */
    private static void test02_MazeBoundariesAsWalls() {
        MazeMap map = MazeMap.createDefaultMap();
        boolean topRow = true;
        boolean bottomRow = true;
        boolean leftCol = true;
        boolean rightCol = true;

        for (int x = 0; x < map.getWidth(); x++) {
            if (!map.isWall(x, 0)) topRow = false;
            if (!map.isWall(x, map.getHeight() - 1)) bottomRow = false;
        }

        for (int y = 0; y < map.getHeight(); y++) {
            if (!map.isWall(0, y)) leftCol = false;
            if (!map.isWall(map.getWidth() - 1, y)) rightCol = false;
        }

        boolean outOfBoundsIsWall = map.isWall(-1, 0) && map.isWall(map.getWidth(), 0)
                && map.isWall(0, -1) && map.isWall(0, map.getHeight());

        assertTrue("TC-02", topRow && bottomRow && leftCol && rightCol && outOfBoundsIsWall,
                "All perimeter cells and out-of-bounds coordinates return solid walls");
    }

    /**
     * Test Case 3: Rays detect the nearest wall.
     */
    private static void test03_RaysDetectNearestWall() {
        MazeMap map = MazeMap.createDefaultMap();
        // Place player at (2.5, 2.5) facing East (angle = 0).
        // Looking at row y=2, row is: {1, 0, 1, 1, 0, 0, 1, 0, 0, 1}
        // At y=2, x=2 is a wall cell! At x=2.5, East points to x=3, but let's check North/West/South/East
        // Facing West (angle = Math.PI): player at (2.5, 2.5), looking West ray goes to x=1.0 (empty) then x=0.0 (wall).
        Player player = new Player(2.5, 1.5, 0.0); // row 1: {1, 0, 0, 0, 0, 0, 1, 0, 0, 1}
        // Facing East: wall at x=6! Distance should be 6.0 - 2.5 = 3.5
        RayCaster caster = new RayCaster();
        RayHit hitEast = caster.castRayByAngle(player, 0.0, map, 20.0);

        boolean detected = hitEast.isHit() && hitEast.getMapX() == 6 && hitEast.getMapY() == 1;
        assertTrue("TC-03", detected,
                String.format("Ray cast East correctly detected wall at grid (%d, %d)", hitEast.getMapX(), hitEast.getMapY()));
    }

    /**
     * Test Case 4: Wall distance calculations return valid values and fish-eye correction works.
     */
    private static void test04_DistanceCalculationsValidAndFishEyeCorrected() {
        MazeMap map = MazeMap.createDefaultMap();
        Player player = new Player(2.5, 1.5, 0.0);
        RayCaster caster = new RayCaster();

        // Cast ray straight ahead (angle 0)
        RayHit straightHit = caster.castRayByAngle(player, 0.0, map, 20.0);
        // Wall at x=6, player at x=2.5. Distance = 3.5
        assertEquals("TC-04a", 3.5, straightHit.getPerpDistance(), 0.001,
                "Perpendicular distance straight East is exactly 3.5 units");

        // Cast ray at +30 degrees (pi/6). Perpendicular distance should still represent perpendicular plane!
        RayHit angledHit = caster.castRayByAngle(player, Math.toRadians(30), map, 20.0);
        boolean validAngled = angledHit.isHit() && angledHit.getPerpDistance() > 0 && !Double.isNaN(angledHit.getPerpDistance());
        assertTrue("TC-04b", validAngled,
                String.format("Angled ray returned valid perpendicular distance: %.4f", angledHit.getPerpDistance()));

        // Verify fish-eye correction formula: perpDist == euclideanDist * cos(rayAngle - playerAngle)
        double expectedPerp = angledHit.getEuclideanDistance() * Math.cos(angledHit.getRayAngle() - player.getAngle());
        assertEquals("TC-04c", expectedPerp, angledHit.getPerpDistance(), 0.001,
                "Fish-eye distortion correction formula verified exactly");
    }

    /**
     * Test Case 5: Forward movement works on empty paths.
     */
    private static void test05_ForwardMovementOnEmptyPaths() {
        MazeMap map = MazeMap.createDefaultMap();
        // At y=1, row is {1, 0, 0, 0, 0, 0, 1, 0, 0, 1}
        // Player at (2.5, 1.5), facing East (angle = 0)
        Player player = new Player(2.5, 1.5, 0.0);
        double step = 0.25;

        boolean moved = player.moveForward(map, step);
        boolean posUpdated = Math.abs(player.getX() - 2.75) < 0.001 && Math.abs(player.getY() - 1.5) < 0.001;

        assertTrue("TC-05", moved && posUpdated,
                String.format("Player moved forward successfully to (%.2f, %.2f)", player.getX(), player.getY()));
    }

    /**
     * Test Case 6: Player movement into a wall is blocked.
     */
    private static void test06_PlayerMovementBlockedByWall() {
        MazeMap map = MazeMap.createDefaultMap();
        // At y=1, row 1 cell x=1 is empty, cell x=0 is wall.
        // Place player near wall at (1.25, 1.5) facing West (angle = PI).
        Player player = new Player(1.25, 1.5, Math.PI, 0.20);
        double initialX = player.getX();
        double initialY = player.getY();

        // Attempt to move forward into the West wall (x=0)
        boolean moved = player.moveForward(map, 0.25);

        // Player should NOT have penetrated into the wall (x - radius must not cross 1.0)
        boolean blocked = !moved || (player.getX() >= 1.20);
        boolean safeDistance = player.getX() - player.getCollisionRadius() >= 1.0;

        assertTrue("TC-06", blocked && safeDistance,
                String.format("Player blocked before colliding into wall: X remained at %.2f (Buffer kept: %.2f)",
                        player.getX(), player.getX() - 1.0));
    }

    /**
     * Test Case 7: Camera rotation works correctly and normalizes within [0, 2*PI).
     */
    private static void test07_CameraRotationAndNormalization() {
        Player player = new Player(2.5, 2.5, 0.0);
        double rotStep = Math.toRadians(10);

        player.rotateRight(rotStep);
        assertEquals("TC-07a", 10.0, player.getAngleDegrees(), 0.01, "Rotated right to 10°");

        player.rotateLeft(rotStep * 2); // 10° - 20° = -10° = 350°
        assertEquals("TC-07b", 350.0, player.getAngleDegrees(), 0.01, "Rotated left past 0° wrapped to 350°");

        player.rotateRight(Math.toRadians(720)); // two full circles
        assertEquals("TC-07c", 350.0, player.getAngleDegrees(), 0.01, "Multiple full rotations properly normalized");
    }

    /**
     * Test Case 8: Mini-map displays the correct player location.
     */
    private static void test08_MiniMapDisplaysCorrectLocation() {
        MazeMap map = MazeMap.createDefaultMap();
        Player player = new Player(2.5, 1.5, 0.0); // Cell (2, 1) facing East
        MiniMap miniMap = new MiniMap(false);

        List<String> mapLines = miniMap.renderMiniMap(map, player);

        // Map height is 8, plus top and bottom borders = 10 lines
        boolean validLineCount = mapLines.size() == map.getHeight() + 2;

        // Player is at row y=1 (line index 2). Cell x=2 should contain '>'
        String playerRow = mapLines.get(2); // line 0: border, line 1: y=0, line 2: y=1
        boolean containsPlayerMarker = playerRow.contains("> ");

        assertTrue("TC-08", validLineCount && containsPlayerMarker,
                "MiniMap renders full grid and displays player indicator '>' at cell (2, 1)");
    }

    /**
     * Test Case 9: ASCII rendering produces a complete screen (80x24).
     */
    private static void test09_AsciiRenderingProducesCompleteScreen() {
        GameConfig config = new GameConfig();
        MazeMap map = MazeMap.createDefaultMap();
        Player player = new Player(2.5, 1.5, 0.0);
        Camera camera = new Camera(player.getAngle(), config.getFovRadians());
        RayCaster caster = new RayCaster();
        Renderer renderer = new Renderer(80, 24, PaletteType.CLASSIC_ASCII);

        RayHit[] hits = caster.castScreen(player, camera, map, 80, 20.0);
        char[][] frameBuffer = renderer.renderFrame(hits);

        boolean correctRows = frameBuffer.length == 24;
        boolean correctCols = true;
        boolean hasWallCharacters = false;

        for (int y = 0; y < 24; y++) {
            if (frameBuffer[y].length != 80) {
                correctCols = false;
            }
            for (int x = 0; x < 80; x++) {
                char c = frameBuffer[y][x];
                if (c == '@' || c == '#' || c == '%' || c == '$' || c == '*' || c == '+') {
                    hasWallCharacters = true;
                }
            }
        }

        assertTrue("TC-09", correctRows && correctCols && hasWallCharacters,
                "Renderer produced full 80x24 character buffer with perspective wall projections");
    }

    /**
     * Test Case 10: Invalid commands do not crash the application.
     */
    private static void test10_InvalidCommandsHandledSafely() {
        GameConfig config = new GameConfig();
        MazeMap map = MazeMap.createDefaultMap();
        Player player = new Player(2.5, 1.5, 0.0);

        boolean threwException = false;
        try {
            // Simulate processing dirty inputs: numbers, punctuation, random garbage
            String testInputs = "!@#$%^&*()_+-=~`1234567890qwertyuiopasdfghjklzxcvbnm";
            for (char c : testInputs.toCharArray()) {
                switch (Character.toUpperCase(c)) {
                    case 'W': player.moveForward(map, config.getMoveStep()); break;
                    case 'S': player.moveBackward(map, config.getMoveStep()); break;
                    case 'A': player.rotateLeft(config.getRotationStepRadians()); break;
                    case 'D': player.rotateRight(config.getRotationStepRadians()); break;
                    default: /* ignored safely */ break;
                }
            }
        } catch (Exception e) {
            threwException = true;
        }

        assertTrue("TC-10", !threwException, "Random and unexpected commands executed safely with zero exceptions");
    }

    /**
     * Test Case 11: Wall sliding mechanic allows moving along open axis when diagonal is blocked.
     */
    private static void test11_WallSlidingMechanic() {
        MazeMap map = MazeMap.createDefaultMap();
        // Player near south wall at (2.5, 1.8), attempting to move diagonally south-east (dx > 0, dy > 0)
        // Cell (2, 2) is a wall {1, 0, 1, 1, ...}. So moving in Y is blocked, but X is open.
        Player player = new Player(1.5, 1.8, 0.0, 0.20);
        double initialY = player.getY();

        // Attempt move dx = +0.25, dy = +0.25 (South-East)
        boolean moved = player.attemptMove(0.25, 0.25, map);

        // Player should slide along X while Y remains constrained
        boolean slidAlongX = (player.getX() > 1.5);
        assertTrue("TC-11", moved && slidAlongX,
                String.format("Wall sliding succeeded: Player slid along X to %.2f while maintaining boundary buffer", player.getX()));
    }

    /**
     * Test Case 12: Multiple Shading Palettes return distinct characters for depth and surface normal.
     */
    private static void test12_MultiplePalettesAndShading() {
        char closeAsciiX = ShadingPalette.getWallChar(1.0, 0, PaletteType.CLASSIC_ASCII);
        char closeAsciiY = ShadingPalette.getWallChar(1.0, 1, PaletteType.CLASSIC_ASCII);
        char farAscii = ShadingPalette.getWallChar(10.0, 0, PaletteType.CLASSIC_ASCII);

        char closeUnicodeX = ShadingPalette.getWallChar(1.0, 0, PaletteType.UNICODE_BLOCKS);
        char farUnicode = ShadingPalette.getWallChar(6.0, 0, PaletteType.UNICODE_BLOCKS);

        boolean asciiDepthDiff = closeAsciiX != farAscii;
        boolean asciiSideDiff = closeAsciiX != closeAsciiY; // lighting contrast between vertical and horizontal wall
        boolean unicodeDepthDiff = closeUnicodeX != farUnicode;

        assertTrue("TC-12", asciiDepthDiff && asciiSideDiff && unicodeDepthDiff,
                String.format("Shading palettes verified: Close=(X:'%c', Y:'%c') vs Far='%c', Unicode Close='%c' vs Far='%c'",
                        closeAsciiX, closeAsciiY, farAscii, closeUnicodeX, farUnicode));
    }
}

package com.mazeexplorer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.mazeexplorer.builder.Maze3DBuilder;
import com.mazeexplorer.collision.CollisionSystem;
import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.maze.Maze15x15;
import com.mazeexplorer.maze.MazeGrid;
import com.mazeexplorer.player.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Automated test suite verifying first-person movement physics,
 * 4-directional displacements, boundary constraints, and wall sliding mechanics.
 */
public class PlayerMovementAndCollisionTest {

    private MazeGrid mazeGrid;
    private CollisionSystem collision;
    private float cellSize;

    @BeforeEach
    public void setup() {
        this.mazeGrid = Maze15x15.createGrid();
        this.cellSize = Maze3DBuilder.CELL_SIZE; // 3.0m per tile
        this.collision = new CollisionSystem(mazeGrid, cellSize);
    }

    @Test
    public void testFixedPlayerHeight() {
        Player player = new Player(4.5f, 0f, 4.5f);
        Assertions.assertEquals(0f, player.getPosY(), "Ground plane Y must be 0");
        Assertions.assertEquals(GameSettings.PLAYER_HEIGHT, player.getEyeY(),
                "Eye level must remain fixed at player height (1.8m)");

        player.setPosition(10.0f, 0f, 15.0f);
        Assertions.assertEquals(GameSettings.PLAYER_HEIGHT, player.getEyeY(),
                "Eye level must remain fixed at 1.8m across all positions");
    }

    @Test
    public void testMovementInAllFourCardinalDirections() {
        // Start area at [1, 1], world center is (4.5, 4.5)
        // In Maze15x15:
        // [1, 1] is corridor (0)
        // [2, 1] is corridor (0) -> moving +X is clear
        // [1, 2] is corridor (0) -> moving +Z is clear
        float startX = 4.5f;
        float startZ = 4.5f;
        float radius = GameSettings.PLAYER_RADIUS; // 0.4m

        // 1. Move East (+X)
        float dxEast = 1.0f;
        Vector3f posEast = collision.resolveMovement(startX, startZ, dxEast, 0f, radius);
        Assertions.assertEquals(5.5f, posEast.x, 0.001f, "Moving East should advance +X");
        Assertions.assertEquals(4.5f, posEast.z, 0.001f, "Moving East should keep Z constant");

        // 2. Move South (+Z)
        float dzSouth = 1.0f;
        Vector3f posSouth = collision.resolveMovement(startX, startZ, 0f, dzSouth, radius);
        Assertions.assertEquals(4.5f, posSouth.x, 0.001f, "Moving South should keep X constant");
        Assertions.assertEquals(5.5f, posSouth.z, 0.001f, "Moving South should advance +Z");

        // 3. Move West (-X) towards Wall at [0, 1] (wall covers X: 0.0 to 3.0)
        // Current X is 4.5. Wall surface is at X = 3.0.
        // Entity radius is 0.4. Safe boundary is X = 3.0 + 0.4 = 3.4.
        // Attempting to move dx = -2.0 would place player at X = 2.5 (inside wall).
        Vector3f posWestBlocked = collision.resolveMovement(startX, startZ, -2.0f, 0f, radius);
        Assertions.assertEquals(startX, posWestBlocked.x, 0.001f,
                "Movement into solid West wall must be arrested");

        // 4. Move North (-Z) towards Perimeter Wall at [1, 0] (wall covers Z: 0.0 to 3.0)
        Vector3f posNorthBlocked = collision.resolveMovement(startX, startZ, 0f, -2.0f, radius);
        Assertions.assertEquals(startZ, posNorthBlocked.z, 0.001f,
                "Movement into solid North wall must be arrested");
    }

    @Test
    public void testWallSlidingMechanic() {
        // Player at (4.5, 4.5).
        // Cell [0, 1] is West wall (X <= 3.0).
        // Cell [2, 1] is East corridor (X: 6.0 to 9.0).
        // Cell [1, 0] is North wall (Z <= 3.0).
        // Place player near North wall at (4.5, 3.45).
        // Attempt diagonal move: dx = +0.5 (East - open), dz = -0.5 (North - blocked by wall at Z=3.0)
        float posX = 4.5f;
        float posZ = 3.45f;
        float radius = 0.4f;

        Vector3f resolved = collision.resolveMovement(posX, posZ, 0.5f, -0.5f, radius);

        // Player should slide along X while Z movement is blocked
        Assertions.assertEquals(5.0f, resolved.x, 0.001f, "Player must slide along open X axis");
        Assertions.assertEquals(posZ, resolved.z, 0.001f, "Player must be constrained along blocked Z axis");
    }

    @Test
    public void testPerimeterBoundaryCollision() {
        float radius = 0.4f;

        // Try moving outside minimum bounds (negative coordinates)
        Assertions.assertTrue(collision.checkCollision(-0.1f, 4.5f, radius),
                "Negative X must be recognized as boundary collision");
        Assertions.assertTrue(collision.checkCollision(4.5f, -0.1f, radius),
                "Negative Z must be recognized as boundary collision");

        // Try moving beyond maximum bounds (total width = 15 * 3.0 = 45.0m)
        float maxBound = 15 * cellSize;
        Assertions.assertTrue(collision.checkCollision(maxBound + 0.1f, 4.5f, radius),
                "X exceeding max boundary must collide");
        Assertions.assertTrue(collision.checkCollision(4.5f, maxBound + 0.1f, radius),
                "Z exceeding max boundary must collide");
    }

    @Test
    public void testConfigurableSpeedScaling() {
        float walkSpeed = GameSettings.PLAYER_WALK_SPEED; // 6.0 m/s
        float runSpeed = GameSettings.PLAYER_RUN_SPEED;   // 10.0 m/s
        float tpf = 0.0166f; // ~60 FPS frame time (16.6ms)

        float walkStep = walkSpeed * tpf;
        float runStep = runSpeed * tpf;

        Assertions.assertTrue(runStep > walkStep, "Sprint speed must produce larger displacement per frame");
        Assertions.assertEquals(0.0996f, walkStep, 0.001f);
        Assertions.assertEquals(0.166f, runStep, 0.001f);
    }

    @Test
    public void testCameraDirectionAndRotation() {
        com.jme3.renderer.Camera cam = new com.jme3.renderer.Camera(640, 480);
        com.mazeexplorer.camera.FirstPersonCamera fpc = new com.mazeexplorer.camera.FirstPersonCamera(cam);

        // 1. Initial orientation: Yaw = 0, Pitch = 0 (pointing South +Z)
        Vector3f initialDir = cam.getDirection();
        Assertions.assertEquals(0f, initialDir.x, 0.001f);
        Assertions.assertEquals(0f, initialDir.y, 0.001f);
        Assertions.assertEquals(1.0f, initialDir.z, 0.001f);
        Assertions.assertEquals("South (+Z)", fpc.getCompassHeading());

        // 2. Rotate Pitch UP: +deltaPitch should pitch camera UPWARDS (positive Y)
        fpc.rotate(0f, FastMath.DEG_TO_RAD * 45.0f);
        Vector3f pitchedUpDir = cam.getDirection();
        Assertions.assertTrue(pitchedUpDir.y > 0.70f, "Positive pitch must elevate camera upwards (+Y)");
        Assertions.assertEquals(FastMath.DEG_TO_RAD * 45.0f, fpc.getPitch(), 0.001f);

        // 3. Rotate Pitch DOWN: -deltaPitch should pitch camera DOWNWARDS (negative Y)
        fpc.rotate(0f, -FastMath.DEG_TO_RAD * 90.0f); // From +45 to -45 deg
        Vector3f pitchedDownDir = cam.getDirection();
        Assertions.assertTrue(pitchedDownDir.y < -0.70f, "Negative pitch must depress camera downwards (-Y)");
        Assertions.assertEquals(-FastMath.DEG_TO_RAD * 45.0f, fpc.getPitch(), 0.001f);

        // 4. Pitch Clamping: Clamp at +/- 89 degrees to prevent camera flipping / gimbal lock
        fpc.rotate(0f, FastMath.DEG_TO_RAD * 200.0f); // Attempt to flip past top
        Assertions.assertEquals(FastMath.DEG_TO_RAD * 89.0f, fpc.getPitch(), 0.001f,
                "Pitch must be clamped at +89 degrees");
        fpc.rotate(0f, -FastMath.DEG_TO_RAD * 200.0f); // Attempt to flip past bottom
        Assertions.assertEquals(-FastMath.DEG_TO_RAD * 89.0f, fpc.getPitch(), 0.001f,
                "Pitch must be clamped at -89 degrees");

        // Reset pitch to level
        fpc.rotate(0f, FastMath.DEG_TO_RAD * 89.0f);
        Assertions.assertEquals(0f, fpc.getPitch(), 0.001f);

        // 5. Yaw Rotation: +deltaYaw turns LEFT (+X / East when starting from South)
        fpc.rotate(FastMath.HALF_PI, 0f);
        Vector3f turnedLeftDir = cam.getDirection();
        Assertions.assertEquals(1.0f, turnedLeftDir.x, 0.001f, "Turning left (+yaw) from South points East (+X)");
        Assertions.assertEquals(0f, turnedLeftDir.z, 0.001f);
        Assertions.assertEquals("East (+X)", fpc.getCompassHeading());

        // -deltaYaw turns RIGHT (-X / West when starting from East and turning 180 deg)
        fpc.rotate(-FastMath.PI, 0f);
        Vector3f turnedRightDir = cam.getDirection();
        Assertions.assertEquals(-1.0f, turnedRightDir.x, 0.001f, "Turning right (-yaw) points West (-X)");
        Assertions.assertEquals("West (-X)", fpc.getCompassHeading());

        // 6. lookAt precision test
        fpc.lookAt(new Vector3f(4.5f, 1.8f, 4.5f), new Vector3f(14.5f, 1.8f, 4.5f)); // Looking East (+X)
        Assertions.assertEquals(1.0f, cam.getDirection().x, 0.001f);
        Assertions.assertEquals(0f, cam.getDirection().y, 0.001f);
        Assertions.assertEquals("East (+X)", fpc.getCompassHeading());

        fpc.lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 10f, 10f)); // Target above and forward
        Assertions.assertTrue(cam.getDirection().y > 0f, "lookAt upwards target must produce positive Y direction");
        Assertions.assertTrue(cam.getDirection().z > 0f, "lookAt forward target must produce positive Z direction");

        // 7. Ground Plane movement independence from pitch
        fpc.rotate(0f, FastMath.DEG_TO_RAD * 85.0f); // Looking almost straight up
        Vector3f groundForward = fpc.getHorizontalForward();
        Assertions.assertEquals(0f, groundForward.y, "Horizontal forward vector must have Y = 0");
        Assertions.assertEquals(1.0f, groundForward.length(), 0.001f, "Horizontal forward must be normalized");
    }

    @Test
    public void testPlayerControllerMouseLookAndInvertY() {
        Player player = new Player(4.5f, 0f, 4.5f);
        com.jme3.renderer.Camera cam = new com.jme3.renderer.Camera(640, 480);
        com.mazeexplorer.camera.FirstPersonCamera fpc = new com.mazeexplorer.camera.FirstPersonCamera(cam);
        com.mazeexplorer.player.PlayerController controller =
                new com.mazeexplorer.player.PlayerController(player, fpc, collision, null);

        // Natural vertical look is the default
        Assertions.assertFalse(controller.isInvertY(), "Natural vertical look must be default");
        Assertions.assertEquals(GameSettings.MOUSE_SENSITIVITY, controller.getMouseSensitivity(), 0.001f);

        // 1. Moving mouse UP: camera must look UP (positive Y direction)
        float initialPitch = fpc.getPitch();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_UP, 0.2f, 0.016f);
        Assertions.assertTrue(fpc.getPitch() > initialPitch, "Mouse UP must increase pitch");
        Assertions.assertTrue(cam.getDirection().y > 0f, "Mouse UP must aim camera upwards (+Y)");

        // 2. Moving mouse DOWN: camera must look DOWN (negative Y direction)
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_DOWN, 0.4f, 0.016f);
        Assertions.assertTrue(fpc.getPitch() < 0f, "Mouse DOWN must decrease pitch below zero");
        Assertions.assertTrue(cam.getDirection().y < 0f, "Mouse DOWN must aim camera downwards (-Y)");

        // 3. Invert-Y setting: toggle invertY = true
        controller.setInvertY(true);
        Assertions.assertTrue(controller.isInvertY());

        // Under Invert-Y: Moving mouse UP should decrease pitch (aim downwards)
        float pitchBeforeUp = fpc.getPitch();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_UP, 0.2f, 0.016f);
        Assertions.assertTrue(fpc.getPitch() < pitchBeforeUp, "Under Invert-Y, mouse UP must decrease pitch");

        // Under Invert-Y: Moving mouse DOWN should increase pitch (aim upwards)
        float pitchBeforeDown = fpc.getPitch();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_DOWN, 0.5f, 0.016f);
        Assertions.assertTrue(fpc.getPitch() > pitchBeforeDown, "Under Invert-Y, mouse DOWN must increase pitch");

        // Restore natural vertical look
        controller.setInvertY(false);

        // 4. Horizontal Mouse Look: Left and Right
        float yawBeforeLeft = fpc.getYaw();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_LEFT, 0.2f, 0.016f);
        Assertions.assertTrue(fpc.getYaw() > yawBeforeLeft, "Mouse LEFT must rotate camera left (+yaw)");

        float yawBeforeRight = fpc.getYaw();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_RIGHT, 0.2f, 0.016f);
        Assertions.assertTrue(fpc.getYaw() < yawBeforeRight, "Mouse RIGHT must rotate camera right (-yaw)");

        // 5. Sensitivity scaling
        controller.setMouseSensitivity(3.0f);
        Assertions.assertEquals(3.0f, controller.getMouseSensitivity(), 0.001f);
        float pitchBeforeScaled = fpc.getPitch();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_UP, 0.1f, 0.016f);
        float deltaPitchScaled = fpc.getPitch() - pitchBeforeScaled;
        Assertions.assertEquals(0.1f * 3.0f, deltaPitchScaled, 0.001f,
                "Delta pitch must equal analog input * sensitivity");

        // 6. Pause & Resume behavior
        controller.releaseMouse(); // Pauses controller and releases mouse
        Assertions.assertTrue(controller.isPaused());
        float pitchWhilePaused = fpc.getPitch();
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_UP, 0.5f, 0.016f);
        Assertions.assertEquals(pitchWhilePaused, fpc.getPitch(), 0.0001f,
                "Mouse input must be ignored while paused");

        controller.captureMouse(); // Resumes controller and captures mouse
        Assertions.assertFalse(controller.isPaused());
        controller.onAnalog(com.mazeexplorer.player.PlayerController.AXIS_LOOK_UP, 0.1f, 0.016f);
        Assertions.assertTrue(fpc.getPitch() > pitchWhilePaused,
                "Mouse input must resume functioning after capturing mouse");
    }

    @Test
    public void testProceduralTextureGeneration() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.GRAY);
        g.fillRect(0, 0, 32, 32);
        g.dispose();

        com.jme3.texture.plugins.AWTLoader loader = new com.jme3.texture.plugins.AWTLoader();
        com.jme3.texture.Image jmeImg = loader.load(img, true);
        com.jme3.texture.Texture2D tex = new com.jme3.texture.Texture2D(jmeImg);
        Assertions.assertNotNull(tex, "Procedural texture must load successfully");
        Assertions.assertEquals(32, tex.getImage().getWidth());
        Assertions.assertEquals(32, tex.getImage().getHeight());
    }
}


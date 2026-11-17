package com.mazeexplorer.player;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.Vector3f;
import com.mazeexplorer.camera.FirstPersonCamera;
import com.mazeexplorer.collision.CollisionSystem;
import com.mazeexplorer.config.GameSettings;

/**
 * First-person player controller implementing WASD movement, mouse look,
 * frame-time movement integration, grid collision resolution with wall sliding,
 * mouse cursor grabbing, and ESC key pause toggling.
 */
public class PlayerController implements ActionListener, AnalogListener {

    public static final String ACTION_MOVE_FORWARD = "PLAYER_MOVE_FORWARD";
    public static final String ACTION_MOVE_BACKWARD = "PLAYER_MOVE_BACKWARD";
    public static final String ACTION_STRAFE_LEFT = "PLAYER_STRAFE_LEFT";
    public static final String ACTION_STRAFE_RIGHT = "PLAYER_STRAFE_RIGHT";
    public static final String ACTION_SPRINT = "PLAYER_SPRINT";
    public static final String ACTION_TOGGLE_PAUSE = "PLAYER_TOGGLE_PAUSE";
    public static final String ACTION_RESPAWN = "PLAYER_RESPAWN";

    public static final String AXIS_LOOK_LEFT = "PLAYER_LOOK_LEFT";
    public static final String AXIS_LOOK_RIGHT = "PLAYER_LOOK_RIGHT";
    public static final String AXIS_LOOK_UP = "PLAYER_LOOK_UP";
    public static final String AXIS_LOOK_DOWN = "PLAYER_LOOK_DOWN";

    private final Player player;
    private final FirstPersonCamera cameraRig;
    private final CollisionSystem collisionSystem;
    private final InputManager inputManager;

    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean strafingLeft = false;
    private boolean strafingRight = false;
    private boolean sprinting = false;

    private boolean paused = false;
    private boolean frozen = false;
    private Runnable respawnCallback;
    private Runnable pauseCallback;

    private float walkSpeed = GameSettings.PLAYER_WALK_SPEED;
    private float runSpeed = GameSettings.PLAYER_RUN_SPEED;
    private float mouseSensitivity = GameSettings.MOUSE_SENSITIVITY;
    private boolean invertY = GameSettings.INVERT_Y;

    public PlayerController(Player player, FirstPersonCamera cameraRig,
                            CollisionSystem collisionSystem, InputManager inputManager) {
        this.player = player;
        this.cameraRig = cameraRig;
        this.collisionSystem = collisionSystem;
        this.inputManager = inputManager;

        if (inputManager != null) {
            setupInputMappings();
            captureMouse();
        }
    }

    private void setupInputMappings() {
        // Keyboard Triggers (WASD + Shift + Esc)
        inputManager.addMapping(ACTION_MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(ACTION_MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(ACTION_STRAFE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(ACTION_STRAFE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(ACTION_SPRINT, new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping(ACTION_TOGGLE_PAUSE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping(ACTION_RESPAWN, new KeyTrigger(KeyInput.KEY_R));

        // Mouse Look Axis Triggers
        inputManager.addMapping(AXIS_LOOK_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(AXIS_LOOK_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(AXIS_LOOK_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(AXIS_LOOK_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        // Register Listeners
        inputManager.addListener(this,
                ACTION_MOVE_FORWARD, ACTION_MOVE_BACKWARD,
                ACTION_STRAFE_LEFT, ACTION_STRAFE_RIGHT,
                ACTION_SPRINT, ACTION_TOGGLE_PAUSE, ACTION_RESPAWN);

        inputManager.addListener(this,
                AXIS_LOOK_LEFT, AXIS_LOOK_RIGHT,
                AXIS_LOOK_UP, AXIS_LOOK_DOWN);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(ACTION_RESPAWN) && isPressed) {
            if (respawnCallback != null) {
                respawnCallback.run();
            }
            return;
        }

        if (name.equals(ACTION_TOGGLE_PAUSE) && isPressed) {
            if (pauseCallback != null) {
                pauseCallback.run();
            } else {
                togglePause();
            }
            return;
        }

        if (paused || frozen) return;

        if (name.equals(ACTION_MOVE_FORWARD)) {
            movingForward = isPressed;
        } else if (name.equals(ACTION_MOVE_BACKWARD)) {
            movingBackward = isPressed;
        } else if (name.equals(ACTION_STRAFE_LEFT)) {
            strafingLeft = isPressed;
        } else if (name.equals(ACTION_STRAFE_RIGHT)) {
            strafingRight = isPressed;
        } else if (name.equals(ACTION_SPRINT)) {
            sprinting = isPressed;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (paused || frozen) return;

        float rotAmount = value * mouseSensitivity;

        if (name.equals(AXIS_LOOK_LEFT)) {
            cameraRig.rotate(rotAmount, 0f);
        } else if (name.equals(AXIS_LOOK_RIGHT)) {
            cameraRig.rotate(-rotAmount, 0f);
        } else if (name.equals(AXIS_LOOK_UP)) {
            float pitchDelta = invertY ? -rotAmount : rotAmount;
            cameraRig.rotate(0f, pitchDelta);
        } else if (name.equals(AXIS_LOOK_DOWN)) {
            float pitchDelta = invertY ? rotAmount : -rotAmount;
            cameraRig.rotate(0f, pitchDelta);
        }
    }

    /**
     * Updates player position smoothly using frame delta time and resolves grid collisions with wall sliding.
     *
     * @param tpf Time per frame in seconds
     */
    public void update(float tpf) {
        if (paused || frozen) return;

        // 1. Calculate horizontal movement direction vector
        Vector3f moveVector = new Vector3f(0f, 0f, 0f);
        Vector3f forward = cameraRig.getHorizontalForward();
        Vector3f left = cameraRig.getHorizontalLeft();

        if (movingForward) moveVector.addLocal(forward);
        if (movingBackward) moveVector.subtractLocal(forward);
        if (strafingLeft) moveVector.addLocal(left);
        if (strafingRight) moveVector.subtractLocal(left);

        if (moveVector.lengthSquared() > 0f) {
            moveVector.normalizeLocal();

            // 2. Compute smooth frame-rate independent displacement
            float currentSpeed = sprinting ? runSpeed : walkSpeed;
            float stepDistance = currentSpeed * tpf;
            float dx = moveVector.x * stepDistance;
            float dz = moveVector.z * stepDistance;

            // 3. Resolve collision against 2D maze walls supporting sliding along open axes
            Vector3f resolved = collisionSystem.resolveMovement(
                    player.getPosX(), player.getPosZ(), dx, dz, player.getCollisionRadius()
            );

            player.setPosition(resolved.x, player.getPosY(), resolved.z);
        }

        // 4. Update camera world position with fixed eye height
        cameraRig.setPosition(player.getPosX(), player.getEyeY(), player.getPosZ());
    }

    public void captureMouse() {
        this.paused = false;
        if (inputManager != null) {
            inputManager.setCursorVisible(false);
        }
    }

    public void releaseMouse() {
        this.paused = true;
        this.movingForward = false;
        this.movingBackward = false;
        this.strafingLeft = false;
        this.strafingRight = false;
        this.sprinting = false;
        if (inputManager != null) {
            inputManager.setCursorVisible(true);
        }
    }

    public void togglePause() {
        if (paused) {
            captureMouse();
        } else {
            releaseMouse();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        if (walkSpeed > 0f) {
            this.walkSpeed = walkSpeed;
        }
    }

    public float getRunSpeed() {
        return runSpeed;
    }

    public void setRunSpeed(float runSpeed) {
        if (runSpeed > 0f) {
            this.runSpeed = runSpeed;
        }
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        if (mouseSensitivity > 0f) {
            this.mouseSensitivity = mouseSensitivity;
        }
    }

    public boolean isInvertY() {
        return invertY;
    }

    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        if (frozen) {
            this.movingForward = false;
            this.movingBackward = false;
            this.strafingLeft = false;
            this.strafingRight = false;
            this.sprinting = false;
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setOnPauseCallback(Runnable pauseCallback) {
        this.pauseCallback = pauseCallback;
    }

    public void setOnRespawnCallback(Runnable respawnCallback) {
        this.respawnCallback = respawnCallback;
    }

    public Player getPlayer() {
        return player;
    }

    public FirstPersonCamera getCameraRig() {
        return cameraRig;
    }
}


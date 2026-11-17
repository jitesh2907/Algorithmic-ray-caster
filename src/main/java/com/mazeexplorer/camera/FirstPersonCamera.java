package com.mazeexplorer.camera;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * First-person camera rig providing decoupled yaw/pitch rotations,
 * pitch clamping to eliminate gimbal lock, and ground-plane direction vectors.
 */
public class FirstPersonCamera {

    private final Camera camera;

    private float yaw = 0f;   // Horizontal rotation (radians)
    private float pitch = 0f; // Vertical tilt (radians)

    private final float maxPitch = FastMath.DEG_TO_RAD * 89.0f;
    private final float minPitch = -FastMath.DEG_TO_RAD * 89.0f;

    public FirstPersonCamera(Camera camera) {
        this.camera = camera;
        updateCameraRotation();
    }

    /**
     * Rotates the camera by delta yaw and delta pitch.
     *
     * @param deltaYaw Radians to rotate around Y axis (horizontal)
     * @param deltaPitch Radians to rotate around X axis (vertical tilt)
     */
    public void rotate(float deltaYaw, float deltaPitch) {
        this.yaw += deltaYaw;
        // Wrap yaw within [-PI, PI]
        if (this.yaw > FastMath.PI) {
            this.yaw -= FastMath.TWO_PI;
        } else if (this.yaw < -FastMath.PI) {
            this.yaw += FastMath.TWO_PI;
        }

        // Clamp pitch to prevent camera flip
        this.pitch = FastMath.clamp(this.pitch + deltaPitch, minPitch, maxPitch);

        updateCameraRotation();
    }

    private void updateCameraRotation() {
        Quaternion qYaw = new Quaternion().fromAngleAxis(yaw, Vector3f.UNIT_Y);
        // In jME3 / OpenGL camera coordinate space, rotating around +X by a positive angle
        // tilts the camera downward (towards -Y). Therefore, -pitch is applied so that
        // a positive pitch angle produces a natural upward rotation (towards +Y).
        Quaternion qPitch = new Quaternion().fromAngleAxis(-pitch, Vector3f.UNIT_X);
        Quaternion result = qYaw.mult(qPitch);
        camera.setRotation(result);
    }

    /**
     * Sets looking angle directly towards a target world point from an eye position.
     */
    public void lookAt(Vector3f eyePos, Vector3f targetPos) {
        Vector3f dir = targetPos.subtract(eyePos).normalizeLocal();
        this.yaw = FastMath.atan2(dir.x, dir.z);
        float xzLen = FastMath.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.pitch = FastMath.atan2(dir.y, xzLen);
        this.pitch = FastMath.clamp(this.pitch, minPitch, maxPitch);
        updateCameraRotation();
    }

    /**
     * Updates camera world location keeping fixed eye height.
     */
    public void setPosition(float x, float eyeY, float z) {
        camera.setLocation(new Vector3f(x, eyeY, z));
    }

    /**
     * Gets the normalized horizontal forward direction vector on the X-Z ground plane (Y = 0).
     */
    public Vector3f getHorizontalForward() {
        Vector3f dir = camera.getDirection();
        Vector3f forward = new Vector3f(dir.x, 0f, dir.z);
        if (forward.lengthSquared() < 1e-6f) {
            return new Vector3f(0f, 0f, 1f);
        }
        return forward.normalizeLocal();
    }

    /**
     * Gets the normalized horizontal left direction vector on the X-Z ground plane (Y = 0).
     */
    public Vector3f getHorizontalLeft() {
        Vector3f forward = getHorizontalForward();
        // Left is perpendicular to forward on X-Z plane: (forward.z, 0, -forward.x)
        return new Vector3f(forward.z, 0f, -forward.x).normalizeLocal();
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * Returns human-readable compass heading (e.g., "East (+X)", "South (+Z)").
     */
    public String getCompassHeading() {
        float deg = yaw * FastMath.RAD_TO_DEG;
        if (deg < 0f) deg += 360f;

        if (deg >= 337.5f || deg < 22.5f) return "South (+Z)";
        if (deg >= 22.5f && deg < 67.5f) return "South-East";
        if (deg >= 67.5f && deg < 112.5f) return "East (+X)";
        if (deg >= 112.5f && deg < 157.5f) return "North-East";
        if (deg >= 157.5f && deg < 202.5f) return "North (-Z)";
        if (deg >= 202.5f && deg < 247.5f) return "North-West";
        if (deg >= 247.5f && deg < 292.5f) return "West (-X)";
        return "South-West";
    }
}


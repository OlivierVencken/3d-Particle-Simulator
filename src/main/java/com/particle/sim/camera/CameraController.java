package com.particle.sim.camera;

import com.particle.sim.math.Math3d;
import com.particle.sim.settings.SimulationDefaults;

/** Pure camera state updated from toolkit-neutral input snapshots. */
public final class CameraController {
    private float posX;
    private float posY;
    private float posZ;
    private float yaw;
    private float pitch;
    private float sensitivity = SimulationDefaults.CAMERA_SENSITIVITY;
    private float flySpeed = SimulationDefaults.CAMERA_FLY_SPEED;
    private final CameraCaptureState captureState = new CameraCaptureState();

    public CameraController() {
        reset();
    }

    public CameraCaptureTransition update(float deltaTime, CameraInput input) {
        float cosPitch = (float) Math.cos(pitch);
        float[] forward = {
            cosPitch * (float) Math.sin(yaw),
            (float) Math.sin(pitch),
            cosPitch * (float) Math.cos(yaw)
        };
        float[] right =
                Math3d.normalize(
                        Math3d.cross(forward[0], forward[1], forward[2], 0.0f, 1.0f, 0.0f));

        CameraInput.Pointer pointer = input.pointer();
        CameraCaptureTransition transition =
                captureState.update(
                        pointer.primaryDown(),
                        pointer.secondaryDown(),
                        pointer.escapeDown(),
                        input.windowFocused(),
                        input.captureAllowed(),
                        input.modalOpen());

        if (captureState.captured()) {
            yaw -= pointer.deltaX() * sensitivity;
            pitch = Math3d.clamp(pitch - pointer.deltaY() * sensitivity, -1.5f, 1.5f);
        }

        if (input.windowFocused() && (captureState.captured() || input.keyboardAllowed())) {
            updateMovement(input.movement(), deltaTime, forward, right);
        }
        return transition;
    }

    public float[] viewMatrix() {
        float cosPitch = (float) Math.cos(pitch);
        float targetX = posX + (cosPitch * (float) Math.sin(yaw));
        float targetY = posY + (float) Math.sin(pitch);
        float targetZ = posZ + (cosPitch * (float) Math.cos(yaw));
        return Math3d.lookAt(posX, posY, posZ, targetX, targetY, targetZ);
    }

    public void reset() {
        posX = 0.0f;
        posY = 0.0f;
        posZ = -18.5f;
        yaw = 0.0f;
        pitch = 0.0f;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = Math.max(0.0001f, sensitivity);
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = Math.max(0.1f, flySpeed);
    }

    public boolean isMouseCaptured() {
        return captureState.captured();
    }

    private void updateMovement(
            CameraInput.Movement movement, float deltaTime, float[] forward, float[] right) {
        float step = flySpeed * deltaTime;
        if (movement.forward()) {
            move(forward, step);
        }
        if (movement.backward()) {
            move(forward, -step);
        }
        if (movement.right()) {
            move(right, step);
        }
        if (movement.left()) {
            move(right, -step);
        }
        if (movement.up()) {
            posY += step;
        }
        if (movement.down()) {
            posY -= step;
        }
        if (movement.reset()) {
            reset();
        }
    }

    private void move(float[] direction, float amount) {
        posX += direction[0] * amount;
        posY += direction[1] * amount;
        posZ += direction[2] * amount;
    }
}

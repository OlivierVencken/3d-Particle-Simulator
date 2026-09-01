package com.particle.sim.camera;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUSED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

import com.particle.sim.math.Math3d;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.InputOwnership;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;

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

    public void update(long window, float deltaTime, InputOwnership ownership) {
        var io = ImGui.getIO();

        float cosPitch = (float) Math.cos(pitch);
        float[] forward = {
            cosPitch * (float) Math.sin(yaw),
            (float) Math.sin(pitch),
            cosPitch * (float) Math.cos(yaw)
        };

        float[] right =
                Math3d.normalize(
                        Math3d.cross(forward[0], forward[1], forward[2], 0.0f, 1.0f, 0.0f));

        boolean windowFocused = glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
        CameraCaptureState.Transition captureTransition =
                captureState.update(
                        glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS,
                        glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS,
                        isPressed(window, GLFW_KEY_ESCAPE),
                        windowFocused,
                        ownership);
        applyCaptureTransition(window, captureTransition);

        if (captureState.captured()) {
            yaw -= io.getMouseDeltaX() * sensitivity;
            pitch = Math3d.clamp(pitch - io.getMouseDeltaY() * sensitivity, -1.5f, 1.5f);
        }

        if (windowFocused && (captureState.captured() || ownership.allowsSimulationKeyboard())) {
            updateKeyboard(window, deltaTime, forward, right);
        }
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

    private void applyCaptureTransition(long window, CameraCaptureState.Transition transition) {
        if (transition == CameraCaptureState.Transition.CAPTURED) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NoMouse);
        } else if (transition == CameraCaptureState.Transition.RELEASED) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            ImGui.getIO().removeConfigFlags(ImGuiConfigFlags.NoMouse);
        }
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

    private void updateKeyboard(long window, float deltaTime, float[] forward, float[] right) {
        float step = flySpeed * deltaTime;

        if (isPressed(window, GLFW_KEY_W)) {
            move(forward, step);
        }
        if (isPressed(window, GLFW_KEY_S)) {
            move(forward, -step);
        }
        if (isPressed(window, GLFW_KEY_D)) {
            move(right, step);
        }
        if (isPressed(window, GLFW_KEY_A)) {
            move(right, -step);
        }
        if (isPressed(window, GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW_KEY_RIGHT_SHIFT)) {
            posY += step;
        }
        if (isPressed(window, GLFW_KEY_LEFT_CONTROL) || isPressed(window, GLFW_KEY_RIGHT_CONTROL)) {
            posY -= step;
        }
        if (isPressed(window, GLFW_KEY_HOME)) {
            reset();
        }
    }

    private void move(float[] direction, float amount) {
        posX += direction[0] * amount;
        posY += direction[1] * amount;
        posZ += direction[2] * amount;
    }

    private static boolean isPressed(long window, int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}

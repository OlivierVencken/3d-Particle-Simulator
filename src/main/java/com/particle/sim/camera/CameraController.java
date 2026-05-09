package com.particle.sim.camera;

import com.particle.sim.math.Math3d;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

public final class CameraController {
    private float posX;
    private float posY;
    private float posZ;
    
    private float yaw;
    private float pitch;
    
    private float sensitivity = 0.002f;
    private boolean mouseCaptured = false;

    public CameraController() {
        reset();
    }

    public void update(long window, float deltaTime) {
        var io = ImGui.getIO();
        
        float cosPitch = (float) Math.cos(pitch);
        float[] forward = {
                cosPitch * (float) Math.sin(yaw),
                (float) Math.sin(pitch),
                cosPitch * (float) Math.cos(yaw)
        };
        
        float[] right = Math3d.normalize(Math3d.cross(forward[0], forward[1], forward[2], 0.0f, 1.0f, 0.0f));
        
        if (!io.getWantCaptureMouse()) {
            updateMouse(window, io.getMouseDeltaX(), io.getMouseDeltaY(), io.getMouseWheel());
        }

        if (!io.getWantCaptureKeyboard()) {
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
        pitch = 0.25f;
    }

    private void updateMouse(long window, float dx, float dy, float wheel) {
        boolean leftClick = ImGui.getIO().getMouseClicked(GLFW_MOUSE_BUTTON_LEFT);
        boolean rightClick = ImGui.getIO().getMouseClicked(GLFW_MOUSE_BUTTON_RIGHT);
        boolean escPressed = isPressed(window, GLFW_KEY_ESCAPE);

        if (leftClick && !mouseCaptured) {
            mouseCaptured = true;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NoMouse);
        } else if ((rightClick || escPressed) && mouseCaptured) {
            mouseCaptured = false;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            ImGui.getIO().removeConfigFlags(ImGuiConfigFlags.NoMouse);
        }

        if (mouseCaptured) {
            yaw -= dx * sensitivity;
            pitch = Math3d.clamp(pitch - dy * sensitivity, -1.5f, 1.5f);
        }
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = Math.max(0.0001f, sensitivity);
    }

    private void updateKeyboard(long window, float deltaTime, float[] forward, float[] right) {
        float speed = (isPressed(window, GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW_KEY_RIGHT_SHIFT)) ? 16.0f : 7.0f;
        float step = speed * deltaTime;

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
        if (isPressed(window, GLFW_KEY_SPACE)) {
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

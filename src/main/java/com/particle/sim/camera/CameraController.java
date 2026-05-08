package com.particle.sim.camera;

import com.particle.sim.math.Math3d;
import imgui.ImGui;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public final class CameraController {
    private float targetX;
    private float targetY;
    private float targetZ;
    private float yaw;
    private float pitch = 0.25f;
    private float distance = 18.5f;

    public void update(long window, float deltaTime) {
        var io = ImGui.getIO();
        float[] eye = eye();
        float[] forward = Math3d.normalize(targetX - eye[0], targetY - eye[1], targetZ - eye[2]);
        float[] right = Math3d.normalize(Math3d.cross(forward[0], forward[1], forward[2], 0.0f, 1.0f, 0.0f));
        float[] up = Math3d.normalize(Math3d.cross(right[0], right[1], right[2], -forward[0], -forward[1], -forward[2]));

        if (!io.getWantCaptureMouse()) {
            updateMouse(window, io.getMouseDeltaX(), io.getMouseDeltaY(), io.getMouseWheel(), right, up);
        }

        if (!io.getWantCaptureKeyboard()) {
            updateKeyboard(window, deltaTime, forward, right);
        }
    }

    public float[] viewMatrix() {
        float[] eye = eye();
        return Math3d.lookAt(eye[0], eye[1], eye[2], targetX, targetY, targetZ);
    }

    public void reset() {
        targetX = 0.0f;
        targetY = 0.0f;
        targetZ = 0.0f;
        yaw = 0.0f;
        pitch = 0.25f;
        distance = 18.5f;
    }

    private void updateMouse(long window, float dx, float dy, float wheel, float[] right, float[] up) {
        boolean leftDragging = ImGui.getIO().getMouseDown(GLFW_MOUSE_BUTTON_LEFT);
        boolean middleDragging = ImGui.getIO().getMouseDown(GLFW_MOUSE_BUTTON_MIDDLE);
        boolean shiftDown = isPressed(window, GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW_KEY_RIGHT_SHIFT);

        if (leftDragging && !shiftDown) {
            yaw -= dx * 0.006f;
            pitch = Math3d.clamp(pitch - dy * 0.006f, -1.45f, 1.45f);
        }

        if (middleDragging || (leftDragging && shiftDown)) {
            float panScale = distance * 0.0015f;
            targetX += (-right[0] * dx + up[0] * dy) * panScale;
            targetY += (-right[1] * dx + up[1] * dy) * panScale;
            targetZ += (-right[2] * dx + up[2] * dy) * panScale;
        }

        if (wheel != 0.0f) {
            distance = Math3d.clamp(distance * (float) Math.pow(0.88f, wheel), 1.5f, 120.0f);
        }
    }

    private void updateKeyboard(long window, float deltaTime, float[] forward, float[] right) {
        float speed = (isPressed(window, GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW_KEY_RIGHT_SHIFT)) ? 16.0f : 7.0f;
        float step = speed * deltaTime;

        if (isPressed(window, GLFW_KEY_W)) {
            moveTarget(forward, step);
        }
        if (isPressed(window, GLFW_KEY_S)) {
            moveTarget(forward, -step);
        }
        if (isPressed(window, GLFW_KEY_D)) {
            moveTarget(right, step);
        }
        if (isPressed(window, GLFW_KEY_A)) {
            moveTarget(right, -step);
        }
        if (isPressed(window, GLFW_KEY_SPACE)) {
            targetY += step;
        }
        if (isPressed(window, GLFW_KEY_LEFT_CONTROL) || isPressed(window, GLFW_KEY_RIGHT_CONTROL)) {
            targetY -= step;
        }
        if (isPressed(window, GLFW_KEY_HOME)) {
            reset();
        }
    }

    private void moveTarget(float[] direction, float amount) {
        targetX += direction[0] * amount;
        targetY += direction[1] * amount;
        targetZ += direction[2] * amount;
    }

    private float[] eye() {
        float cosPitch = (float) Math.cos(pitch);
        return new float[]{
                targetX + distance * cosPitch * (float) Math.sin(yaw),
                targetY + distance * (float) Math.sin(pitch),
                targetZ + distance * cosPitch * (float) Math.cos(yaw)
        };
    }

    private static boolean isPressed(long window, int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}

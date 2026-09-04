package com.particle.sim.app;

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

import com.particle.sim.camera.CameraCaptureTransition;
import com.particle.sim.camera.CameraController;
import com.particle.sim.camera.CameraInput;
import com.particle.sim.ui.InputOwnership;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;

/** Translates GLFW and ImGui state into toolkit-neutral camera input. */
final class CameraInputAdapter {
    void update(CameraController camera, long window, float deltaTime, InputOwnership ownership) {
        var io = ImGui.getIO();
        CameraInput input =
                new CameraInput(
                        new CameraInput.Pointer(
                                mouseDown(window, GLFW_MOUSE_BUTTON_LEFT),
                                mouseDown(window, GLFW_MOUSE_BUTTON_RIGHT),
                                keyDown(window, GLFW_KEY_ESCAPE),
                                io.getMouseDeltaX(),
                                io.getMouseDeltaY()),
                        new CameraInput.Movement(
                                keyDown(window, GLFW_KEY_W),
                                keyDown(window, GLFW_KEY_S),
                                keyDown(window, GLFW_KEY_D),
                                keyDown(window, GLFW_KEY_A),
                                keyDown(window, GLFW_KEY_LEFT_SHIFT)
                                        || keyDown(window, GLFW_KEY_RIGHT_SHIFT),
                                keyDown(window, GLFW_KEY_LEFT_CONTROL)
                                        || keyDown(window, GLFW_KEY_RIGHT_CONTROL),
                                keyDown(window, GLFW_KEY_HOME)),
                        glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE,
                        ownership.canStartCameraCapture(),
                        ownership.allowsSimulationKeyboard(),
                        ownership.modalOpen());
        applyCaptureTransition(window, camera.update(deltaTime, input));
    }

    private static void applyCaptureTransition(long window, CameraCaptureTransition transition) {
        if (transition == CameraCaptureTransition.CAPTURED) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NoMouse);
        } else if (transition == CameraCaptureTransition.RELEASED) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            ImGui.getIO().removeConfigFlags(ImGuiConfigFlags.NoMouse);
        }
    }

    private static boolean keyDown(long window, int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    private static boolean mouseDown(long window, int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }
}

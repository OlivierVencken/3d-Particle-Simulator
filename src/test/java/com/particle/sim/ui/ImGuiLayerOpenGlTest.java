package com.particle.sim.ui;

import imgui.ImGui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.lwjgl.opengl.GL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL43C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL43C.glGetError;
import static org.lwjgl.system.MemoryUtil.NULL;

@EnabledIfSystemProperty(named = "gpuTests", matches = "true")
class ImGuiLayerOpenGlTest {
    @Test
    void buildsAndUploadsTheScaledFontAtlas() {
        assertTrue(glfwInit(), "GLFW initialization failed");
        long window = NULL;
        ImGuiLayer layer = null;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(320, 240, "ImGui DPI smoke test", NULL, NULL);
            assertNotEquals(NULL, window, "OpenGL 4.3 context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            layer = new ImGuiLayer();
            layer.init(window);
            layer.beginFrame();
            ImGui.begin("Font atlas smoke test");
            ImGui.textUnformatted("IBM Plex Sans");
            ImGui.end();
            layer.render();

            assertTrue(ImGui.getIO().getFonts().isBuilt());
            assertEquals(GL_NO_ERROR, glGetError());
        } finally {
            if (layer != null) {
                layer.dispose();
            }
            if (window != NULL) {
                glfwMakeContextCurrent(NULL);
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }
    }
}

package com.particle.sim.ui;

import com.particle.sim.ui.components.UIAttractionMatrix;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UICheckbox;
import com.particle.sim.ui.components.UICombo;
import com.particle.sim.ui.components.UIIntegerInput;
import com.particle.sim.ui.components.UIMetric;
import com.particle.sim.ui.components.UISlider;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.testing.FakeSimulationUiModel;
import com.particle.sim.ui.testing.RecordingSimulationUiActions;
import com.particle.sim.ui.theme.UIComponentVariant;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
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
    void buildsFontAtlasAndRendersSharedComponents() {
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
            ImGui.setNextWindowSize(700.0f, 800.0f);
            ImGui.begin("Component library smoke test");
            ImGui.textUnformatted("IBM Plex Sans");
            UIButton.text("Primary", "smoke-primary", UIComponentVariant.PRIMARY);
            ImGui.sameLine();
            UIButton.text("Disabled", "smoke-disabled", UIComponentVariant.DISABLED,
                    0.0f, 32.0f, false);
            UISlider.render("Float", "smoke-float", new float[] {0.5f}, 0.0f, 1.0f, 2);
            UISlider.render("Integer", "smoke-integer", new int[] {4}, 0, 10);
            UICheckbox.render("Checkbox", "smoke-checkbox", new ImBoolean(true));
            UICombo.render("Combo", "smoke-combo", new ImInt(0), new String[] {"One", "Two"});
            UIIntegerInput.render("Stepper", "smoke-stepper", new ImInt(4), 1, 2, 1, 16, 120.0f);
            UIMetric.card("smoke-metric", "PARTICLES", "1,000", 140.0f);
            UIText.helper("Shared helper text");
            FakeSimulationUiModel model = new FakeSimulationUiModel();
            RecordingSimulationUiActions actions = new RecordingSimulationUiActions();
            UIAttractionMatrix.render(model.particles(), actions.particles());
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

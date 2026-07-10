package com.particle.sim.particles;

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
import static org.lwjgl.opengl.GL43C.glFinish;
import static org.lwjgl.opengl.GL43C.glGetError;
import static org.lwjgl.system.MemoryUtil.NULL;

@EnabledIfSystemProperty(named = "gpuTests", matches = "true")
class GpuParticleSystemOpenGlTest {
    @Test
    void compilesShadersAndExecutesOneSimulationStep() {
        assertTrue(glfwInit(), "GLFW initialization failed");
        long window = NULL;
        GpuParticleSystem system = null;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(64, 64, "GPU smoke test", NULL, NULL);
            assertNotEquals(NULL, window, "OpenGL 4.3 context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            system = new GpuParticleSystem();
            system.setParticleCount(1_024);
            system.bounds(2.0f);
            system.interactionRange(0.4f);
            system.spawnMode(SpawnMode.POINT);
            system.effectEnabled(EffectMode.TRAILS, true);
            system.effectEnabled(EffectMode.GLOW, true);
            system.init();
            assertTrue(system.maxParticleCount() > 1_000_000);
            for (int i = 0; i < 6; i++) {
                system.step();
            }
            system.render(64, 64, new float[] {
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            });
            glFinish();

            assertEquals(GL_NO_ERROR, glGetError());
            assertTrue(system.performanceSnapshot().simulationMilliseconds() >= 0.0);
        } finally {
            if (system != null) {
                system.dispose();
            }
            if (window != NULL) {
                glfwMakeContextCurrent(NULL);
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }
    }
}

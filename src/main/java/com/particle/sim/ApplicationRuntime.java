package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.AppHotkeys;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.ImguiLayer;
import com.particle.sim.ui.SimulationUi;
import com.particle.sim.window.WindowManager;

import java.util.function.DoubleConsumer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glDepthMask;
import static org.lwjgl.opengl.GL43C.glViewport;

public final class ApplicationRuntime {
    private final WindowManager window;
    private final ImguiLayer imgui;
    private final AppHotkeys hotkeys;
    private final CameraController camera;
    private final GpuParticleSystem particles;
    private final SimulationUi ui;
    private final DoubleConsumer saveSettingsIfDue;

    private double lastFrameTime;
    private double startTime;

    public ApplicationRuntime(WindowManager window, ImguiLayer imgui, AppHotkeys hotkeys, CameraController camera,
            GpuParticleSystem particles, SimulationUi ui, DoubleConsumer saveSettingsIfDue) {
        this.window = window;
        this.imgui = imgui;
        this.hotkeys = hotkeys;
        this.camera = camera;
        this.particles = particles;
        this.ui = ui;
        this.saveSettingsIfDue = saveSettingsIfDue;
    }

    public void run() {
        lastFrameTime = glfwGetTime();
        startTime = lastFrameTime;

        while (!window.shouldClose()) {
            window.pollEvents();
            hotkeys.update(window.handle());

            double now = glfwGetTime();
            float deltaTime = (float) Math.min(now - lastFrameTime, 1.0 / 30.0);
            lastFrameTime = now;

            window.updateFramebufferSize();
            imgui.beginFrame();
            camera.update(window.handle(), deltaTime);

            if (!ui.isPaused()) {
                particles.update(deltaTime, (float) (now - startTime));
            }

            renderScene();
            ui.render(deltaTime, particles, camera);
            imgui.render();
            saveSettingsIfDue.accept(glfwGetTime());

            window.swapBuffers();
        }
    }

    private void renderScene() {
        glViewport(0, 0, window.width(), window.height());
        glClearColor(0.015f, 0.018f, 0.024f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDepthMask(false);
        try {
            particles.render(window.width(), window.height(), camera.viewMatrix());
        } finally {
            glDepthMask(true);
        }
    }
}

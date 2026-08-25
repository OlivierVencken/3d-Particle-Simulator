package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.HotkeyManager;
import com.particle.sim.input.HotkeyRoutingContext;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SettingsController;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.ImGuiLayer;
import com.particle.sim.ui.PreparedUiFrame;
import com.particle.sim.ui.SimulationUI;
import com.particle.sim.window.WindowManager;

import java.util.concurrent.locks.LockSupport;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL43C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL43C.glBindFramebuffer;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glDisable;
import static org.lwjgl.opengl.GL43C.glDepthMask;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glViewport;

public final class ApplicationRuntime {
    private static final double FRAME_LIMIT_SPIN_SECONDS = 0.0005;
    private static final double MAX_FRAME_DELTA_SECONDS = 0.25;
    private static final int MAX_SIMULATION_STEPS_PER_FRAME = 4;

    private final WindowManager window;
    private final ImGuiLayer imgui;
    private final HotkeyManager hotkeys;
    private final CameraController camera;
    private final GpuParticleSystem particles;
    private final SimulationUI ui;
    private final SimulationUiAdapter uiAdapter;
    private final SettingsController settingsController;
    private final FixedSimulationClock simulationClock = new FixedSimulationClock(
            SimulationDefaults.SIMULATION_STEP_SECONDS);

    private double lastFrameTime;

    ApplicationRuntime(WindowManager window, ImGuiLayer imgui, HotkeyManager hotkeys,
            CameraController camera,
            GpuParticleSystem particles, SimulationUI ui, SimulationUiAdapter uiAdapter,
            SettingsController settingsController) {
        this.window = window;
        this.imgui = imgui;
        this.hotkeys = hotkeys;
        this.camera = camera;
        this.particles = particles;
        this.ui = ui;
        this.settingsController = settingsController;
        this.uiAdapter = Objects.requireNonNull(uiAdapter, "uiAdapter");
    }

    public void run() {
        lastFrameTime = glfwGetTime();

        while (!window.shouldClose()) {
            window.pollEvents();

            double now = glfwGetTime();
            double frameDelta = Math.min(Math.max(now - lastFrameTime, 0.0), MAX_FRAME_DELTA_SECONDS);
            float deltaTime = (float) frameDelta;
            lastFrameTime = now;

            window.updateFramebufferSize();
            imgui.beginFrame();
            PreparedUiFrame uiFrame = ui.prepareFrame(window.width(), window.height(), deltaTime);
            var inputOwnership = uiFrame.inputOwnership();
            hotkeys.update(window.handle(), new HotkeyRoutingContext(
                    inputOwnership.allowsSimulationKeyboard(),
                    inputOwnership.keyboardOwnedByUi(),
                    inputOwnership.modalOpen()));
            camera.update(window.handle(), deltaTime, inputOwnership);

            if (!ui.isPaused()) {
                simulationClock.addFrameTime(frameDelta);
                int simulationSteps = 0;
                while (simulationClock.hasStep() && simulationSteps < MAX_SIMULATION_STEPS_PER_FRAME) {
                    particles.update(simulationClock.stepSeconds(), simulationClock.consumeStep());
                    simulationSteps++;
                }
                if (simulationClock.hasStep()) {
                    simulationClock.discardExcessSteps();
                }
            }

            renderScene(uiFrame);
            uiAdapter.prepareFrame();
            ui.render(deltaTime);
            imgui.render();
            settingsController.tick(now);

            window.swapBuffers();
            limitFrameRate(now);
        }
    }

    private void limitFrameRate(double frameStartTime) {
        int fpsCap = ui.fpsCap();
        if (fpsCap <= 0) {
            return;
        }

        double targetFrameSeconds = 1.0 / fpsCap;
        while (!Thread.currentThread().isInterrupted()) {
            double remainingSeconds = targetFrameSeconds - (glfwGetTime() - frameStartTime);
            if (remainingSeconds <= 0.0) {
                return;
            }

            if (remainingSeconds > FRAME_LIMIT_SPIN_SECONDS) {
                long sleepNanos = (long) ((remainingSeconds - FRAME_LIMIT_SPIN_SECONDS) * 1_000_000_000.0);
                LockSupport.parkNanos(Math.max(1L, sleepNanos));
            } else {
                Thread.onSpinWait();
            }
        }
    }

    private void renderScene(PreparedUiFrame uiFrame) {
        if (window.width() <= 0 || window.height() <= 0) {
            return;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_SCISSOR_TEST);
        glViewport(0, 0, window.width(), window.height());
        glClearColor(0.031f, 0.031f, 0.031f, 1.0f);
        glDepthMask(true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (!uiFrame.simulationViewport().visible()) {
            restoreUiRenderState();
            return;
        }

        glDepthMask(false);
        try {
            particles.render(uiFrame.simulationViewport(), camera.viewMatrix());
        } finally {
            restoreUiRenderState();
        }
    }

    private void restoreUiRenderState() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, window.width(), window.height());
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
    }
}

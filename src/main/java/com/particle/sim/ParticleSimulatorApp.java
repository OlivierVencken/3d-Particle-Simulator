package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.AppHotkeys;
import com.particle.sim.input.HotkeyManager;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SettingsController;
import com.particle.sim.system.StartupFailureException;
import com.particle.sim.ui.ImguiLayer;
import com.particle.sim.ui.PresetFileDialog;
import com.particle.sim.ui.SimulationUi;
import com.particle.sim.window.WindowManager;

import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL43C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL43C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.glBlendFunc;
import static org.lwjgl.opengl.GL43C.glEnable;

public final class ParticleSimulatorApp {
    private static final String WINDOW_TITLE = "3D Particle Simulator";

    private final WindowManager window = new WindowManager(WINDOW_TITLE);
    private final ImguiLayer imgui = new ImguiLayer();
    private final CameraController camera = new CameraController();
    private final GpuParticleSystem particles = new GpuParticleSystem();
    private final SimulationUi ui = new SimulationUi();
    private final HotkeyManager hotkeys = new HotkeyManager();
    private final SettingsController settingsController = new SettingsController(particles, camera, ui);
    private boolean windowInitialized;
    private boolean presetDialogInitialized;
    private boolean imguiInitialized;
    private boolean particlesInitialized;

    public static void main(String[] args) {
        if (ParticleBenchmarkRunner.requested(args)) {
            ParticleBenchmarkRunner.run(args);
            return;
        }
        try {
            new ParticleSimulatorApp().run();
        } catch (StartupFailureException ignored) {
            System.exit(1);
        }
    }

    private void run() {
        window.init();
        windowInitialized = true;
        try {
            PresetFileDialog.init();
            presetDialogInitialized = true;
            initOpenGl();
            imgui.init(window.handle());
            imguiInitialized = true;
            particles.init();
            particlesInitialized = true;
            initSettings();
            AppHotkeys.register(hotkeys, this);

            new ApplicationRuntime(
                    window,
                    imgui,
                    hotkeys,
                    camera,
                    particles,
                    ui,
                    settingsController).run();
        } finally {
            dispose();
        }
    }

    private void initOpenGl() {
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initSettings() {
        ui.onSettingsChanged(settingsController::onSettingsChanged);
        ui.onResetSettings(settingsController::onResetRequested);
        ui.onSavePreset(() -> PresetFileDialog.showSaveDialog()
                .ifPresent(settingsController::savePresetTo));
        ui.onLoadPreset(() -> PresetFileDialog.showOpenDialog()
                .ifPresent(settingsController::loadPresetFrom));
        ui.onExitApplication(() -> {
            settingsController.flush();
            window.requestClose();
        });

        settingsController.load();
    }

    public WindowManager getWindow() {
        return window;
    }

    public GpuParticleSystem getParticles() {
        return particles;
    }

    public SimulationUi getUi() {
        return ui;
    }

    private void dispose() {
        try {
            settingsController.flush();
        } finally {
            try {
                if (particlesInitialized) {
                    particles.dispose();
                    particlesInitialized = false;
                }
            } finally {
                try {
                    if (imguiInitialized) {
                        imgui.dispose();
                        imguiInitialized = false;
                    }
                } finally {
                    try {
                        if (presetDialogInitialized) {
                            PresetFileDialog.shutdown();
                            presetDialogInitialized = false;
                        }
                    } finally {
                        if (windowInitialized) {
                            window.dispose();
                            windowInitialized = false;
                        }
                    }
                }
            }
        }
    }
}

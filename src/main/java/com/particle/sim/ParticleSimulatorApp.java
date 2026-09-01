package com.particle.sim;

import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL43C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL43C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.glBlendFunc;
import static org.lwjgl.opengl.GL43C.glEnable;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.AppHotkeys;
import com.particle.sim.input.HotkeyManager;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.settings.SettingsController;
import com.particle.sim.system.StartupFailureException;
import com.particle.sim.ui.ImGuiLayer;
import com.particle.sim.ui.PresetFileDialog;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.window.WindowManager;

public final class ParticleSimulatorApp {
    private static final String WINDOW_TITLE = "3D Particle Simulator";
    private final WindowManager window = new WindowManager(WINDOW_TITLE);
    private final ImGuiLayer imGui = new ImGuiLayer();
    private final CameraController camera = new CameraController();
    private final ParticleSystem particles = new ParticleSystem();
    private final SimulationView ui = new SimulationView();
    private final HotkeyManager hotkeys = new HotkeyManager();
    private final SettingsController settingsController =
            new SettingsController(particles, camera, ui);
    private final SimulationViewAdapter viewAdapter =
            new SimulationViewAdapter(particles, camera, ui, settingsController);
    private boolean windowInitialized;
    private boolean presetDialogInitialized;
    private boolean imGuiInitialized;
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
            imGui.init(window.handle());
            imGuiInitialized = true;
            particles.init();
            particlesInitialized = true;
            AppHotkeys.register(hotkeys, this);
            initSettings();

            new ApplicationRuntime(
                            window,
                            imGui,
                            hotkeys,
                            camera,
                            particles,
                            ui,
                            viewAdapter,
                            settingsController)
                    .run();
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
        ui.connect(viewAdapter.model(), viewAdapter.actions());
        viewAdapter.onSavePreset(
                () ->
                        runPresetAction(
                                "Could not save the preset.",
                                () ->
                                        PresetFileDialog.showSaveDialog()
                                                .ifPresent(settingsController::savePresetTo)));
        viewAdapter.onLoadPreset(
                () ->
                        runPresetAction(
                                "Could not load the preset.",
                                () ->
                                        PresetFileDialog.showOpenDialog()
                                                .ifPresent(settingsController::loadPresetFrom)));
        viewAdapter.onExitApplication(
                () -> {
                    settingsController.flush();
                    window.requestClose();
                });

        settingsController.load();
    }

    private void runPresetAction(String summary, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            ui.showError(summary, exception.getMessage());
        }
    }

    public WindowManager getWindow() {
        return window;
    }

    public ParticleSystem getParticles() {
        return particles;
    }

    public SimulationView getUi() {
        return ui;
    }

    private void dispose() {
        try {
            settingsController.flush();
        } finally {
            try {
                if (imGuiInitialized) {
                    ui.dispose();
                }
            } finally {
                try {
                    if (particlesInitialized) {
                        particles.dispose();
                        particlesInitialized = false;
                    }
                } finally {
                    try {
                        if (imGuiInitialized) {
                            imGui.dispose();
                            imGuiInitialized = false;
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
}

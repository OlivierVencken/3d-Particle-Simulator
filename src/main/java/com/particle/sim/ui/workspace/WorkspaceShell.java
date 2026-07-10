package com.particle.sim.ui.workspace;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

public final class WorkspaceShell {
    private final WorkspaceState state = new WorkspaceState();
    private final WorkspaceCommandBar commandBar = new WorkspaceCommandBar();
    private final WorkspaceNavigation navigation = new WorkspaceNavigation();
    private final WorkspaceInspector inspector = new WorkspaceInspector();
    private final WorkspaceStatusBar statusBar = new WorkspaceStatusBar();
    private final DebugPanel debugPanel = new DebugPanel();
    private final ImBoolean showDebug = new ImBoolean(false);

    public void render(float deltaTime, float fps, int fpsCap, GpuParticleSystem particles, CameraController camera,
            boolean paused, Runnable togglePause, Runnable settingsChanged, Runnable resetSettings,
            Runnable savePreset, Runnable loadPreset, Runnable exitApplication, Runnable hideUi,
            java.util.function.IntConsumer fpsCapChanged) {
        WorkspaceLayout layout = WorkspaceLayoutCalculator.calculate(
                ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY(), state.activeSection(),
                state.inspectorVisible());
        state.setLayoutMode(layout.mode());
        commandBar.render(layout, state, particles, fps, paused, togglePause, savePreset, loadPreset,
                resetSettings, () -> showDebug.set(true), hideUi, exitApplication);
        navigation.render(layout.navigation(), state);
        inspector.render(layout, state, particles, camera, settingsChanged);
        statusBar.render(layout.statusBar(), paused, particles);
        if (showDebug.get()) {
            debugPanel.render(deltaTime, fps, fpsCap, fpsCapChanged, settingsChanged, particles, showDebug);
        }
    }

    public int customSpawnAmount() { return inspector.customSpawnAmount(); }
    public void setCustomSpawnAmount(int amount) { inspector.setCustomSpawnAmount(amount); }
    public float matrixEditStep() { return inspector.matrixEditStep(); }
    public void setMatrixEditStep(float step) { inspector.setMatrixEditStep(step); }
    public void toggleDebug() { showDebug.set(!showDebug.get()); }
}

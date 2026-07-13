package com.particle.sim.ui.workspace;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

public final class WorkspaceShell {
    private final WorkspaceState state = new WorkspaceState();
    private final WorkspaceCommandBar commandBar = new WorkspaceCommandBar();
    private final WorkspaceSidebar sidebar = new WorkspaceSidebar();
    private final DebugPanel debugPanel = new DebugPanel();
    private final ImBoolean showDebug = new ImBoolean(false);

    public void render(float deltaTime, float fps, int fpsCap, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged, Runnable resetSettings, Runnable savePreset, Runnable loadPreset,
            Runnable exitApplication, Runnable hideUi, java.util.function.IntConsumer fpsCapChanged) {
        WorkspaceLayout layout = WorkspaceLayoutCalculator.calculate(
                ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY(), state.sidebarVisible());
        state.setLayoutMode(layout.mode());
        commandBar.render(layout, state, particles, fps, savePreset, loadPreset, resetSettings, showDebug,
                hideUi, exitApplication);
        sidebar.render(layout.sidebar(), state, particles, camera, settingsChanged);
        if (showDebug.get()) {
            debugPanel.render(deltaTime, fps, fpsCap, fpsCapChanged, settingsChanged, particles, showDebug);
        }
    }

    public int customSpawnAmount() { return sidebar.customSpawnAmount(); }
    public void setCustomSpawnAmount(int amount) { sidebar.setCustomSpawnAmount(amount); }
    public float matrixEditStep() { return sidebar.matrixEditStep(); }
    public void setMatrixEditStep(float step) { sidebar.setMatrixEditStep(step); }
    public void toggleDebug() { showDebug.set(!showDebug.get()); }
    public void dispose() { commandBar.dispose(); }
}

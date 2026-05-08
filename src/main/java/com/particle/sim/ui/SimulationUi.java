package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

public final class SimulationUi {
    private boolean paused;

    public void render(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        ImGui.begin("Simulation");
        ImGui.text("Particles: %,d".formatted(particles.particleCount()));
        ImGui.text("Frame: %.2f ms".formatted(deltaTime * 1000.0f));

        ImGui.separator();
        ImGui.text("Camera");
        if (ImGui.button("Reset camera")) {
            camera.reset();
        }

        ImGui.separator();
        ImGui.text("Particles");
        ImGui.text("Groups: %d".formatted(particles.groupCount()));
        ImGui.text("Grid: %d x %d x %d".formatted(particles.gridSize(), particles.gridSize(), particles.gridSize()));

        float[] pointSize = {particles.pointSize()};
        if (ImGui.sliderFloat("Point size", pointSize, 1.0f, 8.0f)) {
            particles.pointSize(pointSize[0]);
        }

        float[] forceFactor = {particles.forceFactor()};
        if (ImGui.sliderFloat("Force", forceFactor, 0.0f, 25.0f)) {
            particles.forceFactor(forceFactor[0]);
        }

        float[] interactionRange = {particles.interactionRange()};
        if (ImGui.sliderFloat("Range", interactionRange, 0.2f, 3.0f)) {
            particles.interactionRange(interactionRange[0]);
        }

        float[] velocityDamping = {particles.velocityDamping()};
        if (ImGui.sliderFloat("Damping", velocityDamping, 0.85f, 1.0f)) {
            particles.velocityDamping(velocityDamping[0]);
        }

        ImBoolean pausedRef = new ImBoolean(paused);
        if (ImGui.checkbox("Paused", pausedRef)) {
            paused = pausedRef.get();
        }

        if (ImGui.button("Reset particles")) {
            particles.reset();
        }

        ImGui.end();
    }

    public boolean isPaused() {
        return paused;
    }
}

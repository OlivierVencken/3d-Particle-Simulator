package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

final class SimulationPanel {
    private boolean paused;
    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);

    void render(GpuParticleSystem particles, CameraController camera, Runnable settingsChanged,
            Runnable resetSettings) {
        ImGui.begin("Simulation");

        renderStatus(particles);
        renderPhysics(particles, settingsChanged);
        renderRendering(particles, settingsChanged);
        renderParticles(particles, settingsChanged);
        renderSpawnControls(particles, settingsChanged);
        renderCamera(camera, settingsChanged);
        renderPlayback(particles, settingsChanged, resetSettings);

        ImGui.end();
    }

    private void renderStatus(GpuParticleSystem particles) {
        ImGui.separatorText("Status");
        ImGui.text("Particles: %,d".formatted(particles.particleCount()));
    }

    private void renderPhysics(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.separatorText("Physics");
        UiControls.settingSlider("Force", particles.forceFactor(), 0.0f, 10.0f, particles::forceFactor,
                settingsChanged);
        UiControls.settingSlider("Interaction range", particles.interactionRange(), 0.2f, 3.0f,
                particles::interactionRange, settingsChanged);
        UiControls.settingSlider("Repulsion radius", particles.repulsionRadius(), 0.02f, 0.95f,
                particles::repulsionRadius, settingsChanged);
        UiControls.settingSlider("Velocity damping", particles.velocityDamping(), 0.85f, 1.0f,
                particles::velocityDamping, settingsChanged);
        UiControls.settingSlider("Max velocity", particles.maxVelocity(), 0.5f, 16.0f, particles::maxVelocity,
                settingsChanged);
        UiControls.settingSlider("Boundary bounce", particles.boundaryBounce(), 0.0f, 1.0f,
                particles::boundaryBounce, settingsChanged);
        UiControls.settingSlider("Bounds", particles.bounds(), 2.0f, 10.0f, particles::bounds, settingsChanged);

        ImBoolean toroidalWrap = new ImBoolean(particles.toroidalWrap());
        if (ImGui.checkbox("Wrap around", toroidalWrap)) {
            particles.toroidalWrap(toroidalWrap.get());
            settingsChanged.run();
        }
    }

    private void renderRendering(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.separatorText("Rendering");
        UiControls.settingSlider("Point size", particles.pointSize(), 1.0f, 8.0f, particles::pointSize,
                settingsChanged);
        ImBoolean fixedParticleScreenSize = new ImBoolean(particles.fixedParticleScreenSize());
        if (ImGui.checkbox("Fixed particle size", fixedParticleScreenSize)) {
            particles.fixedParticleScreenSize(fixedParticleScreenSize.get());
            settingsChanged.run();
        }
    }

    private void renderParticles(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.separatorText("Particles");

        ImInt groupCount = new ImInt(particles.groupCount());
        ImGui.setNextItemWidth(120.0f);
        if (ImGui.inputInt("Groups", groupCount, 1, 2)) {
            particles.groupCount(groupCount.get());
            settingsChanged.run();
        }

        ImInt currentColorMode = new ImInt(particles.colorMode().ordinal());
        String[] colorModes = { "Group", "Velocity", "Position", "Distance", "Direction", "Density" };
        if (ImGui.combo("Color Mode", currentColorMode, colorModes)) {
            particles.colorMode(ColorMode.values()[currentColorMode.get()]);
            settingsChanged.run();
        }
    }

    private void renderSpawnControls(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.separatorText("Spawn");
        ImGui.text("Max: %,d".formatted(particles.maxParticleCount()));

        spawnButton("+1k", particles, 1_000, settingsChanged);
        ImGui.sameLine();
        spawnButton("-1k", particles, -1_000, settingsChanged);
        ImGui.sameLine();
        spawnButton("+10k", particles, 10_000, settingsChanged);
        ImGui.sameLine();
        spawnButton("-10k", particles, -10_000, settingsChanged);

        spawnButton("+100k", particles, 100_000, settingsChanged);
        ImGui.sameLine();
        spawnButton("-100k", particles, -100_000, settingsChanged);
        ImGui.sameLine();
        if (ImGui.button("Clear")) {
            particles.clearParticles();
            settingsChanged.run();
        }

        if (customSpawnAmount.get() < 0) {
            customSpawnAmount.set(0);
        }

        ImGui.setNextItemWidth(120.0f);
        if (ImGui.inputInt("Amount", customSpawnAmount, 100, 1_000)) {
            if (customSpawnAmount.get() < 0) {
                customSpawnAmount.set(0);
            }
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Add")) {
            particles.addParticles(customSpawnAmount.get());
            settingsChanged.run();
        }

        ImInt currentSpawnMode = new ImInt(particles.spawnMode().ordinal());
        String[] spawnModes = { "Random", "Spherical", "Grid", "Shell", "Spiral", "Disc", "Clusters", "Point" };
        if (ImGui.combo("Mode", currentSpawnMode, spawnModes)) {
            particles.spawnMode(SpawnMode.values()[currentSpawnMode.get()]);
            settingsChanged.run();
        }
    }

    private void spawnButton(String label, GpuParticleSystem particles, int amount, Runnable settingsChanged) {
        if (ImGui.button(label)) {
            if (amount >= 0) {
                particles.addParticles(amount);
            } else {
                particles.removeParticles(-amount);
            }
            settingsChanged.run();
        }
    }

    private void renderCamera(CameraController camera, Runnable settingsChanged) {
        ImGui.separatorText("Camera");
        UiControls.settingSlider("Sensitivity", camera.getSensitivity(), 0.0001f, 0.01f, camera::setSensitivity,
                settingsChanged);
        if (ImGui.button("Reset camera")) {
            camera.reset();
        }
    }

    private void renderPlayback(GpuParticleSystem particles, Runnable settingsChanged, Runnable resetSettings) {
        ImGui.separatorText("Playback");
        ImBoolean pausedRef = new ImBoolean(paused);
        if (ImGui.checkbox("Paused", pausedRef)) {
            paused = pausedRef.get();
            settingsChanged.run();
        }

        if (ImGui.button("Reset particles")) {
            particles.reset();
        }

        if (ImGui.button("Reset simulation settings")) {
            resetSettings.run();
        }
    }

    boolean isPaused() {
        return paused;
    }

    void setPaused(boolean paused) {
        this.paused = paused;
    }

    int customSpawnAmount() {
        return customSpawnAmount.get();
    }

    void setCustomSpawnAmount(int customSpawnAmount) {
        this.customSpawnAmount.set(Math.max(0, customSpawnAmount));
    }
}

package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

public final class SimulationUi {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private boolean paused;
    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);
    private Runnable settingsChanged = () -> {
    };
    private Runnable resetSettings = () -> {
    };

    public void onSettingsChanged(Runnable settingsChanged) {
        this.settingsChanged = settingsChanged == null ? () -> {
        } : settingsChanged;
    }

    public void onResetSettings(Runnable resetSettings) {
        this.resetSettings = resetSettings == null ? () -> {
        } : resetSettings;
    }

    public void render(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        updateFps(deltaTime);
        renderSimulationWindow(deltaTime, particles, camera);
        renderAttractionMatrixWindow(particles);
    }

    private void renderSimulationWindow(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        ImGui.begin("Simulation");

        ImGui.separatorText("Status");
        ImGui.text("FPS: %.0f".formatted(currentFps));
        ImGui.text("Frame: %.2f ms".formatted(deltaTime * 1000.0f));

        ImGui.separatorText("Config");
        ImGui.text("Particles: %,d".formatted(particles.particleCount()));
        ImGui.text("Groups: %d".formatted(particles.groupCount()));
        ImGui.text("Grid: %d x %d x %d".formatted(particles.gridSize(), particles.gridSize(), particles.gridSize()));
        ImGui.text("Cell capacity: %d".formatted(particles.maxParticlesPerCell()));

        ImGui.separatorText("Physics");
        settingSlider("Force", particles.forceFactor(), 0.0f, 10.0f, particles::forceFactor);
        settingSlider("Interaction range", particles.interactionRange(), 0.2f, 3.0f, particles::interactionRange);
        settingSlider("Repulsion radius", particles.repulsionRadius(), 0.02f, 0.95f, particles::repulsionRadius);
        settingSlider("Velocity damping", particles.velocityDamping(), 0.85f, 1.0f, particles::velocityDamping);
        settingSlider("Max velocity", particles.maxVelocity(), 0.5f, 16.0f, particles::maxVelocity);
        settingSlider("Boundary bounce", particles.boundaryBounce(), 0.0f, 1.0f, particles::boundaryBounce);
        settingSlider("Bounds", particles.bounds(), 2.0f, 10.0f, particles::bounds);
        ImBoolean toroidalWrap = new ImBoolean(particles.toroidalWrap());
        if (ImGui.checkbox("Wrap around", toroidalWrap)) {
            particles.toroidalWrap(toroidalWrap.get());
            settingsChanged.run();
        }

        ImGui.separatorText("Rendering");
        settingSlider("Point size", particles.pointSize(), 1.0f, 8.0f, particles::pointSize);

        ImGui.separatorText("Particles");
        ImInt currentColorMode = new ImInt(particles.colorMode().ordinal());
        String[] colorModes = {"Group", "Velocity", "Position", "Distance", "Direction", "Density"};
        if (ImGui.combo("Color Mode", currentColorMode, colorModes)) {
            particles.colorMode(ColorMode.values()[currentColorMode.get()]);
            settingsChanged.run();
        }

        ImGui.separatorText("Spawn");
        renderSpawnControls(particles);

        ImGui.separatorText("Camera");
        settingSlider("Sensitivity", camera.getSensitivity(), 0.0001f, 0.01f, camera::setSensitivity);
        if (ImGui.button("Reset camera")) {
            camera.reset();
        }

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

        ImGui.end();
    }

    private void renderSpawnControls(GpuParticleSystem particles) {
        ImGui.text("Max: %,d".formatted(particles.maxParticleCount()));

        spawnButton("+1k", particles, 1_000);
        ImGui.sameLine();
        spawnButton("-1k", particles, -1_000);
        ImGui.sameLine();
        spawnButton("+10k", particles, 10_000);
        ImGui.sameLine();
        spawnButton("-10k", particles, -10_000);

        spawnButton("+100k", particles, 100_000);
        ImGui.sameLine();
        spawnButton("-100k", particles, -100_000);
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
        String[] spawnModes = {"Random", "Spherical", "Grid", "Shell", "Spiral", "Disc", "Clusters", "Point"};
        if (ImGui.combo("Mode", currentSpawnMode, spawnModes)) {
            particles.spawnMode(SpawnMode.values()[currentSpawnMode.get()]);
            settingsChanged.run();
        }
    }

    private void spawnButton(String label, GpuParticleSystem particles, int amount) {
        if (ImGui.button(label)) {
            if (amount >= 0) {
                particles.addParticles(amount);
            } else {
                particles.removeParticles(-amount);
            }
            settingsChanged.run();
        }
    }

    private void renderAttractionMatrixWindow(GpuParticleSystem particles) {
        ImGui.begin("Attraction Matrix");

        settingSlider("Edit step", matrixEditStep, 0.01f, 0.5f, value -> matrixEditStep = value);

        if (ImGui.button("Randomize")) {
            particles.randomizeAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Zero")) {
            particles.zeroAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Symmetrize")) {
            particles.symmetrizeAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Invert")) {
            particles.invertAttractionMatrix();
            settingsChanged.run();
        }

        ImGui.spacing();

        int groupCount = particles.groupCount();
        if (ImGui.beginTable("attraction-matrix-table", groupCount + 1)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.textUnformatted("A/B");
            for (int column = 0; column < groupCount; column++) {
                ImGui.tableNextColumn();
                ImGui.textUnformatted("G%d".formatted(column));
            }

            for (int row = 0; row < groupCount; row++) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.textUnformatted("G%d".formatted(row));

                for (int column = 0; column < groupCount; column++) {
                    ImGui.tableNextColumn();
                    renderMatrixTile(particles, row, column);
                }
            }

            ImGui.endTable();
        }

        ImGui.spacing();
        ImGui.textColored(0.2f, 1.0f, 0.25f, 1.0f, "Green: attraction");
        ImGui.sameLine();
        ImGui.textColored(0.55f, 0.55f, 0.55f, 1.0f, "Grey: zero");
        ImGui.sameLine();
        ImGui.textColored(1.0f, 0.2f, 0.16f, 1.0f, "Red: repulsion");

        ImGui.end();
    }

    private void renderMatrixTile(GpuParticleSystem particles, int row, int column) {
        float value = particles.attraction(row, column);
        ImVec4 color = attractionColor(value);
        ImVec2 size = new ImVec2(34.0f, 24.0f);


        ImGui.pushID("matrix-%d-%d".formatted(row, column));
        ImGui.colorButton("##tile", color, size);

        if (ImGui.isItemClicked(LEFT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, matrixEditStep);
            settingsChanged.run();
        }
        if (ImGui.isItemClicked(RIGHT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, -matrixEditStep);
            settingsChanged.run();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("G%d -> G%d: %.2f".formatted(row, column, value));
        }

        ImGui.popID();
    }

    private ImVec4 attractionColor(float value) {
        float strength = Math.min(1.0f, Math.abs(value));
        float neutral = 0.16f;
        if (value >= 0.0f) {
            return new ImVec4(
                    neutral * (1.0f - strength),
                    neutral * (1.0f - strength) + strength,
                    neutral * (1.0f - strength),
                    1.0f
            );
        }

        return new ImVec4 (
                neutral * (1.0f - strength) + strength,
                neutral * (1.0f - strength),
                neutral * (1.0f - strength),
                1.0f
        );
    }

    private void updateFps(float deltaTime) {
        fpsTimeAccumulator += deltaTime;
        fpsFrameAccumulator++;

        if (fpsTimeAccumulator >= 0.35f) {
            currentFps = fpsFrameAccumulator / fpsTimeAccumulator;
            fpsTimeAccumulator = 0.0f;
            fpsFrameAccumulator = 0;
        }
    }

    private void settingSlider(String label, float value, float min, float max, FloatSetter setter) {
        if (slider(label, value, min, max, setter)) {
            settingsChanged.run();
        }
    }

    private boolean slider(String label, float value, float min, float max, FloatSetter setter) {
        float[] valueRef = { value };
        if (ImGui.sliderFloat(label, valueRef, min, max)) {
            setter.set(valueRef[0]);
            return true;
        }
        return false;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public float matrixEditStep() {
        return matrixEditStep;
    }

    public void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }

    public int customSpawnAmount() {
        return customSpawnAmount.get();
    }

    public void setCustomSpawnAmount(int customSpawnAmount) {
        this.customSpawnAmount.set(Math.max(0, customSpawnAmount));
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}

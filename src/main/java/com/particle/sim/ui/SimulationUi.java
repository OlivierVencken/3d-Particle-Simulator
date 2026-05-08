package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

public final class SimulationUi {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private boolean paused;
    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private float matrixEditStep = 0.1f;

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
        slider("Force", particles.forceFactor(), 0.0f, 25.0f, particles::forceFactor);
        slider("Interaction range", particles.interactionRange(), 0.2f, 3.0f, particles::interactionRange);
        slider("Repulsion radius", particles.repulsionRadius(), 0.02f, 0.95f, particles::repulsionRadius);
        slider("Velocity damping", particles.velocityDamping(), 0.85f, 1.0f, particles::velocityDamping);
        slider("Max velocity", particles.maxVelocity(), 0.5f, 30.0f, particles::maxVelocity);
        slider("Boundary bounce", particles.boundaryBounce(), 0.0f, 1.0f, particles::boundaryBounce);
        slider("Bounds", particles.bounds(), 2.0f, 24.0f, particles::bounds);
        ImBoolean toroidalWrap = new ImBoolean(particles.toroidalWrap());
        if (ImGui.checkbox("Wrap around", toroidalWrap)) {
            particles.toroidalWrap(toroidalWrap.get());
        }

        ImGui.separatorText("Rendering");
        slider("Point size", particles.pointSize(), 1.0f, 8.0f, particles::pointSize);

        ImGui.separatorText("Camera");
        if (ImGui.button("Reset camera")) {
            camera.reset();
        }

        ImGui.separatorText("Playback");
        ImBoolean pausedRef = new ImBoolean(paused);
        if (ImGui.checkbox("Paused", pausedRef)) {
            paused = pausedRef.get();
        }

        if (ImGui.button("Reset particles")) {
            particles.reset();
        }

        ImGui.end();
    }

    private void renderAttractionMatrixWindow(GpuParticleSystem particles) {
        ImGui.begin("Attraction Matrix");

        slider("Edit step", matrixEditStep, 0.01f, 0.5f, value -> matrixEditStep = value);

        if (ImGui.button("Randomize")) {
            particles.randomizeAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Zero")) {
            particles.zeroAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Symmetrize")) {
            particles.symmetrizeAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Invert")) {
            particles.invertAttractionMatrix();
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
        float[] color = attractionColor(value);

        ImGui.pushID("matrix-%d-%d".formatted(row, column));
        ImGui.colorButton("##tile", color, 0, 34.0f, 24.0f);

        if (ImGui.isItemClicked(LEFT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, matrixEditStep);
        }
        if (ImGui.isItemClicked(RIGHT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, -matrixEditStep);
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("G%d -> G%d: %.2f".formatted(row, column, value));
        }

        ImGui.popID();
    }

    private float[] attractionColor(float value) {
        float strength = Math.min(1.0f, Math.abs(value));
        float neutral = 0.16f;
        if (value >= 0.0f) {
            return new float[]{
                    neutral * (1.0f - strength),
                    neutral * (1.0f - strength) + strength,
                    neutral * (1.0f - strength),
                    1.0f
            };
        }

        return new float[]{
                neutral * (1.0f - strength) + strength,
                neutral * (1.0f - strength),
                neutral * (1.0f - strength),
                1.0f
        };
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

    private void slider(String label, float value, float min, float max, FloatSetter setter) {
        float[] valueRef = {value};
        if (ImGui.sliderFloat(label, valueRef, min, max)) {
            setter.set(valueRef[0]);
        }
    }

    public boolean isPaused() {
        return paused;
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}

package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

final class InspectorSections {
    private static final String[] COLOR_MODES = labels(ColorMode.values());
    private static final String[] DISTANCE_METRICS = labels(DistanceMetric.values());
    private static final String[] SPAWN_MODES = labels(SpawnMode.values());

    private final AttractionMatrixEditor matrixEditor = new AttractionMatrixEditor();
    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);

    void render(UiSection section, GpuParticleSystem particles, CameraController camera, Runnable settingsChanged) {
        switch (section) {
            case SIMULATION -> renderSimulation(particles, settingsChanged);
            case PARTICLES -> renderParticles(particles, settingsChanged);
            case APPEARANCE -> renderAppearance(particles, settingsChanged);
            case CAMERA -> renderCamera(camera, settingsChanged);
            case INTERACTIONS -> matrixEditor.renderSettings(particles, settingsChanged);
        }
    }

    private void renderSimulation(GpuParticleSystem particles, Runnable changed) {
        section("World");
        checkbox("Wrap boundaries", "world-wrap", particles.toroidalWrap(), particles::toroidalWrap, changed);
        UiControls.settingSlider("Bounds", "world-bounds", particles.bounds(), 2.0f, 10.0f, 1,
                particles::bounds, changed);
        if (!particles.toroidalWrap()) {
            UiControls.settingSlider("Boundary bounce", "world-bounce", particles.boundaryBounce(), 0.0f, 1.0f, 2,
                    particles::boundaryBounce, changed);
        }

        section("Dynamics");
        UiControls.settingSlider("Force", "dynamics-force", particles.forceFactor(), 0.0f, 10.0f, 1,
                particles::forceFactor, changed);
        UiControls.settingSlider("Interaction range", "dynamics-range", particles.interactionRange(), 0.2f, 3.0f, 2,
                particles::interactionRange, changed);
        UiControls.settingSlider("Repulsion radius", "dynamics-repulsion", particles.repulsionRadius(), 0.02f, 0.95f,
                2, particles::repulsionRadius, changed);
        UiControls.settingSlider("Velocity damping", "dynamics-damping", particles.velocityDamping(), 0.85f, 1.0f,
                3, particles::velocityDamping, changed);
        UiControls.settingSlider("Max velocity", "dynamics-max-velocity", particles.maxVelocity(), 0.5f, 16.0f, 1,
                particles::maxVelocity, changed);
        checkbox("Density regulation", "dynamics-density", particles.densityRegulationEnabled(),
                particles::densityRegulationEnabled, changed);
        if (particles.densityRegulationEnabled()) {
            UiControls.settingSlider("Density limit", "dynamics-density-limit", particles.densityLimit(), 0.0f,
                    500.0f, 0, particles::densityLimit, changed);
        }
        enumCombo("Distance metric", "dynamics-distance", particles.distanceMetric().ordinal(), DISTANCE_METRICS,
                value -> particles.distanceMetric(DistanceMetric.values()[value]), changed);
    }

    private void renderParticles(GpuParticleSystem particles, Runnable changed) {
        float summaryWidth = ImGui.getContentRegionAvailX();
        float particleCardWidth = Math.max(140.0f, (summaryWidth - 8.0f) * 0.62f);
        metricCard("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()), particleCardWidth);
        ImGui.sameLine();
        metricCard("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                Math.max(88.0f, summaryWidth - particleCardWidth - 8.0f));

        section("Population");
        ImInt groups = new ImInt(particles.groupCount());
        ImGui.textDisabled("Groups");
        ImGui.setNextItemWidth(-1.0f);
        if (ImGui.inputInt("##particle-groups", groups, 1, 2)) {
            particles.groupCount(groups.get());
            changed.run();
        }
        enumCombo("Spawn mode", "particle-spawn-mode", particles.spawnMode().ordinal(), SPAWN_MODES,
                value -> particles.spawnMode(SpawnMode.values()[value]), changed);

        section("Spawn particles");
        float pairWidth = Math.max(80.0f, (ImGui.getContentRegionAvailX() - 8.0f) * 0.5f);
        spawnButton("Add 1k", 1_000, pairWidth, particles, changed);
        ImGui.sameLine();
        spawnButton("Remove 1k", -1_000, pairWidth, particles, changed);
        spawnButton("Add 10k", 10_000, pairWidth, particles, changed);
        ImGui.sameLine();
        spawnButton("Remove 10k", -10_000, pairWidth, particles, changed);
        spawnButton("Add 100k", 100_000, pairWidth, particles, changed);
        ImGui.sameLine();
        spawnButton("Remove 100k", -100_000, pairWidth, particles, changed);

        customSpawnAmount.set(Math.max(0, customSpawnAmount.get()));
        ImGui.spacing();
        ImGui.textDisabled("Custom amount");
        ImGui.setNextItemWidth(Math.max(100.0f, ImGui.getContentRegionAvailX() - 72.0f));
        if (ImGui.inputInt("##custom-spawn-amount", customSpawnAmount, 100, 1_000)) {
            changed.run();
        }
        customSpawnAmount.set(Math.max(0, customSpawnAmount.get()));
        ImGui.sameLine();
        if (ImGui.button("Add##custom-spawn", 64.0f, 0.0f)) {
            particles.addParticles(customSpawnAmount.get());
            changed.run();
        }
        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.Text, UiPalette.DESTRUCTIVE.vec4());
        if (ImGui.button("Clear particles##clear-particles")) {
            particles.clearParticles();
            changed.run();
        }
        ImGui.popStyleColor();
    }

    private void renderAppearance(GpuParticleSystem particles, Runnable changed) {
        section("Particle rendering");
        UiControls.settingSlider("Particle size", "appearance-size", particles.pointSize(), 1.0f, 8.0f, 1,
                particles::pointSize, changed);
        checkbox("Fixed screen size", "appearance-fixed-size", particles.fixedParticleScreenSize(),
                particles::fixedParticleScreenSize, changed);
        enumCombo("Color mode", "appearance-color", particles.colorMode().ordinal(), COLOR_MODES,
                value -> particles.colorMode(ColorMode.values()[value]), changed);

        effectHeader("Glow", EffectMode.GLOW, particles, changed);
        if (particles.effectEnabled(EffectMode.GLOW)) {
            UiControls.settingIntSlider("Passes", "glow-passes", particles.glowBlurPasses(), 1, 64, 0,
                    particles::glowBlurPasses, changed);
            UiControls.settingSlider("Strength", "glow-strength", particles.glowStrength(), 0.0f, 6.0f, 1,
                    particles::glowStrength, changed);
            UiControls.settingSlider("Radius", "glow-radius", particles.glowRadius(), 0.5f, 12.0f, 1,
                    particles::glowRadius, changed);
            UiControls.settingSlider("Falloff", "glow-falloff", particles.glowFalloff(), 0.05f, 3.0f, 2,
                    particles::glowFalloff, changed);
            ImGui.textDisabled("Bloom resolution: 1/%d per axis".formatted(particles.effectiveBloomDivisor()));
        }

        effectHeader("Trails", EffectMode.TRAILS, particles, changed);
        if (particles.effectEnabled(EffectMode.TRAILS)) {
            UiControls.settingIntSlider("Trail length", "trail-length", particles.trailLength(),
                    SimulationDefaults.MIN_TRAIL_LENGTH, SimulationDefaults.MAX_TRAIL_LENGTH, 0,
                    particles::trailLength, changed);
            UiControls.settingSlider("Thickness", "trail-thickness", particles.trailThickness(),
                    SimulationDefaults.MIN_TRAIL_THICKNESS, particles.pointSize(), 1, particles::trailThickness,
                    changed);
            String quality = WorkspaceStatusBar.qualityMessage(particles);
            if (!quality.isEmpty()) ImGui.textDisabled(quality);
        }
    }

    private void renderCamera(CameraController camera, Runnable changed) {
        section("Movement");
        UiControls.settingSlider("Sensitivity", "camera-sensitivity", camera.getSensitivity(), 0.0001f, 0.01f, 4,
                camera::setSensitivity, changed);
        UiControls.settingSlider("Fly speed", "camera-speed", camera.getFlySpeed(), 0.1f, 30.0f, 1,
                camera::setFlySpeed, changed);
        if (ImGui.button("Reset camera##camera-reset")) camera.reset();
        ImGui.spacing();
        ImGui.textDisabled("WASD move  |  Mouse look");
        ImGui.textDisabled("Shift accelerates movement");
    }

    private void effectHeader(String label, EffectMode mode, GpuParticleSystem particles, Runnable changed) {
        section(label);
        checkbox("Enabled", "effect-" + mode.name(), particles.effectEnabled(mode),
                value -> particles.effectEnabled(mode, value), changed);
    }

    private void checkbox(String label, String id, boolean value, BooleanSetter setter, Runnable changed) {
        ImBoolean ref = new ImBoolean(value);
        if (ImGui.checkbox(label + "##" + id, ref)) {
            setter.set(ref.get());
            changed.run();
        }
    }

    private void enumCombo(String label, String id, int value, String[] values, IntSetter setter, Runnable changed) {
        ImGui.textDisabled(label);
        ImInt ref = new ImInt(value);
        ImGui.setNextItemWidth(-1.0f);
        if (ImGui.combo("##" + id, ref, values)) {
            setter.set(ref.get());
            changed.run();
        }
    }

    private void spawnButton(String label, int amount, float width, GpuParticleSystem particles, Runnable changed) {
        if (ImGui.button(label + "##spawn-" + amount, width, 32.0f)) {
            if (amount > 0) particles.addParticles(amount); else particles.removeParticles(-amount);
            changed.run();
        }
    }

    private void metricCard(String id, String label, String value, float width) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UiPalette.SURFACE.withAlpha(0.72f).vec4());
        if (ImGui.beginChild("##metric-" + id, width, 64.0f, true)) {
            ImGui.textDisabled(label);
            ImGui.pushFont(UiFonts.section());
            ImGui.textUnformatted(value);
            ImGui.popFont();
        }
        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void section(String label) {
        ImGui.spacing();
        ImGui.pushFont(UiFonts.section());
        ImGui.separatorText(label);
        ImGui.popFont();
    }

    private static String[] labels(Enum<?>[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            String raw = values[i].name().toLowerCase().replace('_', ' ');
            labels[i] = Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
        }
        return labels;
    }

    int customSpawnAmount() { return customSpawnAmount.get(); }
    void setCustomSpawnAmount(int amount) { customSpawnAmount.set(Math.max(0, amount)); }
    float matrixEditStep() { return matrixEditor.matrixEditStep(); }
    void setMatrixEditStep(float step) { matrixEditor.setMatrixEditStep(step); }

    @FunctionalInterface private interface BooleanSetter { void set(boolean value); }
    @FunctionalInterface private interface IntSetter { void set(int value); }
}

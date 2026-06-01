package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

final class SettingsSidebar {
    private static final float COLLAPSED_SIDEBAR_WIDTH = 24.0f;
    private static final float COLLAPSE_BUTTON_WIDTH = 24.0f;
    private static final float COLLAPSE_BUTTON_HEIGHT = 56.0f;
    private static final float MENU_BAR_HEIGHT_FALLBACK = 24.0f;
    private static final float MIN_SIDEBAR_WIDTH = 280.0f;
    private static final float MAX_SIDEBAR_WIDTH = 460.0f;
    private static final float SIDEBAR_WIDTH_RATIO = 0.28f;
    private static final int SECTION_FLAGS = ImGuiTreeNodeFlags.DefaultOpen;
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoTitleBar
            | ImGuiWindowFlags.NoSavedSettings;
    private static final int HANDLE_WINDOW_FLAGS = ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoTitleBar
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar
            | ImGuiWindowFlags.NoScrollWithMouse;
    private static final String[] COLOR_MODE_LABELS = enumLabels(ColorMode.values());
    private static final String[] DISTANCE_METRIC_LABELS = enumLabels(DistanceMetric.values());
    private static final String[] EFFECT_MODE_LABELS = enumLabels(EffectMode.values());
    private static final String[] SPAWN_MODE_LABELS = enumLabels(SpawnMode.values());

    private final AttractionMatrixEditor attractionMatrixEditor = new AttractionMatrixEditor();
    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);
    private boolean paused;
    private boolean collapsed;

    void render(GpuParticleSystem particles, CameraController camera, Runnable settingsChanged) {
        positionSidebar();

        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 0.0f, 0.0f);
        if (ImGui.begin("Settings", WINDOW_FLAGS)) {
            if (!collapsed) {
                renderWorld(particles, settingsChanged);
                renderPhysics(particles, settingsChanged);
                renderParticles(particles, settingsChanged);
                renderGraphics(particles, settingsChanged);
                renderCamera(camera, settingsChanged);
                renderMatrix(particles, settingsChanged);
            }
        }

        ImGui.end();
        ImGui.popStyleVar();
        renderCollapseButton();
    }

    private void positionSidebar() {
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        float menuBarHeight = Math.max(MENU_BAR_HEIGHT_FALLBACK, ImGui.getFrameHeight());
        float sidebarWidth = collapsed ? COLLAPSED_SIDEBAR_WIDTH : expandedSidebarWidth(displayWidth);

        ImGui.setNextWindowPos(0.0f, menuBarHeight);
        ImGui.setNextWindowSize(sidebarWidth, Math.max(0.0f, displayHeight - menuBarHeight));
    }

    private float expandedSidebarWidth(float displayWidth) {
        return Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, displayWidth * SIDEBAR_WIDTH_RATIO));
    }

    private void renderCollapseButton() {
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        float menuBarHeight = Math.max(MENU_BAR_HEIGHT_FALLBACK, ImGui.getFrameHeight());
        float sidebarWidth = collapsed ? COLLAPSED_SIDEBAR_WIDTH : expandedSidebarWidth(displayWidth);
        float x = Math.min(displayWidth - COLLAPSE_BUTTON_WIDTH, sidebarWidth);
        float y = menuBarHeight + Math.max(0.0f, (displayHeight - menuBarHeight - COLLAPSE_BUTTON_HEIGHT) * 0.5f);

        ImGui.setNextWindowPos(x, y);
        ImGui.setNextWindowSize(COLLAPSE_BUTTON_WIDTH, COLLAPSE_BUTTON_HEIGHT);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0.0f, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 0.0f, 0.0f);
        if (ImGui.begin("##settings-sidebar-handle", HANDLE_WINDOW_FLAGS)
                && ImGui.button(collapsed ? ">" : "<", COLLAPSE_BUTTON_WIDTH, COLLAPSE_BUTTON_HEIGHT)) {
            collapsed = !collapsed;
        }
        ImGui.end();
        ImGui.popStyleVar(3);
    }

    private void renderWorld(GpuParticleSystem particles, Runnable settingsChanged) {
        if (!ImGui.collapsingHeader("World", SECTION_FLAGS)) {
            return;
        }

        ImBoolean toroidalWrap = new ImBoolean(particles.toroidalWrap());
        if (ImGui.checkbox("Wrap around", toroidalWrap)) {
            particles.toroidalWrap(toroidalWrap.get());
            settingsChanged.run();
        }

        ImBoolean densityRegulationEnabled = new ImBoolean(particles.densityRegulationEnabled());
        if (ImGui.checkbox("Density regulation", densityRegulationEnabled)) {
            particles.densityRegulationEnabled(densityRegulationEnabled.get());
            settingsChanged.run();
        }

        UiControls.settingSlider("Density limit", particles.densityLimit(), 0.0f, 500.0f, particles::densityLimit,
                settingsChanged);

        ImInt currentDistanceMetric = new ImInt(particles.distanceMetric().ordinal());
        if (ImGui.combo("Distance", currentDistanceMetric, DISTANCE_METRIC_LABELS)) {
            particles.distanceMetric(DistanceMetric.values()[currentDistanceMetric.get()]);
            settingsChanged.run();
        }

        UiControls.settingSlider("Bounds", particles.bounds(), 2.0f, 10.0f, particles::bounds, settingsChanged);
    }

    private void renderPhysics(GpuParticleSystem particles, Runnable settingsChanged) {
        if (!ImGui.collapsingHeader("Physics", SECTION_FLAGS)) {
            return;
        }

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
    }

    private void renderParticles(GpuParticleSystem particles, Runnable settingsChanged) {
        if (!ImGui.collapsingHeader("Particles", SECTION_FLAGS)) {
            return;
        }

        ImGui.text("Particle count: %,d / %,d".formatted(particles.particleCount(), particles.maxParticleCount()));

        ImInt groupCount = new ImInt(particles.groupCount());
        ImGui.setNextItemWidth(120.0f);
        if (ImGui.inputInt("Groups", groupCount, 1, 2)) {
            particles.groupCount(groupCount.get());
            settingsChanged.run();
        }

        renderSpawnControls(particles, settingsChanged);
    }

    private void renderGraphics(GpuParticleSystem particles, Runnable settingsChanged) {
        if (!ImGui.collapsingHeader("Graphics", SECTION_FLAGS)) {
            return;
        }

        UiControls.settingSlider("Particle size", particles.pointSize(), 1.0f, 8.0f, particles::pointSize,
                settingsChanged);

        ImBoolean fixedParticleScreenSize = new ImBoolean(particles.fixedParticleScreenSize());
        if (ImGui.checkbox("Fixed particle size", fixedParticleScreenSize)) {
            particles.fixedParticleScreenSize(fixedParticleScreenSize.get());
            settingsChanged.run();
        }

        ImInt currentColorMode = new ImInt(particles.colorMode().ordinal());
        if (ImGui.combo("Color Mode", currentColorMode, COLOR_MODE_LABELS)) {
            particles.colorMode(ColorMode.values()[currentColorMode.get()]);
            settingsChanged.run();
        }

        ImInt currentEffect = new ImInt(particles.effectMode().ordinal());
        if (ImGui.combo("Effect", currentEffect, EFFECT_MODE_LABELS)) {
            particles.effectMode(EffectMode.values()[currentEffect.get()]);
            settingsChanged.run();
        }
    }

    private void renderSpawnControls(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.separatorText("Spawn");

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
        if (ImGui.combo("Mode", currentSpawnMode, SPAWN_MODE_LABELS)) {
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
        if (!ImGui.collapsingHeader("Camera", SECTION_FLAGS)) {
            return;
        }

        UiControls.settingSlider("Sensitivity", camera.getSensitivity(), 0.0001f, 0.01f, camera::setSensitivity,
                settingsChanged);
        UiControls.settingSlider("Fly speed", camera.getFlySpeed(), 0.1f, 30.0f, camera::setFlySpeed,
                settingsChanged);
        if (ImGui.button("Reset camera")) {
            camera.reset();
        }
    }

    private void renderMatrix(GpuParticleSystem particles, Runnable settingsChanged) {
        if (ImGui.collapsingHeader("Matrix", SECTION_FLAGS)) {
            attractionMatrixEditor.renderSettings(particles, settingsChanged);
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

    float matrixEditStep() {
        return attractionMatrixEditor.matrixEditStep();
    }

    void setMatrixEditStep(float matrixEditStep) {
        attractionMatrixEditor.setMatrixEditStep(matrixEditStep);
    }

    private static String[] enumLabels(Enum<?>[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = displayName(values[i]);
        }
        return labels;
    }

    private static String displayName(Enum<?> value) {
        String label = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }
}

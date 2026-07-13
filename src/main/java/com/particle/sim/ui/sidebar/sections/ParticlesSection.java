package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

final class ParticlesSection {
    private static final String[] SPAWN_MODES = UIControls.enumLabels(SpawnMode.values());

    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);

    void render(GpuParticleSystem particles, Runnable settingsChanged) {
        float summaryWidth = ImGui.getContentRegionAvailX();
        float particleCardWidth = Math.max(140.0f, (summaryWidth - 8.0f) * 0.62f);
        metricCard("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()), particleCardWidth);
        ImGui.sameLine();
        metricCard("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                Math.max(88.0f, summaryWidth - particleCardWidth - 8.0f));

        ImGui.separatorText("");

        UIControls.sectionHeading("Population");
        ImInt groups = new ImInt(particles.groupCount());
        ImGui.textDisabled("Groups");
        ImGui.setNextItemWidth(-1.0f);
        if (ImGui.inputInt("##particle-groups", groups, 1, 2)) {
            particles.groupCount(groups.get());
            settingsChanged.run();
        }
        UIControls.settingCombo("Spawn mode", "particle-spawn-mode", particles.spawnMode().ordinal(), SPAWN_MODES,
                value -> particles.spawnMode(SpawnMode.values()[value]), settingsChanged);

        UIControls.sectionHeading("Spawn particles");
        float pairWidth = Math.max(80.0f, (ImGui.getContentRegionAvailX() - 8.0f) * 0.5f);
        spawnButton("Add 1k", 1_000, pairWidth, particles, settingsChanged);
        ImGui.sameLine();
        spawnButton("Remove 1k", -1_000, pairWidth, particles, settingsChanged);
        spawnButton("Add 10k", 10_000, pairWidth, particles, settingsChanged);
        ImGui.sameLine();
        spawnButton("Remove 10k", -10_000, pairWidth, particles, settingsChanged);
        spawnButton("Add 100k", 100_000, pairWidth, particles, settingsChanged);
        ImGui.sameLine();
        spawnButton("Remove 100k", -100_000, pairWidth, particles, settingsChanged);

        customSpawnAmount.set(Math.max(0, customSpawnAmount.get()));
        ImGui.spacing();
        ImGui.textDisabled("Custom amount");
        ImGui.setNextItemWidth(Math.max(100.0f, ImGui.getContentRegionAvailX() - 72.0f));
        if (ImGui.inputInt("##custom-spawn-amount", customSpawnAmount, 100, 1_000)) {
            settingsChanged.run();
        }
        customSpawnAmount.set(Math.max(0, customSpawnAmount.get()));
        ImGui.sameLine();
        if (ImGui.button("Add##custom-spawn", 64.0f, 0.0f)) {
            particles.addParticles(customSpawnAmount.get());
            settingsChanged.run();
        }
        ImGui.spacing();
        if (ImGui.button("Clear particles##clear-particles")) {
            particles.clearParticles();
            settingsChanged.run();
        }
    }

    int customSpawnAmount() {
        return customSpawnAmount.get();
    }

    void setCustomSpawnAmount(int amount) {
        customSpawnAmount.set(Math.max(0, amount));
    }

    private void spawnButton(String label, int amount, float width, GpuParticleSystem particles,
            Runnable settingsChanged) {
        if (ImGui.button(label + "##spawn-" + amount, width, 32.0f)) {
            if (amount > 0) {
                particles.addParticles(amount);
            } else {
                particles.removeParticles(-amount);
            }
            settingsChanged.run();
        }
    }

    private void metricCard(String id, String label, String value, float width) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UIColors.SURFACE_DEFAULT.withAlpha(0.72f).vec4());
        int flags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        if (ImGui.beginChild("##metric-" + id, width, 64.0f, true, flags)) {
            ImGui.textDisabled(label);
            ImGui.pushFont(UIFonts.section());
            ImGui.textUnformatted(value);
            ImGui.popFont();
        }
        ImGui.endChild();
        ImGui.popStyleColor();
    }
}

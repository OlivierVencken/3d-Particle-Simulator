package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

final class ParticlesSection {
    private static final String[] SPAWN_MODES = UIControls.enumLabels(SpawnMode.values());

    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);

    void render(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        UIDesignTokens tokens = UITheme.tokens();
        float summaryWidth = ImGui.getContentRegionAvailX();
        float particleCardWidth = Math.max(
                tokens.primaryMetricMinimumWidth(), (summaryWidth - tokens.spaceMd()) * 0.62f);
        metricCard("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()),
                particleCardWidth, tokens);
        ImGui.sameLine();
        metricCard("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                Math.max(tokens.secondaryMetricMinimumWidth(),
                        summaryWidth - particleCardWidth - tokens.spaceMd()), tokens);

        ImGui.separatorText("");

        UIControls.sectionHeading("Population");
        ImInt groups = new ImInt(particles.groupCount());
        ImGui.textDisabled("Groups");
        ImGui.setNextItemWidth(-1.0f);
        if (ImGui.inputInt("##particle-groups", groups, 1, 2)) {
            actions.setGroupCount(groups.get());
        }
        UIControls.settingCombo("Spawn mode", "particle-spawn-mode", particles.spawnMode().ordinal(), SPAWN_MODES,
                value -> actions.setSpawnMode(SpawnMode.values()[value]));

        UIControls.sectionHeading("Spawn particles");
        float pairWidth = Math.max(tokens.pairedControlMinimumWidth(),
                (ImGui.getContentRegionAvailX() - tokens.spaceMd()) * 0.5f);
        spawnButton("Add 1k", 1_000, pairWidth, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 1k", -1_000, pairWidth, actions, tokens);
        spawnButton("Add 10k", 10_000, pairWidth, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 10k", -10_000, pairWidth, actions, tokens);
        spawnButton("Add 100k", 100_000, pairWidth, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 100k", -100_000, pairWidth, actions, tokens);

        customSpawnAmount.set(particles.customSpawnAmount());
        ImGui.spacing();
        ImGui.textDisabled("Custom amount");
        ImGui.setNextItemWidth(Math.max(tokens.inputMinimumWidth(),
                ImGui.getContentRegionAvailX() - tokens.buttonWidthSm() - tokens.spaceMd()));
        if (ImGui.inputInt("##custom-spawn-amount", customSpawnAmount, 100, 1_000)) {
            actions.setCustomSpawnAmount(customSpawnAmount.get());
        }
        customSpawnAmount.set(Math.max(0, customSpawnAmount.get()));
        ImGui.sameLine();
        if (ImGui.button("Add##custom-spawn", tokens.buttonWidthSm(), tokens.controlHeight())) {
            actions.add(customSpawnAmount.get());
        }
        ImGui.spacing();
        if (ImGui.button("Clear particles##clear-particles")) {
            actions.clear();
        }
    }

    int customSpawnAmount() {
        return customSpawnAmount.get();
    }

    void setCustomSpawnAmount(int amount) {
        customSpawnAmount.set(Math.max(0, amount));
    }

    private void spawnButton(String label, int amount, float width, SimulationUiActions.Particles actions,
            UIDesignTokens tokens) {
        if (ImGui.button(label + "##spawn-" + amount, width, tokens.controlHeight())) {
            if (amount > 0) {
                actions.add(amount);
            } else {
                actions.remove(-amount);
            }
        }
    }

    private void metricCard(String id, String label, String value, float width, UIDesignTokens tokens) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UIColors.SURFACE_DEFAULT.withAlpha(0.72f).vec4());
        int flags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        if (ImGui.beginChild("##metric-" + id, width, tokens.metricCardHeight(), true, flags)) {
            ImGui.textDisabled(label);
            ImGui.pushFont(UIFonts.section());
            ImGui.textUnformatted(value);
            ImGui.popFont();
        }
        ImGui.endChild();
        ImGui.popStyleColor();
    }
}

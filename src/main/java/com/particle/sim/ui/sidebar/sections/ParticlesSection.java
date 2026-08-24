package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIIntegerInput;
import com.particle.sim.ui.components.UIMetric;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.type.ImInt;

final class ParticlesSection {
    private static final String[] SPAWN_MODES = UIControls.enumLabels(SpawnMode.values());

    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);
    private final ImInt groupCount = new ImInt(SimulationDefaults.GROUP_COUNT);
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        UIDesignTokens tokens = UITheme.tokens();
        float summaryWidth = ImGui.getContentRegionAvailX();
        float particleCardWidth = Math.max(
                tokens.primaryMetricMinimumWidth(), (summaryWidth - tokens.spaceMd()) * 0.62f);
        UIMetric.card("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()), particleCardWidth);
        ImGui.sameLine();
        UIMetric.card("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                Math.max(tokens.secondaryMetricMinimumWidth(), summaryWidth - particleCardWidth - tokens.spaceMd()));

        UIText.divider();

        UIControls.sectionHeading("Population");
        groupCount.set(particles.groupCount());
        if (UIIntegerInput.render("Groups", "particle-groups", groupCount, 1, 2,
                1, SimulationDefaults.MAX_GROUP_COUNT, -1.0f)) {
            actions.setGroupCount(groupCount.get());
        }
        controls.settingCombo("Spawn mode", "particle-spawn-mode", particles.spawnMode().ordinal(), SPAWN_MODES,
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
        float inputWidth = Math.max(tokens.inputMinimumWidth(),
                ImGui.getContentRegionAvailX() - tokens.buttonWidthSm() - tokens.spaceMd());
        if (UIIntegerInput.render("Custom amount", "custom-spawn-amount", customSpawnAmount,
                100, 1_000, 0, SimulationDefaults.MAX_PARTICLE_COUNT, inputWidth)) {
            actions.setCustomSpawnAmount(customSpawnAmount.get());
        }
        ImGui.sameLine();
        if (UIButton.text("Add", "custom-spawn", UIComponentVariant.PRIMARY,
                tokens.buttonWidthSm(), tokens.controlHeight())) {
            actions.add(customSpawnAmount.get());
        }
        ImGui.spacing();
        if (UIButton.text("Clear particles", "clear-particles", UIComponentVariant.DESTRUCTIVE)) {
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
        if (UIButton.text(label, "spawn-" + amount,
                amount > 0 ? UIComponentVariant.SECONDARY : UIComponentVariant.GHOST,
                width, tokens.controlHeight())) {
            if (amount > 0) {
                actions.add(amount);
            } else {
                actions.remove(-amount);
            }
        }
    }

}

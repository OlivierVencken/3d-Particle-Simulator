package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.components.Controls;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.IntegerInput;
import com.particle.sim.ui.components.Metric;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.type.ImInt;

final class ParticlesSection {
    private static final String[] SPAWN_MODES = Controls.enumLabels(SpawnMode.values());

    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);
    private final ImInt groupCount = new ImInt(SimulationDefaults.GROUP_COUNT);
    private final Controls controls = new Controls();
    private final ClearParticlesPopup clearParticlesPopup = new ClearParticlesPopup();

    void render(SimulationViewModel.Particles particles, SimulationViewActions.Particles actions) {
        DesignTokens tokens = Theme.tokens();
        float summaryWidth = ImGui.getContentRegionAvailX();
        if (metricsFitSideBySide(summaryWidth, tokens)) {
            float particleCardWidth = Math.max(
                    tokens.primaryMetricMinimumWidth(), (summaryWidth - tokens.spaceMd()) * 0.62f);
            Metric.card("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()),
                    particleCardWidth);
            ImGui.sameLine();
            Metric.card("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                    Math.max(tokens.secondaryMetricMinimumWidth(),
                            summaryWidth - particleCardWidth - tokens.spaceMd()));
        } else {
            Metric.card("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()), summaryWidth);
            Metric.card("group-count", "GROUPS", Integer.toString(particles.groupCount()), summaryWidth);
        }
        Metric.row("Capacity", "%,d / %,d".formatted(
                particles.particleCount(), particles.maximumParticleCount()));

        Text.divider();

        if (particles.particleCount() == 0) {
            Text.emptyState("No particles are active. Choose a spawn mode and add a population below.");
        }

        Controls.sectionHeading("Population");
        groupCount.set(particles.groupCount());
        if (IntegerInput.render("Groups", "particle-groups", groupCount, 1, 2,
                1, particles.maximumGroupCount(), -1.0f)) {
            actions.setGroupCount(groupCount.get());
        }
        controls.settingCombo("Spawn mode", "particle-spawn-mode", particles.spawnMode().ordinal(), SPAWN_MODES,
                value -> actions.setSpawnMode(SpawnMode.values()[value]));

        Controls.sectionHeading("Spawn particles");
        float pairWidth = Math.max(tokens.pairedControlMinimumWidth(),
                (ImGui.getContentRegionAvailX() - tokens.spaceMd()) * 0.5f);
        spawnButton("Add 1k", 1_000, pairWidth, particles, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 1k", -1_000, pairWidth, particles, actions, tokens);
        spawnButton("Add 10k", 10_000, pairWidth, particles, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 10k", -10_000, pairWidth, particles, actions, tokens);
        spawnButton("Add 100k", 100_000, pairWidth, particles, actions, tokens);
        ImGui.sameLine();
        spawnButton("Remove 100k", -100_000, pairWidth, particles, actions, tokens);

        customSpawnAmount.set(particles.customSpawnAmount());
        ImGui.spacing();
        float customRowWidth = ImGui.getContentRegionAvailX();
        boolean inlineCustomAction = customControlsFitInline(customRowWidth, tokens);
        float inputWidth = inlineCustomAction
                ? customRowWidth - tokens.buttonWidthSm() - tokens.spaceMd()
                : -1.0f;
        if (IntegerInput.render("Custom amount", "custom-spawn-amount", customSpawnAmount,
                100, 1_000, 0, particles.maximumParticleCount(), inputWidth)) {
            actions.setCustomSpawnAmount(customSpawnAmount.get());
        }
        if (inlineCustomAction) {
            ImGui.sameLine();
        }
        if (Button.text("Add", "custom-spawn", ComponentVariant.PRIMARY,
                inlineCustomAction ? tokens.buttonWidthSm() : 0.0f, tokens.controlHeight(),
                remainingCapacity(particles) > 0
                        && customSpawnAmount.get() > 0)) {
            actions.add(clampedAddition(customSpawnAmount.get(), particles));
        }
        renderCapacityStatus(particles, customSpawnAmount.get());
        ImGui.spacing();
        if (Button.text("Clear particles", "clear-particles", ComponentVariant.DESTRUCTIVE,
                0.0f, tokens.controlHeight(), particles.particleCount() > 0)) {
            clearParticlesPopup.open(particles.particleCount());
        }
    }

    void renderPopups(SimulationViewActions.Particles actions) {
        clearParticlesPopup.render(actions);
    }

    boolean hasOpenModal() {
        return clearParticlesPopup.isOpen();
    }

    int customSpawnAmount() {
        return customSpawnAmount.get();
    }

    void setCustomSpawnAmount(int amount) {
        customSpawnAmount.set(Math.max(0, amount));
    }

    private void spawnButton(String label, int amount, float width, SimulationViewModel.Particles particles,
            SimulationViewActions.Particles actions, DesignTokens tokens) {
        boolean enabled = amount > 0 ? remainingCapacity(particles) > 0 : particles.particleCount() > 0;
        if (Button.text(label, "spawn-" + amount,
                amount > 0 ? ComponentVariant.SECONDARY : ComponentVariant.GHOST,
                width, tokens.controlHeight(), enabled)) {
            if (amount > 0) {
                actions.add(clampedAddition(amount, particles));
            } else {
                actions.remove(Math.min(-amount, particles.particleCount()));
            }
        }
    }

    static int remainingCapacity(SimulationViewModel.Particles particles) {
        return Math.max(0, particles.maximumParticleCount() - particles.particleCount());
    }

    static boolean metricsFitSideBySide(float availableWidth, DesignTokens tokens) {
        return availableWidth >= tokens.primaryMetricMinimumWidth()
                + tokens.secondaryMetricMinimumWidth() + tokens.spaceMd();
    }

    static boolean customControlsFitInline(float availableWidth, DesignTokens tokens) {
        return availableWidth >= tokens.inputMinimumWidth() + tokens.buttonWidthSm() + tokens.spaceMd();
    }

    static int clampedAddition(int requested, SimulationViewModel.Particles particles) {
        return Math.max(0, Math.min(requested, remainingCapacity(particles)));
    }

    private static void renderCapacityStatus(SimulationViewModel.Particles particles, int requested) {
        int remaining = remainingCapacity(particles);
        if (remaining <= 0) {
            Text.warning("Particle capacity reached. Remove particles before adding more.");
        } else if (requested > remaining) {
            Text.helper("Only %,d spaces remain; the next add will be clamped.".formatted(remaining));
        } else {
            Text.helper("%,d particle spaces available".formatted(remaining));
        }
    }

}

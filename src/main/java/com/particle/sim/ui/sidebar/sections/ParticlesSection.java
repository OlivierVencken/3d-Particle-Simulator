package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SpawnMode;
import com.particle.sim.particles.SimulationDimension;
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
    private final ImInt customSpawnAmount = new ImInt(SimulationDefaults.CUSTOM_SPAWN_AMOUNT);
    private final ImInt groupCount = new ImInt(SimulationDefaults.GROUP_COUNT);
    private final UIControls controls = new UIControls();
    private final ClearParticlesPopup clearParticlesPopup = new ClearParticlesPopup();

    void render(SimulationUiModel.Simulation simulation, SimulationUiModel.Particles particles,
            SimulationUiActions.Particles actions) {
        UIDesignTokens tokens = UITheme.tokens();
        float summaryWidth = ImGui.getContentRegionAvailX();
        if (metricsFitSideBySide(summaryWidth, tokens)) {
            float particleCardWidth = Math.max(
                    tokens.primaryMetricMinimumWidth(), (summaryWidth - tokens.spaceMd()) * 0.62f);
            UIMetric.card("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()),
                    particleCardWidth);
            ImGui.sameLine();
            UIMetric.card("group-count", "GROUPS", Integer.toString(particles.groupCount()),
                    Math.max(tokens.secondaryMetricMinimumWidth(),
                            summaryWidth - particleCardWidth - tokens.spaceMd()));
        } else {
            UIMetric.card("particle-count", "PARTICLES", "%,d".formatted(particles.particleCount()), summaryWidth);
            UIMetric.card("group-count", "GROUPS", Integer.toString(particles.groupCount()), summaryWidth);
        }
        UIMetric.row("Capacity", "%,d / %,d".formatted(
                particles.particleCount(), particles.maximumParticleCount()));

        UIText.divider();

        if (particles.particleCount() == 0) {
            UIText.emptyState("No particles are active. Choose a spawn mode and add a population below.");
        }

        UIControls.sectionHeading("Population");
        groupCount.set(particles.groupCount());
        if (UIIntegerInput.render("Groups", "particle-groups", groupCount, 1, 2,
                1, particles.maximumGroupCount(), -1.0f)) {
            actions.setGroupCount(groupCount.get());
        }
        SpawnMode[] spawnModes = supportedSpawnModes(simulation.simulationDimension());
        controls.settingCombo("Spawn mode", "particle-spawn-mode", indexOf(spawnModes, particles.spawnMode()),
                UIControls.enumLabels(spawnModes), value -> actions.setSpawnMode(spawnModes[value]));
        if (simulation.simulationDimension() == SimulationDimension.FOUR_D) {
            UIText.helper("Disc and Spiral are unavailable in 4D.");
        }

        UIControls.sectionHeading("Spawn particles");
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
        if (UIIntegerInput.render("Custom amount", "custom-spawn-amount", customSpawnAmount,
                100, 1_000, 0, particles.maximumParticleCount(), inputWidth)) {
            actions.setCustomSpawnAmount(customSpawnAmount.get());
        }
        if (inlineCustomAction) {
            ImGui.sameLine();
        }
        if (UIButton.text("Add", "custom-spawn", UIComponentVariant.PRIMARY,
                inlineCustomAction ? tokens.buttonWidthSm() : 0.0f, tokens.controlHeight(),
                remainingCapacity(particles) > 0
                        && customSpawnAmount.get() > 0)) {
            actions.add(clampedAddition(customSpawnAmount.get(), particles));
        }
        renderCapacityStatus(particles, customSpawnAmount.get());
        ImGui.spacing();
        if (UIButton.text("Clear particles", "clear-particles", UIComponentVariant.DESTRUCTIVE,
                0.0f, tokens.controlHeight(), particles.particleCount() > 0)) {
            clearParticlesPopup.open(particles.particleCount());
        }
    }

    void renderPopups(SimulationUiActions.Particles actions) {
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

    private void spawnButton(String label, int amount, float width, SimulationUiModel.Particles particles,
            SimulationUiActions.Particles actions, UIDesignTokens tokens) {
        boolean enabled = amount > 0 ? remainingCapacity(particles) > 0 : particles.particleCount() > 0;
        if (UIButton.text(label, "spawn-" + amount,
                amount > 0 ? UIComponentVariant.SECONDARY : UIComponentVariant.GHOST,
                width, tokens.controlHeight(), enabled)) {
            if (amount > 0) {
                actions.add(clampedAddition(amount, particles));
            } else {
                actions.remove(Math.min(-amount, particles.particleCount()));
            }
        }
    }

    static int remainingCapacity(SimulationUiModel.Particles particles) {
        return Math.max(0, particles.maximumParticleCount() - particles.particleCount());
    }

    static boolean metricsFitSideBySide(float availableWidth, UIDesignTokens tokens) {
        return availableWidth >= tokens.primaryMetricMinimumWidth()
                + tokens.secondaryMetricMinimumWidth() + tokens.spaceMd();
    }

    static boolean customControlsFitInline(float availableWidth, UIDesignTokens tokens) {
        return availableWidth >= tokens.inputMinimumWidth() + tokens.buttonWidthSm() + tokens.spaceMd();
    }

    static int clampedAddition(int requested, SimulationUiModel.Particles particles) {
        return Math.max(0, Math.min(requested, remainingCapacity(particles)));
    }

    static SpawnMode[] supportedSpawnModes(SimulationDimension dimension) {
        return java.util.Arrays.stream(SpawnMode.values())
                .filter(mode -> mode.supportedIn(dimension))
                .toArray(SpawnMode[]::new);
    }

    private static int indexOf(SpawnMode[] modes, SpawnMode selected) {
        for (int index = 0; index < modes.length; index++) {
            if (modes[index] == selected) {
                return index;
            }
        }
        return 0;
    }

    private static void renderCapacityStatus(SimulationUiModel.Particles particles, int requested) {
        int remaining = remainingCapacity(particles);
        if (remaining <= 0) {
            UIText.warning("Particle capacity reached. Remove particles before adding more.");
        } else if (requested > remaining) {
            UIText.helper("Only %,d spaces remain; the next add will be clamped.".formatted(remaining));
        } else {
            UIText.helper("%,d particle spaces available".formatted(remaining));
        }
    }

}

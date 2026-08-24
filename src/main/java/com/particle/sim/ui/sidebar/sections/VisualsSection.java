package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class VisualsSection {
    private static final String[] COLOR_MODES = UIControls.enumLabels(ColorMode.values());

    void render(SimulationUiModel.Visuals visuals, SimulationUiActions.Visuals actions) {
        UIControls.sectionHeading("Particle rendering");
        UIControls.settingSlider("Particle size", "visuals-size", visuals.pointSize(), 1.0f, 8.0f, 1,
                actions::setPointSize);
        UIControls.settingCheckbox("Fixed screen size", "visuals-fixed-size", visuals.fixedParticleScreenSize(),
                actions::setFixedParticleScreenSize);
        UIControls.settingCombo("Color mode", "visuals-color", visuals.colorMode().ordinal(), COLOR_MODES,
                value -> actions.setColorMode(ColorMode.values()[value]));

        ImGui.separatorText("");

        effectHeader("Glow", EffectMode.GLOW, visuals, actions);
        if (visuals.effectEnabled(EffectMode.GLOW)) {
            UIControls.settingIntSlider("Passes", "glow-passes", visuals.glowBlurPasses(), 1, 64, 0,
                    actions::setGlowBlurPasses);
            UIControls.settingSlider("Strength", "glow-strength", visuals.glowStrength(), 0.0f, 6.0f, 1,
                    actions::setGlowStrength);
            UIControls.settingSlider("Radius", "glow-radius", visuals.glowRadius(), 0.5f, 12.0f, 1,
                    actions::setGlowRadius);
            UIControls.settingSlider("Falloff", "glow-falloff", visuals.glowFalloff(), 0.05f, 3.0f, 2,
                    actions::setGlowFalloff);
            ImGui.textDisabled("Bloom resolution: 1/%d per axis".formatted(visuals.effectiveBloomDivisor()));
        }

        ImGui.separatorText("");

        effectHeader("Trails", EffectMode.TRAILS, visuals, actions);
        if (visuals.effectEnabled(EffectMode.TRAILS)) {
            UIControls.settingIntSlider("Trail length", "trail-length", visuals.trailLength(),
                    SimulationDefaults.MIN_TRAIL_LENGTH, SimulationDefaults.MAX_TRAIL_LENGTH, 0,
                    actions::setTrailLength);
            UIControls.settingSlider("Thickness", "trail-thickness", visuals.trailThickness(),
                    SimulationDefaults.MIN_TRAIL_THICKNESS, visuals.pointSize(), 1, actions::setTrailThickness);
            String quality = qualityMessage(visuals);
            if (!quality.isEmpty()) {
                ImGui.textDisabled(quality);
            }
        }
    }

    private void effectHeader(String label, EffectMode mode, SimulationUiModel.Visuals visuals,
            SimulationUiActions.Visuals actions) {
        UIControls.sectionHeading(label);
        UIControls.settingCheckbox("Enabled", "effect-" + mode.name(), visuals.effectEnabled(mode),
                value -> actions.setEffectEnabled(mode, value));
    }

    private static String qualityMessage(SimulationUiModel.Visuals visuals) {
        if (visuals.effectiveTrailParticleStride() > 1) {
            return "Adaptive quality: trails sample 1/%d particles"
                    .formatted(visuals.effectiveTrailParticleStride());
        }
        if (visuals.effectiveTrailLength() > 0 && visuals.effectiveTrailLength() < visuals.trailLength()) {
            return "Adaptive quality: trail length reduced to %d".formatted(visuals.effectiveTrailLength());
        }
        if (visuals.effectiveBloomDivisor() > 1) {
            return "Adaptive quality: bloom rendered at 1/%d resolution"
                    .formatted(visuals.effectiveBloomDivisor());
        }
        return "";
    }
}

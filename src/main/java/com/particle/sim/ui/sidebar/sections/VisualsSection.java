package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIText;

final class VisualsSection {
    private static final String[] COLOR_MODES = UIControls.enumLabels(ColorMode.values());
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Visuals visuals, SimulationUiActions.Visuals actions) {
        UIControls.sectionHeading("Particle rendering");
        controls.settingSlider("Particle size", "visuals-size", visuals.pointSize(), 1.0f, 8.0f, 1,
                actions::setPointSize);
        controls.settingCheckbox("Fixed screen size", "visuals-fixed-size", visuals.fixedParticleScreenSize(),
                actions::setFixedParticleScreenSize);
        controls.settingCombo("Color mode", "visuals-color", visuals.colorMode().ordinal(), COLOR_MODES,
                value -> actions.setColorMode(ColorMode.values()[value]));

        UIText.divider();

        effectHeader("Glow", EffectMode.GLOW, visuals, actions);
        if (visuals.effectEnabled(EffectMode.GLOW)) {
            controls.settingIntSlider("Passes", "glow-passes", visuals.glowBlurPasses(), 1, 64, 0,
                    actions::setGlowBlurPasses);
            controls.settingSlider("Strength", "glow-strength", visuals.glowStrength(), 0.0f, 6.0f, 1,
                    actions::setGlowStrength);
            controls.settingSlider("Radius", "glow-radius", visuals.glowRadius(), 0.5f, 12.0f, 1,
                    actions::setGlowRadius);
            controls.settingSlider("Falloff", "glow-falloff", visuals.glowFalloff(), 0.05f, 3.0f, 2,
                    actions::setGlowFalloff);
            UIText.helper("Bloom resolution: 1/%d per axis".formatted(visuals.effectiveBloomDivisor()));
        }

        UIText.divider();

        effectHeader("Trails", EffectMode.TRAILS, visuals, actions);
        if (visuals.effectEnabled(EffectMode.TRAILS)) {
            controls.settingIntSlider("Trail length", "trail-length", visuals.trailLength(),
                    SimulationDefaults.MIN_TRAIL_LENGTH, SimulationDefaults.MAX_TRAIL_LENGTH, 0,
                    actions::setTrailLength);
            controls.settingSlider("Thickness", "trail-thickness", visuals.trailThickness(),
                    SimulationDefaults.MIN_TRAIL_THICKNESS, visuals.pointSize(), 1, actions::setTrailThickness);
        }

        String quality = qualityMessage(visuals);
        if (!quality.isEmpty()) {
            UIText.divider();
            UIControls.sectionHeading("Adaptive quality");
            UIText.helper(quality);
        }
    }

    private void effectHeader(String label, EffectMode mode, SimulationUiModel.Visuals visuals,
            SimulationUiActions.Visuals actions) {
        UIControls.sectionHeading(label);
        controls.settingCheckbox("Enabled", "effect-" + mode.name(), visuals.effectEnabled(mode),
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
        if (visuals.effectEnabled(EffectMode.GLOW) && visuals.effectiveBloomDivisor() > 1) {
            return "Adaptive quality: bloom rendered at 1/%d resolution"
                    .formatted(visuals.effectiveBloomDivisor());
        }
        return "";
    }
}

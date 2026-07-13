package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class VisualsSection {
    private static final String[] COLOR_MODES = UIControls.enumLabels(ColorMode.values());

    void render(GpuParticleSystem particles, Runnable settingsChanged) {
        UIControls.sectionHeading("Particle rendering");
        UIControls.settingSlider("Particle size", "visuals-size", particles.pointSize(), 1.0f, 8.0f, 1,
                particles::pointSize, settingsChanged);
        UIControls.settingCheckbox("Fixed screen size", "visuals-fixed-size", particles.fixedParticleScreenSize(),
                particles::fixedParticleScreenSize, settingsChanged);
        UIControls.settingCombo("Color mode", "visuals-color", particles.colorMode().ordinal(), COLOR_MODES,
                value -> particles.colorMode(ColorMode.values()[value]), settingsChanged);

        ImGui.separatorText("");

        effectHeader("Glow", EffectMode.GLOW, particles, settingsChanged);
        if (particles.effectEnabled(EffectMode.GLOW)) {
            UIControls.settingIntSlider("Passes", "glow-passes", particles.glowBlurPasses(), 1, 64, 0,
                    particles::glowBlurPasses, settingsChanged);
            UIControls.settingSlider("Strength", "glow-strength", particles.glowStrength(), 0.0f, 6.0f, 1,
                    particles::glowStrength, settingsChanged);
            UIControls.settingSlider("Radius", "glow-radius", particles.glowRadius(), 0.5f, 12.0f, 1,
                    particles::glowRadius, settingsChanged);
            UIControls.settingSlider("Falloff", "glow-falloff", particles.glowFalloff(), 0.05f, 3.0f, 2,
                    particles::glowFalloff, settingsChanged);
            ImGui.textDisabled("Bloom resolution: 1/%d per axis".formatted(particles.effectiveBloomDivisor()));
        }

        ImGui.separatorText("");

        effectHeader("Trails", EffectMode.TRAILS, particles, settingsChanged);
        if (particles.effectEnabled(EffectMode.TRAILS)) {
            UIControls.settingIntSlider("Trail length", "trail-length", particles.trailLength(),
                    SimulationDefaults.MIN_TRAIL_LENGTH, SimulationDefaults.MAX_TRAIL_LENGTH, 0,
                    particles::trailLength, settingsChanged);
            UIControls.settingSlider("Thickness", "trail-thickness", particles.trailThickness(),
                    SimulationDefaults.MIN_TRAIL_THICKNESS, particles.pointSize(), 1, particles::trailThickness,
                    settingsChanged);
            String quality = qualityMessage(particles);
            if (!quality.isEmpty()) {
                ImGui.textDisabled(quality);
            }
        }
    }

    private void effectHeader(String label, EffectMode mode, GpuParticleSystem particles, Runnable settingsChanged) {
        UIControls.sectionHeading(label);
        UIControls.settingCheckbox("Enabled", "effect-" + mode.name(), particles.effectEnabled(mode),
                value -> particles.effectEnabled(mode, value), settingsChanged);
    }

    private static String qualityMessage(GpuParticleSystem particles) {
        if (particles.effectiveTrailParticleStride() > 1) {
            return "Adaptive quality: trails sample 1/%d particles"
                    .formatted(particles.effectiveTrailParticleStride());
        }
        if (particles.effectiveTrailLength() > 0 && particles.effectiveTrailLength() < particles.trailLength()) {
            return "Adaptive quality: trail length reduced to %d".formatted(particles.effectiveTrailLength());
        }
        if (particles.effectiveBloomDivisor() > 1) {
            return "Adaptive quality: bloom rendered at 1/%d resolution"
                    .formatted(particles.effectiveBloomDivisor());
        }
        return "";
    }
}

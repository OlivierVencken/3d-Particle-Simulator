package com.particle.sim.app;

import com.particle.sim.graphics.RgbaColor;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;

final class VisualControls implements SimulationViewModel.Visuals, SimulationViewActions.Visuals {
    private final ParticleSystem particles;
    private final SettingsChangeHandler changes;

    VisualControls(ParticleSystem particles, SettingsChangeHandler changes) {
        this.particles = particles;
        this.changes = changes;
    }

    @Override
    public float pointSize() {
        return particles.pointSize();
    }

    @Override
    public boolean fixedParticleScreenSize() {
        return particles.fixedParticleScreenSize();
    }

    @Override
    public ColorMode colorMode() {
        return particles.colorMode();
    }

    @Override
    public boolean effectEnabled(EffectMode effectMode) {
        return particles.effectEnabled(effectMode);
    }

    @Override
    public int glowBlurPasses() {
        return particles.glowBlurPasses();
    }

    @Override
    public float glowStrength() {
        return particles.glowStrength();
    }

    @Override
    public float glowRadius() {
        return particles.glowRadius();
    }

    @Override
    public float glowFalloff() {
        return particles.glowFalloff();
    }

    @Override
    public int effectiveBloomDivisor() {
        return particles.effectiveBloomDivisor();
    }

    @Override
    public int trailLength() {
        return particles.trailLength();
    }

    @Override
    public float trailThickness() {
        return particles.trailThickness();
    }

    @Override
    public int effectiveTrailLength() {
        return particles.effectiveTrailLength();
    }

    @Override
    public int effectiveTrailParticleStride() {
        return particles.effectiveTrailParticleStride();
    }

    @Override
    public void setPointSize(float value) {
        changes.apply(() -> particles.pointSize(value));
    }

    @Override
    public void setFixedParticleScreenSize(boolean value) {
        changes.apply(() -> particles.fixedParticleScreenSize(value));
    }

    @Override
    public void setColorMode(ColorMode value) {
        changes.apply(() -> particles.colorMode(value));
    }

    @Override
    public void setGroupColor(int group, RgbaColor color) {
        changes.apply(() -> particles.groupColor(group, color));
    }

    @Override
    public void setEffectEnabled(EffectMode effectMode, boolean enabled) {
        changes.apply(() -> particles.effectEnabled(effectMode, enabled));
    }

    @Override
    public void setGlowBlurPasses(int value) {
        changes.apply(() -> particles.glowBlurPasses(value));
    }

    @Override
    public void setGlowStrength(float value) {
        changes.apply(() -> particles.glowStrength(value));
    }

    @Override
    public void setGlowRadius(float value) {
        changes.apply(() -> particles.glowRadius(value));
    }

    @Override
    public void setGlowFalloff(float value) {
        changes.apply(() -> particles.glowFalloff(value));
    }

    @Override
    public void setTrailLength(int value) {
        changes.apply(() -> particles.trailLength(value));
    }

    @Override
    public void setTrailThickness(float value) {
        changes.apply(() -> particles.trailThickness(value));
    }
}

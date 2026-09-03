package com.particle.sim.particles.rendering;

import com.particle.sim.particles.ParticleSimulationConfig;
import com.particle.sim.particles.gpu.ParticleBuffers;
import com.particle.sim.particles.gpu.SpatialGridBuffers;
import com.particle.sim.particles.gpu.SpatialGridSizing;
import com.particle.sim.particles.gpu.TrailHistoryBuffers;
import com.particle.sim.ui.FramebufferViewport;
import java.util.Objects;

/** Inputs shared by every rendering pass for one frame. */
public record RenderFrame(
        FramebufferViewport viewport,
        float[] viewMatrix,
        ParticleBuffers particleBuffers,
        SpatialGridBuffers spatialGridBuffers,
        TrailHistoryBuffers trailHistoryBuffers,
        int particleCount,
        ParticleSimulationConfig config) {
    public RenderFrame {
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(viewMatrix, "viewMatrix");
        Objects.requireNonNull(particleBuffers, "particleBuffers");
        Objects.requireNonNull(spatialGridBuffers, "spatialGridBuffers");
        Objects.requireNonNull(trailHistoryBuffers, "trailHistoryBuffers");
        Objects.requireNonNull(config, "config");
    }

    float pointSize() {
        return config.pointSize();
    }

    boolean fixedParticleScreenSize() {
        return config.fixedParticleScreenSize();
    }

    boolean glowEnabled() {
        return config.effectEnabled(EffectMode.GLOW);
    }

    boolean trailsEnabled() {
        return config.effectEnabled(EffectMode.TRAILS);
    }

    int colorMode() {
        return config.colorMode().ordinal();
    }

    int groupCount() {
        return config.groupCount();
    }

    float[] groupColorRgbComponents() {
        return config.groupColorRgbComponents();
    }

    float maximumVelocity() {
        return config.maxVelocity();
    }

    float bounds() {
        return config.bounds();
    }

    float interactionRange() {
        return config.interactionRange();
    }

    GlowSettings glowSettings() {
        return config.glowSettings();
    }

    TrailSettings trailSettings() {
        return config.trailSettings();
    }

    int gridSize() {
        return SpatialGridSizing.gridSize(bounds(), interactionRange());
    }
}

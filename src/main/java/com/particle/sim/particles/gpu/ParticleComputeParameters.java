package com.particle.sim.particles.gpu;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.ParticleSimulationConfig;
import java.util.Objects;

/** Immutable simulation inputs consumed by one compute update. */
public record ParticleComputeParameters(
        int particleCount,
        int groupCount,
        float forceFactor,
        float velocityDamping,
        float interactionRange,
        float repulsionRadius,
        float maximumVelocity,
        float boundaryBounce,
        float bounds,
        boolean densityRegulationEnabled,
        float densityLimit,
        DistanceMetric distanceMetric,
        int gridSize,
        int gridCellCount,
        boolean toroidalWrap,
        float[] attractionMatrix) {
    public ParticleComputeParameters {
        Objects.requireNonNull(distanceMetric, "distanceMetric");
        Objects.requireNonNull(attractionMatrix, "attractionMatrix");
        attractionMatrix = attractionMatrix.clone();
    }

    @Override
    public float[] attractionMatrix() {
        return attractionMatrix.clone();
    }

    public static ParticleComputeParameters from(
            ParticleSimulationConfig config, float[] attractionMatrix) {
        int gridSize = SpatialGridSizing.gridSize(config.bounds(), config.interactionRange());
        return new ParticleComputeParameters(
                config.particleCount(),
                config.groupCount(),
                config.forceFactor(),
                config.velocityDamping(),
                config.interactionRange(),
                config.repulsionRadius(),
                config.maxVelocity(),
                config.boundaryBounce(),
                config.bounds(),
                config.densityRegulationEnabled(),
                config.densityLimit(),
                config.distanceMetric(),
                gridSize,
                SpatialGridSizing.gridCellCount(config.bounds(), config.interactionRange()),
                config.toroidalWrap(),
                attractionMatrix);
    }
}

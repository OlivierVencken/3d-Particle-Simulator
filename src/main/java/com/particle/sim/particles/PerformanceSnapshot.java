package com.particle.sim.particles;

public record PerformanceSnapshot(
        double gridCountMilliseconds,
        double gridScanMilliseconds,
        double gridScatterMilliseconds,
        double integrationMilliseconds,
        double simulationMilliseconds,
        long allocatedGpuBytes,
        int particleCount,
        int maximumParticleCount,
        int gridCellCount) {
}

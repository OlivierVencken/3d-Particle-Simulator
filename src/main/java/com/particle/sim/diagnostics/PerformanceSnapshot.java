package com.particle.sim.diagnostics;

public record PerformanceSnapshot(
        double gridCountMilliseconds,
        double gridScanMilliseconds,
        double gridScatterMilliseconds,
        double integrationMilliseconds,
        double simulationMilliseconds,
        double particleRenderMilliseconds,
        double trailRenderMilliseconds,
        double bloomMilliseconds,
        long allocatedGpuBytes,
        int particleCount,
        int maximumParticleCount,
        int gridCellCount) {}

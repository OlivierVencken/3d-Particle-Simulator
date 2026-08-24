package com.particle.sim.ui;

/** One per-frame diagnostic value shared by every UI consumer. */
public record SimulationUiDiagnostics(
        int particleCount,
        int maximumParticleCount,
        int gridSize,
        int gridCellCount,
        double gridCountMilliseconds,
        double gridScanMilliseconds,
        double gridScatterMilliseconds,
        double integrationMilliseconds,
        double simulationMilliseconds,
        double particleRenderMilliseconds,
        double trailRenderMilliseconds,
        double bloomMilliseconds,
        long allocatedGpuBytes) {
    public static SimulationUiDiagnostics unavailable() {
        return new SimulationUiDiagnostics(
                0, 0, 0, 0,
                -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 0L);
    }
}

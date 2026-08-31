package com.particle.sim.ui;

import com.particle.sim.particles.FourDVisualizationMode;
import com.particle.sim.particles.SimulationDimension;

/** One per-frame diagnostic value shared by every UI consumer. */
public record SimulationUiDiagnostics(
        int particleCount,
        int maximumParticleCount,
        int gridSize,
        int gridCellCount,
        SimulationDimension simulationDimension,
        FourDVisualizationMode fourDVisualizationMode,
        double fourDXwAutoSpeedDegrees,
        double fourDYwAutoSpeedDegrees,
        double fourDZwAutoSpeedDegrees,
        double fourDPerspectiveDistance,
        double fourDSliceCenterW,
        double fourDSliceThickness,
        double fourDColorRange,
        double gridCountMilliseconds,
        double gridScanMilliseconds,
        double gridScatterMilliseconds,
        double integrationMilliseconds,
        double simulationMilliseconds,
        double particleRenderMilliseconds,
        double trailRenderMilliseconds,
        double bloomMilliseconds,
        long allocatedGpuBytes,
        long groupBufferBytes) {
    public static SimulationUiDiagnostics unavailable() {
        return new SimulationUiDiagnostics(
                0, 0, 0, 0, SimulationDimension.THREE_D, FourDVisualizationMode.PERSPECTIVE,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 0L, 0L);
    }
}

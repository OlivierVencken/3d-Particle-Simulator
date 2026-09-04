package com.particle.sim.app;

import com.particle.sim.diagnostics.PerformanceSnapshot;
import com.particle.sim.ui.SimulationViewDiagnostics;
import com.particle.sim.ui.SimulationViewModel;

final class PerformanceView implements SimulationViewModel.Performance {
    private SimulationViewDiagnostics diagnostics = SimulationViewDiagnostics.unavailable();

    void update(PerformanceSnapshot snapshot, int gridSize) {
        diagnostics =
                new SimulationViewDiagnostics(
                        snapshot.particleCount(),
                        snapshot.maximumParticleCount(),
                        gridSize,
                        snapshot.gridCellCount(),
                        snapshot.gridCountMilliseconds(),
                        snapshot.gridScanMilliseconds(),
                        snapshot.gridScatterMilliseconds(),
                        snapshot.integrationMilliseconds(),
                        snapshot.simulationMilliseconds(),
                        snapshot.particleRenderMilliseconds(),
                        snapshot.trailRenderMilliseconds(),
                        snapshot.bloomMilliseconds(),
                        snapshot.allocatedGpuBytes());
    }

    @Override
    public SimulationViewDiagnostics diagnostics() {
        return diagnostics;
    }
}

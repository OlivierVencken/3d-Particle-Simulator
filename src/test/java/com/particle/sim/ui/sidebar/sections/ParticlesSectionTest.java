package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.ui.testing.FakeSimulationUiModel;
import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ParticlesSectionTest {
    @Test
    void fourDimensionalSpawnChoicesExcludeUndefinedPatterns() {
        assertArrayEquals(new SpawnMode[] {
                SpawnMode.RANDOM, SpawnMode.SPHERICAL, SpawnMode.GRID, SpawnMode.SHELL,
                SpawnMode.CLUSTERS, SpawnMode.POINT
        }, ParticlesSection.supportedSpawnModes(SimulationDimension.FOUR_D));
        assertArrayEquals(SpawnMode.values(), ParticlesSection.supportedSpawnModes(SimulationDimension.THREE_D));
    }

    @Test
    void capacityRulesNeverRequestMoreThanTheRuntimeCanAccept() {
        FakeSimulationUiModel model = new FakeSimulationUiModel();
        model.particles.particleCount = 950;
        model.particles.maximumParticleCount = 1_000;

        assertEquals(50, ParticlesSection.remainingCapacity(model.particles));
        assertEquals(50, ParticlesSection.clampedAddition(100, model.particles));
        assertEquals(25, ParticlesSection.clampedAddition(25, model.particles));
    }

    @Test
    void fullAndInconsistentCountsResolveToZeroCapacity() {
        FakeSimulationUiModel model = new FakeSimulationUiModel();
        model.particles.maximumParticleCount = 1_000;

        model.particles.particleCount = 1_000;
        assertEquals(0, ParticlesSection.remainingCapacity(model.particles));
        assertEquals(0, ParticlesSection.clampedAddition(100, model.particles));

        model.particles.particleCount = 1_001;
        assertEquals(0, ParticlesSection.remainingCapacity(model.particles));
    }

    @Test
    void metricCardsStackWhenTheSidebarIsTooNarrow() {
        UIDesignTokens tokens = UIDesignTokens.unscaled();

        assertEquals(false, ParticlesSection.metricsFitSideBySide(212.0f, tokens));
        assertEquals(true, ParticlesSection.metricsFitSideBySide(212.4f, tokens));
        assertEquals(false, ParticlesSection.customControlsFitInline(154.0f, tokens));
        assertEquals(true, ParticlesSection.customControlsFitInline(154.8f, tokens));
    }
}

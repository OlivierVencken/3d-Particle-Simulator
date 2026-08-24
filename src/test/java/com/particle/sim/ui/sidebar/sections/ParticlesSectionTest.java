package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.testing.FakeSimulationUiModel;
import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticlesSectionTest {
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

        assertEquals(false, ParticlesSection.metricsFitSideBySide(235.0f, tokens));
        assertEquals(true, ParticlesSection.metricsFitSideBySide(236.0f, tokens));
        assertEquals(false, ParticlesSection.customControlsFitInline(171.0f, tokens));
        assertEquals(true, ParticlesSection.customControlsFitInline(172.0f, tokens));
    }
}

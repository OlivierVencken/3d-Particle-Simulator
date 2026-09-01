package com.particle.sim.ui.sidebar.sections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.particle.sim.ui.testing.FakeSimulationViewModel;
import com.particle.sim.ui.theme.DesignTokens;
import org.junit.jupiter.api.Test;

class ParticlesSectionTest {
    @Test
    void capacityRulesNeverRequestMoreThanTheRuntimeCanAccept() {
        FakeSimulationViewModel model = new FakeSimulationViewModel();
        model.particles.particleCount = 950;
        model.particles.maximumParticleCount = 1_000;

        assertEquals(50, ParticlesSection.remainingCapacity(model.particles));
        assertEquals(50, ParticlesSection.clampedAddition(100, model.particles));
        assertEquals(25, ParticlesSection.clampedAddition(25, model.particles));
    }

    @Test
    void fullAndInconsistentCountsResolveToZeroCapacity() {
        FakeSimulationViewModel model = new FakeSimulationViewModel();
        model.particles.maximumParticleCount = 1_000;

        model.particles.particleCount = 1_000;
        assertEquals(0, ParticlesSection.remainingCapacity(model.particles));
        assertEquals(0, ParticlesSection.clampedAddition(100, model.particles));

        model.particles.particleCount = 1_001;
        assertEquals(0, ParticlesSection.remainingCapacity(model.particles));
    }

    @Test
    void metricCardsStackWhenTheSidebarIsTooNarrow() {
        DesignTokens tokens = DesignTokens.unscaled();

        assertEquals(false, ParticlesSection.metricsFitSideBySide(212.0f, tokens));
        assertEquals(true, ParticlesSection.metricsFitSideBySide(212.4f, tokens));
        assertEquals(false, ParticlesSection.customControlsFitInline(154.0f, tokens));
        assertEquals(true, ParticlesSection.customControlsFitInline(154.8f, tokens));
    }
}

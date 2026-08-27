package com.particle.sim.ui;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.SpawnMode;
import imgui.ImVec4;

/** Read-only application data exposed to the presentation layer. */
public interface SimulationUiModel {
    Simulation simulation();

    Particles particles();

    Visuals visuals();

    Camera camera();

    Performance performance();

    Application application();

    interface Simulation {
        boolean toroidalWrap();

        float bounds();

        float boundaryBounce();

        float forceFactor();

        float interactionRange();

        float repulsionRadius();

        float velocityDamping();

        float maxVelocity();

        boolean densityRegulationEnabled();

        float densityLimit();

        DistanceMetric distanceMetric();
    }

    interface Particles {
        int particleCount();

        int maximumParticleCount();

        int groupCount();

        int maximumGroupCount();

        SpawnMode spawnMode();

        int customSpawnAmount();

        float matrixEditStep();

        float attraction(int row, int column);

        ImVec4 groupColor(int group);
    }

    interface Visuals {
        float pointSize();

        boolean fixedParticleScreenSize();

        ColorMode colorMode();

        boolean effectEnabled(EffectMode effectMode);

        int glowBlurPasses();

        float glowStrength();

        float glowRadius();

        float glowFalloff();

        int effectiveBloomDivisor();

        int trailLength();

        float trailThickness();

        int effectiveTrailLength();

        int effectiveTrailParticleStride();
    }

    interface Camera {
        float sensitivity();

        float flySpeed();
    }

    interface Performance {
        SimulationUiDiagnostics diagnostics();
    }

    interface Application {
        boolean paused();

        int fpsCap();
    }
}

package com.particle.sim.ui;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.spawning.SpawnMode;
import imgui.ImVec4;

/** Read-only application data exposed to the presentation layer. */
public interface SimulationViewModel {
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

        boolean canUndoAttractionMatrix();

        boolean canRedoAttractionMatrix();

        boolean attractionMutationAnimated();

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
        SimulationViewDiagnostics diagnostics();
    }

    interface Application {
        boolean paused();

        int fpsCap();
    }
}

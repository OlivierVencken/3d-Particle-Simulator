package com.particle.sim.ui;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.SpawnMode;

/** Commands available to the presentation layer. Setting commands also schedule persistence. */
public interface SimulationUiActions {
    Simulation simulation();

    Particles particles();

    Visuals visuals();

    Camera camera();

    Application application();

    interface Simulation {
        void step();

        void resetParticles();

        void setToroidalWrap(boolean value);

        void setBounds(float value);

        void setBoundaryBounce(float value);

        void setForceFactor(float value);

        void setInteractionRange(float value);

        void setRepulsionRadius(float value);

        void setVelocityDamping(float value);

        void setMaxVelocity(float value);

        void setDensityRegulationEnabled(boolean value);

        void setDensityLimit(float value);

        void setDistanceMetric(DistanceMetric value);
    }

    interface Particles {
        void setGroupCount(int value);

        void setSpawnMode(SpawnMode value);

        void add(int amount);

        void remove(int amount);

        void clear();

        void setCustomSpawnAmount(int value);

        void setMatrixEditStep(float value);

        void adjustAttraction(int row, int column, float delta);

        void randomizeAttractionMatrix();

        void zeroAttractionMatrix();

        void symmetrizeAttractionMatrix();

        void invertAttractionMatrix();
    }

    interface Visuals {
        void setPointSize(float value);

        void setFixedParticleScreenSize(boolean value);

        void setColorMode(ColorMode value);

        void setEffectEnabled(EffectMode effectMode, boolean enabled);

        void setGlowBlurPasses(int value);

        void setGlowStrength(float value);

        void setGlowRadius(float value);

        void setGlowFalloff(float value);

        void setTrailLength(int value);

        void setTrailThickness(float value);
    }

    interface Camera {
        void setSensitivity(float value);

        void setFlySpeed(float value);

        void reset();
    }

    interface Application {
        void setFpsCap(int value);

        void resetSettings();

        void savePreset();

        void loadPreset();

        void hideUi();

        void exit();
    }
}

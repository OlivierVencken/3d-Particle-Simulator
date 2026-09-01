package com.particle.sim.ui.testing;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.ui.SimulationViewDiagnostics;
import com.particle.sim.ui.SimulationViewModel;
import imgui.ImVec4;
import java.util.EnumSet;

/** Mutable, native-free model fixture for headless UI tests. */
public final class FakeSimulationViewModel implements SimulationViewModel {
    public final SimulationData simulation = new SimulationData();
    public final ParticleData particles = new ParticleData();
    public final VisualData visuals = new VisualData();
    public final CameraData camera = new CameraData();
    public final PerformanceData performance = new PerformanceData();
    public final ApplicationData application = new ApplicationData();

    @Override
    public SimulationData simulation() {
        return simulation;
    }

    @Override
    public ParticleData particles() {
        return particles;
    }

    @Override
    public VisualData visuals() {
        return visuals;
    }

    @Override
    public CameraData camera() {
        return camera;
    }

    @Override
    public PerformanceData performance() {
        return performance;
    }

    @Override
    public ApplicationData application() {
        return application;
    }

    public static final class SimulationData implements SimulationViewModel.Simulation {
        public boolean toroidalWrap;
        public float bounds = 6.0f;
        public float boundaryBounce = 0.8f;
        public float forceFactor = 1.0f;
        public float interactionRange = 1.0f;
        public float repulsionRadius = 0.2f;
        public float velocityDamping = 0.98f;
        public float maxVelocity = 4.0f;
        public boolean densityRegulationEnabled;
        public float densityLimit = 100.0f;
        public DistanceMetric distanceMetric = DistanceMetric.EUCLIDEAN;

        @Override
        public boolean toroidalWrap() {
            return toroidalWrap;
        }

        @Override
        public float bounds() {
            return bounds;
        }

        @Override
        public float boundaryBounce() {
            return boundaryBounce;
        }

        @Override
        public float forceFactor() {
            return forceFactor;
        }

        @Override
        public float interactionRange() {
            return interactionRange;
        }

        @Override
        public float repulsionRadius() {
            return repulsionRadius;
        }

        @Override
        public float velocityDamping() {
            return velocityDamping;
        }

        @Override
        public float maxVelocity() {
            return maxVelocity;
        }

        @Override
        public boolean densityRegulationEnabled() {
            return densityRegulationEnabled;
        }

        @Override
        public float densityLimit() {
            return densityLimit;
        }

        @Override
        public DistanceMetric distanceMetric() {
            return distanceMetric;
        }
    }

    public static final class ParticleData implements SimulationViewModel.Particles {
        public int particleCount = 1_000;
        public int maximumParticleCount = 1_000_000;
        public int groupCount = 4;
        public int maximumGroupCount = 16;
        public SpawnMode spawnMode = SpawnMode.RANDOM;
        public int customSpawnAmount = 1_000;
        public float matrixEditStep = 0.1f;
        public float[][] attraction = new float[16][16];
        public boolean canUndoAttractionMatrix;
        public boolean canRedoAttractionMatrix;
        public boolean attractionMutationAnimated;
        public ImVec4[] groupColors = {new ImVec4(1.0f, 1.0f, 1.0f, 1.0f)};

        @Override
        public int particleCount() {
            return particleCount;
        }

        @Override
        public int maximumParticleCount() {
            return maximumParticleCount;
        }

        @Override
        public int groupCount() {
            return groupCount;
        }

        @Override
        public int maximumGroupCount() {
            return maximumGroupCount;
        }

        @Override
        public SpawnMode spawnMode() {
            return spawnMode;
        }

        @Override
        public int customSpawnAmount() {
            return customSpawnAmount;
        }

        @Override
        public float matrixEditStep() {
            return matrixEditStep;
        }

        @Override
        public float attraction(int row, int column) {
            return attraction[row][column];
        }

        @Override
        public boolean canUndoAttractionMatrix() {
            return canUndoAttractionMatrix;
        }

        @Override
        public boolean canRedoAttractionMatrix() {
            return canRedoAttractionMatrix;
        }

        @Override
        public boolean attractionMutationAnimated() {
            return attractionMutationAnimated;
        }

        @Override
        public ImVec4 groupColor(int group) {
            return groupColors[Math.floorMod(group, groupColors.length)];
        }
    }

    public static final class VisualData implements SimulationViewModel.Visuals {
        public float pointSize = 2.0f;
        public boolean fixedParticleScreenSize;
        public ColorMode colorMode = ColorMode.GROUP;
        public EnumSet<EffectMode> effects = EnumSet.noneOf(EffectMode.class);
        public int glowBlurPasses = 4;
        public float glowStrength = 1.0f;
        public float glowRadius = 2.0f;
        public float glowFalloff = 1.0f;
        public int effectiveBloomDivisor = 1;
        public int trailLength = 16;
        public float trailThickness = 1.0f;
        public int effectiveTrailLength = 16;
        public int effectiveTrailParticleStride = 1;

        @Override
        public float pointSize() {
            return pointSize;
        }

        @Override
        public boolean fixedParticleScreenSize() {
            return fixedParticleScreenSize;
        }

        @Override
        public ColorMode colorMode() {
            return colorMode;
        }

        @Override
        public boolean effectEnabled(EffectMode mode) {
            return effects.contains(mode);
        }

        @Override
        public int glowBlurPasses() {
            return glowBlurPasses;
        }

        @Override
        public float glowStrength() {
            return glowStrength;
        }

        @Override
        public float glowRadius() {
            return glowRadius;
        }

        @Override
        public float glowFalloff() {
            return glowFalloff;
        }

        @Override
        public int effectiveBloomDivisor() {
            return effectiveBloomDivisor;
        }

        @Override
        public int trailLength() {
            return trailLength;
        }

        @Override
        public float trailThickness() {
            return trailThickness;
        }

        @Override
        public int effectiveTrailLength() {
            return effectiveTrailLength;
        }

        @Override
        public int effectiveTrailParticleStride() {
            return effectiveTrailParticleStride;
        }
    }

    public static final class CameraData implements SimulationViewModel.Camera {
        public float sensitivity = 0.002f;
        public float flySpeed = 5.0f;

        @Override
        public float sensitivity() {
            return sensitivity;
        }

        @Override
        public float flySpeed() {
            return flySpeed;
        }
    }

    public static final class PerformanceData implements SimulationViewModel.Performance {
        public SimulationViewDiagnostics diagnostics = SimulationViewDiagnostics.unavailable();

        @Override
        public SimulationViewDiagnostics diagnostics() {
            return diagnostics;
        }
    }

    public static final class ApplicationData implements SimulationViewModel.Application {
        public boolean paused;
        public int fpsCap = 144;

        @Override
        public boolean paused() {
            return paused;
        }

        @Override
        public int fpsCap() {
            return fpsCap;
        }
    }
}

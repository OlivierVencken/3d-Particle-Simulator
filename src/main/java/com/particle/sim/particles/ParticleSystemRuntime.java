package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT;
import static org.lwjgl.opengl.GL43C.GL_MAX_SHADER_STORAGE_BLOCK_SIZE;
import static org.lwjgl.opengl.GL43C.glGetInteger64;
import static org.lwjgl.opengl.GL43C.glGetIntegeri;

import com.particle.sim.diagnostics.PerformanceSnapshot;
import com.particle.sim.graphics.FramebufferViewport;
import com.particle.sim.particles.attraction.AttractionMatrix;
import com.particle.sim.particles.gpu.ParticleBuffers;
import com.particle.sim.particles.gpu.ParticleCompute;
import com.particle.sim.particles.gpu.ParticleComputeParameters;
import com.particle.sim.particles.gpu.SpatialGridBuffers;
import com.particle.sim.particles.gpu.SpatialGridSizing;
import com.particle.sim.particles.gpu.TrailHistoryBuffers;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.rendering.ParticleRenderer;
import com.particle.sim.particles.rendering.RenderFrame;
import com.particle.sim.settings.SimulationDefaults;
import java.util.Random;

/** Owns the OpenGL-backed lifetime and execution of a particle system. */
final class ParticleSystemRuntime {
    private static final int COMPUTE_WORK_GROUP_SIZE = 256;
    private static final int BYTES_PER_PARTICLE = 4 * 4 * Float.BYTES + Integer.BYTES;
    private final ParticleBuffers particleBuffers = new ParticleBuffers();
    private final TrailHistoryBuffers trailHistoryBuffers = new TrailHistoryBuffers();
    private final SpatialGridBuffers spatialGridBuffers = new SpatialGridBuffers();
    private final ParticleRenderer renderer = new ParticleRenderer();
    private final ParticleCompute compute = new ParticleCompute();
    private boolean initialized;

    int init(ParticleSimulationConfig config, AttractionMatrix matrix, Random random) {
        compute.init();
        renderer.init();
        int maximumParticleCount = detectMaximumParticleCount();
        config.particleCount(Math.min(config.particleCount(), maximumParticleCount));
        spatialGridBuffers.init(config.particleCount(), gridCellCount(config));
        matrix.randomize();
        matrix.clearHistory();
        initialized = true;
        resizeParticles(0, config.particleCount(), false, config, random);
        return maximumParticleCount;
    }

    boolean initialized() {
        return initialized;
    }

    void advance(ParticleSimulationConfig config, AttractionMatrix matrix, float deltaTime) {
        if (!initialized || config.particleCount() == 0) {
            return;
        }

        ParticleComputeParameters parameters =
                ParticleComputeParameters.from(config, matrix.values());
        spatialGridBuffers.ensureCapacity(parameters.particleCount(), parameters.gridCellCount());
        compute.buildGrid(parameters, particleBuffers, spatialGridBuffers);
        boolean captureTrail =
                config.effectEnabled(EffectMode.TRAILS)
                        && trailHistoryBuffers.prepareCapture(
                                config.particleCount(), config.trailLength());
        compute.integrate(
                parameters,
                particleBuffers,
                spatialGridBuffers,
                trailHistoryBuffers,
                captureTrail,
                deltaTime);
        particleBuffers.swapState();
        if (captureTrail) {
            trailHistoryBuffers.commitCapture();
        }
    }

    void render(FramebufferViewport viewport, float[] viewMatrix, ParticleSimulationConfig config) {
        renderer.render(
                new RenderFrame(
                        viewport,
                        viewMatrix,
                        particleBuffers,
                        spatialGridBuffers,
                        trailHistoryBuffers,
                        config.particleCount(),
                        config));
    }

    void resizeParticles(
            int oldParticleCount,
            int newParticleCount,
            boolean preserveExisting,
            ParticleSimulationConfig config,
            Random random) {
        particleBuffers.resize(
                oldParticleCount, newParticleCount, preserveExisting, config, random);
        trailHistoryBuffers.clear();
    }

    void applyConfig(int oldParticleCount, ParticleSimulationConfig config, Random random) {
        particleBuffers.resize(oldParticleCount, config.particleCount(), false, config, random);
        if (config.effectEnabled(EffectMode.TRAILS)) {
            trailHistoryBuffers.clear();
        } else {
            trailHistoryBuffers.dispose();
        }
    }

    void effectChanged(EffectMode effectMode, boolean enabled) {
        if (initialized && effectMode == EffectMode.TRAILS && !enabled) {
            trailHistoryBuffers.dispose();
        }
    }

    int effectiveTrailLength() {
        return trailHistoryBuffers.sampleCapacity();
    }

    int effectiveTrailParticleStride() {
        return renderer.effectiveTrailParticleStride();
    }

    int effectiveBloomDivisor() {
        return renderer.effectiveBloomDivisor();
    }

    PerformanceSnapshot performanceSnapshot(
            ParticleSimulationConfig config, int maximumParticleCount) {
        if (!initialized) {
            return unavailablePerformance(config, maximumParticleCount);
        }
        double countMilliseconds = compute.gridCountMilliseconds();
        double scanMilliseconds = compute.gridScanMilliseconds();
        double scatterMilliseconds = compute.gridScatterMilliseconds();
        double integrationMilliseconds = compute.integrationMilliseconds();
        double simulationMilliseconds =
                anyUnavailable(
                                countMilliseconds,
                                scanMilliseconds,
                                scatterMilliseconds,
                                integrationMilliseconds)
                        ? -1.0
                        : countMilliseconds
                                + scanMilliseconds
                                + scatterMilliseconds
                                + integrationMilliseconds;
        long allocatedBytes =
                particleBuffers.allocatedBytes()
                        + spatialGridBuffers.allocatedBytes()
                        + trailHistoryBuffers.allocatedBytes()
                        + renderer.allocatedEffectBytes();
        return new PerformanceSnapshot(
                countMilliseconds,
                scanMilliseconds,
                scatterMilliseconds,
                integrationMilliseconds,
                simulationMilliseconds,
                renderer.particleRenderMilliseconds(),
                renderer.trailRenderMilliseconds(),
                renderer.bloomMilliseconds(),
                allocatedBytes,
                config.particleCount(),
                maximumParticleCount,
                gridCellCount(config));
    }

    float[] readPositions(int particleCount) {
        return particleBuffers.readPositions(particleCount);
    }

    float[] readVelocities(int particleCount) {
        return particleBuffers.readVelocities(particleCount);
    }

    void replaceState(float[] positions, float[] velocities) {
        particleBuffers.replaceState(positions, velocities);
        trailHistoryBuffers.clear();
    }

    int[] readGridCounts(int gridCellCount) {
        return spatialGridBuffers.readCounts(gridCellCount);
    }

    int[] readGridParticleIds(int particleCount) {
        return spatialGridBuffers.readParticleIds(particleCount);
    }

    void dispose() {
        particleBuffers.dispose();
        trailHistoryBuffers.dispose();
        spatialGridBuffers.dispose();
        compute.dispose();
        renderer.dispose();
        initialized = false;
    }

    private static PerformanceSnapshot unavailablePerformance(
            ParticleSimulationConfig config, int maximumParticleCount) {
        return new PerformanceSnapshot(
                -1.0,
                -1.0,
                -1.0,
                -1.0,
                -1.0,
                -1.0,
                -1.0,
                -1.0,
                0L,
                config.particleCount(),
                maximumParticleCount,
                gridCellCount(config));
    }

    private static boolean anyUnavailable(double... timings) {
        for (double timing : timings) {
            if (timing < 0.0) {
                return true;
            }
        }
        return false;
    }

    private static int gridCellCount(ParticleSimulationConfig config) {
        return SpatialGridSizing.gridCellCount(config.bounds(), config.interactionRange());
    }

    private static int detectMaximumParticleCount() {
        long storageBlockLimit =
                glGetInteger64(GL_MAX_SHADER_STORAGE_BLOCK_SIZE) / (4L * Float.BYTES);
        long dispatchLimit =
                (long) glGetIntegeri(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0) * COMPUTE_WORK_GROUP_SIZE;
        long memoryLimit = SimulationDefaults.SIMULATION_MEMORY_BUDGET_BYTES / BYTES_PER_PARTICLE;
        long supported =
                Math.min(
                        SimulationDefaults.MAX_PARTICLE_COUNT,
                        Math.min(storageBlockLimit, Math.min(dispatchLimit, memoryLimit)));
        return (int) Math.max(1L, supported);
    }
}

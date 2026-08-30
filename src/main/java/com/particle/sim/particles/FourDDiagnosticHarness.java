package com.particle.sim.particles;

import com.particle.sim.ui.FramebufferViewport;

import java.util.Random;

/**
 * Development-only OpenGL harness for exercising the 4D point renderer before
 * four-dimensional physics is enabled in the production particle system.
 */
public final class FourDDiagnosticHarness implements AutoCloseable {
    private static final float BOUNDS = 4.0f;
    private static final float INTERACTION_RANGE = 1.0f;

    private final ParticleBuffers particleBuffers = new ParticleBuffers();
    private final SpatialGridBuffers spatialGridBuffers = new SpatialGridBuffers();
    private final TrailHistoryBuffers trailHistoryBuffers = new TrailHistoryBuffers();
    private final ParticleRenderer renderer = new ParticleRenderer();
    private final FourDViewController viewController;
    private final FourDDiagnosticPointSet pointSet;
    private final ParticleSimulationConfig renderConfig = ParticleSimulationConfig.defaults();
    private boolean initialized;

    public FourDDiagnosticHarness() {
        this(FourDDiagnosticPointSet.standard(), new FourDViewController());
    }

    public FourDDiagnosticHarness(FourDDiagnosticPointSet pointSet, FourDViewController viewController) {
        if (pointSet == null || viewController == null) {
            throw new IllegalArgumentException("Diagnostic points and a 4D view controller are required");
        }
        this.pointSet = pointSet;
        this.viewController = viewController;
    }

    public void init() {
        if (initialized) {
            return;
        }
        renderConfig.particleCount(pointSet.particleCount());
        renderConfig.bounds(BOUNDS);
        renderConfig.interactionRange(INTERACTION_RANGE);
        renderConfig.groupCount(16);
        renderer.init();
        particleBuffers.resize(0, pointSet.particleCount(), false, renderConfig, new Random(0L));
        particleBuffers.replaceState(pointSet.positions(), pointSet.velocities(), pointSet.groups());
        spatialGridBuffers.init(pointSet.particleCount(), SpatialGridSizing.gridCellCount(BOUNDS, INTERACTION_RANGE));
        initialized = true;
    }

    public void render(FramebufferViewport viewport, float[] viewMatrix, boolean glowEnabled) {
        requireInitialized();
        renderer.render(viewport, viewMatrix, particleBuffers, spatialGridBuffers, pointSet.particleCount(),
                9.0f, true, glowEnabled, false, ColorMode.GROUP.ordinal(), 16, renderConfig.maxVelocity(),
                BOUNDS, INTERACTION_RANGE, renderConfig.glowSettings(), renderConfig.trailSettings(),
                trailHistoryBuffers, SimulationDimension.FOUR_D, viewController.configuration());
    }

    public FourDViewController viewController() {
        return viewController;
    }

    public float[] positions() {
        requireInitialized();
        return particleBuffers.readPositions(pointSet.particleCount());
    }

    public int particleCount() {
        return pointSet.particleCount();
    }

    @Override
    public void close() {
        if (!initialized) {
            return;
        }
        particleBuffers.dispose();
        spatialGridBuffers.dispose();
        trailHistoryBuffers.dispose();
        renderer.dispose();
        initialized = false;
    }

    private void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException("The 4D diagnostic harness must be initialized first");
        }
    }
}

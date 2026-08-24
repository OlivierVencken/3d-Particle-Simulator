package com.particle.sim.ui.testing;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.ui.SimulationUiActions;

import java.util.ArrayList;
import java.util.List;

/** Native-free action fixture that records calls in invocation order. */
public final class RecordingSimulationUiActions implements SimulationUiActions {
    public final List<String> calls = new ArrayList<>();
    public final SimulationActions simulation = new SimulationActions();
    public final ParticleActions particles = new ParticleActions();
    public final VisualActions visuals = new VisualActions();
    public final CameraActions camera = new CameraActions();
    public final ApplicationActions application = new ApplicationActions();

    @Override public SimulationActions simulation() { return simulation; }
    @Override public ParticleActions particles() { return particles; }
    @Override public VisualActions visuals() { return visuals; }
    @Override public CameraActions camera() { return camera; }
    @Override public ApplicationActions application() { return application; }

    private void record(String call) {
        calls.add(call);
    }

    public final class SimulationActions implements SimulationUiActions.Simulation {
        @Override public void setToroidalWrap(boolean value) { record("simulation.toroidalWrap"); }
        @Override public void setBounds(float value) { record("simulation.bounds"); }
        @Override public void setBoundaryBounce(float value) { record("simulation.boundaryBounce"); }
        @Override public void setForceFactor(float value) { record("simulation.forceFactor"); }
        @Override public void setInteractionRange(float value) { record("simulation.interactionRange"); }
        @Override public void setRepulsionRadius(float value) { record("simulation.repulsionRadius"); }
        @Override public void setVelocityDamping(float value) { record("simulation.velocityDamping"); }
        @Override public void setMaxVelocity(float value) { record("simulation.maxVelocity"); }
        @Override public void setDensityRegulationEnabled(boolean value) { record("simulation.densityRegulation"); }
        @Override public void setDensityLimit(float value) { record("simulation.densityLimit"); }
        @Override public void setDistanceMetric(DistanceMetric value) { record("simulation.distanceMetric"); }
    }

    public final class ParticleActions implements SimulationUiActions.Particles {
        @Override public void setGroupCount(int value) { record("particles.groupCount"); }
        @Override public void setSpawnMode(SpawnMode value) { record("particles.spawnMode"); }
        @Override public void add(int amount) { record("particles.add"); }
        @Override public void remove(int amount) { record("particles.remove"); }
        @Override public void clear() { record("particles.clear"); }
        @Override public void setCustomSpawnAmount(int value) { record("particles.customSpawnAmount"); }
        @Override public void setMatrixEditStep(float value) { record("particles.matrixEditStep"); }
        @Override public void adjustAttraction(int row, int column, float delta) { record("particles.attraction"); }
        @Override public void randomizeAttractionMatrix() { record("particles.matrix.randomize"); }
        @Override public void zeroAttractionMatrix() { record("particles.matrix.zero"); }
        @Override public void symmetrizeAttractionMatrix() { record("particles.matrix.symmetrize"); }
        @Override public void invertAttractionMatrix() { record("particles.matrix.invert"); }
    }

    public final class VisualActions implements SimulationUiActions.Visuals {
        @Override public void setPointSize(float value) { record("visuals.pointSize"); }
        @Override public void setFixedParticleScreenSize(boolean value) { record("visuals.fixedScreenSize"); }
        @Override public void setColorMode(ColorMode value) { record("visuals.colorMode"); }
        @Override public void setEffectEnabled(EffectMode mode, boolean enabled) { record("visuals.effect"); }
        @Override public void setGlowBlurPasses(int value) { record("visuals.glowBlurPasses"); }
        @Override public void setGlowStrength(float value) { record("visuals.glowStrength"); }
        @Override public void setGlowRadius(float value) { record("visuals.glowRadius"); }
        @Override public void setGlowFalloff(float value) { record("visuals.glowFalloff"); }
        @Override public void setTrailLength(int value) { record("visuals.trailLength"); }
        @Override public void setTrailThickness(float value) { record("visuals.trailThickness"); }
    }

    public final class CameraActions implements SimulationUiActions.Camera {
        @Override public void setSensitivity(float value) { record("camera.sensitivity"); }
        @Override public void setFlySpeed(float value) { record("camera.flySpeed"); }
        @Override public void reset() { record("camera.reset"); }
    }

    public final class ApplicationActions implements SimulationUiActions.Application {
        @Override public void setFpsCap(int value) { record("application.fpsCap"); }
        @Override public void resetSettings() { record("application.resetSettings"); }
        @Override public void savePreset() { record("application.savePreset"); }
        @Override public void loadPreset() { record("application.loadPreset"); }
        @Override public void hideUi() { record("application.hideUi"); }
        @Override public void exit() { record("application.exit"); }
    }
}

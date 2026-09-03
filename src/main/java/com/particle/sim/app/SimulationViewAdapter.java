package com.particle.sim.app;

import com.particle.sim.camera.CameraController;
import com.particle.sim.diagnostics.PerformanceSnapshot;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.particles.attraction.AttractionPattern;
import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.spawning.SpawnMode;
import com.particle.sim.settings.SettingsActions;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewDiagnostics;
import com.particle.sim.ui.SimulationViewModel;
import imgui.ImVec4;
import java.util.Objects;

/** Long-lived bridge between runtime implementations and presentation-facing contracts. */
final class SimulationViewAdapter {
    private static final Runnable NO_ACTION = () -> {};
    private final ParticleSystem particles;
    private final CameraController camera;
    private final SimulationView ui;
    private final SettingsActions settingsActions;
    private final SimulationDomain simulation = new SimulationDomain();
    private final ParticleDomain particleDomain = new ParticleDomain();
    private final VisualDomain visuals = new VisualDomain();
    private final CameraDomain cameraDomain = new CameraDomain();
    private final PerformanceDomain performance = new PerformanceDomain();
    private final ApplicationDomain application = new ApplicationDomain();
    private final Model model = new Model();
    private final Actions actions = new Actions();
    private SimulationViewDiagnostics diagnostics = SimulationViewDiagnostics.unavailable();
    private Runnable savePreset = NO_ACTION;
    private Runnable loadPreset = NO_ACTION;
    private Runnable exitApplication = NO_ACTION;

    SimulationViewAdapter(
            ParticleSystem particles,
            CameraController camera,
            SimulationView ui,
            SettingsActions settingsActions) {
        this.particles = Objects.requireNonNull(particles, "particles");
        this.camera = Objects.requireNonNull(camera, "camera");
        this.ui = Objects.requireNonNull(ui, "ui");
        this.settingsActions = Objects.requireNonNull(settingsActions, "settingsActions");
    }

    SimulationViewModel model() {
        return model;
    }

    SimulationViewActions actions() {
        return actions;
    }

    void prepareFrame() {
        PerformanceSnapshot snapshot = particles.performanceSnapshot();
        diagnostics =
                new SimulationViewDiagnostics(
                        snapshot.particleCount(),
                        snapshot.maximumParticleCount(),
                        particles.gridSize(),
                        snapshot.gridCellCount(),
                        snapshot.gridCountMilliseconds(),
                        snapshot.gridScanMilliseconds(),
                        snapshot.gridScatterMilliseconds(),
                        snapshot.integrationMilliseconds(),
                        snapshot.simulationMilliseconds(),
                        snapshot.particleRenderMilliseconds(),
                        snapshot.trailRenderMilliseconds(),
                        snapshot.bloomMilliseconds(),
                        snapshot.allocatedGpuBytes());
    }

    void onSavePreset(Runnable action) {
        savePreset = action == null ? NO_ACTION : action;
    }

    void onLoadPreset(Runnable action) {
        loadPreset = action == null ? NO_ACTION : action;
    }

    void onExitApplication(Runnable action) {
        exitApplication = action == null ? NO_ACTION : action;
    }

    private void settingChanged(Runnable mutation) {
        mutation.run();
        settingsActions.onSettingsChanged();
    }

    private final class Model implements SimulationViewModel {
        @Override
        public SimulationViewModel.Simulation simulation() {
            return simulation;
        }

        @Override
        public SimulationViewModel.Particles particles() {
            return particleDomain;
        }

        @Override
        public SimulationViewModel.Visuals visuals() {
            return visuals;
        }

        @Override
        public SimulationViewModel.Camera camera() {
            return cameraDomain;
        }

        @Override
        public SimulationViewModel.Performance performance() {
            return performance;
        }

        @Override
        public SimulationViewModel.Application application() {
            return application;
        }
    }

    private final class Actions implements SimulationViewActions {
        @Override
        public SimulationViewActions.Simulation simulation() {
            return simulation;
        }

        @Override
        public SimulationViewActions.Particles particles() {
            return particleDomain;
        }

        @Override
        public SimulationViewActions.Visuals visuals() {
            return visuals;
        }

        @Override
        public SimulationViewActions.Camera camera() {
            return cameraDomain;
        }

        @Override
        public SimulationViewActions.Application application() {
            return application;
        }
    }

    private final class SimulationDomain
            implements SimulationViewModel.Simulation, SimulationViewActions.Simulation {
        @Override
        public void togglePause() {
            ui.togglePause();
        }

        @Override
        public void step() {
            particles.step();
        }

        @Override
        public void resetParticles() {
            particles.reset();
        }

        @Override
        public boolean toroidalWrap() {
            return particles.toroidalWrap();
        }

        @Override
        public float bounds() {
            return particles.bounds();
        }

        @Override
        public float boundaryBounce() {
            return particles.boundaryBounce();
        }

        @Override
        public float forceFactor() {
            return particles.forceFactor();
        }

        @Override
        public float interactionRange() {
            return particles.interactionRange();
        }

        @Override
        public float repulsionRadius() {
            return particles.repulsionRadius();
        }

        @Override
        public float velocityDamping() {
            return particles.velocityDamping();
        }

        @Override
        public float maxVelocity() {
            return particles.maxVelocity();
        }

        @Override
        public boolean densityRegulationEnabled() {
            return particles.densityRegulationEnabled();
        }

        @Override
        public float densityLimit() {
            return particles.densityLimit();
        }

        @Override
        public DistanceMetric distanceMetric() {
            return particles.distanceMetric();
        }

        @Override
        public void setToroidalWrap(boolean value) {
            settingChanged(() -> particles.toroidalWrap(value));
        }

        @Override
        public void setBounds(float value) {
            settingChanged(() -> particles.bounds(value));
        }

        @Override
        public void setBoundaryBounce(float value) {
            settingChanged(() -> particles.boundaryBounce(value));
        }

        @Override
        public void setForceFactor(float value) {
            settingChanged(() -> particles.forceFactor(value));
        }

        @Override
        public void setInteractionRange(float value) {
            settingChanged(() -> particles.interactionRange(value));
        }

        @Override
        public void setRepulsionRadius(float value) {
            settingChanged(() -> particles.repulsionRadius(value));
        }

        @Override
        public void setVelocityDamping(float value) {
            settingChanged(() -> particles.velocityDamping(value));
        }

        @Override
        public void setMaxVelocity(float value) {
            settingChanged(() -> particles.maxVelocity(value));
        }

        @Override
        public void setDensityRegulationEnabled(boolean value) {
            settingChanged(() -> particles.densityRegulationEnabled(value));
        }

        @Override
        public void setDensityLimit(float value) {
            settingChanged(() -> particles.densityLimit(value));
        }

        @Override
        public void setDistanceMetric(DistanceMetric value) {
            settingChanged(() -> particles.distanceMetric(value));
        }
    }

    private final class ParticleDomain
            implements SimulationViewModel.Particles, SimulationViewActions.Particles {
        @Override
        public int particleCount() {
            return particles.particleCount();
        }

        @Override
        public int maximumParticleCount() {
            return particles.maximumParticleCount();
        }

        @Override
        public int groupCount() {
            return particles.groupCount();
        }

        @Override
        public int maximumGroupCount() {
            return particles.maximumGroupCount();
        }

        @Override
        public SpawnMode spawnMode() {
            return particles.spawnMode();
        }

        @Override
        public int customSpawnAmount() {
            return ui.customSpawnAmount();
        }

        @Override
        public float matrixEditStep() {
            return ui.matrixEditStep();
        }

        @Override
        public float attraction(int row, int column) {
            return particles.attraction(row, column);
        }

        @Override
        public boolean canUndoAttractionMatrix() {
            return particles.canUndoAttractionMatrix();
        }

        @Override
        public boolean canRedoAttractionMatrix() {
            return particles.canRedoAttractionMatrix();
        }

        @Override
        public boolean attractionMutationAnimated() {
            return particles.attractionMutationAnimated();
        }

        @Override
        public ImVec4 groupColor(int group) {
            return particles.groupColor(group);
        }

        @Override
        public void setGroupCount(int value) {
            settingChanged(() -> particles.groupCount(value));
        }

        @Override
        public void setSpawnMode(SpawnMode value) {
            settingChanged(() -> particles.spawnMode(value));
        }

        @Override
        public void add(int amount) {
            settingChanged(() -> particles.addParticles(amount));
        }

        @Override
        public void remove(int amount) {
            settingChanged(() -> particles.removeParticles(amount));
        }

        @Override
        public void clear() {
            settingChanged(particles::clearParticles);
        }

        @Override
        public void setCustomSpawnAmount(int value) {
            settingChanged(() -> ui.setCustomSpawnAmount(value));
        }

        @Override
        public void setMatrixEditStep(float value) {
            settingChanged(() -> ui.setMatrixEditStep(value));
        }

        @Override
        public void adjustAttraction(int row, int column, float delta) {
            settingChanged(() -> particles.adjustAttraction(row, column, delta));
        }

        @Override
        public void randomizeAttractionMatrix() {
            settingChanged(particles::randomizeAttractionMatrix);
        }

        @Override
        public void zeroAttractionMatrix() {
            settingChanged(particles::zeroAttractionMatrix);
        }

        @Override
        public void symmetrizeAttractionMatrix() {
            settingChanged(particles::symmetrizeAttractionMatrix);
        }

        @Override
        public void invertAttractionMatrix() {
            settingChanged(particles::invertAttractionMatrix);
        }

        @Override
        public void generateAttractionMatrix(AttractionPattern pattern, float variation) {
            settingChanged(() -> particles.generateAttractionMatrix(pattern, variation));
        }

        @Override
        public void mutateAttractionMatrix(float amount) {
            settingChanged(() -> particles.mutateAttractionMatrix(amount));
        }

        @Override
        public void normalizeAttractionMatrix() {
            settingChanged(particles::normalizeAttractionMatrix);
        }

        @Override
        public void undoAttractionMatrix() {
            settingChanged(particles::undoAttractionMatrix);
        }

        @Override
        public void redoAttractionMatrix() {
            settingChanged(particles::redoAttractionMatrix);
        }

        @Override
        public void setAttractionMutationAnimated(boolean enabled) {
            settingChanged(() -> particles.attractionMutationAnimated(enabled));
        }
    }

    private final class VisualDomain
            implements SimulationViewModel.Visuals, SimulationViewActions.Visuals {
        @Override
        public float pointSize() {
            return particles.pointSize();
        }

        @Override
        public boolean fixedParticleScreenSize() {
            return particles.fixedParticleScreenSize();
        }

        @Override
        public ColorMode colorMode() {
            return particles.colorMode();
        }

        @Override
        public boolean effectEnabled(EffectMode effectMode) {
            return particles.effectEnabled(effectMode);
        }

        @Override
        public int glowBlurPasses() {
            return particles.glowBlurPasses();
        }

        @Override
        public float glowStrength() {
            return particles.glowStrength();
        }

        @Override
        public float glowRadius() {
            return particles.glowRadius();
        }

        @Override
        public float glowFalloff() {
            return particles.glowFalloff();
        }

        @Override
        public int effectiveBloomDivisor() {
            return particles.effectiveBloomDivisor();
        }

        @Override
        public int trailLength() {
            return particles.trailLength();
        }

        @Override
        public float trailThickness() {
            return particles.trailThickness();
        }

        @Override
        public int effectiveTrailLength() {
            return particles.effectiveTrailLength();
        }

        @Override
        public int effectiveTrailParticleStride() {
            return particles.effectiveTrailParticleStride();
        }

        @Override
        public void setPointSize(float value) {
            settingChanged(() -> particles.pointSize(value));
        }

        @Override
        public void setFixedParticleScreenSize(boolean value) {
            settingChanged(() -> particles.fixedParticleScreenSize(value));
        }

        @Override
        public void setColorMode(ColorMode value) {
            settingChanged(() -> particles.colorMode(value));
        }

        @Override
        public void setGroupColor(int group, ImVec4 color) {
            settingChanged(() -> particles.groupColor(group, color));
        }

        @Override
        public void setEffectEnabled(EffectMode effectMode, boolean enabled) {
            settingChanged(() -> particles.effectEnabled(effectMode, enabled));
        }

        @Override
        public void setGlowBlurPasses(int value) {
            settingChanged(() -> particles.glowBlurPasses(value));
        }

        @Override
        public void setGlowStrength(float value) {
            settingChanged(() -> particles.glowStrength(value));
        }

        @Override
        public void setGlowRadius(float value) {
            settingChanged(() -> particles.glowRadius(value));
        }

        @Override
        public void setGlowFalloff(float value) {
            settingChanged(() -> particles.glowFalloff(value));
        }

        @Override
        public void setTrailLength(int value) {
            settingChanged(() -> particles.trailLength(value));
        }

        @Override
        public void setTrailThickness(float value) {
            settingChanged(() -> particles.trailThickness(value));
        }
    }

    private final class CameraDomain
            implements SimulationViewModel.Camera, SimulationViewActions.Camera {
        @Override
        public float sensitivity() {
            return camera.getSensitivity();
        }

        @Override
        public float flySpeed() {
            return camera.getFlySpeed();
        }

        @Override
        public void setSensitivity(float value) {
            settingChanged(() -> camera.setSensitivity(value));
        }

        @Override
        public void setFlySpeed(float value) {
            settingChanged(() -> camera.setFlySpeed(value));
        }

        @Override
        public void reset() {
            camera.reset();
        }
    }

    private final class PerformanceDomain implements SimulationViewModel.Performance {
        @Override
        public SimulationViewDiagnostics diagnostics() {
            return diagnostics;
        }
    }

    private final class ApplicationDomain
            implements SimulationViewModel.Application, SimulationViewActions.Application {
        @Override
        public boolean paused() {
            return ui.isPaused();
        }

        @Override
        public int fpsCap() {
            return ui.fpsCap();
        }

        @Override
        public void setFpsCap(int value) {
            settingChanged(() -> ui.setFpsCap(value));
        }

        @Override
        public void resetSettings() {
            settingsActions.onResetRequested();
        }

        @Override
        public void savePreset() {
            savePreset.run();
        }

        @Override
        public void loadPreset() {
            loadPreset.run();
        }

        @Override
        public void hideUi() {
            ui.hide();
        }

        @Override
        public void exit() {
            exitApplication.run();
        }
    }
}

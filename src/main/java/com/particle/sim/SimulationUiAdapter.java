package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.FourDViewConfiguration;
import com.particle.sim.particles.FourDVisualizationMode;
import com.particle.sim.particles.PerformanceSnapshot;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.settings.SettingsController;
import com.particle.sim.ui.SimulationUI;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiDiagnostics;
import com.particle.sim.ui.SimulationUiModel;
import imgui.ImVec4;

import java.util.Objects;

/** Long-lived bridge between runtime implementations and presentation-facing contracts. */
final class SimulationUiAdapter {
    private static final Runnable NO_ACTION = () -> {
    };

    private final GpuParticleSystem particles;
    private final CameraController camera;
    private final SimulationUI ui;
    private final SettingsController settingsController;

    private final SimulationDomain simulation = new SimulationDomain();
    private final ParticleDomain particleDomain = new ParticleDomain();
    private final VisualDomain visuals = new VisualDomain();
    private final CameraDomain cameraDomain = new CameraDomain();
    private final PerformanceDomain performance = new PerformanceDomain();
    private final ApplicationDomain application = new ApplicationDomain();
    private final Model model = new Model();
    private final Actions actions = new Actions();

    private SimulationUiDiagnostics diagnostics = SimulationUiDiagnostics.unavailable();
    private Runnable savePreset = NO_ACTION;
    private Runnable loadPreset = NO_ACTION;
    private Runnable exitApplication = NO_ACTION;

    SimulationUiAdapter(GpuParticleSystem particles, CameraController camera, SimulationUI ui,
            SettingsController settingsController) {
        this.particles = Objects.requireNonNull(particles, "particles");
        this.camera = Objects.requireNonNull(camera, "camera");
        this.ui = Objects.requireNonNull(ui, "ui");
        this.settingsController = Objects.requireNonNull(settingsController, "settingsController");
    }

    SimulationUiModel model() {
        return model;
    }

    SimulationUiActions actions() {
        return actions;
    }

    void prepareFrame() {
        PerformanceSnapshot snapshot = particles.performanceSnapshot();
        diagnostics = new SimulationUiDiagnostics(
                snapshot.particleCount(),
                snapshot.maximumParticleCount(),
                particles.gridSize(),
                snapshot.gridCellCount(),
                particles.simulationDimension(),
                particles.fourDViewConfiguration().visualizationMode(),
                Math.toDegrees(particles.fourDXwAutoSpeed()),
                Math.toDegrees(particles.fourDYwAutoSpeed()),
                Math.toDegrees(particles.fourDZwAutoSpeed()),
                particles.fourDViewConfiguration().perspectiveDistance(),
                particles.fourDViewConfiguration().sliceCenterW(),
                particles.fourDViewConfiguration().sliceThickness(),
                particles.fourDViewConfiguration().colorRange(),
                snapshot.gridCountMilliseconds(),
                snapshot.gridScanMilliseconds(),
                snapshot.gridScatterMilliseconds(),
                snapshot.integrationMilliseconds(),
                snapshot.simulationMilliseconds(),
                snapshot.particleRenderMilliseconds(),
                snapshot.trailRenderMilliseconds(),
                snapshot.bloomMilliseconds(),
                snapshot.allocatedGpuBytes(),
                snapshot.groupBufferBytes());
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
        settingsController.onSettingsChanged();
    }

    private final class Model implements SimulationUiModel {
        @Override
        public SimulationUiModel.Simulation simulation() {
            return simulation;
        }

        @Override
        public SimulationUiModel.Particles particles() {
            return particleDomain;
        }

        @Override
        public SimulationUiModel.Visuals visuals() {
            return visuals;
        }

        @Override
        public SimulationUiModel.Camera camera() {
            return cameraDomain;
        }

        @Override
        public SimulationUiModel.Performance performance() {
            return performance;
        }

        @Override
        public SimulationUiModel.Application application() {
            return application;
        }
    }

    private final class Actions implements SimulationUiActions {
        @Override
        public SimulationUiActions.Simulation simulation() {
            return simulation;
        }

        @Override
        public SimulationUiActions.Particles particles() {
            return particleDomain;
        }

        @Override
        public SimulationUiActions.Visuals visuals() {
            return visuals;
        }

        @Override
        public SimulationUiActions.Camera camera() {
            return cameraDomain;
        }

        @Override
        public SimulationUiActions.Application application() {
            return application;
        }
    }

    private final class SimulationDomain implements SimulationUiModel.Simulation, SimulationUiActions.Simulation {
        @Override
        public SimulationDimension simulationDimension() {
            return particles.simulationDimension();
        }

        @Override
        public void setSimulationDimension(SimulationDimension value) {
            settingChanged(() -> particles.simulationDimension(value));
        }

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

    private final class ParticleDomain implements SimulationUiModel.Particles, SimulationUiActions.Particles {
        @Override
        public int particleCount() {
            return particles.particleCount();
        }

        @Override
        public int maximumParticleCount() {
            return particles.maxParticleCount();
        }

        @Override
        public int groupCount() {
            return particles.groupCount();
        }

        @Override
        public int maximumGroupCount() {
            return particles.maxGroupCount();
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
        public ImVec4 groupColor(int group) {
            ImVec4[] colors = particles.groupColors();
            return colors[Math.floorMod(group, colors.length)];
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
    }

    private final class VisualDomain implements SimulationUiModel.Visuals, SimulationUiActions.Visuals {
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

    private final class CameraDomain implements SimulationUiModel.Camera, SimulationUiActions.Camera {
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

        @Override
        public FourDVisualizationMode fourDVisualizationMode() {
            return particles.fourDViewConfiguration().visualizationMode();
        }

        @Override
        public void setFourDVisualizationMode(FourDVisualizationMode value) {
            settingChanged(() -> particles.fourDVisualizationMode(value));
        }

        @Override
        public float fourDXwAngleDegrees() {
            return degrees(particles.fourDXwAngle());
        }

        @Override
        public void setFourDXwAngleDegrees(float value) {
            settingChanged(() -> particles.fourDXwAngle(Math.toRadians(value)));
        }

        @Override
        public float fourDYwAngleDegrees() {
            return degrees(particles.fourDYwAngle());
        }

        @Override
        public void setFourDYwAngleDegrees(float value) {
            settingChanged(() -> particles.fourDYwAngle(Math.toRadians(value)));
        }

        @Override
        public float fourDZwAngleDegrees() {
            return degrees(particles.fourDZwAngle());
        }

        @Override
        public void setFourDZwAngleDegrees(float value) {
            settingChanged(() -> particles.fourDZwAngle(Math.toRadians(value)));
        }

        @Override
        public float fourDXwAutoSpeedDegrees() {
            return degrees(particles.fourDXwAutoSpeed());
        }

        @Override
        public void setFourDXwAutoSpeedDegrees(float value) {
            settingChanged(() -> particles.fourDXwAutoSpeed(Math.toRadians(value)));
        }

        @Override
        public boolean fourDXwAutoEnabled() {
            return particles.fourDXwAutoEnabled();
        }

        @Override
        public void setFourDXwAutoEnabled(boolean value) {
            settingChanged(() -> particles.fourDXwAutoEnabled(value));
        }

        @Override
        public float fourDYwAutoSpeedDegrees() {
            return degrees(particles.fourDYwAutoSpeed());
        }

        @Override
        public void setFourDYwAutoSpeedDegrees(float value) {
            settingChanged(() -> particles.fourDYwAutoSpeed(Math.toRadians(value)));
        }

        @Override
        public boolean fourDYwAutoEnabled() {
            return particles.fourDYwAutoEnabled();
        }

        @Override
        public void setFourDYwAutoEnabled(boolean value) {
            settingChanged(() -> particles.fourDYwAutoEnabled(value));
        }

        @Override
        public float fourDZwAutoSpeedDegrees() {
            return degrees(particles.fourDZwAutoSpeed());
        }

        @Override
        public void setFourDZwAutoSpeedDegrees(float value) {
            settingChanged(() -> particles.fourDZwAutoSpeed(Math.toRadians(value)));
        }

        @Override
        public boolean fourDZwAutoEnabled() {
            return particles.fourDZwAutoEnabled();
        }

        @Override
        public void setFourDZwAutoEnabled(boolean value) {
            settingChanged(() -> particles.fourDZwAutoEnabled(value));
        }

        @Override
        public boolean fourDViewMotionPaused() {
            return particles.fourDViewMotionPaused();
        }

        @Override
        public void setFourDViewMotionPaused(boolean value) {
            settingChanged(() -> particles.fourDViewMotionPaused(value));
        }

        @Override
        public void resetFourDOrientation() {
            settingChanged(particles::resetFourDOrientation);
        }

        @Override
        public float fourDPerspectiveDistance() {
            return (float) view().perspectiveDistance();
        }

        @Override
        public float minimumFourDPerspectiveDistance() {
            return (float) particles.minimumFourDPerspectiveDistance();
        }

        @Override
        public void setFourDPerspectiveDistance(float value) {
            settingChanged(() -> particles.fourDPerspectiveDistance(value));
        }

        @Override
        public float fourDSliceCenterW() {
            return (float) view().sliceCenterW();
        }

        @Override
        public void setFourDSliceCenterW(float value) {
            settingChanged(() -> particles.fourDSlice(value, view().sliceThickness(), view().sliceFeather()));
        }

        @Override
        public float fourDSliceThickness() {
            return (float) view().sliceThickness();
        }

        @Override
        public void setFourDSliceThickness(float value) {
            settingChanged(() -> particles.fourDSlice(view().sliceCenterW(), value,
                    Math.min(view().sliceFeather(), value * 0.5)));
        }

        @Override
        public float fourDSliceFeather() {
            return (float) view().sliceFeather();
        }

        @Override
        public void setFourDSliceFeather(float value) {
            settingChanged(() -> particles.fourDSlice(view().sliceCenterW(), view().sliceThickness(), value));
        }

        @Override
        public boolean fourDSliceSweepEnabled() {
            return particles.fourDSliceSweepEnabled();
        }

        @Override
        public void setFourDSliceSweepEnabled(boolean value) {
            settingChanged(() -> particles.fourDSliceSweepEnabled(value));
        }

        @Override
        public float fourDSliceSweepSpeed() {
            return (float) particles.fourDSliceSweepSpeed();
        }

        @Override
        public void setFourDSliceSweepSpeed(float value) {
            settingChanged(() -> particles.fourDSliceSweepSpeed(value));
        }

        @Override
        public float fourDColorRange() {
            return (float) view().colorRange();
        }

        @Override
        public void setFourDColorRange(float value) {
            settingChanged(() -> particles.fourDColorRange(value));
        }

        private FourDViewConfiguration view() {
            return particles.fourDViewConfiguration();
        }

        private float degrees(double radians) {
            return (float) Math.toDegrees(radians);
        }
    }

    private final class PerformanceDomain implements SimulationUiModel.Performance {
        @Override
        public SimulationUiDiagnostics diagnostics() {
            return diagnostics;
        }
    }

    private final class ApplicationDomain
            implements SimulationUiModel.Application, SimulationUiActions.Application {
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
            settingsController.onResetRequested();
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

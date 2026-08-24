package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SettingsController;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUI;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiDiagnostics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationUiAdapterTest {
    private GpuParticleSystem particles;
    private CameraController camera;
    private SimulationUI ui;
    private RecordingSettingsController settings;
    private SimulationUiAdapter adapter;
    private SimulationUiActions actions;

    @BeforeEach
    void setUp() {
        particles = new GpuParticleSystem();
        camera = new CameraController();
        ui = new SimulationUI();
        settings = new RecordingSettingsController(particles, camera, ui);
        adapter = new SimulationUiAdapter(particles, camera, ui, settings);
        actions = adapter.actions();
        ui.connect(adapter.model(), actions);
    }

    @Test
    void discreteSettingActionMutatesOnceAndSchedulesOneSave() {
        actions.simulation().setToroidalWrap(true);

        assertTrue(particles.toroidalWrap());
        assertEquals(1, settings.saveRequests);
    }

    @Test
    void repeatedSliderChangesRemainContinuousAndFeedTheDebouncedSaver() {
        actions.simulation().setForceFactor(1.5f);
        actions.simulation().setForceFactor(2.0f);
        actions.simulation().setForceFactor(2.5f);

        assertEquals(2.5f, particles.forceFactor(), 0.0001f);
        assertEquals(3, settings.saveRequests,
                "Each changed slider frame refreshes the SettingsController debounce deadline");
    }

    @Test
    void applicationSettingIsClampedByUiAndSchedulesOneSave() {
        actions.application().setFpsCap(Integer.MAX_VALUE);

        assertEquals(SimulationDefaults.MAX_FPS_CAP, ui.fpsCap());
        assertEquals(1, settings.saveRequests);
    }

    @Test
    void transientCameraResetDoesNotScheduleSettingsPersistence() {
        actions.camera().setFlySpeed(9.0f);
        assertEquals(1, settings.saveRequests);

        actions.camera().reset();

        assertEquals(1, settings.saveRequests);
    }

    @Test
    void playbackStepIsTransient() {
        actions.simulation().step();
        assertEquals(0, settings.saveRequests);
    }

    @Test
    void playbackPauseIsTransient() {
        actions.simulation().togglePause();

        assertTrue(ui.isPaused());
        assertEquals(0, settings.saveRequests);
    }

    @Test
    void diagnosticsArePreparedOnceAndSharedForTheWholeFrame() {
        SimulationUiDiagnostics unavailable = adapter.model().performance().diagnostics();

        adapter.prepareFrame();

        SimulationUiDiagnostics firstRead = adapter.model().performance().diagnostics();
        SimulationUiDiagnostics secondRead = adapter.model().performance().diagnostics();
        assertNotSame(unavailable, firstRead);
        assertSame(firstRead, secondRead);
        assertEquals(particles.particleCount(), firstRead.particleCount());
    }

    @Test
    void modelAndActionViewsAreLongLived() {
        assertSame(adapter.model(), adapter.model());
        assertSame(adapter.actions(), adapter.actions());
        assertSame(adapter.model().simulation(), adapter.model().simulation());
        assertSame(adapter.actions().visuals(), adapter.actions().visuals());
    }

    @Test
    void applicationCommandsDelegateExactlyOnce() {
        AtomicInteger saves = new AtomicInteger();
        AtomicInteger loads = new AtomicInteger();
        AtomicInteger exits = new AtomicInteger();
        adapter.onSavePreset(saves::incrementAndGet);
        adapter.onLoadPreset(loads::incrementAndGet);
        adapter.onExitApplication(exits::incrementAndGet);

        actions.application().savePreset();
        actions.application().loadPreset();
        actions.application().exit();
        actions.application().hideUi();

        assertEquals(1, saves.get());
        assertEquals(1, loads.get());
        assertEquals(1, exits.get());
        assertTrue(ui.isHidden());
        assertEquals(0, settings.saveRequests);
    }

    private static final class RecordingSettingsController extends SettingsController {
        private int saveRequests;

        private RecordingSettingsController(GpuParticleSystem particles, CameraController camera, SimulationUI ui) {
            super(particles, camera, ui);
        }

        @Override
        public void onSettingsChanged() {
            saveRequests++;
        }
    }
}

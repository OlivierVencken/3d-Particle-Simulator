package com.particle.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.settings.SimulationDefaults;
import org.junit.jupiter.api.Test;

class FixedSimulationClockTest {
    private static final double EPSILON = 0.000001;

    @Test
    void capsSimulationAtOneFixedStepPerRenderFrame() {
        assertEquals(30, countFrameLimitedStepsForOneSecondAtFps(30));
        assertEquals(60, countFrameLimitedStepsForOneSecondAtFps(60));
        assertEquals(60, countFrameLimitedStepsForOneSecondAtFps(120));
        assertEquals(60, countFrameLimitedStepsForOneSecondAtFps(240));
    }

    @Test
    void reportsElapsedSimulationTimeFromConsumedFixedSteps() {
        FixedSimulationClock clock =
                new FixedSimulationClock(SimulationDefaults.SIMULATION_STEP_SECONDS);

        clock.addFrameTime(1.0 / 30.0);

        assertTrue(clock.hasStep());
        assertEquals(1.0 / 60.0, clock.consumeStep(), EPSILON);
        assertTrue(clock.hasStep());
        assertEquals(2.0 / 60.0, clock.consumeStep(), EPSILON);
    }

    @Test
    void ignoresNegativeFrameTime() {
        FixedSimulationClock clock =
                new FixedSimulationClock(SimulationDefaults.SIMULATION_STEP_SECONDS);

        clock.addFrameTime(-1.0);

        assertFalse(clock.hasStep());
    }

    @Test
    void discardsWholeBacklogStepsButRetainsFractionalTime() {
        FixedSimulationClock clock = new FixedSimulationClock(0.01);
        clock.addFrameTime(0.035);

        assertEquals(0.03, clock.discardExcessSteps(), EPSILON);
        assertFalse(clock.hasStep());

        clock.addFrameTime(0.005);
        assertTrue(clock.hasStep());
    }

    private int countFrameLimitedStepsForOneSecondAtFps(int fps) {
        FixedSimulationClock clock =
                new FixedSimulationClock(SimulationDefaults.SIMULATION_STEP_SECONDS);
        int steps = 0;

        for (int frame = 0; frame < fps; frame++) {
            clock.addFrameTime(1.0 / fps);
            if (clock.hasStep()) {
                clock.consumeStep();
                clock.discardExcessSteps();
                steps++;
            }
        }

        return steps;
    }
}

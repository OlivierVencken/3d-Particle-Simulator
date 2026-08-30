package com.particle.sim.particles;

import com.particle.sim.math.Math4d;
import com.particle.sim.math.RotationPlane4d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FourDViewControllerTest {
    private static final double EPSILON = 1.0e-12;

    @Test
    void manualWPlaneControlsComposeInInputOrder() {
        FourDViewController controller = new FourDViewController();
        controller.rotateXw(0.4);
        controller.rotateYw(-0.25);
        controller.rotateZw(0.7);

        double[] expected = Math4d.multiply(Math4d.planeRotation(RotationPlane4d.ZW, 0.7),
                Math4d.multiply(Math4d.planeRotation(RotationPlane4d.YW, -0.25),
                        Math4d.planeRotation(RotationPlane4d.XW, 0.4)));
        assertArrayEquals(expected, controller.configuration().rotationMatrix(), EPSILON);
    }

    @Test
    void changingVisualizationDoesNotChangeOrientation() {
        FourDViewController controller = new FourDViewController();
        controller.rotateXw(0.5);
        double[] orientation = controller.configuration().rotationMatrix();

        controller.visualizationMode(FourDVisualizationMode.W_COLOR);
        assertArrayEquals(orientation, controller.configuration().rotationMatrix());
        controller.visualizationMode(FourDVisualizationMode.SLICE);
        assertArrayEquals(orientation, controller.configuration().rotationMatrix());
    }

    @Test
    void resetRestoresIdentityWithoutChangingVisualization() {
        FourDViewController controller = new FourDViewController();
        controller.visualizationMode(FourDVisualizationMode.SLICE);
        controller.rotateZw(1.0);
        assertNotEquals(Math4d.identity()[10], controller.configuration().rotationMatrix()[10]);

        controller.resetOrientation();

        assertEquals(FourDVisualizationMode.SLICE, controller.configuration().visualizationMode());
        assertArrayEquals(Math4d.identity(), controller.configuration().rotationMatrix());
    }

    @Test
    void longRunningManualRotationDoesNotAccumulateScaleOrShear() {
        FourDViewController controller = new FourDViewController();
        for (int i = 0; i < 10_000; i++) {
            controller.rotateXw(0.001);
            controller.rotateYw(-0.0007);
            controller.rotateZw(0.0003);
        }

        double[] matrix = controller.configuration().rotationMatrix();
        for (int first = 0; first < 4; first++) {
            for (int second = first; second < 4; second++) {
                double dot = 0.0;
                for (int row = 0; row < 4; row++) {
                    dot += matrix[first * 4 + row] * matrix[second * 4 + row];
                }
                assertEquals(first == second ? 1.0 : 0.0, dot, 1.0e-10);
            }
        }
    }

    @Test
    void automaticRotationIsFrameRateIndependent() {
        FourDViewController singleFrame = configuredAutomaticController();
        FourDViewController manyFrames = configuredAutomaticController();

        singleFrame.update(1.0, 4.0);
        for (int frame = 0; frame < 60; frame++) {
            manyFrames.update(1.0 / 60.0, 4.0);
        }

        assertArrayEquals(singleFrame.configuration().rotationMatrix(),
                manyFrames.configuration().rotationMatrix(), EPSILON);
        assertEquals(singleFrame.xwAngle(), manyFrames.xwAngle(), EPSILON);
        assertEquals(singleFrame.ywAngle(), manyFrames.ywAngle(), EPSILON);
        assertEquals(singleFrame.zwAngle(), manyFrames.zwAngle(), EPSILON);
    }

    @Test
    void viewPauseStopsRotationAndSliceSweepWithoutDependingOnSimulationPause() {
        FourDViewController controller = new FourDViewController();
        controller.visualizationMode(FourDVisualizationMode.SLICE);
        controller.sliceSweepEnabled(true);
        controller.motionPaused(true);
        FourDViewConfiguration before = controller.configuration();

        controller.update(1.0, 4.0);

        assertEquals(before, controller.configuration());
        controller.motionPaused(false);
        controller.update(1.0, 4.0);
        assertNotEquals(before, controller.configuration());
    }

    @Test
    void sliceSweepReflectsAtSimulationBounds() {
        FourDViewController controller = new FourDViewController();
        controller.visualizationMode(FourDVisualizationMode.SLICE);
        controller.slice(3.5, 1.0, 0.2);
        controller.sliceSweepEnabled(true);
        controller.sliceSweepSpeed(2.0);

        controller.update(1.0, 4.0);

        assertEquals(2.5, controller.configuration().sliceCenterW(), 1.0e-10);
    }

    @Test
    void resetStopsAutomaticRotation() {
        FourDViewController controller = configuredAutomaticController();
        controller.update(0.5, 4.0);

        controller.resetOrientation();
        assertEquals(0.0, controller.xwAutoSpeed(), EPSILON);
        assertEquals(0.0, controller.ywAutoSpeed(), EPSILON);
        assertEquals(0.0, controller.zwAutoSpeed(), EPSILON);
        assertFalse(controller.xwAutoEnabled());
        assertFalse(controller.ywAutoEnabled());
        assertFalse(controller.zwAutoEnabled());
        assertArrayEquals(Math4d.identity(), controller.configuration().rotationMatrix(), EPSILON);

        controller.update(1.0, 4.0);
        assertArrayEquals(Math4d.identity(), controller.configuration().rotationMatrix(), EPSILON);
    }

    @Test
    void perspectiveMinimumCoversAnyRotatedHypercubeCorner() {
        assertEquals(8.25, FourDViewController.minimumPerspectiveDistance(4.0), EPSILON);
        assertThrows(IllegalArgumentException.class,
                () -> FourDViewController.minimumPerspectiveDistance(Double.NaN));
    }

    private static FourDViewController configuredAutomaticController() {
        FourDViewController controller = new FourDViewController();
        controller.xwAutoSpeed(Math.toRadians(13.0));
        controller.ywAutoSpeed(Math.toRadians(-7.0));
        controller.zwAutoSpeed(Math.toRadians(3.0));
        controller.xwAutoEnabled(true);
        controller.ywAutoEnabled(true);
        controller.zwAutoEnabled(true);
        return controller;
    }
}

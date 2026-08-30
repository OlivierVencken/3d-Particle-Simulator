package com.particle.sim.particles;

import com.particle.sim.math.Math4d;
import com.particle.sim.math.RotationPlane4d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}

package com.particle.sim.particles;

import com.particle.sim.math.Math4d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FourDViewConfigurationTest {
    @Test
    void defaultsAreSafeAndPerspectiveOriented() {
        FourDViewConfiguration configuration = FourDViewConfiguration.defaults();

        assertEquals(SimulationDimension.THREE_D, SimulationDimension.DEFAULT);
        assertEquals(3, SimulationDimension.THREE_D.componentCount());
        assertEquals(4, SimulationDimension.FOUR_D.componentCount());
        assertEquals(FourDVisualizationMode.PERSPECTIVE, configuration.visualizationMode());
        assertArrayEquals(Math4d.identity(), configuration.rotationMatrix());
        assertEquals(FourDViewConfiguration.DEFAULT_PERSPECTIVE_DISTANCE,
                configuration.perspectiveDistance());
    }

    @Test
    void rotationMatrixIsDefensivelyCopiedOnInputAndOutput() {
        double[] source = Math4d.identity();
        FourDViewConfiguration configuration = new FourDViewConfiguration(
                FourDVisualizationMode.SLICE, source, 10.0, 0.5, 2.0, 0.25, 3.0);
        source[0] = 99.0;

        double[] firstRead = configuration.rotationMatrix();
        firstRead[5] = 77.0;

        assertArrayEquals(Math4d.identity(), configuration.rotationMatrix());
    }

    @Test
    void featherIsLimitedToHalfTheSliceThickness() {
        FourDViewConfiguration configuration = new FourDViewConfiguration(
                FourDVisualizationMode.W_COLOR, Math4d.identity(), 10.0, 0.0, 2.0, 8.0, 4.0);

        assertEquals(1.0, configuration.sliceFeather());
    }

    @Test
    void valueEqualityIncludesMatrixAndProjectionSettings() {
        FourDViewConfiguration first = FourDViewConfiguration.defaults();
        FourDViewConfiguration same = FourDViewConfiguration.defaults();
        FourDViewConfiguration different = new FourDViewConfiguration(
                FourDVisualizationMode.SLICE, Math4d.identity(), 12.0, 0.0, 1.0, 0.2, 4.0);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new FourDViewConfiguration(null, Math4d.identity(), 10.0, 0.0, 1.0, 0.1, 4.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FourDViewConfiguration(FourDVisualizationMode.PERSPECTIVE, new double[15],
                        10.0, 0.0, 1.0, 0.1, 4.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FourDViewConfiguration(FourDVisualizationMode.PERSPECTIVE, Math4d.identity(),
                        0.0, 0.0, 1.0, 0.1, 4.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FourDViewConfiguration(FourDVisualizationMode.PERSPECTIVE, Math4d.identity(),
                        10.0, 0.0, 0.0, 0.1, 4.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FourDViewConfiguration(FourDVisualizationMode.PERSPECTIVE, Math4d.identity(),
                        10.0, 0.0, 1.0, -0.1, 4.0));
    }
}

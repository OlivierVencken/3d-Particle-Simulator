package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FourDDiagnosticPointSetTest {
    @Test
    void tesseractContainsAllSixteenFourDimensionalCorners() {
        FourDDiagnosticPointSet points = FourDDiagnosticPointSet.tesseract(2.0f);

        assertEquals(16, points.particleCount());
        float[] positions = points.positions();
        for (float coordinate : positions) {
            assertTrue(coordinate == -2.0f || coordinate == 2.0f);
        }
    }

    @Test
    void hypersphereSamplesAreDeterministicAndLieOnRequestedRadius() {
        FourDDiagnosticPointSet first = FourDDiagnosticPointSet.hypersphere(3.0f, 64);
        FourDDiagnosticPointSet second = FourDDiagnosticPointSet.hypersphere(3.0f, 64);

        assertArrayEquals(first.positions(), second.positions());
        float[] positions = first.positions();
        for (int particle = 0; particle < first.particleCount(); particle++) {
            double lengthSquared = 0.0;
            for (int axis = 0; axis < 4; axis++) {
                float component = positions[particle * 4 + axis];
                assertTrue(Float.isFinite(component));
                lengthSquared += component * component;
            }
            assertEquals(9.0, lengthSquared, 1.0e-5);
        }
    }

    @Test
    void diagnosticStateIsDefensivelyCopied() {
        FourDDiagnosticPointSet points = FourDDiagnosticPointSet.standard();
        float[] first = points.positions();
        first[0] = 99.0f;

        assertNotSame(first, points.positions());
        assertTrue(points.positions()[0] != 99.0f);
        assertEquals(points.particleCount() * 4, points.velocities().length);
        assertEquals(points.particleCount(), points.groups().length);
    }

    @Test
    void malformedCustomPointSetsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> FourDDiagnosticPointSet.of(new float[3]));
        assertThrows(IllegalArgumentException.class,
                () -> FourDDiagnosticPointSet.of(new float[] { 0.0f, 0.0f, 0.0f, Float.NaN }));
    }
}

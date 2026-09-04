package com.particle.sim.particles.gpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.particle.sim.particles.ParticleSimulationConfig;
import org.junit.jupiter.api.Test;

class ParticleComputeParametersTest {
    @Test
    void capturesAttractionValuesWithoutExposingMutableState() {
        float[] matrix = {0.25f, -0.5f};

        ParticleComputeParameters parameters =
                ParticleComputeParameters.from(ParticleSimulationConfig.defaults(), matrix);
        matrix[0] = 1.0f;
        float[] firstRead = parameters.attractionMatrix();
        firstRead[1] = 1.0f;

        assertEquals(0.25f, parameters.attractionMatrix()[0]);
        assertEquals(-0.5f, parameters.attractionMatrix()[1]);
        assertNotSame(firstRead, parameters.attractionMatrix());
    }
}

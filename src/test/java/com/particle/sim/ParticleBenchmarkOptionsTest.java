package com.particle.sim;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParticleBenchmarkOptionsTest {
    @Test
    void defaultsSupportAutomaticCapacitySearch() {
        ParticleBenchmarkOptions options = ParticleBenchmarkOptions.parse(new String[] { "--benchmark" });

        assertNull(options.particleCount());
        assertEquals(10, options.warmupSteps());
        assertEquals(30, options.sampleSteps());
    }

    @Test
    void parsesExplicitBenchmarkControls() {
        ParticleBenchmarkOptions options = ParticleBenchmarkOptions.parse(new String[] {
                "--benchmark", "--particles=100000", "--warmup=2", "--samples=5", "--output=results/run.json"
        });

        assertEquals(100_000, options.particleCount());
        assertEquals(2, options.warmupSteps());
        assertEquals(5, options.sampleSteps());
        assertEquals(Path.of("results/run.json"), options.outputPath());
    }

    @Test
    void rejectsUnknownOrInvalidOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> ParticleBenchmarkOptions.parse(new String[] { "--benchmark", "--samples=0" }));
        assertThrows(IllegalArgumentException.class,
                () -> ParticleBenchmarkOptions.parse(new String[] { "--benchmark", "--wat" }));
    }
}

package com.particle.sim;

import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.particles.SpawnMode;
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
        assertEquals(SimulationDimension.THREE_D, options.simulationDimension());
        assertEquals(SpawnMode.RANDOM, options.spawnMode());
    }

    @Test
    void parsesExplicitBenchmarkControls() {
        ParticleBenchmarkOptions options = ParticleBenchmarkOptions.parse(new String[] {
                "--benchmark", "--particles=100000", "--warmup=2", "--samples=5", "--output=results/run.json",
                "--dimension=4d", "--spawn=shell"
        });

        assertEquals(100_000, options.particleCount());
        assertEquals(2, options.warmupSteps());
        assertEquals(5, options.sampleSteps());
        assertEquals(Path.of("results/run.json"), options.outputPath());
        assertEquals(SimulationDimension.FOUR_D, options.simulationDimension());
        assertEquals(SpawnMode.SHELL, options.spawnMode());
    }

    @Test
    void rejectsUnknownOrInvalidOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> ParticleBenchmarkOptions.parse(new String[] { "--benchmark", "--samples=0" }));
        assertThrows(IllegalArgumentException.class,
                () -> ParticleBenchmarkOptions.parse(new String[] { "--benchmark", "--wat" }));
        assertThrows(IllegalArgumentException.class,
                () -> ParticleBenchmarkOptions.parse(new String[] { "--benchmark", "--dimension=4d", "--spawn=spiral" }));
    }
}

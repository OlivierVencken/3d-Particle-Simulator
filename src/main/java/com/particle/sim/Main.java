package com.particle.sim;

import com.particle.sim.app.ParticleSimulatorApp;
import com.particle.sim.benchmark.ParticleBenchmarkRunner;
import com.particle.sim.startup.StartupFailureException;

/** Application entry point. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        if (ParticleBenchmarkRunner.requested(args)) {
            ParticleBenchmarkRunner.run(args);
            return;
        }
        try {
            new ParticleSimulatorApp().run();
        } catch (StartupFailureException ignored) {
            System.exit(1);
        }
    }
}

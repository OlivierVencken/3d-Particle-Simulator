package com.particle.sim.diagnostics;

public record SystemLoadSnapshot(
        double cpuLoad, long usedMemoryBytes, long totalMemoryBytes, double gpuLoad) {}

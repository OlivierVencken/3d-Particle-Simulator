package com.particle.sim.system;

public record SystemLoadSnapshot(double cpuLoad, long usedMemoryBytes, long totalMemoryBytes, double gpuLoad) {
}

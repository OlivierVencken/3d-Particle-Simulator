package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glGenBuffers;
import static org.lwjgl.opengl.GL43C.glMemoryBarrier;

final class TrailHistoryBuffers {
    private static final int COMPONENTS_PER_PARTICLE = 4;
    private static final int BYTES_PER_PARTICLE = COMPONENTS_PER_PARTICLE * Float.BYTES;

    private int historySsbo;
    private int particleCapacity;
    private int sampleCapacity;
    private int nextSampleIndex;
    private int sampleCount;

    int historySsbo() {
        return historySsbo;
    }

    int particleCapacity() {
        return particleCapacity;
    }

    int sampleCapacity() {
        return sampleCapacity;
    }

    int newestSampleIndex() {
        if (sampleCount == 0 || sampleCapacity == 0) {
            return 0;
        }
        return (nextSampleIndex + sampleCapacity - 1) % sampleCapacity;
    }

    int sampleCount() {
        return sampleCount;
    }

    long allocatedBytes() {
        return (long) particleCapacity * sampleCapacity * BYTES_PER_PARTICLE;
    }

    void capture(ParticleBuffers particleBuffers, int particleCount, int desiredSamples) {
        if (particleCount <= 0 || desiredSamples <= 1) {
            clear();
            return;
        }

        ensureCapacity(particleCount, desiredSamples);
        long targetByteOffset = (long) nextSampleIndex * particleCapacity * BYTES_PER_PARTICLE;
        particleBuffers.copyPositionsTo(historySsbo, targetByteOffset, particleCount);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        nextSampleIndex = (nextSampleIndex + 1) % sampleCapacity;
        sampleCount = Math.min(sampleCount + 1, sampleCapacity);
    }

    void clear() {
        nextSampleIndex = 0;
        sampleCount = 0;
    }

    void dispose() {
        glDeleteBuffers(historySsbo);
        historySsbo = 0;
        particleCapacity = 0;
        sampleCapacity = 0;
        clear();
    }

    private void ensureCapacity(int particleCount, int desiredSamples) {
        if (historySsbo != 0 && particleCapacity >= particleCount && sampleCapacity >= desiredSamples) {
            return;
        }

        glDeleteBuffers(historySsbo);
        particleCapacity = Math.max(1, particleCount);
        sampleCapacity = Math.max(2, desiredSamples);
        long bufferBytes = (long) particleCapacity * sampleCapacity * BYTES_PER_PARTICLE;

        historySsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, historySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        clear();
    }
}

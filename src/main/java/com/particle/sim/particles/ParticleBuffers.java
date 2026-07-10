package com.particle.sim.particles;

import java.nio.FloatBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL43C.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_COPY_WRITE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glBufferSubData;
import static org.lwjgl.opengl.GL43C.glCopyBufferSubData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glGenBuffers;
import static org.lwjgl.opengl.GL43C.glGetBufferSubData;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

final class ParticleBuffers {
    private int positionSsbo;
    private int velocitySsbo;
    private int nextPositionSsbo;
    private int nextVelocitySsbo;
    private int particleCapacity;

    int positionSsbo() {
        return positionSsbo;
    }

    int velocitySsbo() {
        return velocitySsbo;
    }

    int nextPositionSsbo() {
        return nextPositionSsbo;
    }

    int nextVelocitySsbo() {
        return nextVelocitySsbo;
    }

    long allocatedBytes() {
        return (long) particleCapacity * 4L * 4L * Float.BYTES;
    }

    float[] readPositions(int particleCount) {
        return readFloatBuffer(positionSsbo, particleCount);
    }

    float[] readVelocities(int particleCount) {
        return readFloatBuffer(velocitySsbo, particleCount);
    }

    void resize(int oldParticleCount, int requestedParticleCount, boolean preserveExisting,
            ParticleSimulationConfig config, Random random) {
        int copiedParticleCount = preserveExisting ? Math.min(oldParticleCount, requestedParticleCount) : 0;
        int appendedParticleCount = requestedParticleCount - copiedParticleCount;

        if (requestedParticleCount <= particleCapacity) {
            if (!preserveExisting && requestedParticleCount > 0) {
                uploadRandomParticles(positionSsbo, velocitySsbo, 0, requestedParticleCount, config, random);
            } else if (appendedParticleCount > 0) {
                long byteOffset = (long) copiedParticleCount * 4L * Float.BYTES;
                uploadRandomParticles(positionSsbo, velocitySsbo, byteOffset, appendedParticleCount, config, random);
            }
            return;
        }

        int newParticleCapacity = grownCapacity(particleCapacity, requestedParticleCount);

        int newPositionSsbo = glGenBuffers();
        int newVelocitySsbo = glGenBuffers();
        int newNextPositionSsbo = glGenBuffers();
        int newNextVelocitySsbo = glGenBuffers();
        long newBufferBytes = particleBufferBytes(newParticleCapacity);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newPositionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newVelocitySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newNextPositionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newNextVelocitySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);

        if (copiedParticleCount > 0) {
            long copiedBytes = (long) copiedParticleCount * 4L * Float.BYTES;
            copyBufferPrefix(positionSsbo, newPositionSsbo, copiedBytes);
            copyBufferPrefix(velocitySsbo, newVelocitySsbo, copiedBytes);
        }

        if (appendedParticleCount > 0) {
            long byteOffset = (long) copiedParticleCount * 4L * Float.BYTES;
            uploadRandomParticles(newPositionSsbo, newVelocitySsbo, byteOffset, appendedParticleCount, config, random);
        }

        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(nextPositionSsbo);
        glDeleteBuffers(nextVelocitySsbo);

        positionSsbo = newPositionSsbo;
        velocitySsbo = newVelocitySsbo;
        nextPositionSsbo = newNextPositionSsbo;
        nextVelocitySsbo = newNextVelocitySsbo;
        particleCapacity = newParticleCapacity;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    void swapState() {
        int oldPositionSsbo = positionSsbo;
        int oldVelocitySsbo = velocitySsbo;
        positionSsbo = nextPositionSsbo;
        velocitySsbo = nextVelocitySsbo;
        nextPositionSsbo = oldPositionSsbo;
        nextVelocitySsbo = oldVelocitySsbo;
    }

    private void uploadRandomParticles(int targetPositionSsbo, int targetVelocitySsbo, long byteOffset, int count,
            ParticleSimulationConfig config, Random random) {
        FloatBuffer positions = null;
        FloatBuffer velocities = null;
        try {
            positions = memAllocFloat(Math.multiplyExact(count, 4));
            velocities = memAllocFloat(Math.multiplyExact(count, 4));
            ParticleSpawner.spawnParticles(positions, velocities, count, config.bounds(), config.groupCount(),
                    config.spawnMode(), random);

            positions.flip();
            velocities.flip();

            glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetPositionSsbo);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, byteOffset, positions);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetVelocitySsbo);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, byteOffset, velocities);
        } finally {
            if (positions != null) {
                memFree(positions);
            }
            if (velocities != null) {
                memFree(velocities);
            }
        }
    }

    private static void copyBufferPrefix(int sourceBuffer, int targetBuffer, long byteCount) {
        glBindBuffer(GL_COPY_READ_BUFFER, sourceBuffer);
        glBindBuffer(GL_COPY_WRITE_BUFFER, targetBuffer);
        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, byteCount);
    }

    private static long particleBufferBytes(int count) {
        return (long) Math.max(count, 1) * 4L * Float.BYTES;
    }

    private static float[] readFloatBuffer(int buffer, int particleCount) {
        int floatCount = Math.multiplyExact(particleCount, 4);
        FloatBuffer data = memAllocFloat(floatCount);
        try {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
            glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
            float[] result = new float[floatCount];
            data.get(result);
            return result;
        } finally {
            memFree(data);
        }
    }

    private static int grownCapacity(int currentCapacity, int requiredCapacity) {
        int capacity = Math.max(currentCapacity, 1);
        while (capacity < requiredCapacity) {
            capacity = Math.max(requiredCapacity, capacity + Math.max(capacity / 2, 1));
        }
        return capacity;
    }

    void dispose() {
        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(nextPositionSsbo);
        glDeleteBuffers(nextVelocitySsbo);
    }
}

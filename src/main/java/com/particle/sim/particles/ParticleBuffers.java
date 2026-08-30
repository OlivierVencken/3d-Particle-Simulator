package com.particle.sim.particles;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;

final class ParticleBuffers {
    private int positionSsbo;
    private int velocitySsbo;
    private int nextPositionSsbo;
    private int nextVelocitySsbo;
    private int groupSsbo;
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

    int groupSsbo() {
        return groupSsbo;
    }

    long allocatedBytes() {
        return (long) particleCapacity * (4L * 4L * Float.BYTES + Integer.BYTES);
    }

    float[] readPositions(int particleCount) {
        return readFloatBuffer(positionSsbo, particleCount);
    }

    float[] readVelocities(int particleCount) {
        return readFloatBuffer(velocitySsbo, particleCount);
    }

    int[] readGroups(int particleCount) {
        return readIntBuffer(groupSsbo, particleCount);
    }

    void replaceState(float[] positions, float[] velocities, int[] groups) {
        writeFloatBuffer(positionSsbo, positions);
        writeFloatBuffer(velocitySsbo, velocities);
        writeIntBuffer(groupSsbo, groups);
    }

    void resize(int oldParticleCount, int requestedParticleCount, boolean preserveExisting,
            ParticleSimulationConfig config, Random random) {
        int copiedParticleCount = preserveExisting ? Math.min(oldParticleCount, requestedParticleCount) : 0;
        int appendedParticleCount = requestedParticleCount - copiedParticleCount;

        if (requestedParticleCount <= particleCapacity) {
            if (!preserveExisting && requestedParticleCount > 0) {
                uploadRandomParticles(positionSsbo, velocitySsbo, groupSsbo, 0, 0, requestedParticleCount, config,
                        random);
            } else if (appendedParticleCount > 0) {
                long vectorByteOffset = (long) copiedParticleCount * 4L * Float.BYTES;
                long groupByteOffset = (long) copiedParticleCount * Integer.BYTES;
                uploadRandomParticles(positionSsbo, velocitySsbo, groupSsbo, vectorByteOffset, groupByteOffset,
                        appendedParticleCount, config, random);
            }
            return;
        }

        int newParticleCapacity = grownCapacity(particleCapacity, requestedParticleCount);

        int newPositionSsbo = glGenBuffers();
        int newVelocitySsbo = glGenBuffers();
        int newNextPositionSsbo = glGenBuffers();
        int newNextVelocitySsbo = glGenBuffers();
        int newGroupSsbo = glGenBuffers();
        long newBufferBytes = particleBufferBytes(newParticleCapacity);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newPositionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newVelocitySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newNextPositionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newNextVelocitySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newGroupSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, groupBufferBytes(newParticleCapacity), GL_DYNAMIC_DRAW);

        if (copiedParticleCount > 0) {
            long copiedBytes = (long) copiedParticleCount * 4L * Float.BYTES;
            copyBufferPrefix(positionSsbo, newPositionSsbo, copiedBytes);
            copyBufferPrefix(velocitySsbo, newVelocitySsbo, copiedBytes);
            copyBufferPrefix(groupSsbo, newGroupSsbo, (long) copiedParticleCount * Integer.BYTES);
        }

        if (appendedParticleCount > 0) {
            long vectorByteOffset = (long) copiedParticleCount * 4L * Float.BYTES;
            long groupByteOffset = (long) copiedParticleCount * Integer.BYTES;
            uploadRandomParticles(newPositionSsbo, newVelocitySsbo, newGroupSsbo, vectorByteOffset, groupByteOffset,
                    appendedParticleCount, config, random);
        }

        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(nextPositionSsbo);
        glDeleteBuffers(nextVelocitySsbo);
        glDeleteBuffers(groupSsbo);

        positionSsbo = newPositionSsbo;
        velocitySsbo = newVelocitySsbo;
        nextPositionSsbo = newNextPositionSsbo;
        nextVelocitySsbo = newNextVelocitySsbo;
        groupSsbo = newGroupSsbo;
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

    private void uploadRandomParticles(int targetPositionSsbo, int targetVelocitySsbo, int targetGroupSsbo,
            long vectorByteOffset, long groupByteOffset, int count, ParticleSimulationConfig config, Random random) {
        FloatBuffer positions = null;
        FloatBuffer velocities = null;
        IntBuffer groups = null;
        try {
            positions = memAllocFloat(Math.multiplyExact(count, 4));
            velocities = memAllocFloat(Math.multiplyExact(count, 4));
            groups = memAllocInt(count);
            ParticleSpawner.spawnParticles(positions, velocities, groups, count, config.bounds(), config.groupCount(),
                    config.spawnMode(), random);

            positions.flip();
            velocities.flip();
            groups.flip();

            glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetPositionSsbo);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, vectorByteOffset, positions);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetVelocitySsbo);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, vectorByteOffset, velocities);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetGroupSsbo);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, groupByteOffset, groups);
        } finally {
            if (positions != null) {
                memFree(positions);
            }
            if (velocities != null) {
                memFree(velocities);
            }
            if (groups != null) {
                memFree(groups);
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

    private static long groupBufferBytes(int count) {
        return (long) Math.max(count, 1) * Integer.BYTES;
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

    private static void writeFloatBuffer(int buffer, float[] values) {
        FloatBuffer data = memAllocFloat(values.length);
        try {
            data.put(values).flip();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        } finally {
            memFree(data);
        }
    }

    private static int[] readIntBuffer(int buffer, int particleCount) {
        IntBuffer data = memAllocInt(particleCount);
        try {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
            glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
            int[] result = new int[particleCount];
            data.get(result);
            return result;
        } finally {
            memFree(data);
        }
    }

    private static void writeIntBuffer(int buffer, int[] values) {
        IntBuffer data = memAllocInt(values.length);
        try {
            data.put(values).flip();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
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
        glDeleteBuffers(groupSsbo);
    }
}

package com.particle.sim.particles;

import org.lwjgl.BufferUtils;

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

final class ParticleBuffers {
    private int positionSsbo;
    private int velocitySsbo;
    private int nextPositionSsbo;
    private int nextVelocitySsbo;
    private long bufferBytes;

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

    void resize(int oldParticleCount, int requestedParticleCount, boolean preserveExisting,
            ParticleSimulationConfig config, Random random) {
        int copiedParticleCount = preserveExisting ? Math.min(oldParticleCount, requestedParticleCount) : 0;
        int appendedParticleCount = requestedParticleCount - copiedParticleCount;

        int newPositionSsbo = glGenBuffers();
        int newVelocitySsbo = glGenBuffers();
        int newNextPositionSsbo = glGenBuffers();
        int newNextVelocitySsbo = glGenBuffers();
        long newBufferBytes = particleBufferBytes(requestedParticleCount);

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
        } else if (requestedParticleCount == 0) {
            uploadZeroParticle(newPositionSsbo, newVelocitySsbo);
        }

        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(nextPositionSsbo);
        glDeleteBuffers(nextVelocitySsbo);

        positionSsbo = newPositionSsbo;
        velocitySsbo = newVelocitySsbo;
        nextPositionSsbo = newNextPositionSsbo;
        nextVelocitySsbo = newNextVelocitySsbo;
        bufferBytes = newBufferBytes;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    void copyPositionsTo(int targetSsbo, long targetByteOffset, int particleCount) {
        long byteCount = particleBufferBytes(particleCount);
        glBindBuffer(GL_COPY_READ_BUFFER, positionSsbo);
        glBindBuffer(GL_COPY_WRITE_BUFFER, targetSsbo);
        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, targetByteOffset, byteCount);
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
        FloatBuffer positions = BufferUtils.createFloatBuffer(count * 4);
        FloatBuffer velocities = BufferUtils.createFloatBuffer(count * 4);

        ParticleSpawner.spawnParticles(positions, velocities, count, config.bounds(), config.groupCount(),
                config.spawnMode(), random);

        positions.flip();
        velocities.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetPositionSsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, byteOffset, positions);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetVelocitySsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, byteOffset, velocities);
    }

    private void uploadZeroParticle(int targetPositionSsbo, int targetVelocitySsbo) {
        FloatBuffer positions = BufferUtils.createFloatBuffer(4);
        FloatBuffer velocities = BufferUtils.createFloatBuffer(4);
        positions.put(0.0f).put(0.0f).put(0.0f).put(0.0f).flip();
        velocities.put(0.0f).put(0.0f).put(0.0f).put(0.0f).flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetPositionSsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, positions);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, targetVelocitySsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, velocities);
    }

    private static void copyBufferPrefix(int sourceBuffer, int targetBuffer, long byteCount) {
        glBindBuffer(GL_COPY_READ_BUFFER, sourceBuffer);
        glBindBuffer(GL_COPY_WRITE_BUFFER, targetBuffer);
        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, byteCount);
    }

    private static long particleBufferBytes(int count) {
        return (long) Math.max(count, 1) * 4L * Float.BYTES;
    }

    void dispose() {
        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(nextPositionSsbo);
        glDeleteBuffers(nextVelocitySsbo);
    }
}

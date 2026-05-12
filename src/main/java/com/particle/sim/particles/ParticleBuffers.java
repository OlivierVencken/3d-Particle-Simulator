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

    int positionSsbo() {
        return positionSsbo;
    }

    int velocitySsbo() {
        return velocitySsbo;
    }

    void resize(int oldParticleCount, int requestedParticleCount, boolean preserveExisting,
            ParticleSimulationConfig config, Random random) {
        int copiedParticleCount = preserveExisting ? Math.min(oldParticleCount, requestedParticleCount) : 0;
        int appendedParticleCount = requestedParticleCount - copiedParticleCount;

        int newPositionSsbo = glGenBuffers();
        int newVelocitySsbo = glGenBuffers();
        long newBufferBytes = particleBufferBytes(requestedParticleCount);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newPositionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, newBufferBytes, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, newVelocitySsbo);
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

        positionSsbo = newPositionSsbo;
        velocitySsbo = newVelocitySsbo;
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
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
    }
}

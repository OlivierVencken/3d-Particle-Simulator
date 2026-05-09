package com.particle.sim.particles;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL43C.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_COPY_WRITE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glCopyBufferSubData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glGenBuffers;
import static org.lwjgl.opengl.GL43C.glBufferSubData;
import static org.lwjgl.opengl.GL43C.glClearBufferData;
import static org.lwjgl.opengl.GL43C.GL_R32I;
import static org.lwjgl.opengl.GL43C.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL43C.GL_INT;

public final class GpuParticleSystem {
    private static final int INITIAL_PARTICLE_COUNT = 65_536;
    private static final int WORK_GROUP_SIZE = 256;
    public static final int GROUP_COUNT = 6;
    public static final int SPATIAL_MAP_SIZE = 524287;
    private static final int MAX_PARTICLES_PER_CELL = 128;
    private static final int MAX_GROUPS = 16;
    private static final int MAX_PARTICLE_COUNT = 1_000_000;

    private int positionSsbo;
    private int velocitySsbo;
    private int gridDataSsbo;
    private int gridCountsSsbo;

    private final ParticleRenderer renderer = new ParticleRenderer();
    private final ParticleCompute compute = new ParticleCompute();

    private int particleCount = INITIAL_PARTICLE_COUNT;
    private float pointSize = 2.2f;
    private float bounds = 4.0f;
    private float forceFactor = 1.0f;
    private float velocityDamping = 0.965f;
    private float interactionRange = 0.95f;
    private float repulsionRadius = 0.3f;
    private float maxVelocity = 8.0f;
    private float boundaryBounce = 0.65f;
    private boolean toroidalWrap;
    private final float[] attractionMatrix = new float[MAX_GROUPS * MAX_GROUPS];
    private final Random matrixRandom = new Random();
    private final Random particleRandom = new Random();

    public void init() {
        compute.init();
        renderer.init();
        initSpatialGrid();
        initAttractionMatrix();
        reset();
    }

    private void initSpatialGrid() {
        gridDataSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridDataSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) SPATIAL_MAP_SIZE * MAX_PARTICLES_PER_CELL * Integer.BYTES,
                GL_DYNAMIC_DRAW);

        gridCountsSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) SPATIAL_MAP_SIZE * Integer.BYTES, GL_STREAM_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void initAttractionMatrix() {
        for (int i = 0; i < GROUP_COUNT; i++) {
            for (int j = 0; j < GROUP_COUNT; j++) {
                float value = -0.6f + matrixRandom.nextFloat() * 1.4f;
                if (i == j) {
                    value += 0.25f;
                }
                attractionMatrix[i * GROUP_COUNT + j] = value;
            }
        }
    }

    public void reset() {
        resizeParticles(particleCount, false);
    }

    public void update(float deltaTime, float elapsedTime) {
        if (particleCount == 0) {
            return;
        }

        compute.bindBuffers(positionSsbo, velocitySsbo, gridDataSsbo, gridCountsSsbo);

        clearGridCounts();
        compute.setUniforms(this, deltaTime, 0);
        compute.dispatch(particleCount, WORK_GROUP_SIZE, false);

        compute.setUniforms(this, deltaTime, 1);
        compute.dispatch(particleCount, WORK_GROUP_SIZE, true);
    }

    private void clearGridCounts() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, new int[] { 0 });
    }

    public void render(int width, int height, float[] viewMatrix) {
        renderer.render(width, height, viewMatrix, positionSsbo, particleCount, pointSize);
    }

    public void dispose() {
        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(gridDataSsbo);
        glDeleteBuffers(gridCountsSsbo);
        compute.dispose();
        renderer.dispose();
    }

    public int particleCount() {
        return particleCount;
    }

    public int maxParticleCount() {
        return MAX_PARTICLE_COUNT;
    }

    public void addParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        resizeParticles(particleCount + amount, true);
    }

    public void removeParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        resizeParticles(particleCount - amount, true);
    }

    public void clearParticles() {
        resizeParticles(0, false);
    }

    private void resizeParticles(int requestedParticleCount, boolean preserveExisting) {
        int oldParticleCount = particleCount;
        int newParticleCount = Math.max(0, Math.min(MAX_PARTICLE_COUNT, requestedParticleCount));
        int copiedParticleCount = preserveExisting ? Math.min(oldParticleCount, newParticleCount) : 0;
        int appendedParticleCount = newParticleCount - copiedParticleCount;

        int newPositionSsbo = glGenBuffers();
        int newVelocitySsbo = glGenBuffers();
        long newBufferBytes = particleBufferBytes(newParticleCount);

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
            uploadRandomParticles(newPositionSsbo, newVelocitySsbo, byteOffset, appendedParticleCount);
        } else if (newParticleCount == 0) {
            uploadZeroParticle(newPositionSsbo, newVelocitySsbo);
        }

        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);

        positionSsbo = newPositionSsbo;
        velocitySsbo = newVelocitySsbo;
        particleCount = newParticleCount;
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void uploadRandomParticles(int targetPositionSsbo, int targetVelocitySsbo, long byteOffset, int count) {
        FloatBuffer positions = BufferUtils.createFloatBuffer(count * 4);
        FloatBuffer velocities = BufferUtils.createFloatBuffer(count * 4);

        for (int i = 0; i < count; i++) {
            appendRandomParticle(positions, velocities);
        }

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

    private void appendRandomParticle(FloatBuffer positions, FloatBuffer velocities) {
        float radius = particleRandom.nextFloat() * bounds * 0.6f;
        float theta = particleRandom.nextFloat() * (float) (Math.PI * 2.0);
        float phi = (float) Math.acos(2.0f * particleRandom.nextFloat() - 1.0f);

        float x = radius * (float) Math.sin(phi) * (float) Math.cos(theta);
        float y = radius * (float) Math.cos(phi);
        float z = radius * (float) Math.sin(phi) * (float) Math.sin(theta);
        int group = particleRandom.nextInt(GROUP_COUNT);

        positions.put(x).put(y).put(z).put(group);
        velocities
                .put((particleRandom.nextFloat() - 0.5f) * 0.2f)
                .put((particleRandom.nextFloat() - 0.5f) * 0.2f)
                .put((particleRandom.nextFloat() - 0.5f) * 0.2f)
                .put(0.0f);
    }

    private static void copyBufferPrefix(int sourceBuffer, int targetBuffer, long byteCount) {
        glBindBuffer(GL_COPY_READ_BUFFER, sourceBuffer);
        glBindBuffer(GL_COPY_WRITE_BUFFER, targetBuffer);
        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, byteCount);
    }

    private static long particleBufferBytes(int count) {
        return (long) Math.max(count, 1) * 4L * Float.BYTES;
    }

    public float pointSize() {
        return pointSize;
    }

    public void pointSize(float pointSize) {
        this.pointSize = pointSize;
    }

    public float forceFactor() {
        return forceFactor;
    }

    public void forceFactor(float forceFactor) {
        this.forceFactor = forceFactor;
    }

    public float interactionRange() {
        return interactionRange;
    }

    public void interactionRange(float interactionRange) {
        this.interactionRange = interactionRange;
    }

    public float velocityDamping() {
        return velocityDamping;
    }

    public void velocityDamping(float velocityDamping) {
        this.velocityDamping = velocityDamping;
    }

    public float repulsionRadius() {
        return repulsionRadius;
    }

    public void repulsionRadius(float repulsionRadius) {
        this.repulsionRadius = repulsionRadius;
    }

    public float maxVelocity() {
        return maxVelocity;
    }

    public void maxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public float boundaryBounce() {
        return boundaryBounce;
    }

    public void boundaryBounce(float boundaryBounce) {
        this.boundaryBounce = boundaryBounce;
    }

    public float bounds() {
        return bounds;
    }

    public void bounds(float bounds) {
        this.bounds = bounds;
    }

    public boolean toroidalWrap() {
        return toroidalWrap;
    }

    public void toroidalWrap(boolean toroidalWrap) {
        this.toroidalWrap = toroidalWrap;
    }

    public float attraction(int groupA, int groupB) {
        return attractionMatrix[groupA * GROUP_COUNT + groupB];
    }

    public void attraction(int groupA, int groupB, float value) {
        attractionMatrix[groupA * GROUP_COUNT + groupB] = clampAttraction(value);
    }

    public float[] getAttractionMatrix() {
        return attractionMatrix;
    }

    public void adjustAttraction(int groupA, int groupB, float delta) {
        attraction(groupA, groupB, attraction(groupA, groupB) + delta);
    }

    public void randomizeAttractionMatrix() {
        initAttractionMatrix();
    }

    public void zeroAttractionMatrix() {
        for (int i = 0; i < GROUP_COUNT; i++) {
            for (int j = 0; j < GROUP_COUNT; j++) {
                attraction(i, j, 0.0f);
            }
        }
    }

    public void symmetrizeAttractionMatrix() {
        for (int i = 0; i < GROUP_COUNT; i++) {
            for (int j = i + 1; j < GROUP_COUNT; j++) {
                float average = (attraction(i, j) + attraction(j, i)) * 0.5f;
                attraction(i, j, average);
                attraction(j, i, average);
            }
        }
    }

    public void invertAttractionMatrix() {
        for (int i = 0; i < GROUP_COUNT; i++) {
            for (int j = 0; j < GROUP_COUNT; j++) {
                attraction(i, j, -attraction(i, j));
            }
        }
    }

    public int groupCount() {
        return GROUP_COUNT;
    }

    public int gridSize() {
        return Math.max(1, (int) Math.ceil((bounds * 2.0f) / interactionRange));
    }

    public int maxParticlesPerCell() {
        return MAX_PARTICLES_PER_CELL;
    }

    private static float clampAttraction(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }
}

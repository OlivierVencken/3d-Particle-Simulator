package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.math.Math3d;
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
    private static final int WORK_GROUP_SIZE = 256;
    public static final int MAX_SPATIAL_MAP_SIZE = 524287;
    private static final int MIN_SPATIAL_MAP_SIZE = 1021;
    private static final float SPATIAL_GRID_MAX_LOAD = 0.5f;
    private static final int MAX_PARTICLES_PER_CELL = 128;

    private int positionSsbo;
    private int velocitySsbo;
    private int gridDataSsbo;
    private int gridCountsSsbo;
    private int gridKeysSsbo;
    private int spatialMapSize;

    private final ParticleRenderer renderer = new ParticleRenderer();
    private final ParticleCompute compute = new ParticleCompute();

    private final ParticleSimulationConfig config = ParticleSimulationConfig.defaults();
    private final AttractionMatrix attractionMatrix = new AttractionMatrix(
            SimulationDefaults.GROUP_COUNT,
            SimulationDefaults.MAX_GROUP_COUNT);
    private final Random particleRandom = new Random();
    private boolean initialized;

    public void init() {
        compute.init();
        renderer.init();
        initSpatialGrid();
        attractionMatrix.randomize();
        initialized = true;
        reset();
    }

    private void initSpatialGrid() {
        gridDataSsbo = glGenBuffers();
        gridCountsSsbo = glGenBuffers();
        gridKeysSsbo = glGenBuffers();
        allocateSpatialGrid(desiredSpatialMapSize());
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void ensureSpatialGridCapacity() {
        int desiredMapSize = desiredSpatialMapSize();
        if (desiredMapSize != spatialMapSize) {
            allocateSpatialGrid(desiredMapSize);
        }
    }

    private void allocateSpatialGrid(int newSpatialMapSize) {
        spatialMapSize = newSpatialMapSize;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridDataSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) spatialMapSize * MAX_PARTICLES_PER_CELL * Integer.BYTES,
                GL_DYNAMIC_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) spatialMapSize * Integer.BYTES, GL_STREAM_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridKeysSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) spatialMapSize * Integer.BYTES, GL_STREAM_DRAW);
    }

    public void reset() {
        resizeParticles(particleCount(), false);
    }

    public void update(float deltaTime, float elapsedTime) {
        if (particleCount() == 0) {
            return;
        }

        ensureSpatialGridCapacity();
        compute.bindBuffers(positionSsbo, velocitySsbo, gridDataSsbo, gridCountsSsbo, gridKeysSsbo);

        clearSpatialGrid();
        compute.setUniforms(this, deltaTime, 0);
        compute.dispatch(particleCount(), WORK_GROUP_SIZE, false);

        compute.setUniforms(this, deltaTime, 1);
        compute.dispatch(particleCount(), WORK_GROUP_SIZE, true);
    }

    private void clearSpatialGrid() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, new int[] { 0 });
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridKeysSsbo);
        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, new int[] { 0 });
    }

    public void render(int width, int height, float[] viewMatrix) {
        renderer.render(width, height, viewMatrix, positionSsbo, velocitySsbo, gridCountsSsbo, gridKeysSsbo,
                particleCount(), pointSize(), colorMode().ordinal(), groupCount(), maxVelocity(), bounds(),
                interactionRange(), spatialMapSize());
    }

    public void dispose() {
        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(gridDataSsbo);
        glDeleteBuffers(gridCountsSsbo);
        glDeleteBuffers(gridKeysSsbo);
        compute.dispose();
        renderer.dispose();
    }

    public int particleCount() {
        return config.particleCount();
    }

    public int maxParticleCount() {
        return SimulationDefaults.MAX_PARTICLE_COUNT;
    }

    public ParticleSimulationConfig config() {
        return config.copy();
    }

    public void applyConfig(ParticleSimulationConfig config) {
        if (config == null) {
            return;
        }

        setParticleCount(config.particleCount());
        pointSize(config.pointSize());
        bounds(config.bounds());
        forceFactor(config.forceFactor());
        velocityDamping(config.velocityDamping());
        interactionRange(config.interactionRange());
        repulsionRadius(config.repulsionRadius());
        maxVelocity(config.maxVelocity());
        boundaryBounce(config.boundaryBounce());
        toroidalWrap(config.toroidalWrap());
        groupCount(config.groupCount());
        colorMode(config.colorMode());
        spawnMode(config.spawnMode());
    }

    public void addParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        setParticleCount(particleCount() + amount, true);
    }

    public void removeParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        setParticleCount(particleCount() - amount, true);
    }

    public void clearParticles() {
        setParticleCount(0, false);
    }

    public void setParticleCount(int particleCount) {
        setParticleCount(particleCount, false);
    }

    private void setParticleCount(int requestedParticleCount, boolean preserveExisting) {
        int newParticleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, requestedParticleCount));
        if (initialized) {
            resizeParticles(newParticleCount, preserveExisting);
        } else {
            config.particleCount(newParticleCount);
        }
    }

    public ColorMode colorMode() {
        return config.colorMode();
    }

    public void colorMode(ColorMode colorMode) {
        config.colorMode(colorMode);
    }

    private void resizeParticles(int requestedParticleCount, boolean preserveExisting) {
        int oldParticleCount = particleCount();
        int newParticleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, requestedParticleCount));
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
        config.particleCount(newParticleCount);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void uploadRandomParticles(int targetPositionSsbo, int targetVelocitySsbo, long byteOffset, int count) {
        FloatBuffer positions = BufferUtils.createFloatBuffer(count * 4);
        FloatBuffer velocities = BufferUtils.createFloatBuffer(count * 4);

        ParticleSpawner.spawnParticles(positions, velocities, count, bounds(), groupCount(), spawnMode(), particleRandom);

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

    public float pointSize() {
        return config.pointSize();
    }

    public void pointSize(float pointSize) {
        config.pointSize(pointSize);
    }

    public float forceFactor() {
        return config.forceFactor();
    }

    public void forceFactor(float forceFactor) {
        config.forceFactor(forceFactor);
    }

    public float interactionRange() {
        return config.interactionRange();
    }

    public void interactionRange(float interactionRange) {
        config.interactionRange(interactionRange);
    }

    public float velocityDamping() {
        return config.velocityDamping();
    }

    public void velocityDamping(float velocityDamping) {
        config.velocityDamping(velocityDamping);
    }

    public float repulsionRadius() {
        return config.repulsionRadius();
    }

    public void repulsionRadius(float repulsionRadius) {
        config.repulsionRadius(repulsionRadius);
    }

    public float maxVelocity() {
        return config.maxVelocity();
    }

    public void maxVelocity(float maxVelocity) {
        config.maxVelocity(maxVelocity);
    }

    public float boundaryBounce() {
        return config.boundaryBounce();
    }

    public void boundaryBounce(float boundaryBounce) {
        config.boundaryBounce(boundaryBounce);
    }

    public float bounds() {
        return config.bounds();
    }

    public void bounds(float bounds) {
        config.bounds(bounds);
    }

    public SpawnMode spawnMode() {
        return config.spawnMode();
    }

    public void spawnMode(SpawnMode spawnMode) {
        config.spawnMode(spawnMode);
    }

    public boolean toroidalWrap() {
        return config.toroidalWrap();
    }

    public void toroidalWrap(boolean toroidalWrap) {
        config.toroidalWrap(toroidalWrap);
    }

    public float attraction(int groupA, int groupB) {
        return attractionMatrix.attraction(groupA, groupB);
    }

    public void attraction(int groupA, int groupB, float value) {
        attractionMatrix.attraction(groupA, groupB, value);
    }

    public void setAttractionMatrix(float[] values) {
        attractionMatrix.setActiveValues(values);
    }

    public float[] getAttractionMatrix() {
        return attractionMatrix.getFlatArray();
    }

    public void adjustAttraction(int groupA, int groupB, float delta) {
        attractionMatrix.adjustAttraction(groupA, groupB, delta);
    }

    public void randomizeAttractionMatrix() {
        attractionMatrix.randomize();
    }

    public void zeroAttractionMatrix() {
        attractionMatrix.zero();
    }

    public void symmetrizeAttractionMatrix() {
        attractionMatrix.symmetrize();
    }

    public void invertAttractionMatrix() {
        attractionMatrix.invert();
    }

    public int groupCount() {
        return config.groupCount();
    }

    public void groupCount(int groupCount) {
        int previousGroupCount = config.groupCount();
        config.groupCount(groupCount);
        attractionMatrix.groupCount(config.groupCount());
        if (initialized && previousGroupCount != config.groupCount()) {
            reset();
        }
    }

    public int maxGroupCount() {
        return attractionMatrix.maxGroups();
    }

    public int gridSize() {
        return Math.max(1, (int) Math.ceil((bounds() * 2.0f) / interactionRange()));
    }

    public int spatialMapSize() {
        return initialized ? spatialMapSize : desiredSpatialMapSize();
    }

    private int desiredSpatialMapSize() {
        long gridCellCount = gridCellCount();
        long occupiedCellLimit = Math.max(1L, Math.min((long) Math.max(particleCount(), 1), gridCellCount));
        long targetBuckets = (long) Math.ceil(occupiedCellLimit / SPATIAL_GRID_MAX_LOAD);
        int clampedBuckets = (int) Math.max(MIN_SPATIAL_MAP_SIZE, Math.min(MAX_SPATIAL_MAP_SIZE, targetBuckets));
        return Math3d.previousPrime(clampedBuckets);
    }

    private long gridCellCount() {
        long size = gridSize();
        return size * size * size;
    }

    public int maxParticlesPerCell() {
        return MAX_PARTICLES_PER_CELL;
    }
}

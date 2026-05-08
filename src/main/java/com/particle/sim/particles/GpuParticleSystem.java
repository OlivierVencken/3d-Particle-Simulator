package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.math.Math3d;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.glBindVertexArray;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43C.glDispatchCompute;
import static org.lwjgl.opengl.GL43C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.glGenBuffers;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glMemoryBarrier;
import static org.lwjgl.opengl.GL43C.glBufferSubData;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1fv;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;
import static org.lwjgl.opengl.GL43C.GL_POINTS;

public final class GpuParticleSystem {
    private static final int INITIAL_PARTICLE_COUNT = 65_536;
    private static final int WORK_GROUP_SIZE = 256;
    private static final int GROUP_COUNT = 6;
    private static final int GRID_SIZE = 16;
    private static final int GRID_CELL_COUNT = GRID_SIZE * GRID_SIZE * GRID_SIZE;
    private static final int MAX_PARTICLES_PER_CELL = 96;
    private static final int MAX_GROUPS = 16;

    private int computeProgram;
    private int renderProgram;
    private int vao;
    private int positionSsbo;
    private int velocitySsbo;
    private int gridDataSsbo;
    private int gridCountsSsbo;
    private IntBuffer zeroGridCounts;

    private int particleCount = INITIAL_PARTICLE_COUNT;
    private float pointSize = 2.2f;
    private float bounds = 8.0f;
    private float forceFactor = 9.0f;
    private float velocityDamping = 0.965f;
    private float interactionRange = 0.95f;
    private float repulsionRadius = 0.3f;
    private float maxVelocity = 8.0f;
    private float boundaryBounce = 0.65f;
    private boolean toroidalWrap;
    private final float[] attractionMatrix = new float[MAX_GROUPS * MAX_GROUPS];
    private final Random matrixRandom = new Random();

    public void init() {
        computeProgram = ShaderProgram.compute("/shaders/particle.comp");
        renderProgram = ShaderProgram.render("/shaders/particle.vert", "/shaders/particle.frag");
        vao = glGenVertexArrays();
        initSpatialGrid();
        initAttractionMatrix();
        reset();
    }

    private void initSpatialGrid() {
        gridDataSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridDataSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) GRID_CELL_COUNT * MAX_PARTICLES_PER_CELL * Integer.BYTES, GL_DYNAMIC_DRAW);

        gridCountsSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) GRID_CELL_COUNT * Integer.BYTES, GL_STREAM_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        zeroGridCounts = BufferUtils.createIntBuffer(GRID_CELL_COUNT);
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
        if (positionSsbo != 0) {
            glDeleteBuffers(positionSsbo);
        }
        if (velocitySsbo != 0) {
            glDeleteBuffers(velocitySsbo);
        }

        Random random = new Random(42L);
        FloatBuffer positions = BufferUtils.createFloatBuffer(particleCount * 4);
        FloatBuffer velocities = BufferUtils.createFloatBuffer(particleCount * 4);

        for (int i = 0; i < particleCount; i++) {
            float radius = random.nextFloat() * bounds * 0.6f;
            float theta = random.nextFloat() * (float) (Math.PI * 2.0);
            float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);

            float x = radius * (float) Math.sin(phi) * (float) Math.cos(theta);
            float y = radius * (float) Math.cos(phi);
            float z = radius * (float) Math.sin(phi) * (float) Math.sin(theta);

            positions.put(x).put(y).put(z).put(i % GROUP_COUNT);
            velocities
                    .put((random.nextFloat() - 0.5f) * 0.2f)
                    .put((random.nextFloat() - 0.5f) * 0.2f)
                    .put((random.nextFloat() - 0.5f) * 0.2f)
                    .put(0.0f);
        }

        positions.flip();
        velocities.flip();

        positionSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, positionSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, positions, GL_DYNAMIC_DRAW);

        velocitySsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, velocitySsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, velocities, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void update(float deltaTime, float elapsedTime) {
        glUseProgram(computeProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, positionSsbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, velocitySsbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, gridDataSsbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, gridCountsSsbo);

        clearGridCounts();
        setSimulationUniforms(deltaTime, 0);
        dispatchParticles();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        setSimulationUniforms(deltaTime, 1);
        dispatchParticles();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }

    private void clearGridCounts() {
        zeroGridCounts.clear();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gridCountsSsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, zeroGridCounts);
    }

    private void setSimulationUniforms(float deltaTime, int pass) {
        glUniform1f(glGetUniformLocation(computeProgram, "uDeltaTime"), deltaTime);
        glUniform1i(glGetUniformLocation(computeProgram, "uParticleCount"), particleCount);
        glUniform1i(glGetUniformLocation(computeProgram, "uGroupCount"), GROUP_COUNT);
        glUniform1f(glGetUniformLocation(computeProgram, "uForceFactor"), forceFactor);
        glUniform1f(glGetUniformLocation(computeProgram, "uVelocityDamping"), velocityDamping);
        glUniform1f(glGetUniformLocation(computeProgram, "uInteractionRange"), interactionRange);
        glUniform1f(glGetUniformLocation(computeProgram, "uRepulsionRadius"), repulsionRadius);
        glUniform1f(glGetUniformLocation(computeProgram, "uMaxVelocity"), maxVelocity);
        glUniform1f(glGetUniformLocation(computeProgram, "uBoundaryBounce"), boundaryBounce);
        glUniform1f(glGetUniformLocation(computeProgram, "uBounds"), bounds);
        glUniform1i(glGetUniformLocation(computeProgram, "uGridSize"), GRID_SIZE);
        glUniform1f(glGetUniformLocation(computeProgram, "uGridCellSize"), gridCellSize());
        glUniform1i(glGetUniformLocation(computeProgram, "uMaxParticlesPerCell"), MAX_PARTICLES_PER_CELL);
        glUniform1i(glGetUniformLocation(computeProgram, "uNeighborRadius"), neighborRadius());
        glUniform1i(glGetUniformLocation(computeProgram, "uToroidalWrap"), toroidalWrap ? 1 : 0);
        glUniform1i(glGetUniformLocation(computeProgram, "uPass"), pass);
        glUniform1fv(glGetUniformLocation(computeProgram, "uAttractionMatrix"), attractionMatrix);
    }

    private void dispatchParticles() {
        int groups = Math.ceilDiv(particleCount, WORK_GROUP_SIZE);
        glDispatchCompute(groups, 1, 1);
    }

    public void render(int width, int height, float[] viewMatrix) {
        glUseProgram(renderProgram);
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, positionSsbo);

        float aspect = width / (float) height;
        float[] viewProjection = Math3d.multiply(
                Math3d.perspective((float) Math.toRadians(60.0), aspect, 0.1f, 100.0f),
                viewMatrix
        );

        glUniformMatrix4fv(glGetUniformLocation(renderProgram, "uViewProjection"), false, viewProjection);
        glUniform1f(glGetUniformLocation(renderProgram, "uPointSize"), pointSize);
        glDrawArrays(GL_POINTS, 0, particleCount);
    }

    public void dispose() {
        glDeleteBuffers(positionSsbo);
        glDeleteBuffers(velocitySsbo);
        glDeleteBuffers(gridDataSsbo);
        glDeleteBuffers(gridCountsSsbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(renderProgram);
        glDeleteProgram(computeProgram);
    }

    public int particleCount() {
        return particleCount;
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
        return GRID_SIZE;
    }

    public int maxParticlesPerCell() {
        return MAX_PARTICLES_PER_CELL;
    }

    private float gridCellSize() {
        return (bounds * 2.0f) / GRID_SIZE;
    }

    private int neighborRadius() {
        return Math.max(1, (int) Math.ceil(interactionRange / gridCellSize()));
    }

    private static float clampAttraction(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }
}

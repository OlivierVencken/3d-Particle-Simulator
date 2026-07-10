package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.graphics.GpuTimerQuery;

import static org.lwjgl.opengl.GL43C.GL_BUFFER_UPDATE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDispatchCompute;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glMemoryBarrier;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1fv;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUseProgram;

public final class ParticleCompute {
    private static final int PARTICLE_WORK_GROUP_SIZE = 256;

    private int countProgram;
    private int scanProgram;
    private int addScanOffsetsProgram;
    private int scatterProgram;
    private int integrateProgram;

    private int countParticleCountLoc, countBoundsLoc, countInteractionRangeLoc, countGridSizeLoc;
    private int scanElementCountLoc, addElementCountLoc;
    private int scatterParticleCountLoc, scatterBoundsLoc, scatterInteractionRangeLoc, scatterGridSizeLoc;
    private int uDeltaTimeLoc, uParticleCountLoc, uGroupCountLoc, uForceFactorLoc;
    private int uVelocityDampingLoc, uInteractionRangeLoc, uRepulsionRadiusLoc;
    private int uMaxVelocityLoc, uBoundaryBounceLoc, uBoundsLoc, uGridSizeLoc;
    private int uToroidalWrapLoc, uDensityRegulationEnabledLoc, uDensityLimitLoc;
    private int uDistanceMetricLoc, uAttractionMatrixLoc;
    private GpuTimerQuery countTimer;
    private GpuTimerQuery scanTimer;
    private GpuTimerQuery scatterTimer;
    private GpuTimerQuery integrationTimer;

    public void init() {
        countProgram = ShaderProgram.compute("/shaders/grid_count.comp");
        scanProgram = ShaderProgram.compute("/shaders/grid_scan.comp");
        addScanOffsetsProgram = ShaderProgram.compute("/shaders/grid_scan_add.comp");
        scatterProgram = ShaderProgram.compute("/shaders/grid_scatter.comp");
        integrateProgram = ShaderProgram.compute("/shaders/particle.comp");
        countTimer = new GpuTimerQuery();
        scanTimer = new GpuTimerQuery();
        scatterTimer = new GpuTimerQuery();
        integrationTimer = new GpuTimerQuery();

        countParticleCountLoc = glGetUniformLocation(countProgram, "uParticleCount");
        countBoundsLoc = glGetUniformLocation(countProgram, "uBounds");
        countInteractionRangeLoc = glGetUniformLocation(countProgram, "uInteractionRange");
        countGridSizeLoc = glGetUniformLocation(countProgram, "uGridSize");
        scanElementCountLoc = glGetUniformLocation(scanProgram, "uElementCount");
        addElementCountLoc = glGetUniformLocation(addScanOffsetsProgram, "uElementCount");
        scatterParticleCountLoc = glGetUniformLocation(scatterProgram, "uParticleCount");
        scatterBoundsLoc = glGetUniformLocation(scatterProgram, "uBounds");
        scatterInteractionRangeLoc = glGetUniformLocation(scatterProgram, "uInteractionRange");
        scatterGridSizeLoc = glGetUniformLocation(scatterProgram, "uGridSize");

        uDeltaTimeLoc = glGetUniformLocation(integrateProgram, "uDeltaTime");
        uParticleCountLoc = glGetUniformLocation(integrateProgram, "uParticleCount");
        uGroupCountLoc = glGetUniformLocation(integrateProgram, "uGroupCount");
        uForceFactorLoc = glGetUniformLocation(integrateProgram, "uForceFactor");
        uVelocityDampingLoc = glGetUniformLocation(integrateProgram, "uVelocityDamping");
        uInteractionRangeLoc = glGetUniformLocation(integrateProgram, "uInteractionRange");
        uRepulsionRadiusLoc = glGetUniformLocation(integrateProgram, "uRepulsionRadius");
        uMaxVelocityLoc = glGetUniformLocation(integrateProgram, "uMaxVelocity");
        uBoundaryBounceLoc = glGetUniformLocation(integrateProgram, "uBoundaryBounce");
        uBoundsLoc = glGetUniformLocation(integrateProgram, "uBounds");
        uDensityRegulationEnabledLoc = glGetUniformLocation(integrateProgram, "uDensityRegulationEnabled");
        uDensityLimitLoc = glGetUniformLocation(integrateProgram, "uDensityLimit");
        uDistanceMetricLoc = glGetUniformLocation(integrateProgram, "uDistanceMetric");
        uGridSizeLoc = glGetUniformLocation(integrateProgram, "uGridSize");
        uToroidalWrapLoc = glGetUniformLocation(integrateProgram, "uToroidalWrap");
        uAttractionMatrixLoc = glGetUniformLocation(integrateProgram, "uAttractionMatrix");
    }

    public void buildGrid(GpuParticleSystem system, ParticleBuffers particles, SpatialGridBuffers grid) {
        int particleCount = system.particleCount();
        int cellCount = system.gridCellCount();
        grid.clearCounts(cellCount);
        glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT);

        countTimer.begin();
        glUseProgram(countProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particles.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, grid.countsSsbo());
        glUniform1i(countParticleCountLoc, particleCount);
        glUniform1f(countBoundsLoc, system.bounds());
        glUniform1f(countInteractionRangeLoc, system.interactionRange());
        glUniform1i(countGridSizeLoc, system.gridSize());
        dispatchParticles(particleCount);
        countTimer.end();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        scanTimer.begin();
        scan(grid, grid.countsSsbo(), grid.offsetsSsbo(), cellCount, 0);
        scanTimer.end();
        glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT);
        grid.copyOffsetsToCursors(cellCount);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        scatterTimer.begin();
        glUseProgram(scatterProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particles.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, grid.particleIdsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, grid.cursorsSsbo());
        glUniform1i(scatterParticleCountLoc, particleCount);
        glUniform1f(scatterBoundsLoc, system.bounds());
        glUniform1f(scatterInteractionRangeLoc, system.interactionRange());
        glUniform1i(scatterGridSizeLoc, system.gridSize());
        dispatchParticles(particleCount);
        scatterTimer.end();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
    }

    public void integrate(GpuParticleSystem system, ParticleBuffers particles, SpatialGridBuffers grid,
            float deltaTime) {
        integrationTimer.begin();
        glUseProgram(integrateProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particles.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, particles.velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, grid.particleIdsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, grid.countsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, grid.offsetsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, particles.nextPositionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 7, particles.nextVelocitySsbo());

        glUniform1f(uDeltaTimeLoc, deltaTime);
        glUniform1i(uParticleCountLoc, system.particleCount());
        glUniform1i(uGroupCountLoc, system.groupCount());
        glUniform1f(uForceFactorLoc, system.forceFactor());
        glUniform1f(uVelocityDampingLoc, system.velocityDamping());
        glUniform1f(uInteractionRangeLoc, system.interactionRange());
        glUniform1f(uRepulsionRadiusLoc, system.repulsionRadius());
        glUniform1f(uMaxVelocityLoc, system.maxVelocity());
        glUniform1f(uBoundaryBounceLoc, system.boundaryBounce());
        glUniform1f(uBoundsLoc, system.bounds());
        glUniform1i(uDensityRegulationEnabledLoc, system.densityRegulationEnabled() ? 1 : 0);
        glUniform1f(uDensityLimitLoc, system.densityLimit());
        glUniform1i(uDistanceMetricLoc, system.distanceMetric().ordinal());
        glUniform1i(uGridSizeLoc, system.gridSize());
        glUniform1i(uToroidalWrapLoc, system.toroidalWrap() ? 1 : 0);
        glUniform1fv(uAttractionMatrixLoc, system.getAttractionMatrix());

        dispatchParticles(system.particleCount());
        integrationTimer.end();
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
    }

    double gridCountMilliseconds() {
        return countTimer.latestMilliseconds();
    }

    double gridScanMilliseconds() {
        return scanTimer.latestMilliseconds();
    }

    double gridScatterMilliseconds() {
        return scatterTimer.latestMilliseconds();
    }

    double integrationMilliseconds() {
        return integrationTimer.latestMilliseconds();
    }

    private void scan(SpatialGridBuffers grid, int inputBuffer, int outputBuffer, int elementCount, int level) {
        if (level >= grid.scanLevelCount()) {
            throw new IllegalStateException("Spatial-grid scan scratch is undersized");
        }

        int groupCount = Math.ceilDiv(elementCount, SpatialGridBuffers.SCAN_ELEMENTS_PER_GROUP);
        glUseProgram(scanProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, inputBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, outputBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, grid.blockSumsSsbo(level));
        glUniform1i(scanElementCountLoc, elementCount);
        glDispatchCompute(groupCount, 1, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        if (groupCount <= 1) {
            return;
        }

        int scannedBlockSums = grid.scannedBlockSumsSsbo(level);
        scan(grid, grid.blockSumsSsbo(level), scannedBlockSums, groupCount, level + 1);

        glUseProgram(addScanOffsetsProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, outputBuffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, scannedBlockSums);
        glUniform1i(addElementCountLoc, elementCount);
        glDispatchCompute(Math.ceilDiv(elementCount, PARTICLE_WORK_GROUP_SIZE), 1, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
    }

    private void dispatchParticles(int particleCount) {
        if (particleCount > 0) {
            glDispatchCompute(Math.ceilDiv(particleCount, PARTICLE_WORK_GROUP_SIZE), 1, 1);
        }
    }

    public void dispose() {
        glDeleteProgram(countProgram);
        glDeleteProgram(scanProgram);
        glDeleteProgram(addScanOffsetsProgram);
        glDeleteProgram(scatterProgram);
        glDeleteProgram(integrateProgram);
        countTimer.dispose();
        scanTimer.dispose();
        scatterTimer.dispose();
        integrationTimer.dispose();
    }
}

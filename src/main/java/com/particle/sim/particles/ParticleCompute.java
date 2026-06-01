package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;

import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
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
    private int computeProgram;
    private int uDeltaTimeLoc, uParticleCountLoc, uGroupCountLoc, uForceFactorLoc;
    private int uVelocityDampingLoc, uInteractionRangeLoc, uRepulsionRadiusLoc;
    private int uMaxVelocityLoc, uBoundaryBounceLoc, uBoundsLoc, uGridSizeLoc;
    private int uMapSizeLoc, uMaxParticlesPerCellLoc, uToroidalWrapLoc, uPassLoc;
    private int uDensityRegulationEnabledLoc, uDensityLimitLoc;
    private int uDistanceMetricLoc, uAttractionMatrixLoc;

    public void init() {
        computeProgram = ShaderProgram.compute("/shaders/particle.comp");
        uDeltaTimeLoc = glGetUniformLocation(computeProgram, "uDeltaTime");
        uParticleCountLoc = glGetUniformLocation(computeProgram, "uParticleCount");
        uGroupCountLoc = glGetUniformLocation(computeProgram, "uGroupCount");
        uForceFactorLoc = glGetUniformLocation(computeProgram, "uForceFactor");
        uVelocityDampingLoc = glGetUniformLocation(computeProgram, "uVelocityDamping");
        uInteractionRangeLoc = glGetUniformLocation(computeProgram, "uInteractionRange");
        uRepulsionRadiusLoc = glGetUniformLocation(computeProgram, "uRepulsionRadius");
        uMaxVelocityLoc = glGetUniformLocation(computeProgram, "uMaxVelocity");
        uBoundaryBounceLoc = glGetUniformLocation(computeProgram, "uBoundaryBounce");
        uBoundsLoc = glGetUniformLocation(computeProgram, "uBounds");
        uDensityRegulationEnabledLoc = glGetUniformLocation(computeProgram, "uDensityRegulationEnabled");
        uDensityLimitLoc = glGetUniformLocation(computeProgram, "uDensityLimit");
        uDistanceMetricLoc = glGetUniformLocation(computeProgram, "uDistanceMetric");
        uGridSizeLoc = glGetUniformLocation(computeProgram, "uGridSize");
        uMapSizeLoc = glGetUniformLocation(computeProgram, "uMapSize");
        uMaxParticlesPerCellLoc = glGetUniformLocation(computeProgram, "uMaxParticlesPerCell");
        uToroidalWrapLoc = glGetUniformLocation(computeProgram, "uToroidalWrap");
        uPassLoc = glGetUniformLocation(computeProgram, "uPass");
        uAttractionMatrixLoc = glGetUniformLocation(computeProgram, "uAttractionMatrix");
    }

    public void bindBuffers(ParticleBuffers particleBuffers, SpatialGridBuffers spatialGridBuffers) {
        glUseProgram(computeProgram);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleBuffers.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, particleBuffers.velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, spatialGridBuffers.dataSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, spatialGridBuffers.countsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, spatialGridBuffers.keysSsbo());
    }

    public void setUniforms(GpuParticleSystem system, float deltaTime, int pass) {
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
        glUniform1i(uMapSizeLoc, system.spatialMapSize());
        glUniform1i(uMaxParticlesPerCellLoc, system.maxParticlesPerCell());
        glUniform1i(uToroidalWrapLoc, system.toroidalWrap() ? 1 : 0);
        glUniform1i(uPassLoc, pass);
        glUniform1fv(uAttractionMatrixLoc, system.getAttractionMatrix());
    }

    public void dispatch(int particleCount, int workGroupSize, boolean lastPass) {
        int groups = Math.ceilDiv(particleCount, workGroupSize);
        glDispatchCompute(groups, 1, 1);
        if (lastPass) {
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
        } else {
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        }
    }

    public void dispose() {
        glDeleteProgram(computeProgram);
    }
}

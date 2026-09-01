package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_POINTS;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.glBindVertexArray;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;

import com.particle.sim.graphics.GpuTimerQuery;
import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.settings.SimulationDefaults;

final class ParticleRenderPass {
    private int program;
    private int vao;
    private int viewProjectionLocation;
    private int viewLocation;
    private int pointSizeLocation;
    private int fixedParticleScreenSizeLocation;
    private int pointSizeReferenceDistanceLocation;
    private int colorModeLocation;
    private int groupCountLocation;
    private int maximumVelocityLocation;
    private int boundsLocation;
    private int interactionRangeLocation;
    private int gridSizeLocation;
    private GpuTimerQuery timer;

    void init() {
        program = ShaderProgram.render("/shaders/particle.vert", "/shaders/particle.frag");
        timer = new GpuTimerQuery();
        vao = glGenVertexArrays();

        viewProjectionLocation = glGetUniformLocation(program, "uViewProjection");
        viewLocation = glGetUniformLocation(program, "uView");
        pointSizeLocation = glGetUniformLocation(program, "uPointSize");
        fixedParticleScreenSizeLocation = glGetUniformLocation(program, "uFixedParticleScreenSize");
        pointSizeReferenceDistanceLocation =
                glGetUniformLocation(program, "uPointSizeReferenceDistance");
        colorModeLocation = glGetUniformLocation(program, "uColorMode");
        groupCountLocation = glGetUniformLocation(program, "uGroupCount");
        maximumVelocityLocation = glGetUniformLocation(program, "uMaxVelocity");
        boundsLocation = glGetUniformLocation(program, "uBounds");
        interactionRangeLocation = glGetUniformLocation(program, "uInteractionRange");
        gridSizeLocation = glGetUniformLocation(program, "uGridSize");
    }

    void render(RenderFrame frame, float[] viewProjection) {
        glUseProgram(program);
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, frame.particleBuffers().positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, frame.particleBuffers().velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, frame.spatialGridBuffers().countsSsbo());

        glUniformMatrix4fv(viewProjectionLocation, false, viewProjection);
        setMatrix(viewLocation, frame.viewMatrix());
        glUniform1f(pointSizeLocation, frame.pointSize());
        setInteger(fixedParticleScreenSizeLocation, frame.fixedParticleScreenSize() ? 1 : 0);
        setFloat(
                pointSizeReferenceDistanceLocation,
                SimulationDefaults.POINT_SIZE_REFERENCE_DISTANCE);
        setInteger(colorModeLocation, frame.colorMode());
        setInteger(groupCountLocation, frame.groupCount());
        setFloat(maximumVelocityLocation, frame.maximumVelocity());
        setFloat(boundsLocation, frame.bounds());
        setFloat(interactionRangeLocation, frame.interactionRange());
        setInteger(gridSizeLocation, frame.gridSize());

        timer.begin();
        glDrawArrays(GL_POINTS, 0, frame.particleCount());
        timer.end();
    }

    double latestMilliseconds() {
        return timer.latestMilliseconds();
    }

    void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        timer.dispose();
    }

    private void setMatrix(int location, float[] value) {
        if (location != -1) {
            glUniformMatrix4fv(location, false, value);
        }
    }

    private void setFloat(int location, float value) {
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    private void setInteger(int location, int value) {
        if (location != -1) {
            glUniform1i(location, value);
        }
    }
}

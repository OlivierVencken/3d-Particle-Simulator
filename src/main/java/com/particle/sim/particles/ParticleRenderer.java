package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.math.Math3d;

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
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;

public final class ParticleRenderer {
    private int renderProgram;
    private int vao;
    private int uViewProjectionLoc;
    private int uPointSizeLoc;

    public void init() {
        renderProgram = ShaderProgram.render("/shaders/particle.vert", "/shaders/particle.frag");
        vao = glGenVertexArrays();
        uViewProjectionLoc = glGetUniformLocation(renderProgram, "uViewProjection");
        uPointSizeLoc = glGetUniformLocation(renderProgram, "uPointSize");
    }

    public void render(int width, int height, float[] viewMatrix, int positionSsbo, int particleCount,
            float pointSize) {
        if (particleCount == 0) {
            return;
        }

        glUseProgram(renderProgram);
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, positionSsbo);

        float aspect = width / (float) height;
        float[] viewProjection = Math3d.multiply(
                Math3d.perspective((float) Math.toRadians(60.0), aspect, 0.1f, 100.0f),
                viewMatrix);

        glUniformMatrix4fv(uViewProjectionLoc, false, viewProjection);
        glUniform1f(uPointSizeLoc, pointSize);
        glDrawArrays(GL_POINTS, 0, particleCount);
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteProgram(renderProgram);
    }
}
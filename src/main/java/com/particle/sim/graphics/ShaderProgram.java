package com.particle.sim.graphics;

import com.particle.sim.util.ResourceLoader;

import static org.lwjgl.opengl.GL43C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.GL43C.GL_FALSE;
import static org.lwjgl.opengl.GL43C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL43C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL43C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL43C.glAttachShader;
import static org.lwjgl.opengl.GL43C.glCompileShader;
import static org.lwjgl.opengl.GL43C.glCreateProgram;
import static org.lwjgl.opengl.GL43C.glCreateShader;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteShader;
import static org.lwjgl.opengl.GL43C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL43C.glGetProgrami;
import static org.lwjgl.opengl.GL43C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL43C.glGetShaderi;
import static org.lwjgl.opengl.GL43C.glLinkProgram;
import static org.lwjgl.opengl.GL43C.glShaderSource;

public final class ShaderProgram {
    private ShaderProgram() {
    }

    public static int compute(String shaderResource) {
        int shader = compile(GL_COMPUTE_SHADER, ResourceLoader.loadString(shaderResource));
        return link(shader);
    }

    public static int render(String vertexResource, String fragmentResource) {
        int vertexShader = compile(GL_VERTEX_SHADER, ResourceLoader.loadString(vertexResource));
        int fragmentShader = compile(GL_FRAGMENT_SHADER, ResourceLoader.loadString(fragmentResource));
        return link(vertexShader, fragmentShader);
    }

    private static int link(int... shaders) {
        int program = glCreateProgram();
        for (int shader : shaders) {
            glAttachShader(program, shader);
        }

        glLinkProgram(program);
        for (int shader : shaders) {
            glDeleteShader(shader);
        }

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            throw new IllegalStateException("Program link failed:\n" + log);
        }

        return program;
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Shader compile failed:\n" + log);
        }

        return shader;
    }
}

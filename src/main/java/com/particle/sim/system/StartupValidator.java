package com.particle.sim.system;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.opengl.GL43C.glGetInteger;
import static org.lwjgl.opengl.GL43C.glGetString;
import static org.lwjgl.opengl.GL43C.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL43C.GL_MINOR_VERSION;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.GL_SHADING_LANGUAGE_VERSION;

public final class StartupValidator {

    private static final int REQUIRED_MAJOR = 4;
    private static final int REQUIRED_MINOR = 3;

    private StartupValidator() {
    }

    public static StartupValidationResult validate() {
        GLCapabilities caps = GL.getCapabilities();

        int major = glGetInteger(GL_MAJOR_VERSION);
        int minor = glGetInteger(GL_MINOR_VERSION);

        String glVersion = safe(glGetString(GL_VERSION));
        String glslVersion = safe(glGetString(GL_SHADING_LANGUAGE_VERSION));

        boolean versionOk = major > REQUIRED_MAJOR ||
                (major == REQUIRED_MAJOR && minor >= REQUIRED_MINOR);

        boolean computeOk = caps.OpenGL43 || caps.GL_ARB_compute_shader;

        if (!versionOk || !computeOk) {
            return StartupValidationResult.fail(
                    "Unsupported graphics driver",
                    "This application requires OpenGL 4.3+ with compute shader support.\n\n"
                            + "Detected OpenGL: " + glVersion + "\n"
                            + "Detected GLSL: " + glslVersion + "\n\n"
                            + "Please update your graphics driver or use a system with a newer GPU.");
        }

        return StartupValidationResult.ok();
    }

    private static String safe(String value) {
        return value == null ? "unknown" : value;
    }
}

package com.particle.sim.graphics;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderResourceTest {
    @Test
    void shaderResourcesArePackagedWithExpectedEntrypoints() throws IOException {
        assertShaderContains("/shaders/particle.comp", "layout(local_size_x = 256) in;", "void main()");
        assertShaderContains("/shaders/particle.vert", "uniform mat4 uViewProjection;", "void main()");
        assertShaderContains("/shaders/particle.frag", "out vec4 fragColor;", "void main()");
        assertShaderContains("/shaders/fullscreen.vert", "out vec2 vUv;", "void main()");
        assertShaderContains("/shaders/blur.frag", "uniform float uRadius;", "uniform float uFalloff;", "void main()");
        assertShaderContains("/shaders/bloom_composite.frag", "uniform sampler2D uBloom;", "void main()");
        assertShaderContains("/shaders/trail.vert", "layout(std430, binding = 4) readonly buffer TrailHistory",
                "uniform float uTrailThickness;", "void main()");
        assertShaderContains("/shaders/trail.geom", "layout(lines) in;", "layout(triangle_strip, max_vertices = 4) out;",
                "void main()");
        assertShaderContains("/shaders/trail.frag", "out vec4 fragColor;", "void main()");
    }

    private static void assertShaderContains(String path, String... snippets) throws IOException {
        try (InputStream stream = ShaderProgram.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing shader resource " + path);
            String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            for (String snippet : snippets) {
                assertTrue(source.contains(snippet), path + " did not contain " + snippet);
            }
        }
    }
}

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
        assertShaderContains("/shaders/grid_count.comp", "atomicAdd", "void main()");
        assertShaderContains("/shaders/grid_scan.comp", "shared int values[512]", "void main()");
        assertShaderContains("/shaders/grid_scan_add.comp", "block_offsets", "void main()");
        assertShaderContains("/shaders/grid_scatter.comp", "particle_ids[destination]", "void main()");
        assertShaderContains("/shaders/particle.vert", "uniform mat4 uViewProjection;", "void main()");
        assertShaderContains("/shaders/particle.frag", "out vec4 fragColor;", "void main()");
        assertShaderContains("/shaders/fullscreen.vert", "out vec2 vUv;", "void main()");
        assertShaderContains("/shaders/bloom_extract.frag", "uniform sampler2D uScene;", "void main()");
        assertShaderContains("/shaders/blur.frag", "uniform float uRadius;", "1.384615", "void main()");
        assertShaderContains("/shaders/bloom_composite.frag", "uniform sampler2D uBloom;", "void main()");
        assertShaderContains("/shaders/trail.vert", "layout(std430, binding = 4) readonly buffer TrailHistory",
                "uniform float uTrailThickness;", "gl_InstanceID", "void main()");
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

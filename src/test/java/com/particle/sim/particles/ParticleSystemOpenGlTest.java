package com.particle.sim.particles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL43C.GL_RGBA;
import static org.lwjgl.opengl.GL43C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL43C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glDisable;
import static org.lwjgl.opengl.GL43C.glFinish;
import static org.lwjgl.opengl.GL43C.glGetError;
import static org.lwjgl.opengl.GL43C.glReadPixels;
import static org.lwjgl.opengl.GL43C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.FramebufferViewport;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.lwjgl.opengl.GL;

@EnabledIfSystemProperty(named = "gpuTests", matches = "true")
class ParticleSystemOpenGlTest {
    @Test
    void compilesShadersAndExecutesOneSimulationStep() {
        assertTrue(glfwInit(), "GLFW initialization failed");
        long window = NULL;
        ParticleSystem system = null;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(96, 80, "GPU smoke test", NULL, NULL);
            assertNotEquals(NULL, window, "OpenGL 4.3 context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            system = new ParticleSystem();
            system.particleCount(1_024);
            system.bounds(2.0f);
            system.interactionRange(0.4f);
            system.spawnMode(SpawnMode.POINT);
            system.effectEnabled(EffectMode.TRAILS, true);
            system.effectEnabled(EffectMode.GLOW, true);
            system.init();
            assertTrue(system.maximumParticleCount() > 1_000_000);
            for (int i = 0; i < 6; i++) {
                system.step();
            }
            float[] identity = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
            glDisable(GL_SCISSOR_TEST);
            glViewport(0, 0, 96, 80);
            glClearColor(0.75f, 0.125f, 0.5f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);
            system.render(new FramebufferViewport(24, 16, 48, 40), identity);
            glFinish();

            assertEquals(GL_NO_ERROR, glGetError());
            assertPixelClose(new int[] {191, 32, 128, 255}, readPixel(2, 2), 2);

            system.render(new FramebufferViewport(8, 8, 32, 24), identity);
            system.render(new FramebufferViewport(0, 0, 96, 80), identity);
            system.render(new FramebufferViewport(0, 0, 0, 0), identity);
            glFinish();
            assertEquals(GL_NO_ERROR, glGetError());
            assertTrue(system.performanceSnapshot().simulationMilliseconds() >= 0.0);

            int totalGridParticles = java.util.Arrays.stream(system.readGridCounts()).sum();
            assertEquals(system.particleCount(), totalGridParticles);
            boolean[] particleSeen = new boolean[system.particleCount()];
            for (int particleId : system.readGridParticleIds()) {
                assertTrue(particleId >= 0 && particleId < particleSeen.length);
                assertTrue(
                        !particleSeen[particleId],
                        "Particle occurred more than once in compact grid");
                particleSeen[particleId] = true;
            }

            system.dispose();
            system = accuracySystem();
            for (boolean toroidal : new boolean[] {false, true}) {
                system.toroidalWrap(toroidal);
                for (DistanceMetric metric : DistanceMetric.values()) {
                    system.distanceMetric(metric);
                    system.reset();
                    float[] initialPositions = system.readPositions();
                    float[] initialVelocities = system.readVelocities();
                    ReferenceState expected =
                            referenceStep(system, initialPositions, initialVelocities);

                    system.step();
                    glFinish();
                    assertStateClose(expected.positions(), system.readPositions(), 0.002f);
                    assertStateClose(expected.velocities(), system.readVelocities(), 0.002f);
                }
            }

            system.dispose();
            system = seamInteractionSystem();
            float[] seamPositions = {3.59f, 0.0f, 0.0f, 0.0f, -3.99f, 0.0f, 0.0f, 0.0f};
            float[] seamVelocities = new float[seamPositions.length];
            system.replaceState(seamPositions, seamVelocities);
            ReferenceState seamExpected = referenceStep(system, seamPositions, seamVelocities);

            system.step();
            glFinish();
            float[] actualSeamVelocities = system.readVelocities();
            assertTrue(
                    actualSeamVelocities[0] > 0.0001f,
                    "Particle near the positive seam did not interact across the toroidal boundary");
            assertTrue(
                    actualSeamVelocities[4] < -0.0001f,
                    "Particle near the negative seam did not interact across the toroidal boundary");
            assertStateClose(seamExpected.positions(), system.readPositions(), 0.00001f);
            assertStateClose(seamExpected.velocities(), actualSeamVelocities, 0.00001f);
        } finally {
            if (system != null) {
                system.dispose();
            }
            if (window != NULL) {
                glfwMakeContextCurrent(NULL);
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }
    }

    private static int[] readPixel(int x, int y) {
        ByteBuffer pixel = createByteBuffer(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        return new int[] {
            Byte.toUnsignedInt(pixel.get(0)),
            Byte.toUnsignedInt(pixel.get(1)),
            Byte.toUnsignedInt(pixel.get(2)),
            Byte.toUnsignedInt(pixel.get(3))
        };
    }

    private static void assertPixelClose(int[] expected, int[] actual, int tolerance) {
        assertEquals(expected.length, actual.length);
        for (int channel = 0; channel < expected.length; channel++) {
            assertEquals(
                    expected[channel],
                    actual[channel],
                    tolerance,
                    "Pixel differs at channel " + channel);
        }
    }

    private static ParticleSystem accuracySystem() {
        ParticleSystem system = new ParticleSystem();
        system.particleCount(96);
        system.bounds(2.0f);
        system.interactionRange(3.0f);
        system.groupCount(3);
        system.randomSeed(0xACC0_1234L);
        system.init();
        system.zeroAttractionMatrix();
        for (int row = 0; row < system.groupCount(); row++) {
            for (int column = 0; column < system.groupCount(); column++) {
                system.attraction(row, column, ((row * 3 + column * 5) % 7 - 3) / 3.0f);
            }
        }
        return system;
    }

    private static ParticleSystem seamInteractionSystem() {
        ParticleSystem system = new ParticleSystem();
        system.particleCount(2);
        system.bounds(4.0f);
        system.interactionRange(0.95f);
        system.groupCount(1);
        system.toroidalWrap(true);
        system.init();
        system.zeroAttractionMatrix();
        system.attraction(0, 0, 1.0f);
        assertEquals(8, system.gridSize());
        return system;
    }

    private static ReferenceState referenceStep(
            ParticleSystem system, float[] positions, float[] velocities) {
        float[] nextPositions = positions.clone();
        float[] nextVelocities = velocities.clone();
        float deltaTime = (float) SimulationDefaults.SIMULATION_STEP_SECONDS;
        float interactionRange = system.interactionRange();
        float repulsionRadius = system.repulsionRadius();

        for (int particle = 0; particle < system.particleCount(); particle++) {
            int base = particle * 4;
            float forceX = 0.0f;
            float forceY = 0.0f;
            float forceZ = 0.0f;
            int groupI = (int) positions[base + 3];

            for (int other = 0; other < system.particleCount(); other++) {
                if (particle == other) {
                    continue;
                }
                int otherBase = other * 4;
                float directionX = positions[otherBase] - positions[base];
                float directionY = positions[otherBase + 1] - positions[base + 1];
                float directionZ = positions[otherBase + 2] - positions[base + 2];
                if (system.toroidalWrap()) {
                    float worldSize = system.bounds() * 2.0f;
                    directionX -= worldSize * Math.round(directionX / worldSize);
                    directionY -= worldSize * Math.round(directionY / worldSize);
                    directionZ -= worldSize * Math.round(directionZ / worldSize);
                }

                float squaredDistance =
                        directionX * directionX + directionY * directionY + directionZ * directionZ;
                if (squaredDistance <= 0.00000001f) {
                    continue;
                }
                float euclideanDistance = (float) Math.sqrt(squaredDistance);
                float metricDistance =
                        switch (system.distanceMetric()) {
                            case MANHATTAN ->
                                    Math.abs(directionX)
                                            + Math.abs(directionY)
                                            + Math.abs(directionZ);
                            case CHEBYSHEV ->
                                    Math.max(
                                            Math.max(Math.abs(directionX), Math.abs(directionY)),
                                            Math.abs(directionZ));
                            case EUCLIDEAN -> euclideanDistance;
                        };
                float normalizedDistance = metricDistance / interactionRange;
                if (normalizedDistance >= 1.0f) {
                    continue;
                }

                int groupJ = (int) positions[otherBase + 3];
                float magnitude =
                        normalizedDistance < repulsionRadius
                                ? normalizedDistance / repulsionRadius - 1.0f
                                : system.attraction(groupI, groupJ)
                                        * (1.0f
                                                - Math.abs(
                                                                2.0f * normalizedDistance
                                                                        - 1.0f
                                                                        - repulsionRadius)
                                                        / (1.0f - repulsionRadius));
                float scale = magnitude * system.forceFactor() / euclideanDistance;
                forceX += directionX * scale;
                forceY += directionY * scale;
                forceZ += directionZ * scale;
            }

            float velocityX =
                    (velocities[base] + forceX * deltaTime * 0.1f) * system.velocityDamping();
            float velocityY =
                    (velocities[base + 1] + forceY * deltaTime * 0.1f) * system.velocityDamping();
            float velocityZ =
                    (velocities[base + 2] + forceZ * deltaTime * 0.1f) * system.velocityDamping();
            float squaredVelocity =
                    velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ;
            if (squaredVelocity > system.maxVelocity() * system.maxVelocity()) {
                float scale = system.maxVelocity() / (float) Math.sqrt(squaredVelocity);
                velocityX *= scale;
                velocityY *= scale;
                velocityZ *= scale;
            }

            float positionX = positions[base] + velocityX * deltaTime;
            float positionY = positions[base + 1] + velocityY * deltaTime;
            float positionZ = positions[base + 2] + velocityZ * deltaTime;
            if (system.toroidalWrap()) {
                float worldSize = system.bounds() * 2.0f;
                positionX = wrap(positionX, system.bounds(), worldSize);
                positionY = wrap(positionY, system.bounds(), worldSize);
                positionZ = wrap(positionZ, system.bounds(), worldSize);
            } else {
                float[] bounded = {positionX, positionY, positionZ};
                float[] velocity = {velocityX, velocityY, velocityZ};
                for (int axis = 0; axis < 3; axis++) {
                    if (bounded[axis] > system.bounds()) {
                        bounded[axis] = system.bounds();
                        velocity[axis] *= -system.boundaryBounce();
                    } else if (bounded[axis] < -system.bounds()) {
                        bounded[axis] = -system.bounds();
                        velocity[axis] *= -system.boundaryBounce();
                    }
                }
                positionX = bounded[0];
                positionY = bounded[1];
                positionZ = bounded[2];
                velocityX = velocity[0];
                velocityY = velocity[1];
                velocityZ = velocity[2];
            }

            nextPositions[base] = positionX;
            nextPositions[base + 1] = positionY;
            nextPositions[base + 2] = positionZ;
            nextVelocities[base] = velocityX;
            nextVelocities[base + 1] = velocityY;
            nextVelocities[base + 2] = velocityZ;
            nextVelocities[base + 3] = 0.0f;
        }
        return new ReferenceState(nextPositions, nextVelocities);
    }

    private static float wrap(float value, float bounds, float worldSize) {
        float shifted = value + bounds;
        return shifted - (float) Math.floor(shifted / worldSize) * worldSize - bounds;
    }

    private static void assertStateClose(float[] expected, float[] actual, float tolerance) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], tolerance, "State differs at float index " + i);
        }
    }

    private record ReferenceState(float[] positions, float[] velocities) {}
}

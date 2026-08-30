package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.FramebufferViewport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

@EnabledIfSystemProperty(named = "gpuTests", matches = "true")
class GpuParticleSystemOpenGlTest {
    @Test
    void compilesShadersAndExecutesOneSimulationStep() {
        assertTrue(glfwInit(), "GLFW initialization failed");
        long window = NULL;
        GpuParticleSystem system = null;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(96, 80, "GPU smoke test", NULL, NULL);
            assertNotEquals(NULL, window, "OpenGL 4.3 context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();

            system = new GpuParticleSystem();
            system.setParticleCount(1_024);
            system.bounds(2.0f);
            system.interactionRange(0.4f);
            system.spawnMode(SpawnMode.POINT);
            system.effectEnabled(EffectMode.TRAILS, true);
            system.effectEnabled(EffectMode.GLOW, true);
            system.init();
            assertTrue(system.maxParticleCount() > 1_000_000);
            for (int i = 0; i < 6; i++) {
                system.step();
            }
            float[] identity = {
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            };
            glDisable(GL_SCISSOR_TEST);
            glViewport(0, 0, 96, 80);
            glClearColor(0.75f, 0.125f, 0.5f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);
            system.render(new FramebufferViewport(24, 16, 48, 40), identity);
            glFinish();

            assertEquals(GL_NO_ERROR, glGetError());
            assertPixelClose(new int[] { 191, 32, 128, 255 }, readPixel(2, 2), 2);

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
                assertTrue(!particleSeen[particleId], "Particle occurred more than once in compact grid");
                particleSeen[particleId] = true;
            }

            int preservedCount = system.particleCount();
            int[] preservedGroups = system.readGroups();
            assertValidThreeDimensionalState(system);
            system.addParticles(17);
            assertEquals(preservedCount + 17, system.particleCount());
            assertGroupPrefixEquals(preservedGroups, system.readGroups());
            assertValidThreeDimensionalState(system);
            system.removeParticles(17);
            assertEquals(preservedCount, system.particleCount());
            assertGroupPrefixEquals(preservedGroups, system.readGroups());
            system.reset();
            assertValidThreeDimensionalState(system);

            system.dispose();
            system = accuracySystem();
            for (SimulationDimension dimension : SimulationDimension.values()) {
                system.simulationDimension(dimension);
                for (boolean densityRegulated : new boolean[] { false, true }) {
                    if (dimension == SimulationDimension.THREE_D && densityRegulated) {
                        continue;
                    }
                    system.densityRegulationEnabled(densityRegulated);
                    system.densityLimit(4.0f);
                    for (boolean toroidal : new boolean[] { false, true }) {
                        system.toroidalWrap(toroidal);
                        for (DistanceMetric metric : DistanceMetric.values()) {
                            system.distanceMetric(metric);
                            system.reset();
                            float[] initialPositions = system.readPositions();
                            float[] initialVelocities = system.readVelocities();
                            int[] initialGroups = system.readGroups();
                            ReferenceState expected = referenceStep(system, initialPositions, initialVelocities,
                                    initialGroups);

                            system.step();
                            glFinish();
                            assertStateClose(expected.positions(), system.readPositions(), 0.003f);
                            assertStateClose(expected.velocities(), system.readVelocities(), 0.003f);
                            assertGroupPrefixEquals(initialGroups, system.readGroups());
                        }
                    }
                }
            }

            system.dispose();
            system = seamInteractionSystem();
            float[] seamPositions = {
                    3.59f, 0.0f, 0.0f, 0.0f,
                    -3.99f, 0.0f, 0.0f, 0.0f
            };
            float[] seamVelocities = new float[seamPositions.length];
            int[] seamGroups = { 0, 0 };
            system.replaceState(seamPositions, seamVelocities, seamGroups);
            ReferenceState seamExpected = referenceStep(system, seamPositions, seamVelocities, seamGroups);

            system.step();
            glFinish();
            float[] actualSeamVelocities = system.readVelocities();
            assertTrue(actualSeamVelocities[0] > 0.0001f,
                    "Particle near the positive seam did not interact across the toroidal boundary");
            assertTrue(actualSeamVelocities[4] < -0.0001f,
                    "Particle near the negative seam did not interact across the toroidal boundary");
            assertStateClose(seamExpected.positions(), system.readPositions(), 0.00001f);
            assertStateClose(seamExpected.velocities(), actualSeamVelocities, 0.00001f);

            system.simulationDimension(SimulationDimension.FOUR_D);
            float[] wSeamPositions = {
                    0.0f, 0.0f, 0.0f, 3.7f,
                    0.0f, 0.0f, 0.0f, -3.7f
            };
            system.replaceState(wSeamPositions, seamVelocities, seamGroups);
            ReferenceState wSeamExpected = referenceStep(system, wSeamPositions, seamVelocities, seamGroups);
            system.step();
            glFinish();
            float[] actualWSeamVelocities = system.readVelocities();
            assertTrue(actualWSeamVelocities[3] > 0.0001f,
                    "Particle near the positive W seam did not interact across the toroidal boundary");
            assertTrue(actualWSeamVelocities[7] < -0.0001f,
                    "Particle near the negative W seam did not interact across the toroidal boundary");
            assertStateClose(wSeamExpected.positions(), system.readPositions(), 0.00001f);
            assertStateClose(wSeamExpected.velocities(), actualWSeamVelocities, 0.00001f);

            system.toroidalWrap(false);
            system.setParticleCount(1);
            system.maxVelocity(1.0f);
            float[] origin = new float[4];
            float[] fastVelocity = { 2.0f, 2.0f, 2.0f, 2.0f };
            system.replaceState(origin, fastVelocity, new int[] { 0 });
            system.step();
            glFinish();
            float[] clampedVelocity = system.readVelocities();
            assertEquals(1.0f, length4d(clampedVelocity), 0.00001f);

            system.maxVelocity(16.0f);
            system.boundaryBounce(0.75f);
            float[] bouncePosition = { 0.0f, 0.0f, 0.0f, 3.999f };
            float[] bounceVelocity = { 0.0f, 0.0f, 0.0f, 2.0f };
            int[] bounceGroup = { 0 };
            system.replaceState(bouncePosition, bounceVelocity, bounceGroup);
            ReferenceState bounceExpected = referenceStep(system, bouncePosition, bounceVelocity, bounceGroup);
            system.step();
            glFinish();
            assertEquals(4.0f, system.readPositions()[3], 0.0f);
            assertStateClose(bounceExpected.positions(), system.readPositions(), 0.00001f);
            assertStateClose(bounceExpected.velocities(), system.readVelocities(), 0.00001f);
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
            assertEquals(expected[channel], actual[channel], tolerance,
                    "Pixel differs at channel " + channel);
        }
    }

    private static GpuParticleSystem accuracySystem() {
        GpuParticleSystem system = new GpuParticleSystem();
        system.setParticleCount(96);
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

    private static GpuParticleSystem seamInteractionSystem() {
        GpuParticleSystem system = new GpuParticleSystem();
        system.setParticleCount(2);
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

    private static ReferenceState referenceStep(GpuParticleSystem system, float[] positions, float[] velocities,
            int[] groups) {
        float[] nextPositions = new float[positions.length];
        float[] nextVelocities = new float[velocities.length];
        float deltaTime = (float) SimulationDefaults.SIMULATION_STEP_SECONDS;
        int dimensions = system.simulationDimension().componentCount();

        for (int particle = 0; particle < system.particleCount(); particle++) {
            int base = particle * 4;
            float[] force = new float[4];
            float[] positiveAttractionForce = new float[4];
            float localDensity = 0.0f;
            int groupI = groups[particle];

            for (int other = 0; other < system.particleCount(); other++) {
                if (particle == other) {
                    continue;
                }
                int otherBase = other * 4;
                float[] direction = new float[4];
                float squaredDistance = 0.0f;
                for (int axis = 0; axis < dimensions; axis++) {
                    direction[axis] = positions[otherBase + axis] - positions[base + axis];
                    if (system.toroidalWrap()) {
                        float worldSize = system.bounds() * 2.0f;
                        direction[axis] -= worldSize * Math.round(direction[axis] / worldSize);
                    }
                    squaredDistance += direction[axis] * direction[axis];
                }
                if (squaredDistance <= 0.00000001f) {
                    continue;
                }
                float euclideanDistance = (float) Math.sqrt(squaredDistance);
                float metricDistance = metricDistance(direction, dimensions, system.distanceMetric(),
                        euclideanDistance);
                float normalizedDistance = metricDistance / system.interactionRange();
                if (normalizedDistance >= 1.0f) {
                    continue;
                }

                int groupJ = groups[other];
                float attraction = system.attraction(groupI, groupJ);
                if (system.densityRegulationEnabled() && localDensity < system.densityLimit() + 1.0f) {
                    float densityWeight = 1.0f - normalizedDistance;
                    localDensity += groupI == groupJ ? densityWeight : densityWeight * 0.5f;
                }
                float magnitude = normalizedDistance < system.repulsionRadius()
                        ? normalizedDistance / system.repulsionRadius() - 1.0f
                        : attraction * (1.0f - Math.abs(2.0f * normalizedDistance - 1.0f
                                - system.repulsionRadius()) / (1.0f - system.repulsionRadius()));
                float scale = magnitude * system.forceFactor() / euclideanDistance;
                float[] target = system.densityRegulationEnabled() && attraction > 0.0f
                        && normalizedDistance >= system.repulsionRadius() ? positiveAttractionForce : force;
                for (int axis = 0; axis < dimensions; axis++) {
                    target[axis] += direction[axis] * scale;
                }
            }

            if (system.densityRegulationEnabled()) {
                float densityFactor = Math.max(0.0f,
                        Math.min(1.0f, 1.0f - Math.max(0.0f, localDensity - system.densityLimit())));
                for (int axis = 0; axis < dimensions; axis++) {
                    force[axis] += positiveAttractionForce[axis] * densityFactor;
                }
            }

            float squaredVelocity = 0.0f;
            for (int axis = 0; axis < dimensions; axis++) {
                nextVelocities[base + axis] = (velocities[base + axis] + force[axis] * deltaTime * 0.1f)
                        * system.velocityDamping();
                squaredVelocity += nextVelocities[base + axis] * nextVelocities[base + axis];
            }
            if (squaredVelocity > system.maxVelocity() * system.maxVelocity()) {
                float scale = system.maxVelocity() / (float) Math.sqrt(squaredVelocity);
                for (int axis = 0; axis < dimensions; axis++) {
                    nextVelocities[base + axis] *= scale;
                }
            }

            for (int axis = 0; axis < dimensions; axis++) {
                float position = positions[base + axis] + nextVelocities[base + axis] * deltaTime;
                if (system.toroidalWrap()) {
                    position = wrap(position, system.bounds(), system.bounds() * 2.0f);
                } else if (position > system.bounds()) {
                    position = system.bounds();
                    nextVelocities[base + axis] *= -system.boundaryBounce();
                } else if (position < -system.bounds()) {
                    position = -system.bounds();
                    nextVelocities[base + axis] *= -system.boundaryBounce();
                }
                nextPositions[base + axis] = position;
            }
        }
        return new ReferenceState(nextPositions, nextVelocities);
    }

    private static float metricDistance(float[] direction, int dimensions, DistanceMetric metric,
            float euclideanDistance) {
        if (metric == DistanceMetric.EUCLIDEAN) {
            return euclideanDistance;
        }
        float result = metric == DistanceMetric.MANHATTAN ? 0.0f : Float.NEGATIVE_INFINITY;
        for (int axis = 0; axis < dimensions; axis++) {
            result = metric == DistanceMetric.MANHATTAN
                    ? result + Math.abs(direction[axis])
                    : Math.max(result, Math.abs(direction[axis]));
        }
        return result;
    }

    private static float length4d(float[] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]
                + vector[2] * vector[2] + vector[3] * vector[3]);
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

    private static void assertValidThreeDimensionalState(GpuParticleSystem system) {
        float[] positions = system.readPositions();
        float[] velocities = system.readVelocities();
        int[] groups = system.readGroups();
        assertEquals(system.particleCount(), groups.length);
        for (int particle = 0; particle < system.particleCount(); particle++) {
            assertEquals(0.0f, positions[particle * 4 + 3], 0.0f,
                    "Generated position W must remain zero in Phase 1");
            assertEquals(0.0f, velocities[particle * 4 + 3], 0.0f,
                    "Generated velocity W must remain zero in Phase 1");
            assertTrue(groups[particle] >= 0 && groups[particle] < system.groupCount(),
                    "Generated particle group was outside the active range");
        }
    }

    private static void assertGroupPrefixEquals(int[] expected, int[] actual) {
        assertTrue(actual.length >= expected.length);
        for (int particle = 0; particle < expected.length; particle++) {
            assertEquals(expected[particle], actual[particle], "Group changed for particle " + particle);
        }
    }

    private record ReferenceState(float[] positions, float[] velocities) {
    }
}

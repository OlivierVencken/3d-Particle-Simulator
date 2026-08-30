package com.particle.sim.particles;

import com.particle.sim.math.Math3d;
import com.particle.sim.ui.FramebufferViewport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL43C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL43C.GL_RGBA;
import static org.lwjgl.opengl.GL43C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glBlendFunc;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glFinish;
import static org.lwjgl.opengl.GL43C.glGetError;
import static org.lwjgl.opengl.GL43C.glReadPixels;
import static org.lwjgl.opengl.GL43C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@EnabledIfSystemProperty(named = "gpuTests", matches = "true")
class FourDRendererOpenGlTest {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 96;
    private static final int[] BACKGROUND = { 5, 5, 8, 255 };
    private static final float[] VIEW = Math3d.lookAt(0.0f, 0.0f, 8.0f, 0.0f, 0.0f, 0.0f);

    @Test
    void diagnosticHarnessExercisesEveryModeWithoutMutatingStateAndRejectsSingularities() throws IOException {
        assertTrue(glfwInit(), "GLFW initialization failed");
        long window = NULL;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(WIDTH, HEIGHT, "4D renderer diagnostic", NULL, NULL);
            assertNotEquals(NULL, window, "OpenGL 4.3 context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();
            glEnable(GL_PROGRAM_POINT_SIZE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            assertWColorReferencePalette();
            exerciseAllModesAndCaptureReferences();
            assertPerspectiveSingularitiesStayHiddenWithGlow();
            assertEquals(GL_NO_ERROR, glGetError());
        } finally {
            if (window != NULL) {
                glfwMakeContextCurrent(NULL);
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }
    }

    private static void assertWColorReferencePalette() {
        FourDDiagnosticPointSet colorReferences = FourDDiagnosticPointSet.of(new float[] {
                -2.0f, 0.0f, 0.0f, -4.0f,
                0.0f, 0.0f, 0.0f, 0.0f,
                2.0f, 0.0f, 0.0f, 4.0f
        });
        FourDViewController controller = new FourDViewController();
        controller.visualizationMode(FourDVisualizationMode.W_COLOR);
        controller.colorRange(4.0);
        try (FourDDiagnosticHarness harness = new FourDDiagnosticHarness(colorReferences, controller)) {
            harness.init();
            clear();
            harness.render(new FramebufferViewport(0, 0, WIDTH, HEIGHT), VIEW, false);
            glFinish();

            int[] negative = readPixel(43, HEIGHT / 2);
            int[] center = readPixel(WIDTH / 2, HEIGHT / 2);
            int[] positive = readPixel(85, HEIGHT / 2);
            assertTrue(negative[2] > negative[0] + 80, "Negative W was not distinctly blue");
            assertTrue(Math.abs(center[0] - center[2]) < 30, "Central W was not neutral");
            assertTrue(positive[0] > positive[2] + 80, "Positive W was not distinctly warm");
        }
    }

    private static void exerciseAllModesAndCaptureReferences() throws IOException {
        FourDViewController controller = new FourDViewController();
        controller.rotateXw(0.42);
        controller.rotateYw(-0.27);
        controller.rotateZw(0.18);
        controller.perspectiveDistance(8.0);
        controller.slice(0.0, 1.25, 0.3);
        try (FourDDiagnosticHarness harness = new FourDDiagnosticHarness(
                FourDDiagnosticPointSet.standard(), controller)) {
            harness.init();
            float[] originalState = harness.positions();
            for (FourDVisualizationMode mode : FourDVisualizationMode.values()) {
                controller.visualizationMode(mode);
                clear();
                harness.render(new FramebufferViewport(0, 0, WIDTH, HEIGHT), VIEW, false);
                glFinish();
                assertFrameHasVisiblePoint(readFrame(), mode);
                assertArrayEquals(originalState, harness.positions(),
                        "Visualization mode changed diagnostic particle state");
                writeReferenceScreenshot(mode.name().toLowerCase() + ".png", readFrame());
            }
        }
    }

    private static void assertPerspectiveSingularitiesStayHiddenWithGlow() {
        FourDDiagnosticPointSet unsafePoints = FourDDiagnosticPointSet.of(new float[] {
                Float.MAX_VALUE, 0.0f, 0.0f, 12.0f,
                1.0f, 1.0f, 0.0f, 13.0f,
                -1.0f, -1.0f, 0.0f, 11.9f
        });
        FourDViewController controller = new FourDViewController();
        controller.visualizationMode(FourDVisualizationMode.PERSPECTIVE);
        controller.perspectiveDistance(12.0);
        try (FourDDiagnosticHarness harness = new FourDDiagnosticHarness(unsafePoints, controller)) {
            harness.init();
            clear();
            harness.render(new FramebufferViewport(0, 0, WIDTH, HEIGHT), VIEW, true);
            glFinish();

            for (int[] pixel : new int[][] {
                    readPixel(0, 0), readPixel(WIDTH - 1, 0), readPixel(0, HEIGHT - 1),
                    readPixel(WIDTH - 1, HEIGHT - 1), readPixel(WIDTH / 2, HEIGHT / 2)
            }) {
                assertTrue(pixel[0] < 32 && pixel[1] < 32 && pixel[2] < 32,
                        "A clipped perspective point produced a framebuffer flash");
            }
        }
    }

    private static void clear() {
        glViewport(0, 0, WIDTH, HEIGHT);
        glClearColor(BACKGROUND[0] / 255.0f, BACKGROUND[1] / 255.0f, BACKGROUND[2] / 255.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    private static void assertFrameHasVisiblePoint(ByteBuffer frame, FourDVisualizationMode mode) {
        boolean signal = false;
        for (int pixel = 0; pixel < WIDTH * HEIGHT; pixel++) {
            int base = pixel * 4;
            if (Byte.toUnsignedInt(frame.get(base)) > BACKGROUND[0] + 20
                    || Byte.toUnsignedInt(frame.get(base + 1)) > BACKGROUND[1] + 20
                    || Byte.toUnsignedInt(frame.get(base + 2)) > BACKGROUND[2] + 20) {
                signal = true;
                break;
            }
        }
        assertTrue(signal, mode + " rendered no visible diagnostic points");
    }

    private static ByteBuffer readFrame() {
        ByteBuffer frame = createByteBuffer(WIDTH * HEIGHT * 4);
        glReadPixels(0, 0, WIDTH, HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, frame);
        return frame;
    }

    private static int[] readPixel(int x, int y) {
        ByteBuffer pixel = createByteBuffer(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        return new int[] { Byte.toUnsignedInt(pixel.get(0)), Byte.toUnsignedInt(pixel.get(1)),
                Byte.toUnsignedInt(pixel.get(2)), Byte.toUnsignedInt(pixel.get(3)) };
    }

    private static void writeReferenceScreenshot(String fileName, ByteBuffer frame) throws IOException {
        Path directory = Path.of("target", "phase2-qa");
        Files.createDirectories(directory);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int base = (y * WIDTH + x) * 4;
                int rgba = Byte.toUnsignedInt(frame.get(base)) << 24
                        | Byte.toUnsignedInt(frame.get(base + 1)) << 16
                        | Byte.toUnsignedInt(frame.get(base + 2)) << 8
                        | Byte.toUnsignedInt(frame.get(base + 3));
                int argb = (rgba >>> 8) | (rgba << 24);
                image.setRGB(x, HEIGHT - y - 1, argb);
            }
        }
        ImageIO.write(image, "png", directory.resolve(fileName).toFile());
    }
}

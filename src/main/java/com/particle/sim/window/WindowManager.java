package com.particle.sim.window;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import com.particle.sim.util.ResourceLoader;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class WindowManager {
    private static final int DEFAULT_WINDOWED_WIDTH = 1920;
    private static final int DEFAULT_WINDOWED_HEIGHT = 1080;
    private static final int WINDOW_MARGIN_X = 160;
    private static final int WINDOW_MARGIN_Y = 120;

    private final String title;

    private long handle;
    private int width = DEFAULT_WINDOWED_WIDTH;
    private int height = DEFAULT_WINDOWED_HEIGHT;

    private int windowedX;
    private int windowedY;
    private int windowedWidth = DEFAULT_WINDOWED_WIDTH;
    private int windowedHeight = DEFAULT_WINDOWED_HEIGHT;

    private boolean fullscreen = true;

    public WindowManager(String title) {
        this.title = title;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GLFW.");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode videoMode = monitor == NULL ? null : glfwGetVideoMode(monitor);

        if (videoMode == null) {
            fullscreen = false;
            width = DEFAULT_WINDOWED_WIDTH;
            height = DEFAULT_WINDOWED_HEIGHT;
        } else {
            width = videoMode.width();
            height = videoMode.height();
        }

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new IllegalStateException("Could not create the GLFW window.");
        }

        setWindowIcon("/assets/favicon.png");

        if (fullscreen && monitor != NULL && videoMode != null) {
            setDefaultWindowedBounds(videoMode);
            enterBorderlessFullscreen(monitor, videoMode);
        } else {
            fullscreen = false;
            centerWindow();
        }

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(0);
        glfwShowWindow(handle);
    }

    public long handle() {
        return handle;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public void toggleFullscreen() {
        if (fullscreen) {
            exitBorderlessFullscreen();
            return;
        }

        saveWindowedBounds();

        long monitor = glfwGetPrimaryMonitor();
        if (monitor == NULL) {
            return;
        }

        GLFWVidMode videoMode = glfwGetVideoMode(monitor);
        if (videoMode == null) {
            return;
        }

        enterBorderlessFullscreen(monitor, videoMode);
    }

    public void updateFramebufferSize() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetFramebufferSize(handle, pWidth, pHeight);
            width = Math.max(pWidth.get(0), 1);
            height = Math.max(pHeight.get(0), 1);
        }
    }

    private void enterBorderlessFullscreen(long monitor, GLFWVidMode videoMode) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pX = stack.mallocInt(1);
            IntBuffer pY = stack.mallocInt(1);
            glfwGetMonitorPos(monitor, pX, pY);

            int monitorX = pX.get(0);
            int monitorY = pY.get(0);

            fullscreen = true;
            glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);
            glfwSetWindowMonitor(handle, NULL, monitorX, monitorY, videoMode.width(), videoMode.height(), 0);
        }
    }

    private void exitBorderlessFullscreen() {
        fullscreen = false;
        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
        glfwSetWindowMonitor(handle, NULL, windowedX, windowedY, windowedWidth, windowedHeight, 0);
    }

    private void saveWindowedBounds() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pX = stack.mallocInt(1);
            IntBuffer pY = stack.mallocInt(1);
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowPos(handle, pX, pY);
            glfwGetWindowSize(handle, pWidth, pHeight);

            windowedX = pX.get(0);
            windowedY = pY.get(0);
            windowedWidth = Math.max(pWidth.get(0), 1);
            windowedHeight = Math.max(pHeight.get(0), 1);
        }
    }

    private void setDefaultWindowedBounds(GLFWVidMode videoMode) {
        windowedWidth = Math.min(DEFAULT_WINDOWED_WIDTH, Math.max(1, videoMode.width() - WINDOW_MARGIN_X));
        windowedHeight = Math.min(DEFAULT_WINDOWED_HEIGHT, Math.max(1, videoMode.height() - WINDOW_MARGIN_Y));
        windowedX = (videoMode.width() - windowedWidth) / 2;
        windowedY = (videoMode.height() - windowedHeight) / 2;
    }

    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode videoMode = monitor == NULL ? null : glfwGetVideoMode(monitor);
            if (videoMode == null) {
                return;
            }

            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(handle, pWidth, pHeight);

            glfwSetWindowPos(
                    handle,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2);
        }
    }

    private void setWindowIcon(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer icon = ResourceLoader.loadBytes(path);

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer pixels = STBImage.stbi_load_from_memory(icon, w, h, channels, 4);
            if (pixels == null) {
                throw new RuntimeException("Failed to load icon: " + STBImage.stbi_failure_reason());
            }

            GLFWImage image = GLFWImage.malloc(stack);
            image.set(w.get(0), h.get(0), pixels);

            GLFWImage.Buffer images = GLFWImage.malloc(1, stack);
            images.put(0, image);

            glfwSetWindowIcon(handle, images);
            STBImage.stbi_image_free(pixels);
        }
    }

    public void dispose() {
        glfwDestroyWindow(handle);
        glfwTerminate();

        var callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}

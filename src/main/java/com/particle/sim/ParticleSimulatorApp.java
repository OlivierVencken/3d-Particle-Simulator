package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.AppHotkeys;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.AppSettings;
import com.particle.sim.settings.DebouncedSettingsSaver;
import com.particle.sim.ui.SimulationUi;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL43C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL43C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.glBlendFunc;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glDepthMask;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class ParticleSimulatorApp {
    private static final double SETTINGS_SAVE_DEBOUNCE_SECONDS = 0.5;

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final CameraController camera = new CameraController();
    private final GpuParticleSystem particles = new GpuParticleSystem();
    private final SimulationUi ui = new SimulationUi();
    private final AppHotkeys hotkeys = new AppHotkeys(
            this::toggleFullscreen,
            () -> !camera.isMouseCaptured(),
            this::requestClose);
    private final Path settingsPath = AppSettings.defaultPath();
    private final DebouncedSettingsSaver settingsSaver = new DebouncedSettingsSaver(
            SETTINGS_SAVE_DEBOUNCE_SECONDS,
            this::saveSettings);

    private long window;
    private int width = 1920;
    private int height = 1080;
    private int windowedX;
    private int windowedY;
    private int windowedWidth = 1920;
    private int windowedHeight = 1080;
    private boolean fullscreen = true;

    private double lastFrameTime;
    private double startTime;

    public static void main(String[] args) {
        new ParticleSimulatorApp().run();
    }

    private void run() {
        initWindow();
        initOpenGl();
        initImGui();
        particles.init();
        initSettings();

        lastFrameTime = glfwGetTime();
        startTime = lastFrameTime;

        loop();
        dispose();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GLFW.");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        var monitor = glfwGetPrimaryMonitor();
        var videoMode = monitor == NULL ? null : glfwGetVideoMode(monitor);
        long fullscreenMonitor = videoMode == null ? NULL : monitor;
        if (videoMode != null) {
            width = videoMode.width();
            height = videoMode.height();
        } else {
            fullscreen = false;
        }

        window = glfwCreateWindow(width, height, "3D Particle Simulator", fullscreenMonitor, NULL);
        if (window == NULL) {
            throw new IllegalStateException("Could not create the GLFW window.");
        }

        if (!fullscreen) {
            centerWindow();
        } else {
            setDefaultWindowedBounds();
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void toggleFullscreen() {
        if (fullscreen) {
            fullscreen = false;
            glfwSetWindowMonitor(window, NULL, windowedX, windowedY, windowedWidth, windowedHeight, 0);
            return;
        }

        saveWindowedBounds();
        var monitor = glfwGetPrimaryMonitor();
        var videoMode = monitor == NULL ? null : glfwGetVideoMode(monitor);
        if (videoMode == null) {
            return;
        }

        fullscreen = true;
        glfwSetWindowMonitor(window, monitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate());
    }

    private void saveWindowedBounds() {
        try (MemoryStack stack = stackPush()) {
            var pX = stack.mallocInt(1);
            var pY = stack.mallocInt(1);
            var pWidth = stack.mallocInt(1);
            var pHeight = stack.mallocInt(1);
            glfwGetWindowPos(window, pX, pY);
            glfwGetWindowSize(window, pWidth, pHeight);
            windowedX = pX.get(0);
            windowedY = pY.get(0);
            windowedWidth = Math.max(pWidth.get(0), 1);
            windowedHeight = Math.max(pHeight.get(0), 1);
        }
    }

    private void setDefaultWindowedBounds() {
        var monitor = glfwGetPrimaryMonitor();
        var videoMode = monitor == NULL ? null : glfwGetVideoMode(monitor);
        if (videoMode == null) {
            windowedX = 0;
            windowedY = 0;
            return;
        }

        windowedWidth = Math.min(1920, Math.max(1, videoMode.width() - 160));
        windowedHeight = Math.min(1080, Math.max(1, videoMode.height() - 120));
        windowedX = (videoMode.width() - windowedWidth) / 2;
        windowedY = (videoMode.height() - windowedHeight) / 2;
    }

    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            var monitor = glfwGetPrimaryMonitor();
            var videoMode = glfwGetVideoMode(monitor);
            var pWidth = stack.mallocInt(1);
            var pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);

            if (videoMode != null) {
                glfwSetWindowPos(
                        window,
                        (videoMode.width() - pWidth.get(0)) / 2,
                        (videoMode.height() - pHeight.get(0)) / 2);
            }
        }
    }

    private void initOpenGl() {
        GL.createCapabilities();
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initImGui() {
        ImGui.createContext();
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 430");
    }

    private void initSettings() {
        ui.onSettingsChanged(this::requestSettingsSave);
        ui.onResetSettings(this::resetSettings);

        if (Files.exists(settingsPath)) {
            AppSettings.load(settingsPath).applyTo(particles, camera, ui);
        }
    }

    private void saveSettings() {
        AppSettings.capture(particles, camera, ui).save(settingsPath);
    }

    private void requestSettingsSave() {
        settingsSaver.requestSave(glfwGetTime());
    }

    private void saveSettingsIfDue(double now) {
        settingsSaver.saveIfDue(now);
    }

    private void flushPendingSettingsSave() {
        settingsSaver.flush();
    }

    private void resetSettings() {
        AppSettings.defaults().applySimulationTo(particles, camera, ui);
        particles.reset();
        requestSettingsSave();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            hotkeys.update(window);

            double now = glfwGetTime();
            float deltaTime = (float) Math.min(now - lastFrameTime, 1.0 / 30.0);
            lastFrameTime = now;

            updateFramebufferSize();
            beginImGuiFrame();
            camera.update(window, deltaTime);

            if (!ui.isPaused()) {
                particles.update(deltaTime, (float) (now - startTime));
            }

            renderScene();
            ui.render(deltaTime, particles, camera);
            renderImGui();
            saveSettingsIfDue(glfwGetTime());

            glfwSwapBuffers(window);
        }
    }

    private void requestClose() {
        glfwSetWindowShouldClose(window, true);
    }

    private void updateFramebufferSize() {
        try (MemoryStack stack = stackPush()) {
            var pWidth = stack.mallocInt(1);
            var pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window, pWidth, pHeight);
            width = Math.max(pWidth.get(0), 1);
            height = Math.max(pHeight.get(0), 1);
        }
    }

    private void beginImGuiFrame() {
        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();
    }

    private void renderScene() {
        glViewport(0, 0, width, height);
        glClearColor(0.015f, 0.018f, 0.024f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDepthMask(false);
        try {
            particles.render(width, height, camera.viewMatrix());
        } finally {
            glDepthMask(true);
        }
    }

    private void renderImGui() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void dispose() {
        flushPendingSettingsSave();
        particles.dispose();

        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();

        glfwDestroyWindow(window);
        glfwTerminate();
        var callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}

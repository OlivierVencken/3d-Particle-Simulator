package com.particle.sim.ui;

import static imgui.ImGui.getIO;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;

import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import com.particle.sim.util.ResourceLoader;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class ImGuiLayer {
    private static final float MATERIAL_SCALE_CHANGE = 0.05f;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final float[] contentScaleX = new float[1];
    private final float[] contentScaleY = new float[1];
    private final byte[] regularFontBytes =
            ResourceLoader.loadBytesArray("/assets/IBMPlexSans-Regular.ttf");
    private final byte[] mediumFontBytes =
            ResourceLoader.loadBytesArray("/assets/IBMPlexSans-Medium.ttf");
    private long window;
    private float uiScale = 1.0f;
    private boolean rendererInitialized;

    public void init(long window) {
        this.window = window;
        ImGui.createContext();
        ImGuiIO io = getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        uiScale = readWindowScale();
        rebuildFonts(io, uiScale);
        Theme.applyDarkTheme(uiScale);
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 430");
        rendererInitialized = true;
    }

    private void rebuildFonts(ImGuiIO io, float scale) {
        if (rendererInitialized) {
            imGuiGl3.destroyFontsTexture();
        }

        ImFontAtlas fontAtlas = io.getFonts();
        Fonts.clear();
        fontAtlas.clear();
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setOversampleH(2);
        fontConfig.setOversampleV(2);
        fontConfig.setPixelSnapH(false);
        try {
            DesignTokens tokens = DesignTokens.atScale(scale);
            ImFont body =
                    fontAtlas.addFontFromMemoryTTF(
                            regularFontBytes, tokens.bodyFontSize(), fontConfig);
            Fonts.setBody(body);
            Fonts.setMedium(
                    fontAtlas.addFontFromMemoryTTF(
                            mediumFontBytes, tokens.mediumFontSize(), fontConfig));
            Fonts.setCommandBar(
                    fontAtlas.addFontFromMemoryTTF(
                            mediumFontBytes, tokens.commandBarFontSize(), fontConfig));
            Fonts.setSection(
                    fontAtlas.addFontFromMemoryTTF(
                            mediumFontBytes, tokens.sectionFontSize(), fontConfig));
            Fonts.setTitle(
                    fontAtlas.addFontFromMemoryTTF(
                            mediumFontBytes, tokens.titleFontSize(), fontConfig));
            io.setFontDefault(body);

            if (!fontAtlas.build()) {
                throw new IllegalStateException("Could not build the ImGui font atlas");
            }
        } finally {
            fontConfig.destroy();
        }
    }

    public void beginFrame() {
        imGuiGlfw.newFrame();
        applyPendingDisplayScale();
        imGuiGl3.newFrame();
        ImGui.newFrame();
    }

    private void applyPendingDisplayScale() {
        float observedScale = readWindowScale();
        if (!materiallyDifferent(uiScale, observedScale)) {
            return;
        }

        uiScale = observedScale;
        rebuildFonts(getIO(), uiScale);
        Theme.applyDarkTheme(uiScale);
    }

    private float readWindowScale() {
        glfwGetWindowContentScale(window, contentScaleX, contentScaleY);
        return DesignTokens.sanitizeScale(Math.max(contentScaleX[0], contentScaleY[0]));
    }

    static boolean materiallyDifferent(float currentScale, float observedScale) {
        return Math.abs(currentScale - observedScale) >= MATERIAL_SCALE_CHANGE;
    }

    public float uiScale() {
        return uiScale;
    }

    public void render() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        imGuiGl3.shutdown();
        rendererInitialized = false;
        imGuiGlfw.shutdown();
        Fonts.clear();
        ImGui.destroyContext();
        window = 0L;
    }
}

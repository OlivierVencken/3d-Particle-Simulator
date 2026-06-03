package com.particle.sim.ui;

import static imgui.ImGui.getIO;

import java.nio.ByteBuffer;

import com.particle.sim.util.ResourceLoader;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class ImguiLayer {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public void init(long window) {
        ImGui.createContext();
        ImGuiIO io = getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        initFonts(io);
        UiTheme.applyDarkTheme();
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 430");
    }

    private void initFonts(ImGuiIO io) {
        ImFontAtlas fontAtlas = io.getFonts();
        ImFontConfig fontConfig = new ImFontConfig();

        byte[] fontData = ResourceLoader.loadBytesArray("/assets/Futura Heavy font.ttf");
        fontAtlas.addFontFromMemoryTTF(fontData, 18.0f);

        fontAtlas.build();
        fontConfig.destroy();
    }

    public void beginFrame() {
        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();
    }

    public void render() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}

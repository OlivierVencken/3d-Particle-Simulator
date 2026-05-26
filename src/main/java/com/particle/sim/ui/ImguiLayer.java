package com.particle.sim.ui;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class ImguiLayer {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public void init(long window) {
        ImGui.createContext();
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        UiTheme.applyDarkTheme();
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 430");
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

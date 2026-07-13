package com.particle.sim.ui.commandbar;

import com.particle.sim.input.AppHotkeys;
import com.particle.sim.input.HotkeyDefinition;
import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.theme.UIFonts;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

final class HotkeyPopup extends PopupWindow {
    HotkeyPopup() {
        super("Hotkeys", "hotkeys-popup", 430.0f, 340.0f);
    }

    @Override
    protected void renderContent() {
        ImGui.pushFont(UIFonts.medium());
        ImGui.textUnformatted("Keyboard shortcuts");
        ImGui.separator();
        renderHotkeys();
        ImGui.popFont();
    }

    private void renderHotkeys() {
        int flags = ImGuiTableFlags.BordersInnerV | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingStretchProp;
        if (!ImGui.beginTable("hotkey-list", 2, flags)) {
            return;
        }

        ImGui.tableSetupColumn("Key");
        ImGui.tableSetupColumn("Action");
        ImGui.tableHeadersRow();

        for (HotkeyDefinition hotkey : AppHotkeys.defaultHotkeys()) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.textUnformatted(keyName(hotkey.key()));
            ImGui.tableNextColumn();
            ImGui.textUnformatted(hotkey.action().displayName());
        }

        ImGui.endTable();
    }

    private String keyName(int key) {
        return switch (key) {
            case GLFW_KEY_ESCAPE -> "Esc";
            case GLFW_KEY_F -> "F";
            case GLFW_KEY_F3 -> "F3";
            case GLFW_KEY_F11 -> "F11";
            case GLFW_KEY_LEFT -> "Left Arrow";
            case GLFW_KEY_RIGHT -> "Right Arrow";
            case GLFW_KEY_R -> "R";
            case GLFW_KEY_SPACE -> "Space";
            default -> "Key " + key;
        };
    }
}

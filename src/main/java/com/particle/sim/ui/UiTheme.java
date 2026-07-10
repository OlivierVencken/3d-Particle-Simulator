package com.particle.sim.ui;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

final class UiTheme {
    private UiTheme() {
    }

    static void applyDarkTheme() {
        ImGui.styleColorsDark();

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowPadding(16.0f, 12.0f);
        style.setFramePadding(8.0f, 7.0f);
        style.setItemSpacing(8.0f, 10.0f);
        style.setItemInnerSpacing(8.0f, 6.0f);
        style.setCellPadding(6.0f, 6.0f);
        style.setWindowRounding(0.0f);
        style.setChildRounding(0.0f);
        style.setFrameRounding(3.0f);
        style.setPopupRounding(5.0f);
        style.setScrollbarRounding(3.0f);
        style.setGrabRounding(3.0f);
        style.setTabRounding(3.0f);
        style.setWindowBorderSize(1.0f);
        style.setFrameBorderSize(1.0f);
        style.setPopupBorderSize(1.0f);
        style.setSeparatorTextBorderSize(1.0f);
        style.setSeparatorTextPadding(12.0f, 4.0f);
        style.setWindowTitleAlign(0.0f, 0.5f);

        color(ImGuiCol.Text, UiPalette.TEXT);
        color(ImGuiCol.TextDisabled, UiPalette.TEXT_DISABLED);
        color(ImGuiCol.WindowBg, UiPalette.WINDOW_BG);
        color(ImGuiCol.ChildBg, UiPalette.PANEL_BG);
        color(ImGuiCol.PopupBg, UiPalette.POPUP_BG);
        color(ImGuiCol.Border, UiPalette.BORDER);
        color(ImGuiCol.BorderShadow, UiPalette.CLEAR);

        color(ImGuiCol.FrameBg, UiPalette.SURFACE);
        color(ImGuiCol.FrameBgHovered, UiPalette.SURFACE_HOVERED);
        color(ImGuiCol.FrameBgActive, UiPalette.SURFACE_ACTIVE);
        color(ImGuiCol.TitleBg, UiPalette.TITLE_BG);
        color(ImGuiCol.TitleBgActive, UiPalette.TITLE_BG_ACTIVE);
        color(ImGuiCol.TitleBgCollapsed, UiPalette.TITLE_BG_COLLAPSED);
        color(ImGuiCol.MenuBarBg, UiPalette.MENU_BG);

        color(ImGuiCol.ScrollbarBg, UiPalette.MENU_BG.withAlpha(0.90f));
        color(ImGuiCol.ScrollbarGrab, UiPalette.BORDER.withAlpha(1.00f));
        color(ImGuiCol.ScrollbarGrabHovered, UiPalette.BORDER_STRONG);
        color(ImGuiCol.ScrollbarGrabActive, UiPalette.INTERACTIVE_ACTIVE);
        color(ImGuiCol.CheckMark, UiPalette.ACCENT);
        color(ImGuiCol.SliderGrab, UiPalette.ACCENT_MUTED);
        color(ImGuiCol.SliderGrabActive, UiPalette.ACCENT_BRIGHT);

        color(ImGuiCol.Button, UiPalette.INTERACTIVE);
        color(ImGuiCol.ButtonHovered, UiPalette.INTERACTIVE_HOVERED);
        color(ImGuiCol.ButtonActive, UiPalette.INTERACTIVE_ACTIVE);
        color(ImGuiCol.Header, UiPalette.SURFACE_ACTIVE);
        color(ImGuiCol.HeaderHovered, UiPalette.INTERACTIVE_HOVERED);
        color(ImGuiCol.HeaderActive, UiPalette.INTERACTIVE_ACTIVE);

        color(ImGuiCol.Separator, UiPalette.SEPARATOR);
        color(ImGuiCol.SeparatorHovered, UiPalette.ACCENT_MUTED.withAlpha(1.00f));
        color(ImGuiCol.SeparatorActive, UiPalette.ACCENT_BRIGHT.withAlpha(1.00f));
        color(ImGuiCol.ResizeGrip, UiPalette.ACCENT_MUTED.withAlpha(0.28f));
        color(ImGuiCol.ResizeGripHovered, UiPalette.ACCENT_MUTED.withAlpha(0.62f));
        color(ImGuiCol.ResizeGripActive, UiPalette.ACCENT_BRIGHT.withAlpha(0.92f));

        color(ImGuiCol.Tab, UiPalette.SURFACE);
        color(ImGuiCol.TabHovered, UiPalette.INTERACTIVE_HOVERED);
        color(ImGuiCol.TabSelected, UiPalette.INTERACTIVE);
        color(ImGuiCol.TabDimmed, UiPalette.TITLE_BG);
        color(ImGuiCol.TabDimmedSelected, UiPalette.SURFACE_HOVERED);

        color(ImGuiCol.TableHeaderBg, UiPalette.TABLE_HEADER);
        color(ImGuiCol.TableBorderStrong, UiPalette.BORDER_STRONG);
        color(ImGuiCol.TableBorderLight, UiPalette.BORDER_LIGHT);
        color(ImGuiCol.TableRowBg, UiPalette.CLEAR);
        color(ImGuiCol.TableRowBgAlt, UiPalette.TABLE_ROW_ALT);
        color(ImGuiCol.TextSelectedBg, UiPalette.TEXT_SELECTED);
        color(ImGuiCol.NavHighlight, UiPalette.ACCENT);
        color(ImGuiCol.ModalWindowDimBg, UiPalette.OVERLAY_DIM);
    }

    private static void color(int target, UiColor color) {
        ImGui.getStyle().setColor(target, color.red(), color.green(), color.blue(), color.alpha());
    }
}

package com.particle.sim.ui.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public final class UITheme {
    private UITheme() {
    }

    public static void applyDarkTheme() {
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

        color(ImGuiCol.Text, UIColors.TEXT_PRIMARY);
        color(ImGuiCol.TextDisabled, UIColors.TEXT_MUTED);
        color(ImGuiCol.WindowBg, UIColors.BACKGROUND_WINDOW);
        color(ImGuiCol.ChildBg, UIColors.BACKGROUND_PANEL);
        color(ImGuiCol.PopupBg, UIColors.BACKGROUND_POPUP);
        color(ImGuiCol.Border, UIColors.BORDER_DEFAULT);
        color(ImGuiCol.BorderShadow, UIColors.TRANSPARENT);

        color(ImGuiCol.FrameBg, UIColors.SURFACE_DEFAULT);
        color(ImGuiCol.FrameBgHovered, UIColors.SURFACE_HOVER);
        color(ImGuiCol.FrameBgActive, UIColors.SURFACE_ACTIVE);
        color(ImGuiCol.TitleBg, UIColors.BACKGROUND_TITLE);
        color(ImGuiCol.TitleBgActive, UIColors.BACKGROUND_TITLE_ACTIVE);
        color(ImGuiCol.TitleBgCollapsed, UIColors.BACKGROUND_TITLE_COLLAPSED);
        color(ImGuiCol.MenuBarBg, UIColors.BACKGROUND_MENU);

        color(ImGuiCol.ScrollbarBg, UIColors.BACKGROUND_MENU.withAlpha(0.90f));
        color(ImGuiCol.ScrollbarGrab, UIColors.BORDER_DEFAULT.withAlpha(1.00f));
        color(ImGuiCol.ScrollbarGrabHovered, UIColors.BORDER_STRONG);
        color(ImGuiCol.ScrollbarGrabActive, UIColors.CONTROL_ACTIVE);
        color(ImGuiCol.CheckMark, UIColors.TEXT_PRIMARY);
        color(ImGuiCol.SliderGrab, UIColors.BORDER_STRONG);
        color(ImGuiCol.SliderGrabActive, UIColors.TEXT_MUTED);

        color(ImGuiCol.Button, UIColors.CONTROL_DEFAULT);
        color(ImGuiCol.ButtonHovered, UIColors.CONTROL_HOVER);
        color(ImGuiCol.ButtonActive, UIColors.CONTROL_ACTIVE);
        color(ImGuiCol.Header, UIColors.SURFACE_ACTIVE);
        color(ImGuiCol.HeaderHovered, UIColors.CONTROL_HOVER);
        color(ImGuiCol.HeaderActive, UIColors.CONTROL_ACTIVE);

        color(ImGuiCol.Separator, UIColors.BORDER_DEFAULT);
        color(ImGuiCol.SeparatorHovered, UIColors.BORDER_STRONG);
        color(ImGuiCol.SeparatorActive, UIColors.TEXT_MUTED);
        color(ImGuiCol.ResizeGrip, UIColors.BORDER_DEFAULT.withAlpha(0.28f));
        color(ImGuiCol.ResizeGripHovered, UIColors.BORDER_STRONG.withAlpha(0.62f));
        color(ImGuiCol.ResizeGripActive, UIColors.TEXT_MUTED.withAlpha(0.92f));

        color(ImGuiCol.Tab, UIColors.SURFACE_DEFAULT);
        color(ImGuiCol.TabHovered, UIColors.CONTROL_HOVER);
        color(ImGuiCol.TabSelected, UIColors.CONTROL_DEFAULT);
        color(ImGuiCol.TabDimmed, UIColors.BACKGROUND_TITLE);
        color(ImGuiCol.TabDimmedSelected, UIColors.SURFACE_HOVER);

        color(ImGuiCol.TableHeaderBg, UIColors.TABLE_HEADER);
        color(ImGuiCol.TableBorderStrong, UIColors.BORDER_STRONG);
        color(ImGuiCol.TableBorderLight, UIColors.BORDER_SUBTLE);
        color(ImGuiCol.TableRowBg, UIColors.TRANSPARENT);
        color(ImGuiCol.TableRowBgAlt, UIColors.TABLE_ROW_ALTERNATE);
        color(ImGuiCol.TextSelectedBg, UIColors.TEXT_SELECTION);
        color(ImGuiCol.NavHighlight, UIColors.BORDER_STRONG);
        color(ImGuiCol.ModalWindowDimBg, UIColors.SCRIM);
    }

    private static void color(int target, UIColor color) {
        ImGui.getStyle().setColor(target, color.red(), color.green(), color.blue(), color.alpha());
    }
}

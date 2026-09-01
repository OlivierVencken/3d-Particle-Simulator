package com.particle.sim.ui.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public final class Theme {
    private static final ComponentPalette PRIMARY = new ComponentPalette(
            Colors.PRIMARY_DEFAULT, Colors.PRIMARY_HOVER, Colors.PRIMARY_ACTIVE,
            Colors.PRIMARY_HOVER, Colors.TEXT_PRIMARY);
    private static final ComponentPalette SECONDARY = new ComponentPalette(
            Colors.CONTROL_DEFAULT, Colors.CONTROL_HOVER, Colors.CONTROL_ACTIVE,
            Colors.BORDER_DEFAULT, Colors.TEXT_PRIMARY);
    private static final ComponentPalette GHOST = new ComponentPalette(
            Colors.TRANSPARENT, Colors.SURFACE_HOVER, Colors.CONTROL_ACTIVE,
            Colors.TRANSPARENT, Colors.TEXT_PRIMARY);
    private static final ComponentPalette DESTRUCTIVE = new ComponentPalette(
            Colors.DESTRUCTIVE_DEFAULT, Colors.DESTRUCTIVE_HOVER, Colors.DESTRUCTIVE_ACTIVE,
            Colors.DESTRUCTIVE_HOVER, Colors.TEXT_PRIMARY);
    private static final ComponentPalette SELECTED = new ComponentPalette(
            Colors.SURFACE_ACTIVE, Colors.CONTROL_ACTIVE, Colors.CONTROL_ACTIVE,
            Colors.BORDER_STRONG, Colors.TEXT_PRIMARY);
    private static final ComponentPalette DISABLED = new ComponentPalette(
            Colors.SURFACE_DEFAULT, Colors.SURFACE_DEFAULT, Colors.SURFACE_DEFAULT,
            Colors.BORDER_SUBTLE, Colors.TEXT_MUTED);

    private static DesignTokens tokens = DesignTokens.unscaled();

    private Theme() {
    }

    public static void applyDarkTheme() {
        applyDarkTheme(1.0f);
    }

    /** Applies every style value from the unscaled design contract. */
    public static void applyDarkTheme(float scale) {
        tokens = DesignTokens.atScale(scale);
        ImGui.styleColorsDark();

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowPadding(tokens.windowInsetHorizontal(), tokens.windowInsetVertical());
        style.setFramePadding(tokens.frameInsetHorizontal(), tokens.frameInsetVertical());
        style.setItemSpacing(tokens.spaceMd(), tokens.spaceLg());
        style.setItemInnerSpacing(tokens.spaceMd(), tokens.spaceSm());
        style.setCellPadding(tokens.cellInset(), tokens.cellInset());
        style.setTouchExtraPadding(tokens.spaceXxs(), tokens.spaceXxs());
        style.setWindowRounding(0.0f);
        style.setChildRounding(0.0f);
        style.setFrameRounding(tokens.radiusSm());
        style.setPopupRounding(tokens.radiusLg());
        style.setScrollbarRounding(tokens.radiusSm());
        style.setGrabRounding(tokens.radiusSm());
        style.setTabRounding(tokens.radiusSm());
        style.setWindowBorderSize(tokens.borderWidth());
        style.setFrameBorderSize(tokens.borderWidth());
        style.setPopupBorderSize(tokens.borderWidth());
        style.setSeparatorTextBorderSize(tokens.borderWidth());
        style.setSeparatorTextPadding(tokens.spaceXl(), tokens.spaceXs());
        style.setScrollbarSize(tokens.scrollbarWidth());
        style.setGrabMinSize(tokens.minimumHitTarget());
        style.setWindowTitleAlign(0.0f, 0.5f);

        color(ImGuiCol.Text, Colors.TEXT_PRIMARY);
        color(ImGuiCol.TextDisabled, Colors.TEXT_MUTED);
        color(ImGuiCol.WindowBg, Colors.BACKGROUND_WINDOW);
        color(ImGuiCol.ChildBg, Colors.BACKGROUND_PANEL);
        color(ImGuiCol.PopupBg, Colors.BACKGROUND_POPUP);
        color(ImGuiCol.Border, Colors.BORDER_DEFAULT);
        color(ImGuiCol.BorderShadow, Colors.TRANSPARENT);

        color(ImGuiCol.FrameBg, Colors.SURFACE_DEFAULT);
        color(ImGuiCol.FrameBgHovered, Colors.SURFACE_HOVER);
        color(ImGuiCol.FrameBgActive, Colors.SURFACE_ACTIVE);
        color(ImGuiCol.TitleBg, Colors.BACKGROUND_TITLE);
        color(ImGuiCol.TitleBgActive, Colors.BACKGROUND_TITLE_ACTIVE);
        color(ImGuiCol.TitleBgCollapsed, Colors.BACKGROUND_TITLE_COLLAPSED);
        color(ImGuiCol.MenuBarBg, Colors.BACKGROUND_MENU);

        color(ImGuiCol.ScrollbarBg, Colors.BACKGROUND_MENU.withAlpha(0.90f));
        color(ImGuiCol.ScrollbarGrab, Colors.BORDER_DEFAULT.withAlpha(1.00f));
        color(ImGuiCol.ScrollbarGrabHovered, Colors.BORDER_STRONG);
        color(ImGuiCol.ScrollbarGrabActive, Colors.CONTROL_ACTIVE);
        color(ImGuiCol.CheckMark, Colors.TEXT_PRIMARY);
        color(ImGuiCol.SliderGrab, Colors.BORDER_STRONG);
        color(ImGuiCol.SliderGrabActive, Colors.TEXT_MUTED);

        color(ImGuiCol.Button, Colors.CONTROL_DEFAULT);
        color(ImGuiCol.ButtonHovered, Colors.CONTROL_HOVER);
        color(ImGuiCol.ButtonActive, Colors.CONTROL_ACTIVE);
        color(ImGuiCol.Header, Colors.SURFACE_ACTIVE);
        color(ImGuiCol.HeaderHovered, Colors.CONTROL_HOVER);
        color(ImGuiCol.HeaderActive, Colors.CONTROL_ACTIVE);

        color(ImGuiCol.Separator, Colors.BORDER_DEFAULT);
        color(ImGuiCol.SeparatorHovered, Colors.BORDER_STRONG);
        color(ImGuiCol.SeparatorActive, Colors.TEXT_MUTED);
        color(ImGuiCol.ResizeGrip, Colors.BORDER_DEFAULT.withAlpha(0.28f));
        color(ImGuiCol.ResizeGripHovered, Colors.BORDER_STRONG.withAlpha(0.62f));
        color(ImGuiCol.ResizeGripActive, Colors.TEXT_MUTED.withAlpha(0.92f));

        color(ImGuiCol.Tab, Colors.SURFACE_DEFAULT);
        color(ImGuiCol.TabHovered, Colors.CONTROL_HOVER);
        color(ImGuiCol.TabSelected, Colors.CONTROL_DEFAULT);
        color(ImGuiCol.TabDimmed, Colors.BACKGROUND_TITLE);
        color(ImGuiCol.TabDimmedSelected, Colors.SURFACE_HOVER);

        color(ImGuiCol.TableHeaderBg, Colors.TABLE_HEADER);
        color(ImGuiCol.TableBorderStrong, Colors.BORDER_STRONG);
        color(ImGuiCol.TableBorderLight, Colors.BORDER_SUBTLE);
        color(ImGuiCol.TableRowBg, Colors.TRANSPARENT);
        color(ImGuiCol.TableRowBgAlt, Colors.TABLE_ROW_ALTERNATE);
        color(ImGuiCol.TextSelectedBg, Colors.TEXT_SELECTION);
        color(ImGuiCol.NavHighlight, Colors.FOCUS_RING);
        color(ImGuiCol.ModalWindowDimBg, Colors.SCRIM);
    }

    public static DesignTokens tokens() {
        return tokens;
    }

    public static ComponentPalette palette(ComponentVariant variant) {
        return switch (variant) {
            case PRIMARY -> PRIMARY;
            case SECONDARY -> SECONDARY;
            case GHOST -> GHOST;
            case DESTRUCTIVE -> DESTRUCTIVE;
            case SELECTED -> SELECTED;
            case DISABLED -> DISABLED;
        };
    }

    private static void color(int target, Color color) {
        ImGui.getStyle().setColor(target, color.red(), color.green(), color.blue(), color.alpha());
    }
}

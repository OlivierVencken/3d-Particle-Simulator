package com.particle.sim.ui.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public final class UITheme {
    private static final UIComponentPalette PRIMARY = new UIComponentPalette(
            UIColors.PRIMARY_DEFAULT, UIColors.PRIMARY_HOVER, UIColors.PRIMARY_ACTIVE,
            UIColors.PRIMARY_HOVER, UIColors.TEXT_PRIMARY);
    private static final UIComponentPalette SECONDARY = new UIComponentPalette(
            UIColors.CONTROL_DEFAULT, UIColors.CONTROL_HOVER, UIColors.CONTROL_ACTIVE,
            UIColors.BORDER_DEFAULT, UIColors.TEXT_PRIMARY);
    private static final UIComponentPalette GHOST = new UIComponentPalette(
            UIColors.TRANSPARENT, UIColors.SURFACE_HOVER, UIColors.CONTROL_ACTIVE,
            UIColors.TRANSPARENT, UIColors.TEXT_PRIMARY);
    private static final UIComponentPalette DESTRUCTIVE = new UIComponentPalette(
            UIColors.DESTRUCTIVE_DEFAULT, UIColors.DESTRUCTIVE_HOVER, UIColors.DESTRUCTIVE_ACTIVE,
            UIColors.DESTRUCTIVE_HOVER, UIColors.TEXT_PRIMARY);
    private static final UIComponentPalette SELECTED = new UIComponentPalette(
            UIColors.SURFACE_ACTIVE, UIColors.CONTROL_ACTIVE, UIColors.CONTROL_ACTIVE,
            UIColors.BORDER_STRONG, UIColors.TEXT_PRIMARY);
    private static final UIComponentPalette DISABLED = new UIComponentPalette(
            UIColors.SURFACE_DEFAULT, UIColors.SURFACE_DEFAULT, UIColors.SURFACE_DEFAULT,
            UIColors.BORDER_SUBTLE, UIColors.TEXT_MUTED);

    private static UIDesignTokens tokens = UIDesignTokens.unscaled();

    private UITheme() {
    }

    public static void applyDarkTheme() {
        applyDarkTheme(1.0f);
    }

    /** Applies every style value from the unscaled design contract. */
    public static void applyDarkTheme(float scale) {
        tokens = UIDesignTokens.atScale(scale);
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
        color(ImGuiCol.NavHighlight, UIColors.FOCUS_RING);
        color(ImGuiCol.ModalWindowDimBg, UIColors.SCRIM);
    }

    public static UIDesignTokens tokens() {
        return tokens;
    }

    public static UIComponentPalette palette(UIComponentVariant variant) {
        return switch (variant) {
            case PRIMARY -> PRIMARY;
            case SECONDARY -> SECONDARY;
            case GHOST -> GHOST;
            case DESTRUCTIVE -> DESTRUCTIVE;
            case SELECTED -> SELECTED;
            case DISABLED -> DISABLED;
        };
    }

    private static void color(int target, UIColor color) {
        ImGui.getStyle().setColor(target, color.red(), color.green(), color.blue(), color.alpha());
    }
}

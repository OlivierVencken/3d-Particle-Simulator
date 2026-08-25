package com.particle.sim.ui.theme;

/**
 * Semantic UI measurements expressed at the active display scale.
 */
public final class UIDesignTokens {
    public static final float MINIMUM_SCALE = 0.75f;
    public static final float MAXIMUM_SCALE = 4.0f;

    private final float scale;

    private UIDesignTokens(float scale) {
        this.scale = sanitizeScale(scale);
    }

    public static UIDesignTokens atScale(float scale) {
        return new UIDesignTokens(scale);
    }

    public static UIDesignTokens unscaled() {
        return atScale(1.0f);
    }

    public float scale() {
        return scale;
    }

    public float dp(float value) {
        return value * scale;
    }

    public float spaceXxs() { return dp(1.8f); }
    public float spaceXs() { return dp(3.6f); }
    public float spaceSm() { return dp(5.4f); }
    public float spaceMd() { return dp(7.2f); }
    public float spaceLg() { return dp(9.0f); }
    public float spaceXl() { return dp(10.8f); }
    public float spaceXxl() { return dp(14.4f); }

    public float windowInsetHorizontal() { return spaceXxl(); }
    public float windowInsetVertical() { return spaceXl(); }
    public float frameInsetHorizontal() { return spaceMd(); }
    public float frameInsetVertical() { return spaceMd(); }
    public float cellInset() { return spaceSm(); }

    public float commandBarHeight() { return dp(36.0f); }
    public float sidebarWidth() { return dp(378.0f); }
    public float mediumSidebarWidth() { return dp(324.0f); }
    public float compactBreakpoint() { return dp(648.0f); }
    public float mediumBreakpoint() { return dp(990.0f); }
    public float wideBreakpoint() { return dp(1296.0f); }
    public float compactCommandMenuBreakpoint() { return dp(378.0f); }

    public float minimumHitTarget() { return dp(28.8f); }
    public float compactControlHeight() { return minimumHitTarget(); }
    public float controlHeight() { return minimumHitTarget(); }
    public float navigationControlHeight() { return minimumHitTarget(); }
    public float iconSize() { return dp(16.2f); }
    public int iconRasterSize() { return Math.max(64, Math.round(64.0f * scale)); }

    public float checkboxSize() { return dp(18.0f); }
    public float sliderTrackHeight() { return dp(5.4f); }
    public float sliderThumbRadius() { return dp(6.3f); }
    public float sliderGrabPadding() { return spaceXxs(); }
    public float chevronWidth() { return spaceMd(); }

    public float borderWidth() { return dp(0.9f); }
    public float emphasizedBorderWidth() { return dp(1.8f); }
    public float scrollbarWidth() { return dp(10.8f); }
    public float checkmarkWidth() { return dp(2.025f); }
    public float radiusSm() { return dp(2.7f); }
    public float radiusMd() { return spaceXs(); }
    public float radiusLg() { return spaceSm(); }

    public float bodyFontSize() { return dp(14.4f); }
    public float commandBarFontSize() { return dp(15.3f); }
    public float mediumFontSize() { return dp(16.2f); }
    public float sectionFontSize() { return dp(21.6f); }
    public float titleFontSize() { return dp(28.8f); }

    public float popupWidth() { return dp(387.0f); }
    public float aboutPopupHeight() { return dp(234.0f); }
    public float hotkeysPopupHeight() { return dp(342.0f); }
    public float errorPopupHeight() { return dp(171.0f); }
    public float tooltipWrapWidth() { return dp(288.0f); }
    public float buttonWidthSm() { return dp(57.6f); }
    public float buttonWidthMd() { return dp(79.2f); }
    public float buttonWidthLg() { return dp(86.4f); }
    public float buttonWidthXl() { return dp(100.8f); }
    public float buttonWidthXxl() { return dp(115.2f); }

    public float metricCardHeight() { return dp(57.6f); }
    public float primaryMetricMinimumWidth() { return dp(126.0f); }
    public float secondaryMetricMinimumWidth() { return dp(79.2f); }
    public float pairedControlMinimumWidth() { return dp(72.0f); }
    public float inputMinimumWidth() { return dp(90.0f); }
    public float debugInputWidth() { return dp(108.0f); }
    public float emptyStateMinimumHeight() { return dp(64.8f); }

    public float matrixGap() { return spaceXs(); }
    public float matrixCellInset() { return borderWidth(); }
    public float matrixCellMaximumSize() { return dp(39.6f); }
    public float matrixViewportMaximumHeight() { return dp(378.0f); }

    public static float sanitizeScale(float scale) {
        if (!Float.isFinite(scale) || scale <= 0.0f) {
            return 1.0f;
        }
        return Math.max(MINIMUM_SCALE, Math.min(MAXIMUM_SCALE, scale));
    }
}

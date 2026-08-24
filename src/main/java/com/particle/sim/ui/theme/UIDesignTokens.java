package com.particle.sim.ui.theme;

/**
 * Semantic UI measurements expressed at the active display scale.
 *
 * <p>The unscaled values are the design contract at 100% display scale. Keeping
 * scaling here prevents individual screens from applying DPI multipliers
 * inconsistently.</p>
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

    public float spaceXxs() { return dp(2.0f); }
    public float spaceXs() { return dp(4.0f); }
    public float spaceSm() { return dp(6.0f); }
    public float spaceMd() { return dp(8.0f); }
    public float spaceLg() { return dp(10.0f); }
    public float spaceXl() { return dp(12.0f); }
    public float spaceXxl() { return dp(16.0f); }

    public float windowInsetHorizontal() { return spaceXxl(); }
    public float windowInsetVertical() { return spaceXl(); }
    public float frameInsetHorizontal() { return spaceMd(); }
    public float frameInsetVertical() { return spaceMd(); }
    public float cellInset() { return spaceSm(); }

    public float commandBarHeight() { return dp(40.0f); }
    public float sidebarWidth() { return dp(420.0f); }
    public float mediumSidebarWidth() { return dp(360.0f); }
    public float compactBreakpoint() { return dp(720.0f); }
    public float mediumBreakpoint() { return dp(1100.0f); }
    public float wideBreakpoint() { return dp(1440.0f); }
    public float compactCommandMenuBreakpoint() { return dp(420.0f); }

    public float minimumHitTarget() { return dp(32.0f); }
    public float compactControlHeight() { return minimumHitTarget(); }
    public float controlHeight() { return minimumHitTarget(); }
    public float navigationControlHeight() { return minimumHitTarget(); }
    public float iconSize() { return dp(18.0f); }
    public int iconRasterSize() { return Math.max(64, Math.round(64.0f * scale)); }

    public float checkboxSize() { return dp(20.0f); }
    public float sliderTrackHeight() { return dp(6.0f); }
    public float sliderThumbRadius() { return dp(7.0f); }
    public float sliderGrabPadding() { return spaceXxs(); }
    public float chevronWidth() { return spaceMd(); }

    public float borderWidth() { return dp(1.0f); }
    public float emphasizedBorderWidth() { return dp(2.0f); }
    public float scrollbarWidth() { return dp(10.0f); }
    public float checkmarkWidth() { return dp(2.25f); }
    public float radiusSm() { return dp(3.0f); }
    public float radiusMd() { return spaceXs(); }
    public float radiusLg() { return spaceSm(); }

    public float bodyFontSize() { return dp(16.0f); }
    public float commandBarFontSize() { return dp(17.0f); }
    public float mediumFontSize() { return dp(18.0f); }
    public float sectionFontSize() { return dp(24.0f); }
    public float titleFontSize() { return dp(32.0f); }

    public float popupWidth() { return dp(430.0f); }
    public float aboutPopupHeight() { return dp(230.0f); }
    public float hotkeysPopupHeight() { return dp(340.0f); }
    public float buttonWidthSm() { return dp(64.0f); }
    public float buttonWidthMd() { return dp(88.0f); }
    public float buttonWidthLg() { return dp(96.0f); }
    public float buttonWidthXl() { return dp(112.0f); }
    public float buttonWidthXxl() { return dp(128.0f); }

    public float metricCardHeight() { return dp(64.0f); }
    public float primaryMetricMinimumWidth() { return dp(140.0f); }
    public float secondaryMetricMinimumWidth() { return dp(88.0f); }
    public float pairedControlMinimumWidth() { return dp(80.0f); }
    public float inputMinimumWidth() { return dp(100.0f); }
    public float debugInputWidth() { return dp(120.0f); }
    public float emptyStateMinimumHeight() { return dp(72.0f); }

    public float matrixGap() { return spaceXs(); }
    public float matrixCellInset() { return borderWidth(); }
    public float matrixCellMaximumSize() { return dp(44.0f); }
    public float matrixViewportMaximumHeight() { return dp(420.0f); }

    public static float sanitizeScale(float scale) {
        if (!Float.isFinite(scale) || scale <= 0.0f) {
            return 1.0f;
        }
        return Math.max(MINIMUM_SCALE, Math.min(MAXIMUM_SCALE, scale));
    }
}

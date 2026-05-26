package com.particle.sim.ui;

import imgui.ImVec4;

record UiColor(float red, float green, float blue, float alpha) {
    static UiColor fromHex(String hex) {
        String normalized = normalizeHex(hex);
        int red = Integer.parseInt(normalized.substring(0, 2), 16);
        int green = Integer.parseInt(normalized.substring(2, 4), 16);
        int blue = Integer.parseInt(normalized.substring(4, 6), 16);
        int alpha = normalized.length() == 8 ? Integer.parseInt(normalized.substring(6, 8), 16) : 255;

        return new UiColor(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
    }

    ImVec4 vec4() {
        return new ImVec4(red, green, blue, alpha);
    }

    UiColor withAlpha(float alpha) {
        return new UiColor(red, green, blue, alpha);
    }

    UiColor blend(UiColor other, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        return new UiColor(
                red + (other.red - red) * t,
                green + (other.green - green) * t,
                blue + (other.blue - blue) * t,
                alpha + (other.alpha - alpha) * t
        );
    }

    private static String normalizeHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex color cannot be null");
        }

        String normalized = hex.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
            throw new IllegalArgumentException("Hex color must be #RRGGBB or #RRGGBBAA: " + hex);
        }

        return normalized;
    }
}

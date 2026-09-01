package com.particle.sim.ui;

public record Layout(Mode mode, Panel commandBar, Panel sidebar, Panel simulation) {
    public enum Mode {
        WIDE,
        MEDIUM,
        COMPACT,
        FOCUS
    }

    public record Panel(float x, float y, float width, float height) {
        public static Panel hidden() {
            return new Panel(0.0f, 0.0f, 0.0f, 0.0f);
        }

        public boolean visible() {
            return width > 0.0f && height > 0.0f;
        }

        public float right() {
            return x + width;
        }

        public float bottom() {
            return y + height;
        }

        public boolean contains(float pointX, float pointY) {
            return visible() && pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
        }
    }
}

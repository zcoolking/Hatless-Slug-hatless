package dev.hatless;

/**
 * Head appearance of one player.
 *
 * @param hidden whether the head (and any helmet / head item) is completely hidden
 * @param size   uniform head scale
 * @param width  extra multiplier for the left-right axis (thin ... wide)
 */
public record HeadSettings(boolean hidden, float size, float width) {

    public static final float MIN_SIZE = 0.25f;
    public static final float MAX_SIZE = 4.0f;
    public static final float MIN_WIDTH = 0.25f;
    public static final float MAX_WIDTH = 3.0f;

    public static final HeadSettings DEFAULT = new HeadSettings(false, 1.0f, 1.0f);

    public HeadSettings {
        size = clamp(size, MIN_SIZE, MAX_SIZE);
        width = clamp(width, MIN_WIDTH, MAX_WIDTH);
    }

    public float scaleX() {
        return hidden ? 0.001f : size * width;
    }

    public float scaleY() {
        return hidden ? 0.001f : size;
    }

    public float scaleZ() {
        return hidden ? 0.001f : size;
    }

    private static float clamp(float v, float min, float max) {
        if (Float.isNaN(v)) return 1.0f;
        return Math.max(min, Math.min(max, v));
    }
}

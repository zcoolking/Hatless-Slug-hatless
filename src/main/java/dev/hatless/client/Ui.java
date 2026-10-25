package dev.hatless.client;

import net.minecraft.client.gui.DrawContext;

/** Small drawing helpers + palette for the Hatless GUI. */
final class Ui {
    static final int ACCENT = 0xFF7C5CFF;
    static final int ACCENT2 = 0xFF22D3EE;

    private Ui() {}

    static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((a >> 16) & 255) + ((((b >> 16) & 255) - ((a >> 16) & 255)) * t));
        int g = (int) (((a >> 8) & 255) + ((((b >> 8) & 255) - ((a >> 8) & 255)) * t));
        int bl = (int) ((a & 255) + (((b & 255) - (a & 255)) * t));
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    /** 1px rectangle outline made of four fills. */
    static void border(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + 1, color);
        c.fill(x, y + h - 1, x + w, y + h, color);
        c.fill(x, y + 1, x + 1, y + h - 1, color);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}

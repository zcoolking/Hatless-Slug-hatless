package dev.hatless.client;

import dev.hatless.HeadSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** The "ultra modern" head settings window. */
public class HeadScreen extends Screen {
    private static final int W = 300;
    private static final int H = 216;

    private boolean hidden;
    private float size;
    private float widthMul;

    private ModernSlider sizeSlider;
    private ModernSlider widthSlider;
    private ToggleButton hideToggle;
    private int px, py;

    public HeadScreen() {
        super(Text.translatable("screen.hatless.title"));
        HeadSettings s = HatlessClientState.local();
        hidden = s.hidden();
        size = s.size();
        widthMul = s.width();
    }

    @Override
    protected void init() {
        px = (this.width - W) / 2;
        py = (this.height - H) / 2;

        sizeSlider = addDrawableChild(new ModernSlider(px + 112, py + 44, 172, 26,
                Text.translatable("screen.hatless.size"),
                HeadSettings.MIN_SIZE, HeadSettings.MAX_SIZE, size, v -> {
            size = v;
            push();
        }));
        widthSlider = addDrawableChild(new ModernSlider(px + 112, py + 76, 172, 26,
                Text.translatable("screen.hatless.width"),
                HeadSettings.MIN_WIDTH, HeadSettings.MAX_WIDTH, widthMul, v -> {
            widthMul = v;
            push();
        }));
        hideToggle = addDrawableChild(ToggleButton.make(px + 112, py + 108, 172, 22,
                Text.translatable("screen.hatless.hide"), hidden, on -> {
                    hidden = on;
                    push();
                }));
        addDrawableChild(new ModernButton(px + 16, py + 156, 128, 24,
                Text.translatable("screen.hatless.reset"), b -> reset()));
        addDrawableChild(new ModernButton(px + 156, py + 156, 128, 24,
                Text.translatable("screen.hatless.done"), b -> this.close()));
    }

    private void push() {
        HatlessClientState.setLocal(new HeadSettings(hidden, size, widthMul));
    }

    private void reset() {
        size = 1.0f;
        widthMul = 1.0f;
        hidden = false;
        sizeSlider.setFloat(1.0f);
        widthSlider.setFloat(1.0f);
        hideToggle.setOn(false);
        push();
    }

    @Override
    public void removed() {
        HatlessClientState.save();
        HatlessClientState.sendToServer();
        HatlessRelay.requestPush();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Draws the dim overlay, the glass panel and the live preview (widgets are drawn afterwards). */
    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, this.width, this.height, 0x99080A10);

        // panel
        c.fill(px - 1, py - 1, px + W + 1, py + H + 1, 0x33FFFFFF);
        c.fill(px, py, px + W, py + H, 0xF2121520);
        for (int i = 0; i < W; i++) {
            c.fill(px + i, py, px + i + 1, py + 2, Ui.lerp(Ui.ACCENT, Ui.ACCENT2, i / (float) W));
        }

        // header
        c.drawText(this.textRenderer,
                Text.literal(HatlessClientState.SYMBOL + " ").formatted(Formatting.AQUA)
                        .append(Text.translatable("screen.hatless.title").formatted(Formatting.WHITE, Formatting.BOLD)),
                px + 16, py + 12, 0xFFFFFFFF, false);
        c.drawText(this.textRenderer, Text.translatable("screen.hatless.subtitle"),
                px + 16, py + 24, 0xFF6E778C, false);
        c.fill(px + 16, py + 36, px + W - 16, py + 37, 0x22FFFFFF);

        drawPreview(c);

        c.drawText(this.textRenderer, Text.translatable("screen.hatless.footer"),
                px + 16, py + 194, 0xFF5E6679, false);
    }

    private void drawPreview(DrawContext c) {
        int bx = px + 16, by = py + 44, bw = 84, bh = 86;
        c.fill(bx, by, bx + bw, by + bh, 0xFF171B26);
        Ui.border(c, bx, by, bw, bh, 0x33FFFFFF);

        float t = (size - HeadSettings.MIN_SIZE) / (HeadSettings.MAX_SIZE - HeadSettings.MIN_SIZE);
        int hh = Math.min(bh - 6, 14 + (int) ((bh - 18) * t));
        int ww = Math.max(4, Math.min(bw - 6, (int) (hh * widthMul)));
        int l = bx + (bw - ww) / 2;
        int top = by + (bh - hh) / 2;

        if (hidden) {
            Ui.border(c, l, top, ww, hh, 0x55FFFFFF);
            Text msg = Text.translatable("screen.hatless.hidden");
            c.drawText(this.textRenderer, msg,
                    bx + (bw - this.textRenderer.getWidth(msg)) / 2, by + bh / 2 - 4, 0xFF8A93A6, false);
            return;
        }

        c.fill(l, top, l + ww, top + hh, 0xFFC8936B);                       // skin
        c.fill(l, top, l + ww, top + Math.max(2, hh / 5), 0xFF4A3426);      // hair
        int eyeW = Math.max(2, ww / 6);
        int eyeH = Math.max(2, hh / 8);
        int ey = top + hh * 2 / 5;
        c.fill(l + ww / 5, ey, l + ww / 5 + eyeW, ey + eyeH, 0xFF1B1F2A);
        c.fill(l + ww - ww / 5 - eyeW, ey, l + ww - ww / 5, ey + eyeH, 0xFF1B1F2A);
        int my = top + hh * 7 / 10;
        c.fill(l + ww * 3 / 10, my, l + ww * 7 / 10, my + Math.max(1, hh / 16), 0xFF7A3E34); // mouth
    }
}

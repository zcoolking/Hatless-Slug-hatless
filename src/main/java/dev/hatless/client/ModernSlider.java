package dev.hatless.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.Consumer;

/** Flat slider with a gradient track and a round-ish knob. */
public class ModernSlider extends SliderWidget {
    private final Text label;
    private final float min;
    private final float max;
    private final Consumer<Float> onChange;

    public ModernSlider(int x, int y, int w, int h, Text label, float min, float max, float current,
                        Consumer<Float> onChange) {
        super(x, y, w, h, Text.empty(), (current - min) / (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
    }

    private float current() {
        float v = min + (max - min) * (float) this.value;
        v = Math.round(v * 20f) / 20f; // snap to 0.05 steps
        return Math.max(min, Math.min(max, v));
    }

    public void setFloat(float f) {
        this.value = Math.max(0.0, Math.min(1.0, (f - min) / (max - min)));
    }

    @Override
    protected void updateMessage() {
        // We draw our own text.
    }

    @Override
    protected void applyValue() {
        if (onChange != null) onChange.accept(current());
    }

    @Override
    public void renderWidget(DrawContext c, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hot = isHovered() || isFocused();

        c.drawText(tr, label, x, y + 1, hot ? 0xFFFFFFFF : 0xFFB4BCCD, false);
        String v = String.format(Locale.ROOT, "%.2fx", current());
        c.drawText(tr, v, x + w - tr.getWidth(v), y + 1, Ui.ACCENT2, false);

        int ty = y + h - 9;
        c.fill(x, ty, x + w, ty + 4, 0xFF262B3A);
        int fw = (int) Math.round(w * this.value);
        for (int i = 0; i < fw; i++) {
            c.fill(x + i, ty, x + i + 1, ty + 4, Ui.lerp(Ui.ACCENT, Ui.ACCENT2, i / (float) w));
        }
        int kx = Math.max(x, Math.min(x + w - 6, x + fw - 3));
        c.fill(kx, ty - 3, kx + 6, ty + 7, hot ? Ui.ACCENT2 : 0xFFFFFFFF);
        c.fill(kx + 1, ty - 2, kx + 5, ty + 6, 0xFFF1F3FA);
    }
}

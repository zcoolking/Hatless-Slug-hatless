package dev.hatless.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.function.Consumer;

/** Label on the left, iOS-style switch on the right. */
public class ToggleButton extends ButtonWidget {
    private boolean on;

    public static ToggleButton make(int x, int y, int w, int h, net.minecraft.text.Text label, boolean initial,
                                    Consumer<Boolean> onToggle) {
        ToggleButton[] self = new ToggleButton[1];
        self[0] = new ToggleButton(x, y, w, h, label, initial, btn -> {
            self[0].on = !self[0].on;
            onToggle.accept(self[0].on);
        });
        return self[0];
    }

    private ToggleButton(int x, int y, int w, int h, net.minecraft.text.Text label, boolean on, PressAction action) {
        super(x, y, w, h, label, action, DEFAULT_NARRATION_SUPPLIER);
        this.on = on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public void drawIcon(DrawContext c, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hot = isHovered() || isFocused();

        c.fill(x, y, x + w, y + h, hot ? 0xFF222839 : 0xFF1A1F2C);
        Ui.border(c, x, y, w, h, hot ? Ui.ACCENT : 0x22FFFFFF);
        c.drawText(tr, getMessage(), x + 8, y + (h - 8) / 2, 0xFFD5DAE6, false);

        int pw = 28, ph = 12;
        int px = x + w - pw - 8, py = y + (h - ph) / 2;
        if (on) {
            for (int i = 0; i < pw; i++) {
                c.fill(px + i, py, px + i + 1, py + ph, Ui.lerp(Ui.ACCENT, Ui.ACCENT2, i / (float) pw));
            }
        } else {
            c.fill(px, py, px + pw, py + ph, 0xFF2B3040);
        }
        int kx = on ? px + pw - ph + 1 : px + 1;
        c.fill(kx, py + 1, kx + ph - 2, py + ph - 1, 0xFFFFFFFF);
    }
}

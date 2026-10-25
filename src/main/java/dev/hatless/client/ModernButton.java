package dev.hatless.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

/** Flat button with an accent border on hover. */
public class ModernButton extends ButtonWidget {

    public ModernButton(int x, int y, int w, int h, net.minecraft.text.Text message, PressAction onPress) {
        super(x, y, w, h, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void drawIcon(DrawContext c, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hot = isHovered() || isFocused();

        c.fill(x, y, x + w, y + h, hot ? 0xFF2A3045 : 0xFF1D2230);
        Ui.border(c, x, y, w, h, hot ? Ui.ACCENT : 0x33FFFFFF);
        net.minecraft.text.Text msg = getMessage();
        c.drawText(tr, msg, x + (w - tr.getWidth(msg)) / 2, y + (h - 8) / 2, hot ? 0xFFFFFFFF : 0xFFD5DAE6, false);
    }
}

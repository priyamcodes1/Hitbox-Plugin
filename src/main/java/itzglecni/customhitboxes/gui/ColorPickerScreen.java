package itzglecni.customhitboxes.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.function.IntConsumer;

public class ColorPickerScreen extends Screen {
    private static final int MIN_PANEL_WIDTH = 272;
    private static final int MAX_PANEL_WIDTH = 360;
    private static final int MIN_PANEL_HEIGHT = 212;
    private static final int MAX_PANEL_HEIGHT = 252;

    private final Screen parent;
    private final IntConsumer onSave;

    private int red;
    private int green;
    private int blue;

    public ColorPickerScreen(Screen parent, Text title, int startColor, IntConsumer onSave) {
        super(title);
        this.parent = parent;
        this.onSave = onSave;
        this.red = (startColor >> 16) & 0xFF;
        this.green = (startColor >> 8) & 0xFF;
        this.blue = startColor & 0xFF;
    }

    @Override
    protected void init() {
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int sliderY = panelY + 32;

        addDrawableChild(new RgbSlider(panelX + 20, sliderY, panelWidth - 40, 20, Text.literal("R"), red, value -> red = value));
        sliderY += 30;
        addDrawableChild(new RgbSlider(panelX + 20, sliderY, panelWidth - 40, 20, Text.literal("G"), green, value -> green = value));
        sliderY += 30;
        addDrawableChild(new RgbSlider(panelX + 20, sliderY, panelWidth - 40, 20, Text.literal("B"), blue, value -> blue = value));

        int buttonY = panelY + panelHeight - 30;
        int buttonWidth = MathHelper.clamp((panelWidth - 60) / 2, 106, 152);

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
            .dimensions(panelX + 20, buttonY, buttonWidth, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Save Color"), b -> {
            onSave.accept(getColor());
            close();
        }).dimensions(panelX + panelWidth - buttonWidth - 20, buttonY, buttonWidth, 20)
            .build());
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0101218);

        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC1B1F2A);
        context.drawStrokedRectangle(panelX, panelY, panelWidth, panelHeight, 0xB36E88A8);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);

        int preview = 0xFF000000 | getColor();
        int previewTop = panelY + panelHeight - 72;
        context.fill(panelX + 20, previewTop, panelX + panelWidth - 20, previewTop + 18, preview);
        context.drawStrokedRectangle(panelX + 20, previewTop, panelWidth - 40, 18, 0xCCE6F2FF);

        Text code = Text.literal(String.format(Locale.ROOT, "#%06X", getColor())).formatted(Formatting.AQUA);
        context.drawCenteredTextWithShadow(this.textRenderer, code, this.width / 2, previewTop + 24, 0x66D9FF);
    }

    private int getColor() {
        return (red << 16) | (green << 8) | blue;
    }

    private int getPanelWidth() {
        return MathHelper.clamp(this.width - 40, MIN_PANEL_WIDTH, MAX_PANEL_WIDTH);
    }

    private int getPanelHeight() {
        return MathHelper.clamp(this.height - 40, MIN_PANEL_HEIGHT, MAX_PANEL_HEIGHT);
    }

    private static final class RgbSlider extends SliderWidget {
        private final String channelName;
        private final IntConsumer onChange;

        private RgbSlider(int x, int y, int width, int height, Text label, int start, IntConsumer onChange) {
            super(x, y, width, height, Text.empty(), start / 255.0);
            this.channelName = label.getString();
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(channelName + ": " + getChannelValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getChannelValue());
            updateMessage();
        }

        private int getChannelValue() {
            return (int) Math.round(this.value * 255.0);
        }
    }
}

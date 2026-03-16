package itzglecni.customhitboxes.gui;

import itzglecni.customhitboxes.config.ConfigManager;
import itzglecni.customhitboxes.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomConfigScreen extends Screen {
    private enum Section {
        TOGGLES,
        WIDTH,
        COLORS
    }

    private static final int PANEL_BG_TOP = 0xD0172130;
    private static final int PANEL_BG_BOTTOM = 0xD00C131E;
    private static final int PANEL_BORDER = 0xB8546C88;
    private static final int ROW_BG = 0xB7253449;
    private static final int ROW_HOVER_BG = 0xD53A4E6D;
    private static final int ROW_GLOW = 0x606A8CB8;
    private static final int TEXT_PRIMARY = 0xFFF0F6FF;
    private static final int SWITCH_BG = 0xFF1A222E;
    private static final int SWITCH_OFF = 0xFFD85A5A;
    private static final int SWITCH_ON = 0xFF47C97A;
    private static final int SWITCH_BORDER = 0xFFC7D8EB;
    private static final int RESET_BG = 0xFF2A3442;
    private static final int RESET_HOVER_BG = 0xFF344254;
    private static final int RESET_BORDER = 0xFFBFD0E6;
    private static final int RESET_TEXT = 0xFFFF6A6A;
    private static final float MIN_WIDTH = 0.1f;
    private static final float MAX_WIDTH = 20.0f;
    private static final float DEFAULT_WIDTH = 2.5f;
    private static final String WIDTH_LIMIT_TEXT = "You can only set the values from 0.1 to 20!";

    private final Screen parent;
    private final ModConfig working;

    private final List<ButtonWidget> sectionButtons = new ArrayList<>();
    private final List<Object> sectionElements = new ArrayList<>();
    private final List<RowMeta> rowMetas = new ArrayList<>();
    private final List<ColorPreviewMeta> colorPreviewMetas = new ArrayList<>();
    private final List<ToggleVisualMeta> toggleVisualMetas = new ArrayList<>();
    private final List<ResetVisualMeta> resetVisualMetas = new ArrayList<>();
    private final Map<RowMeta, Float> hoverProgress = new HashMap<>();

    private Section section = Section.TOGGLES;
    private ButtonWidget saveButton;

    private TextFieldWidget hitboxWidthField;
    private TextFieldWidget nonPlayerWidthField;

    private String widthValidationError;
    private int sectionScroll;
    private int maxSectionScroll;

    public CustomConfigScreen(Screen parent) {
        super(Text.literal("Hitbox Plus"));
        this.parent = parent;
        this.working = ConfigManager.getConfig().copy();
    }

    @Override
    protected void init() {
        clearChildren();
        sectionButtons.clear();
        sectionElements.clear();
        rowMetas.clear();
        colorPreviewMetas.clear();
        toggleVisualMetas.clear();
        resetVisualMetas.clear();

        addTopTabs();
        addBottomButtons();
        rebuildSectionWidgets();
    }

    private void addTopTabs() {
        int tabY = 18;
        int gap = 6;
        int available = this.width - 80 - (gap * 2);
        int tabWidth = MathHelper.clamp(available / 3, 84, 144);
        int tabHeight = 20;
        int total = tabWidth * 3 + gap * 2;
        int startX = (this.width - total) / 2;

        addTabButton(startX, tabY, tabWidth, tabHeight, Section.TOGGLES, Text.literal("Toggles"));
        addTabButton(startX + tabWidth + gap, tabY, tabWidth, tabHeight, Section.WIDTH, Text.literal("Width"));
        addTabButton(startX + (tabWidth + gap) * 2, tabY, tabWidth, tabHeight, Section.COLORS, Text.literal("Colors"));
        updateTabButtonStates();
    }

    private void addTabButton(int x, int y, int w, int h, Section target, Text label) {
        ButtonWidget tab = this.addDrawableChild(ButtonWidget.builder(label, b -> {
            this.section = target;
            this.sectionScroll = 0;
            rebuildSectionWidgets();
            updateTabButtonStates();
        }).dimensions(x, y, w, h).build());
        sectionButtons.add(tab);
    }

    private void updateTabButtonStates() {
        for (int i = 0; i < sectionButtons.size(); i++) {
            ButtonWidget button = sectionButtons.get(i);
            Section current = Section.values()[i];
            button.active = this.section != current;
        }
    }

    private void addBottomButtons() {
        int y = this.height - 30;
        int buttonW = MathHelper.clamp((this.width - 40) / 3, 96, 160);
        int gap = 16;
        int total = buttonW * 2 + gap;
        int startX = (this.width - total) / 2;
        int cancelX = startX;
        int saveX = startX + buttonW + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
            .dimensions(cancelX, y, buttonW, 20)
            .build());

        saveButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> saveAndClose())
            .dimensions(saveX, y, buttonW, 20)
            .build());
    }

    private void rebuildSectionWidgets() {
        removeSectionWidgets();
        rowMetas.clear();
        colorPreviewMetas.clear();
        toggleVisualMetas.clear();
        resetVisualMetas.clear();

        switch (section) {
            case TOGGLES -> buildTogglesSection();
            case WIDTH -> buildWidthSection();
            case COLORS -> buildColorsSection();
        }

        validateWidthFields();
    }

    private void removeSectionWidgets() {
        for (Object element : sectionElements) {
            if (element instanceof net.minecraft.client.gui.Element guiElement) {
                remove(guiElement);
            }
        }
        sectionElements.clear();
    }

    private <T extends net.minecraft.client.gui.Element & net.minecraft.client.gui.Drawable & net.minecraft.client.gui.Selectable> T addSectionWidget(T widget) {
        T added = this.addDrawableChild(widget);
        sectionElements.add(added);
        return added;
    }

    private void buildTogglesSection() {
        int firstY = getSectionTop() + 2;
        int rowY = firstY - sectionScroll;
        int rowH = 22;
        int gap = 6;
        int rowCount = 13;

        addToggleRow(rowY, rowH, "Mod Enabled", "Master toggle for the whole mod.",
            () -> working.enabled, v -> working.enabled = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Config Hotkey", "Allow the O key to open this config screen instantly.",
            () -> working.openConfigHotkeyEnabled, v -> working.openConfigHotkeyEnabled = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Elytra Players Only", "Only show player hitboxes when that player has Elytra equipped.",
            () -> working.renderOnlyWhenElytraEquipped, v -> working.renderOnlyWhenElytraEquipped = v, false);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Render Projectiles", "Show hitboxes for projectiles like arrows, pearls, and wind charges.",
            () -> working.renderProjectiles, v -> working.renderProjectiles = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Players + Projectiles Only", "When ON, only players and projectiles show hitboxes; mobs/animals are hidden.",
            () -> working.renderEntities, v -> working.renderEntities = v, false);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Looking Direction Line", "Draw a line showing where each entity is facing.",
            () -> working.lookingDirection, v -> working.lookingDirection = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Remove Direction Arrow", "Hide the arrow tip and keep only the direction line.",
            () -> working.removeDirectionArrow, v -> working.removeDirectionArrow = v, false);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Forehead Line", "Show the small forehead level line on hitboxes.",
            () -> working.foreheadLine, v -> working.foreheadLine = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Self Hitbox", "Render your own hitbox.",
            () -> working.selfHitbox, v -> working.selfHitbox = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Hover Color", "Apply color to hitboxes when your crosshair is over a hittable target.",
            () -> working.hoverColorHitbox, v -> working.hoverColorHitbox = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Hurt Color", "To change the color of hitbox after hitting an entity, enable this option and set 'Hit Color' in the Colors tab.",
            () -> working.hitColorHitbox, v -> working.hitColorHitbox = v, true);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Hide Firework Hitbox", "Do not render hitboxes for fireworks.",
            () -> working.noFireworkHitbox, v -> working.noFireworkHitbox = v, false);
        rowY += rowH + gap;

        addToggleRow(rowY, rowH, "Hide Stuck Arrows", "Skip hitboxes for arrows that are stuck and no longer flying.",
            () -> working.noStuckArrows, v -> working.noStuckArrows = v, false);

        int contentBottom = firstY + rowCount * rowH + (rowCount - 1) * gap;
        maxSectionScroll = computeMaxScroll(firstY, contentBottom);
        sectionScroll = MathHelper.clamp(sectionScroll, 0, maxSectionScroll);
    }

    private void addToggleRow(int y, int h, String label, String description,
                              BooleanGetter getter, BooleanSetter setter, boolean defaultValue) {
        int panelX = getContentPanelX();
        int panelW = getContentPanelWidth();
        if (!isRectFullyInsideSection(panelX, y, panelW, h)) {
            return;
        }

        int controlGap = 6;
        int resetW = MathHelper.clamp(panelW / 8, 58, 84);
        int toggleW = MathHelper.clamp(panelW / 7, 72, 110);

        int resetX = panelX + panelW - resetW - 8;
        int toggleX = resetX - toggleW - controlGap;

        addSectionWidget(ButtonWidget.builder(Text.empty(), b -> {
            setter.set(!getter.get());
        }).dimensions(toggleX, y + 2, toggleW, h - 4).tooltip(Tooltip.of(Text.literal(description))).build());

        ToggleVisualMeta toggleMeta = new ToggleVisualMeta(toggleX, y + 2, toggleW, h - 4, getter);
        toggleVisualMetas.add(toggleMeta);

        ButtonWidget reset = addSectionWidget(ButtonWidget.builder(Text.empty(), b -> {
            setter.set(defaultValue);
        }).dimensions(resetX, y + 2, resetW, h - 4).tooltip(Tooltip.of(Text.literal("Reset this option"))).build());
        resetVisualMetas.add(new ResetVisualMeta(resetX, y + 2, resetW, h - 4, Text.literal("RESET"), reset));

        RowMeta row = new RowMeta(panelX, y, panelW, h, Text.literal(label), Text.literal(description));
        rowMetas.add(row);
        hoverProgress.put(row, 0.0f);
    }

    private void buildWidthSection() {
        int panelX = getContentPanelX();
        int panelW = getContentPanelWidth();
        int rowH = 28;

        int firstY = getSectionTop() + 26;
        int y1 = firstY;
        int y2 = y1 + rowH + 12;

        int resetW = MathHelper.clamp(panelW / 8, 54, 82);
        int fieldW = MathHelper.clamp(panelW / 4, 124, 220);
        int fieldX = panelX + panelW - (resetW + fieldW + 18);
        int resetX = panelX + panelW - resetW - 8;

        maxSectionScroll = 0;
        sectionScroll = 0;

        hitboxWidthField = createNumericField(fieldX, y1 + 3, fieldW, hToField(rowH), working.hitboxWidth, v -> working.hitboxWidth = v);
        addSectionWidget(hitboxWidthField);

        ButtonWidget reset1 = addSectionWidget(ButtonWidget.builder(Text.empty(), b -> {
            hitboxWidthField.setText("2.5");
            validateWidthFields();
        }).dimensions(resetX, y1 + 3, resetW, hToField(rowH)).tooltip(Tooltip.of(Text.literal("Reset to 2.5"))).build());
        resetVisualMetas.add(new ResetVisualMeta(resetX, y1 + 3, resetW, hToField(rowH), Text.literal("RESET"), reset1));

        nonPlayerWidthField = createNumericField(fieldX, y2 + 3, fieldW, hToField(rowH), working.nonPlayerHitboxWidth, v -> working.nonPlayerHitboxWidth = v);
        addSectionWidget(nonPlayerWidthField);

        ButtonWidget reset2 = addSectionWidget(ButtonWidget.builder(Text.empty(), b -> {
            nonPlayerWidthField.setText("2.5");
            validateWidthFields();
        }).dimensions(resetX, y2 + 3, resetW, hToField(rowH)).tooltip(Tooltip.of(Text.literal("Reset to 2.5"))).build());
        resetVisualMetas.add(new ResetVisualMeta(resetX, y2 + 3, resetW, hToField(rowH), Text.literal("RESET"), reset2));

        RowMeta row1 = new RowMeta(panelX, y1, panelW, rowH, Text.literal("Hitbox Width"), Text.literal("Adjust player hitbox width from 0.1 to 20.0 (2.5 is default)."));
        RowMeta row2 = new RowMeta(panelX, y2, panelW, rowH, Text.literal("Non-Player Hitbox Width"), Text.literal("Adjust non-player hitbox width from 0.1 to 20.0 (2.5 is default)."));
        rowMetas.add(row1);
        rowMetas.add(row2);
        hoverProgress.put(row1, 0.0f);
        hoverProgress.put(row2, 0.0f);
    }

    private TextFieldWidget createNumericField(int x, int y, int width, int height, float value, FloatSetter onValidValue) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, height, Text.literal(""));
        field.setText(String.format(Locale.ROOT, "%.2f", value));
        field.setChangedListener(v -> {
            Float parsed = parseRange(v);
            if (parsed != null) {
                onValidValue.set(parsed);
            }
            validateWidthFields();
        });
        return field;
    }

    private void buildColorsSection() {
        int firstY = getSectionTop() + 2;
        int rowY = firstY - sectionScroll;
        int rowH = 24;
        int gap = 6;

        addColorRow(rowY, rowH, "Hitbox Main Color", "Main color used for regular hitboxes.",
            () -> working.hitboxMainColor, v -> working.hitboxMainColor = v,
            () -> working.rainbowMain, v -> working.rainbowMain = v,
            0xFFFFFF, false);
        rowY += rowH + gap;

        addColorRow(rowY, rowH, "Hover Color", "Color shown when your crosshair is over a valid target.",
            () -> working.hoverColor, v -> working.hoverColor = v,
            () -> working.rainbowHover, v -> working.rainbowHover = v,
            0x00FF00, false);
        rowY += rowH + gap;

        addColorRow(rowY, rowH, "Hit Color", "Color shown for a short time after real damage.",
            () -> working.hitColor, v -> working.hitColor = v,
            () -> working.rainbowHit, v -> working.rainbowHit = v,
            0xFF0000, false);
        rowY += rowH + gap;

        addColorRow(rowY, rowH, "Looking Direction Color", "Color of the looking direction line.",
            () -> working.lookingDirectionColor, v -> working.lookingDirectionColor = v,
            () -> working.rainbowLooking, v -> working.rainbowLooking = v,
            0x0000FF, false);
        rowY += rowH + gap;

        addColorRow(rowY, rowH, "Forehead Line Color", "Color of the forehead guide line.",
            () -> working.foreheadLineColor, v -> working.foreheadLineColor = v,
            () -> working.rainbowForehead, v -> working.rainbowForehead = v,
            0xFF0000, false);
        rowY += rowH + gap;

        addColorRow(rowY, rowH, "Projectiles Color", "Color used for projectile hitboxes only.",
            () -> working.projectilesColor, v -> working.projectilesColor = v,
            () -> working.rainbowProjectiles, v -> working.rainbowProjectiles = v,
            0xFFFF00, false);

        int contentBottom = firstY + 6 * rowH + 5 * gap;
        maxSectionScroll = computeMaxScroll(firstY, contentBottom);
        sectionScroll = MathHelper.clamp(sectionScroll, 0, maxSectionScroll);
    }

    private void addColorRow(int y, int h, String label, String description,
                             IntGetter colorGetter, IntSetter colorSetter,
                             BooleanGetter rainbowGetter, BooleanSetter rainbowSetter,
                             int defaultColor, boolean defaultRainbow) {
        int panelX = getContentPanelX();
        int panelW = getContentPanelWidth();
        if (!isRectFullyInsideSection(panelX, y, panelW, h)) {
            return;
        }

        int resetW = MathHelper.clamp(panelW / 8, 58, 84);
        int rainbowW = MathHelper.clamp(panelW / 5, 92, 132);
        int editW = MathHelper.clamp(panelW / 5, 90, 128);
        int previewW = MathHelper.clamp(panelW / 12, 20, 36);

        int resetX = panelX + panelW - resetW - 8;
        int rainbowX = resetX - rainbowW - 6;
        int editX = rainbowX - editW - 6;
        int previewX = editX - previewW - 6;

        colorPreviewMetas.add(new ColorPreviewMeta(previewX, y + 3, previewW, h - 6, colorGetter));

        ButtonWidget edit = addSectionWidget(ButtonWidget.builder(colorButtonText(colorGetter.get()), b -> {
            this.client.setScreen(new ColorPickerScreen(this, Text.literal(label), colorGetter.get(), color -> {
                colorSetter.set(color);
                b.setMessage(colorButtonText(color));
            }));
        }).dimensions(editX, y + 3, editW, h - 6).tooltip(Tooltip.of(Text.literal("Open RGB picker"))).build());

        ButtonWidget rainbow = addSectionWidget(ButtonWidget.builder(rainbowText(rainbowGetter.get()), b -> {
            rainbowSetter.set(!rainbowGetter.get());
            b.setMessage(rainbowText(rainbowGetter.get()));
        }).dimensions(rainbowX, y + 3, rainbowW, h - 6).tooltip(Tooltip.of(Text.literal("Toggle rainbow mode for this color"))).build());

        ButtonWidget reset = addSectionWidget(ButtonWidget.builder(Text.empty(), b -> {
            colorSetter.set(defaultColor);
            rainbowSetter.set(defaultRainbow);
            edit.setMessage(colorButtonText(colorGetter.get()));
            rainbow.setMessage(rainbowText(rainbowGetter.get()));
        }).dimensions(resetX, y + 3, resetW, h - 6).tooltip(Tooltip.of(Text.literal("Reset color and rainbow"))).build());
        resetVisualMetas.add(new ResetVisualMeta(resetX, y + 3, resetW, h - 6, Text.literal("RESET"), reset));

        RowMeta row = new RowMeta(panelX, y, panelW, h, Text.literal(label), Text.literal(description));
        rowMetas.add(row);
        hoverProgress.put(row, 0.0f);
    }

    private void validateWidthFields() {
        if (saveButton == null) {
            return;
        }

        widthValidationError = null;
        if (section == Section.WIDTH) {
            Float player = parseRange(hitboxWidthField == null ? null : hitboxWidthField.getText());
            Float nonPlayer = parseRange(nonPlayerWidthField == null ? null : nonPlayerWidthField.getText());
            if (player == null || nonPlayer == null) {
                widthValidationError = WIDTH_LIMIT_TEXT;
            }
        }

        saveButton.active = widthValidationError == null;
    }

    private Float parseRange(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            float value = Float.parseFloat(text);
            if (value < MIN_WIDTH || value > MAX_WIDTH) {
                return null;
            }
            return value;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void saveAndClose() {
        // If width fields are present, validate and commit them before saving.
        if (hitboxWidthField != null && nonPlayerWidthField != null) {
            Float player = parseRange(hitboxWidthField.getText());
            Float nonPlayer = parseRange(nonPlayerWidthField.getText());
            if (player == null || nonPlayer == null) {
                validateWidthFields();
                return;
            }
            working.hitboxWidth = player;
            working.nonPlayerHitboxWidth = nonPlayer;
        }

        ModConfig live = ConfigManager.getConfig();
        live.copyFrom(working);
        ConfigManager.save();
        close();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseInSectionArea(mouseX, mouseY) && maxSectionScroll > 0) {
            int step = getScrollStep();
            int wheel = (int) Math.signum(verticalAmount);
            if (wheel == 0) {
                return true;
            }
            int next = sectionScroll - (wheel * step);
            sectionScroll = MathHelper.clamp(next, 0, maxSectionScroll);
            rebuildSectionWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Avoid calling Screen background blur here to prevent duplicate blur passes under modded render stacks.
        context.fillGradient(0, 0, this.width, this.height, 0xB00E1623, 0xB0060A12);

        int panelX = getOuterPanelX();
        int panelY = getOuterPanelY();
        int panelW = getOuterPanelWidth();
        int panelH = getOuterPanelHeight();

        context.fillGradient(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG_TOP, PANEL_BG_BOTTOM);
        context.drawStrokedRectangle(panelX, panelY, panelW, panelH, PANEL_BORDER);

        for (RowMeta row : rowMetas) {
            if (!isRowVisible(row)) {
                continue;
            }
            boolean hovered = row.contains(mouseX, mouseY);
            float current = hoverProgress.getOrDefault(row, 0.0f);
            float target = hovered ? 1.0f : 0.0f;
            current += (target - current) * 0.24f;
            hoverProgress.put(row, current);

            int color = blendColor(ROW_BG, ROW_HOVER_BG, current);
            context.fill(row.x(), row.y(), row.x() + row.w(), row.y() + row.h(), color);
            context.drawStrokedRectangle(row.x(), row.y(), row.w(), row.h(), 0xB96E89AD);
            if (current > 0.03f) {
                int glow = withAlpha(ROW_GLOW, (int) (96 * current));
                context.drawStrokedRectangle(row.x(), row.y(), row.w(), row.h(), glow);
            }
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, TEXT_PRIMARY);

        if (section == Section.WIDTH && widthValidationError != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(widthValidationError).formatted(Formatting.RED), this.width / 2, this.height - 42, 0xFFFF5555);
        }

        super.render(context, mouseX, mouseY, delta);

        for (ToggleVisualMeta toggle : toggleVisualMetas) {
            if (!isRectVisible(toggle.x(), toggle.y(), toggle.w(), toggle.h())) {
                continue;
            }
            boolean enabled = toggle.getter().get();
            context.fill(toggle.x(), toggle.y(), toggle.x() + toggle.w(), toggle.y() + toggle.h(), SWITCH_BG);
            context.drawStrokedRectangle(toggle.x(), toggle.y(), toggle.w(), toggle.h(), SWITCH_BORDER);

            int indicatorW = (toggle.w() / 2) - 4;
            int indicatorX = enabled ? toggle.x() + toggle.w() - indicatorW - 2 : toggle.x() + 2;
            int indicatorColor = enabled ? SWITCH_ON : SWITCH_OFF;
            context.fill(indicatorX, toggle.y() + 2, indicatorX + indicatorW, toggle.y() + toggle.h() - 2, indicatorColor);

            Text state = enabled ? Text.literal("YES") : Text.literal("NO");
            int textColor = 0xFFF6FBFF;
            int textY = toggle.y() + (toggle.h() - 8) / 2;
            context.drawCenteredTextWithShadow(this.textRenderer, state, toggle.x() + (toggle.w() / 2), textY, textColor);
        }

        for (ResetVisualMeta reset : resetVisualMetas) {
            if (!isRectVisible(reset.x(), reset.y(), reset.w(), reset.h())) {
                continue;
            }
            boolean hovered = reset.button().isHovered();
            int bg = hovered ? RESET_HOVER_BG : RESET_BG;
            context.fill(reset.x(), reset.y(), reset.x() + reset.w(), reset.y() + reset.h(), bg);
            context.drawStrokedRectangle(reset.x(), reset.y(), reset.w(), reset.h(), RESET_BORDER);
            int textY = reset.y() + (reset.h() - 8) / 2;
            context.drawCenteredTextWithShadow(this.textRenderer, reset.label(), reset.x() + (reset.w() / 2), textY, RESET_TEXT);
        }

        for (RowMeta row : rowMetas) {
            if (!isRowVisible(row)) {
                continue;
            }
            int labelY = row.y() + (row.h() - 8) / 2;
            context.drawTextWithShadow(this.textRenderer, row.label(), row.x() + 10, labelY, TEXT_PRIMARY);
        }

        for (ColorPreviewMeta preview : colorPreviewMetas) {
            if (!isRectVisible(preview.x(), preview.y(), preview.w(), preview.h())) {
                continue;
            }
            int color = 0xFF000000 | (preview.getter().get() & 0x00FFFFFF);
            context.fill(preview.x(), preview.y(), preview.x() + preview.w(), preview.y() + preview.h(), color);
            context.drawStrokedRectangle(preview.x(), preview.y(), preview.w(), preview.h(), 0xCCECF5FF);
        }

        for (RowMeta row : rowMetas) {
            if (!isRowVisible(row)) {
                continue;
            }
            if (row.contains(mouseX, mouseY)) {
                context.drawTooltip(this.textRenderer, row.description(), mouseX, mouseY);
                break;
            }
        }

        if (maxSectionScroll > 0) {
            int top = getSectionTop();
            int bottom = getSectionBottom();
            int trackX = getContentPanelX() + getContentPanelWidth() + 2;
            int trackY = top;
            int trackH = bottom - top;
            int thumbH = Math.max(20, (int) (trackH * 0.28f));
            int thumbRange = Math.max(1, trackH - thumbH);
            int thumbY = trackY + (int) ((sectionScroll / (float) maxSectionScroll) * thumbRange);
            context.fill(trackX, trackY, trackX + 6, trackY + trackH, 0x6C243043);
            context.fill(trackX + 1, thumbY, trackX + 5, thumbY + thumbH, 0xD39DC7FF);
        }
    }

    private int computeMaxScroll(int contentStartY, int contentBottomY) {
        return Math.max(0, contentBottomY - getSectionBottom());
    }

    private int getScrollStep() {
        return switch (section) {
            case TOGGLES -> 28; // row height + gap
            case COLORS -> 30;  // row height + gap
            case WIDTH -> 40;
        };
    }

    private int getOuterPanelX() {
        return MathHelper.clamp(this.width / 24, 12, 28);
    }

    private int getOuterPanelY() {
        return 46;
    }

    private int getOuterPanelWidth() {
        return this.width - (getOuterPanelX() * 2);
    }

    private int getOuterPanelHeight() {
        return this.height - 94;
    }

    private int getContentPanelX() {
        return getOuterPanelX() + 8;
    }

    private int getContentPanelWidth() {
        return getOuterPanelWidth() - 16;
    }

    private int getSectionTop() {
        return 54;
    }

    private int getSectionBottom() {
        return this.height - 56;
    }

    private boolean isMouseInSectionArea(double mouseX, double mouseY) {
        int left = getContentPanelX();
        int right = left + getContentPanelWidth();
        return mouseX >= left && mouseX <= right && mouseY >= getSectionTop() && mouseY <= getSectionBottom();
    }

    private boolean isRowVisible(RowMeta row) {
        return isRectVisible(row.x(), row.y(), row.w(), row.h());
    }

    private boolean isRectVisible(int x, int y, int w, int h) {
        int top = getSectionTop();
        int bottom = getSectionBottom();
        int left = getContentPanelX();
        int right = left + getContentPanelWidth();
        return y + h >= top && y <= bottom && x + w >= left && x <= right;
    }

    private boolean isRectFullyInsideSection(int x, int y, int w, int h) {
        int top = getSectionTop();
        int bottom = getSectionBottom();
        int left = getContentPanelX();
        int right = left + getContentPanelWidth();
        return y >= top && (y + h) <= bottom && x >= left && (x + w) <= right;
    }

    private int blendColor(int from, int to, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a1 = (from >>> 24) & 0xFF;
        int r1 = (from >>> 16) & 0xFF;
        int g1 = (from >>> 8) & 0xFF;
        int b1 = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF;
        int r2 = (to >>> 16) & 0xFF;
        int g2 = (to >>> 8) & 0xFF;
        int b2 = to & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int withAlpha(int color, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private int hToField(int rowHeight) {
        return rowHeight - 4;
    }

    private Text rainbowText(boolean enabled) {
        return enabled
            ? Text.literal("Rainbow ON").formatted(Formatting.GREEN)
            : Text.literal("Rainbow OFF").formatted(Formatting.RED);
    }

    private Text colorButtonText(int color) {
        return Text.literal(String.format(Locale.ROOT, "RGB #%06X", color & 0xFFFFFF));
    }

    private record RowMeta(int x, int y, int w, int h, Text label, Text description) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }
    }

    private record ColorPreviewMeta(int x, int y, int w, int h, IntGetter getter) {
    }

    private record ToggleVisualMeta(int x, int y, int w, int h, BooleanGetter getter) {
    }

    private record ResetVisualMeta(int x, int y, int w, int h, Text label, ButtonWidget button) {
    }

    @FunctionalInterface
    private interface BooleanGetter {
        boolean get();
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }

    @FunctionalInterface
    private interface IntGetter {
        int get();
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}

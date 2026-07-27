package com.shinoow.abyssalcraft.client.screen.config;

import java.util.List;

import com.shinoow.abyssalcraft.config.ConfigEditorModel;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.ConfigCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Mods-list editor for every registered AbyssalCraft COMMON and CLIENT config value. */
public final class ACConfigScreen extends Screen {

    private static final int ROWS_PER_PAGE = 7;

    private final Screen parent;
    private final ConfigEditorModel model = new ConfigEditorModel();
    private int page;
    private String error = "";

    public ACConfigScreen(Screen parent) {
        super(Component.translatable("gui.abyssalcraft.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        List<ConfigCompat.Entry<?>> entries = model.entries();
        int pageCount = Math.max(1, (entries.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        page = Math.max(0, Math.min(page, pageCount - 1));
        int start = page * ROWS_PER_PAGE;
        int fieldX = width / 2;
        int fieldWidth = Math.min(210, width / 2 - 24);

        for (int row = 0; row < ROWS_PER_PAGE && start + row < entries.size(); row++) {
            ConfigCompat.Entry<?> entry = entries.get(start + row);
            int y = 46 + row * 26;
            if (entry.valueType() == ConfigCompat.ValueType.BOOLEAN) {
                Button toggle = Button.builder(booleanLabel(model.value(entry.path())), button -> {
                    boolean next = !Boolean.parseBoolean(model.value(entry.path()));
                    model.setValue(entry.path(), Boolean.toString(next));
                    button.setMessage(booleanLabel(Boolean.toString(next)));
                    error = "";
                }).bounds(fieldX, y, fieldWidth, 20).build();
                addRenderableWidget(toggle);
            } else {
                EditBox edit = new EditBox(font, fieldX, y, fieldWidth, 20, Component.literal(entry.path()));
                edit.setMaxLength(4096);
                edit.setValue(model.value(entry.path()));
                edit.setResponder(value -> {
                    model.setValue(entry.path(), value);
                    error = model.validate(entry.path());
                });
                addRenderableWidget(edit);
            }
        }

        int footerY = height - 28;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
            .bounds(width / 2 - 156, footerY, 24, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.translatable("gui.abyssalcraft.config.reload"), button -> reload())
            .bounds(width / 2 - 126, footerY, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> save())
            .bounds(width / 2 - 50, footerY, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
            .bounds(width / 2 + 26, footerY, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
            .bounds(width / 2 + 102, footerY, 24, 20).build()).active = page + 1 < pageCount;
    }

    private void changePage(int offset) {
        page += offset;
        error = "";
        rebuildControls();
    }

    private void reload() {
        model.reload();
        error = "";
        rebuildControls();
    }

    private void save() {
        error = model.save();
        if (error.isEmpty() && minecraft != null) minecraft.setScreen(parent);
    }

    private static Component booleanLabel(String value) {
        return Component.translatable(Boolean.parseBoolean(value) ? "options.on" : "options.off");
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        List<ConfigCompat.Entry<?>> entries = model.entries();
        int start = page * ROWS_PER_PAGE;
        for (int row = 0; row < ROWS_PER_PAGE && start + row < entries.size(); row++) {
            ConfigCompat.Entry<?> entry = entries.get(start + row);
            String label = font.plainSubstrByWidth(entry.path(), Math.max(40, width / 2 - 36));
            graphics.drawString(font, label, 18, 52 + row * 26, 0xD8D8D8, false);
        }
        int pageCount = Math.max(1, (entries.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        graphics.drawCenteredString(font, Component.literal((page + 1) + " / " + pageCount), width / 2, height - 42, 0xA0A0A0);
        if (!error.isEmpty()) graphics.drawCenteredString(font, Component.literal(error), width / 2, 31, 0xFF5555);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.OpenSpellbookMessage;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

/**
 * The Necronomicon book screen (owned by PH-5), a modernised {@code GuiNecronomicon}: a plain {@link Screen}
 * (opened on demand, not menu-backed) that navigates a {@link NecronomiconEntry} tree &mdash; category
 * buttons drill into children, a back button pops, and gated entries are hidden until the player has the
 * required research (read-only against the synced necrodata, PS-2/PS-8).
 *
 * <p>The faithful book texture / page layout is deferred content (asset migration, PK); this shell uses the
 * vanilla background + text so it is fork-free apart from the {@code renderBackground} signature change,
 * which is absorbed by {@link ClientScreenCompat#background}.
 */
public final class NecronomiconScreen extends Screen {

    private final Deque<NecronomiconEntry> path = new ArrayDeque<>();
    private final int bookType;
    private int page;

    public NecronomiconScreen(NecronomiconEntry root, int bookType) {
        super(Component.translatable(root.titleKey()));
        path.push(root);
        this.bookType = bookType;
    }

    private NecronomiconEntry current() {
        return path.peek();
    }

    @Override
    protected void init() {
        NecronomiconEntry cur = current();
        Player player = Minecraft.getInstance().player;
        NecroData data = player != null ? NecroDataCapability.get(player) : null;

        List<NecronomiconEntry> visible = cur.children().stream()
            .filter(child -> isVisible(child, data)).toList();
        int rows = Math.max(1, (height - 88) / 24);
        int pages = Math.max(1, (visible.size() + rows - 1) / rows);
        page = Math.min(page, pages - 1);
        int from = page * rows;
        int to = Math.min(visible.size(), from + rows);

        int y = 40;
        for (NecronomiconEntry child : visible.subList(from, to)) {
            NecronomiconEntry target = child;
            Component label = entryLabel(child, data);
            addRenderableWidget(Button.builder(label, b -> navigateTo(target))
                    .bounds(width / 2 - 100, y, 200, 20).build());
            y += 24;
        }

        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(width / 2 - 100, height - 52, 45, 20).build());
            addRenderableWidget(Button.builder(Component.translatable(
                "gui.abyssalcraft.necronomicon.page", page + 1, pages), b -> { })
                .bounds(width / 2 - 50, height - 52, 100, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(width / 2 + 55, height - 52, 45, 20).build());
        }

        if (path.size() > 1) {
            addRenderableWidget(Button.builder(Component.translatable("gui.abyssalcraft.necronomicon.back"), b -> back())
                    .bounds(width / 2 - 100, height - 28, 95, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(width / 2 + 5, height - 28, 95, 20).build());
    }

    private void navigateTo(NecronomiconEntry entry) {
        if (entry.id().equals("spellbook")) {
            ACNetwork.sendToServer(new OpenSpellbookMessage());
            onClose();
            return;
        }
        path.push(entry);
        page = 0;
        rebuildWidgets();
    }

    private void back() {
        if (path.size() > 1) {
            path.pop();
            page = 0;
        }
        rebuildWidgets();
    }

    private void changePage(int amount) {
        page = Math.max(0, page + amount);
        rebuildWidgets();
    }

    void refreshKnowledge() {
        rebuildWidgets();
    }

    /**
     * Read-only display gate: visible if no research is required, the player knows all, or the player has
     * completed the research (synced necrodata; PS-8 {@code KnowledgeGate} populates it server-side).
     */
    private boolean isVisible(NecronomiconEntry entry, NecroData data) {
        if (entry.advancementId() != null) {
            return data != null && (data.hasUnlockedAllKnowledge()
                || data.getAdvancementTriggers().contains(entry.advancementId().toString()));
        }
        if (entry.researchId() == null) {
            return true;
        }
        if (entry.isShownWhenLocked()) {
            return true;
        }
        if (data == null) return false;
        return data.hasUnlockedAllKnowledge() || data.getCompletedResearches().contains(entry.researchId().toString());
    }

    private Component entryLabel(NecronomiconEntry entry, NecroData data) {
        if (entry.researchId() == null && entry.advancementId() == null) {
            return Component.translatable(entry.titleKey());
        }
        boolean completed = data != null && (data.hasUnlockedAllKnowledge()
            || entry.researchId() != null && data.getCompletedResearches().contains(entry.researchId().toString())
            || entry.advancementId() != null && data.getAdvancementTriggers().contains(entry.advancementId().toString()));
        String state = completed ? "gui.abyssalcraft.necronomicon.research.completed"
            : "gui.abyssalcraft.necronomicon.research.locked";
        return Component.translatable("gui.abyssalcraft.necronomicon.research.row",
            Component.translatable(entry.titleKey()), Component.translatable(state));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1.20.1 Screen.render draws no background (subclass must); 1.21 Screen.render draws it itself. Draw
        // it via the compat first (correct on 1.20.1; harmlessly redrawn by super on 1.21), then the widgets,
        // then the custom text last so it sits on top on both versions.
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        NecronomiconEntry cur = current();
        graphics.drawCenteredString(font, Component.translatable(cur.titleKey()), width / 2, 18, 0xE0C0FF);
        if (cur.hasText()) {
            List<FormattedCharSequence> lines = font.split(cur.text(), 200);
            int ty = height - 30 - lines.size() * (font.lineHeight + 1);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, width / 2 - 100, ty, 0xB0A0C0);
                ty += font.lineHeight + 1;
            }
        }
    }

    /** Open the Necronomicon to {@code root} for the client player (called by the book item, deferred content). */
    public static void open(NecronomiconEntry root, int bookType) {
        Minecraft.getInstance().setScreen(new NecronomiconScreen(root, bookType));
    }
}

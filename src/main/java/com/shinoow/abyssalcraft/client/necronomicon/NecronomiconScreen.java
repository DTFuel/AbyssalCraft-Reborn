package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.shinoow.abyssalcraft.client.font.AkloFont;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.OpenSpellbookMessage;
import com.shinoow.abyssalcraft.net.server.NecronomiconPageActionMessage;
import com.shinoow.abyssalcraft.platform.RecipeDisplayCompat;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconRecipePages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
    private int recipePage;

    public NecronomiconScreen(NecronomiconEntry root, int bookType) {
        super(root.title());
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
        recipeContent(cur).filter(content -> content.recipes().size() > 1).ifPresent(content -> {
            recipePage = Math.min(recipePage, content.recipes().size() - 1);
            addRenderableWidget(Button.builder(Component.literal("<"), b -> changeRecipe(-1))
                .bounds(width / 2 - 76, height - 76, 24, 20).build());
            addRenderableWidget(Button.builder(Component.translatable(
                "gui.abyssalcraft.necronomicon.recipe.page", recipePage + 1, content.recipes().size()), b -> { })
                .bounds(width / 2 - 48, height - 76, 96, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> changeRecipe(1))
                .bounds(width / 2 + 52, height - 76, 24, 20).build());
        });
    }

    private void navigateTo(NecronomiconEntry entry) {
        if (entry.requiredBookType() > bookType) {
            rebuildWidgets();
            return;
        }
        if (entry.id().equals("spellbook")) {
            ACNetwork.sendToServer(new OpenSpellbookMessage());
            onClose();
            return;
        }
        if (entry.pageActionId() != null && entry.children().isEmpty()) {
            ACNetwork.sendToServer(new NecronomiconPageActionMessage(entry.pageActionId()));
        }
        path.push(entry);
        page = 0;
        recipePage = 0;
        rebuildWidgets();
    }

    private void back() {
        if (path.size() > 1) {
            path.pop();
            page = 0;
            recipePage = 0;
        }
        rebuildWidgets();
    }

    private void changePage(int amount) {
        page = Math.max(0, page + amount);
        rebuildWidgets();
    }

    private void changeRecipe(int amount) {
        recipeContent(current()).ifPresent(content -> {
            recipePage = Math.floorMod(recipePage + amount, content.recipes().size());
            rebuildWidgets();
        });
    }

    void refreshKnowledge() {
        rebuildWidgets();
    }

    /**
     * Read-only display gate: visible if no research is required, the player knows all, or the player has
     * completed the research (synced necrodata; PS-8 {@code KnowledgeGate} populates it server-side).
     */
    private boolean isVisible(NecronomiconEntry entry, NecroData data) {
        if (entry.requiredBookType() > bookType) {
            return false;
        }
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
            return entry.title();
        }
        boolean completed = data != null && (data.hasUnlockedAllKnowledge()
            || entry.researchId() != null && data.getCompletedResearches().contains(entry.researchId().toString())
            || entry.advancementId() != null && data.getAdvancementTriggers().contains(entry.advancementId().toString()));
        String state = completed ? "gui.abyssalcraft.necronomicon.research.completed"
            : "gui.abyssalcraft.necronomicon.research.locked";
        return Component.translatable("gui.abyssalcraft.necronomicon.research.row",
            entry.title(), Component.translatable(state));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1.20.1 Screen.render draws no background (subclass must); 1.21 Screen.render draws it itself. Draw
        // it via the compat first (correct on 1.20.1; harmlessly redrawn by super on 1.21), then the widgets,
        // then the custom text last so it sits on top on both versions.
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        NecronomiconEntry cur = current();
        graphics.drawCenteredString(font, cur.title(), width / 2, 18, 0xE0C0FF);
        int textTop = renderImage(graphics, cur);
        textTop = renderRecipe(graphics, cur, textTop);
        if (cur.hasText()) {
            Component body = cur.usesAkloFont()
                ? cur.text().copy().withStyle(Style.EMPTY.withFont(AkloFont.location()))
                : cur.text();
            List<FormattedCharSequence> lines = font.split(body, 200);
            int ty = textTop;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, width / 2 - 100, ty, 0xB0A0C0);
                ty += font.lineHeight + 1;
            }
        }
        if (cur.children().isEmpty() && !cur.icon().isEmpty()) {
            graphics.renderItem(cur.icon(), width / 2 - 8, height / 2 - 8);
            graphics.renderItemDecorations(font, cur.icon(), width / 2 - 8, height / 2 - 8);
        }
        renderContentStatus(graphics, cur);
    }

    private int renderRecipe(GuiGraphics graphics, NecronomiconEntry entry, int top) {
        var content = recipeContent(entry);
        if (content.isEmpty()) {
            if (isActiveRecipe(entry)) {
                graphics.drawCenteredString(font,
                    Component.translatable("gui.abyssalcraft.necronomicon.recipe.unavailable",
                        entry.content().reference()), width / 2, top, 0xD09070);
                return top + font.lineHeight + 8;
            }
            return top;
        }
        NecronomiconRecipePages.RecipePage pageContent = content.get();
        int index = Math.min(recipePage, pageContent.recipes().size() - 1);
        RecipeDisplayCompat.DisplayRecipe recipe = pageContent.recipes().get(index);
        int left = width / 2 - 78;
        for (int slot = 0; slot < recipe.ingredients().size(); slot++) {
            ItemStack[] options = recipe.ingredients().get(slot).getItems();
            if (options.length == 0) continue;
            ItemStack stack = options[(int) ((System.currentTimeMillis() / 1000L + slot) % options.length)];
            int x = left + slot % recipe.width() * 20;
            int y = top + slot / recipe.width() * 20;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x80605070);
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        }
        graphics.drawString(font, Component.literal("->"), left + 68, top + 21, 0xD8C8E0);
        int outputX = left + 94;
        int outputY = top + 18;
        graphics.fill(outputX - 1, outputY - 1, outputX + 17, outputY + 17, 0x80806040);
        graphics.renderItem(recipe.output(), outputX, outputY);
        graphics.renderItemDecorations(font, recipe.output(), outputX, outputY);
        graphics.drawCenteredString(font, Component.translatable("gui.abyssalcraft.necronomicon.recipe.id",
            pageContent.ids().get(index)), width / 2, top + 66, 0x80C080);
        return top + 80;
    }

    private java.util.Optional<NecronomiconRecipePages.RecipePage> recipeContent(NecronomiconEntry entry) {
        if (!isActiveRecipe(entry)) return java.util.Optional.empty();
        var level = Minecraft.getInstance().level;
        if (level == null) return java.util.Optional.empty();
        String id = entry.id();
        String legacyId = id.substring(id.lastIndexOf('/') + 1).toUpperCase(java.util.Locale.ROOT);
        return NecronomiconRecipePages.resolve(level, legacyId);
    }

    private boolean isActiveRecipe(NecronomiconEntry entry) {
        NecronomiconPageManifest.ContentRef content = entry.content();
        return content != null && content.kind().equals("recipe")
            && content.status() == NecronomiconPageManifest.OwnerStatus.ACTIVE;
    }

    private int renderImage(GuiGraphics graphics, NecronomiconEntry entry) {
        NecronomiconPageManifest.ImageContent image = entry.image();
        if (image == null) return 40;
        int availableWidth = Math.max(1, Math.min(200, width - 24));
        int availableHeight = Math.max(1, Math.min(112, height / 3));
        float scale = Math.min((float) availableWidth / image.width(),
            (float) availableHeight / image.height());
        int drawWidth = Math.max(1, Math.round(image.width() * scale));
        int drawHeight = Math.max(1, Math.round(image.height() * scale));
        int x = (width - drawWidth) / 2;
        int y = 34;
        //? if >=1.21 {
        /*graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, image.texture(), x, y,
            drawWidth, drawHeight, image.u(), image.v(), image.width(), image.height(),
            image.textureWidth(), image.textureHeight());
        *///?} else {
        graphics.blit(image.texture(), x, y, drawWidth, drawHeight, image.u(), image.v(),
            image.width(), image.height(), image.textureWidth(), image.textureHeight());
        //?}
        return y + drawHeight + 8;
    }

    private void renderContentStatus(GuiGraphics graphics, NecronomiconEntry entry) {
        NecronomiconPageManifest.ContentRef content = entry.content();
        if (content == null || content.status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) return;
        String key = "gui.abyssalcraft.necronomicon.content."
            + content.status().name().toLowerCase(java.util.Locale.ROOT);
        Component status = Component.translatable(key, content.kind(), content.owner(), content.reference(),
            content.reason());
        List<FormattedCharSequence> lines = font.split(status, 200);
        int y = height - 30 - lines.size() * (font.lineHeight + 1);
        int color = content.status() == NecronomiconPageManifest.OwnerStatus.ACTIVE ? 0x80C080 : 0xD09070;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, width / 2 - 100, y, color);
            y += font.lineHeight + 1;
        }
    }

    /** Open the Necronomicon to {@code root} for the client player (called by the book item, deferred content). */
    public static void open(NecronomiconEntry root, int bookType) {
        Minecraft.getInstance().setScreen(new NecronomiconScreen(root, bookType));
    }
}

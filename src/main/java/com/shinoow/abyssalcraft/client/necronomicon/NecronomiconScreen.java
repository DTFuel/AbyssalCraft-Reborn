package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.shinoow.abyssalcraft.client.font.AkloFont;
import com.shinoow.abyssalcraft.platform.ACRef;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The Necronomicon book screen (owned by PH-5), a modernised {@code GuiNecronomicon}: a plain {@link Screen}
 * (opened on demand, not menu-backed) that navigates a {@link NecronomiconEntry} tree &mdash; category
 * buttons drill into children, a back button pops, and gated entries are hidden until the player has the
 * required research (read-only against the synced necrodata, PS-2/PS-8).
 *
 * <p>Uses the five faithful 1.12.2 book backgrounds while keeping the modern synchronized content tree,
 * live recipes and research gates. The background override keeps the old world-visible book presentation
 * instead of applying the default 1.21 screen blur.
 */
public final class NecronomiconScreen extends Screen {

    private static final int BOOK_WIDTH = 255;
    private static final int BOOK_HEIGHT = 192;
    private static final int ENTRIES_PER_COLUMN = 7;
    private static final ResourceLocation[] BOOK_TEXTURES = {
        ACRef.id("textures/gui/necronomicon.png"),
        ACRef.id("textures/gui/necronomicon_cor.png"),
        ACRef.id("textures/gui/necronomicon_dre.png"),
        ACRef.id("textures/gui/necronomicon_omt.png"),
        ACRef.id("textures/gui/abyssalnomicon.png"),
    };

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
        int entriesPerPage = ENTRIES_PER_COLUMN * 2;
        int pages = Math.max(1, (visible.size() + entriesPerPage - 1) / entriesPerPage);
        page = Math.min(page, pages - 1);
        int from = page * entriesPerPage;
        int to = Math.min(visible.size(), from + entriesPerPage);

        int index = 0;
        for (NecronomiconEntry child : visible.subList(from, to)) {
            NecronomiconEntry target = child;
            Component label = entryLabel(child, data);
            int column = index / ENTRIES_PER_COLUMN;
            int row = index % ENTRIES_PER_COLUMN;
            addRenderableWidget(Button.builder(label, b -> navigateTo(target))
                    .bounds(bookLeft() + 14 + column * 118, bookTop() + 27 + row * 20, 109, 18).build());
            index++;
        }

        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(width / 2 - 76, bookTop() + 168, 24, 18).build());
            addRenderableWidget(Button.builder(Component.translatable(
                "gui.abyssalcraft.necronomicon.page", page + 1, pages), b -> { })
                .bounds(width / 2 - 48, bookTop() + 168, 96, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(width / 2 + 52, bookTop() + 168, 24, 18).build());
        }

        if (path.size() > 1) {
            addRenderableWidget(Button.builder(Component.translatable("gui.abyssalcraft.necronomicon.back"), b -> back())
                    .bounds(width / 2 - 100, bookTop() + 196, 95, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(width / 2 + 5, bookTop() + 196, 95, 20).build());
        recipeContent(cur).filter(content -> content.recipes().size() > 1).ifPresent(content -> {
            recipePage = Math.min(recipePage, content.recipes().size() - 1);
            addRenderableWidget(Button.builder(Component.literal("<"), b -> changeRecipe(-1))
                .bounds(width / 2 - 76, bookTop() + 168, 24, 18).build());
            addRenderableWidget(Button.builder(Component.translatable(
                "gui.abyssalcraft.necronomicon.recipe.page", recipePage + 1, content.recipes().size()), b -> { })
                .bounds(width / 2 - 48, bookTop() + 168, 96, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> changeRecipe(1))
                .bounds(width / 2 + 52, bookTop() + 168, 24, 18).build());
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
        Component navigationTitle = navigationTitle(entry);
        if (entry.researchId() == null && entry.advancementId() == null) {
            return navigationTitle;
        }
        boolean completed = data != null && (data.hasUnlockedAllKnowledge()
            || entry.researchId() != null && data.getCompletedResearches().contains(entry.researchId().toString())
            || entry.advancementId() != null && data.getAdvancementTriggers().contains(entry.advancementId().toString()));
        String state = completed ? "gui.abyssalcraft.necronomicon.research.completed"
            : "gui.abyssalcraft.necronomicon.research.locked";
        return Component.translatable("gui.abyssalcraft.necronomicon.research.row",
            navigationTitle, Component.translatable(state));
    }

    private Component navigationTitle(NecronomiconEntry entry) {
        Component title = entry.navigationTitle();
        if (!entry.hasNavigationTitle()) {
            return title;
        }
        return Component.literal(font.plainSubstrByWidth(title.getString(), 101));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(BOOK_TEXTURES[Math.max(0, Math.min(BOOK_TEXTURES.length - 1, bookType))],
            bookLeft(), bookTop(), 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        super.render(graphics, mouseX, mouseY, partialTick);

        NecronomiconEntry cur = current();
        int textTop = renderImage(graphics, cur);
    graphics.drawCenteredString(font, cur.title(), width / 2, bookTop() + 12, 0x403020);
        textTop = renderRecipe(graphics, cur, textTop);
        if (cur.hasText()) {
            Component body = cur.usesAkloFont()
                ? cur.text().copy().withStyle(Style.EMPTY.withFont(AkloFont.location()))
                : cur.text();
            List<FormattedCharSequence> lines = font.split(body, 104);
            int ty = textTop;
            int column = 0;
            for (FormattedCharSequence line : lines) {
                if (ty + font.lineHeight > bookTop() + 166 && column == 0) {
                    column = 1;
                    ty = bookTop() + 30;
                }
                if (column > 1 || ty + font.lineHeight > bookTop() + 166) break;
                graphics.drawString(font, line, bookLeft() + 15 + column * 119, ty, 0x404040, false);
                ty += font.lineHeight + 1;
            }
        }
        if (cur.children().isEmpty() && !cur.icon().isEmpty()) {
            graphics.renderItem(cur.icon(), bookLeft() + 179, bookTop() + 88);
            graphics.renderItemDecorations(font, cur.icon(), bookLeft() + 179, bookTop() + 88);
        }
        renderContentStatus(graphics, cur);
    }

    //? if >=1.21 {
    /*@Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }
    *///?} else {
    @Override
    public void renderBackground(GuiGraphics graphics) {
    }
    //?}

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
        int left = bookLeft() + 16;
        for (int slot = 0; slot < recipe.ingredients().size(); slot++) {
            ItemStack[] options = recipe.ingredients().get(slot).getItems();
            if (options.length == 0) continue;
            ItemStack stack = options[(int) ((System.currentTimeMillis() / 1000L + slot) % options.length)];
            int x = left + slot % recipe.width() * 20;
            int y = top + slot / recipe.width() * 20;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x80605040);
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        }
        graphics.drawString(font, Component.literal("->"), left + 68, top + 21, 0x404040);
        int outputX = left + 94;
        int outputY = top + 18;
        graphics.fill(outputX - 1, outputY - 1, outputX + 17, outputY + 17, 0x80806040);
        graphics.renderItem(recipe.output(), outputX, outputY);
        graphics.renderItemDecorations(font, recipe.output(), outputX, outputY);
        graphics.drawCenteredString(font, Component.translatable("gui.abyssalcraft.necronomicon.recipe.id",
            pageContent.ids().get(index)), bookLeft() + 64, top + 66, 0x406040);
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
        if (image == null) return bookTop() + 30;
        graphics.blit(image.texture(), bookLeft(), bookTop(), BOOK_WIDTH, BOOK_HEIGHT, image.u(), image.v(),
            image.width(), image.height(), image.textureWidth(), image.textureHeight());
        return bookTop() + 100;
    }

    private void renderContentStatus(GuiGraphics graphics, NecronomiconEntry entry) {
        NecronomiconPageManifest.ContentRef content = entry.content();
        if (content == null || content.status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) return;
        String key = "gui.abyssalcraft.necronomicon.content."
            + content.status().name().toLowerCase(java.util.Locale.ROOT);
        Component status = Component.translatable(key, content.kind(), content.owner(), content.reference(),
            content.reason());
        List<FormattedCharSequence> lines = font.split(status, 200);
        int y = bookTop() + 166 - lines.size() * (font.lineHeight + 1);
        int color = content.status() == NecronomiconPageManifest.OwnerStatus.ACTIVE ? 0x80C080 : 0xD09070;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, bookLeft() + 15, y, color);
            y += font.lineHeight + 1;
        }
    }

    private int bookLeft() {
        return (width - BOOK_WIDTH) / 2;
    }

    private int bookTop() {
        return Math.max(2, (height - BOOK_HEIGHT - 24) / 2);
    }

    /** Open the Necronomicon to {@code root} for the client player (called by the book item, deferred content). */
    public static void open(NecronomiconEntry root, int bookType) {
        Minecraft.getInstance().setScreen(new NecronomiconScreen(root, bookType));
    }
}

package com.shinoow.abyssalcraft.client.necronomicon;

import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.advancement.AdvancementKnowledge;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.KnowledgePredicate;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Necronomicon content root + open entry point (owned by PH-5). Holds the always-visible information
 * chapters (introduction + lore + the four abyssal dimensions), matching the 1.12.2 {@code necronomicon.
 * information.*} tree. The research-gated chapters (recipe / ritual / spell / places-of-power entries) are
 * added as {@link NecronomiconEntry} children once their research items (PS-8) and the necrodata sync
 * (PS-1) land; the entry bodies here are faithful summaries of the 1.12.2 information pages.
 */
public final class ACNecronomicon {

    private ACNecronomicon() {}

    private static final String K = "gui.abyssalcraft.necronomicon.";

    /** The Necronomicon content tree (always-visible information chapters). */
    public static NecronomiconEntry root(int bookType, net.minecraft.core.HolderLookup.Provider registries) {
        NecronomiconEntry root = new NecronomiconEntry("root", K + "title", null, new ItemStack(Items.BOOK));

        // Introduction (always visible).
        root.addChild(new NecronomiconEntry("intro", K + "intro.title", K + "intro.text", new ItemStack(Items.WRITABLE_BOOK)));

        // Information: lore about the mod and the Great Old Ones.
        NecronomiconEntry info = new NecronomiconEntry("information", K + "information.title");
        info.addChild(new NecronomiconEntry("ac", K + "ac.title", K + "ac.text", new ItemStack(Items.BOOK)));
        info.addChild(new NecronomiconEntry("goo", K + "goo.title", K + "goo.text", new ItemStack(Items.ENDER_EYE)));
        info.addChild(new NecronomiconEntry("knowledge", K + "knowledge.title", K + "knowledge.text", new ItemStack(Items.EXPERIENCE_BOTTLE)));
        root.addChild(info);

        // Dimensions: the four ported abyssal dimensions.
        NecronomiconEntry dimensions = new NecronomiconEntry("dimensions", K + "dimensions.title");
        dimensions.addChild(new NecronomiconEntry("abyssal_wasteland", K + "abyssal_wasteland.title", K + "abyssal_wasteland.text", ItemStack.EMPTY));
        dimensions.addChild(new NecronomiconEntry("dreadlands", K + "dreadlands.title", K + "dreadlands.text", ItemStack.EMPTY));
        dimensions.addChild(new NecronomiconEntry("omothol", K + "omothol.title", K + "omothol.text", ItemStack.EMPTY));
        dimensions.addChild(new NecronomiconEntry("dark_realm", K + "dark_realm.title", K + "dark_realm.text", ItemStack.EMPTY));
        root.addChild(dimensions);

        root.addChild(new NecronomiconEntry("spellbook", "container.abyssalcraft.spellbook",
            null, new ItemStack(Items.PAPER)));

        NecronomiconEntry research = new NecronomiconEntry("research", K + "research.title");
        NecronomiconEntry biome = category("biome");
        NecronomiconEntry dimension = category("dimension");
        NecronomiconEntry entity = category("entity");
        NecronomiconEntry misc = category("misc");
        NecronomiconEntry book = category("book");
        for (IResearchItem item : KnowledgeContent.researches()) {
            IUnlockCondition condition = item.getUnlockConditions()[0];
            categoryFor(condition, biome, dimension, entity, misc, book).addChild(
                new NecronomiconEntry("research_" + item.getID().getPath(), item.getName(),
                K + "research.entry", ItemStack.EMPTY)
                .setTextArgs(conditionLabel(condition), conditionTarget(condition), item.getID().toString())
                .setResearch(item.getID())
                .showWhenLocked());
        }
        research.addChild(biome).addChild(dimension).addChild(entity).addChild(misc).addChild(book);

        NecronomiconEntry progression = category("progression");
        for (AdvancementKnowledge.Entry entry : AdvancementKnowledge.ENTRIES) {
            progression.addChild(new NecronomiconEntry("advancement_" + entry.id().getPath(),
                entry.titleKey(), entry.descriptionKey(),
                new ItemStack(BuiltInRegistries.ITEM.get(entry.icon())))
                .setAdvancement(entry.id()));
        }
        research.addChild(progression);
        root.addChild(research);

        NecronomiconPageManifest.bootstrap(registries);
        NecronomiconEntry catalog = new NecronomiconEntry("catalog", K + "catalog.title");
        for (NecronomiconPageManifest.PageType type : NecronomiconPageManifest.PageType.values()) {
            NecronomiconEntry category = new NecronomiconEntry("catalog_" + type.name().toLowerCase(java.util.Locale.ROOT),
                K + "catalog." + type.name().toLowerCase(java.util.Locale.ROOT));
            for (NecronomiconPageManifest.PageEntry page : NecronomiconPageManifest.pagesByType(type)) {
                if (!NecronomiconPageManifest.isAvailableForBook(page, bookType)) continue;
                NecronomiconEntry entry = new NecronomiconEntry(
                    page.id().getPath(), page.titleKey(), page.textKey(), page.icon()).setContent(page.content())
                    .setImage(page.image())
                    .setPageAction(page.id())
                    .setRequiredBookType(page.requiredBookType());
                if (page.researchId() != null) entry.setResearch(page.researchId());
                category.addChild(entry);
            }
            if (!category.children().isEmpty()) catalog.addChild(category);
        }
        root.addChild(catalog);

        return root;
    }

    private static NecronomiconEntry category(String id) {
        return new NecronomiconEntry("research_" + id, K + "research.category." + id);
    }

    private static NecronomiconEntry categoryFor(IUnlockCondition condition, NecronomiconEntry biome,
            NecronomiconEntry dimension, NecronomiconEntry entity, NecronomiconEntry misc,
            NecronomiconEntry book) {
        return switch (condition.getType()) {
            case 0, 3, 5 -> biome;
            case 2 -> dimension;
            case 1, 4, 6, 11 -> entity;
            case 12 -> book;
            default -> misc;
        };
    }

    private static Component conditionLabel(IUnlockCondition condition) {
        String label = switch (condition.getType()) {
            case 0 -> "visit_biome";
            case 3, 5 -> "visit_any_biome";
            case 1 -> "defeat_entity";
            case 4, 6 -> "defeat_any_entity";
            case 11 -> "defeat_all_entities";
            case 2 -> "visit_dimension";
            case 12 -> "open_book";
            default -> "experience";
        };
        return Component.translatable(K + "research.condition." + label);
    }

    private static Component conditionTarget(IUnlockCondition condition) {
        Object target = condition.getConditionObject();
        if (target instanceof String[] values) {
            return Component.literal(String.join(", ", values));
        }
        if (target instanceof KnowledgePredicate predicate) {
            return Component.literal(predicate.ids().stream().sorted().collect(java.util.stream.Collectors.joining(", ")));
        }
        if (condition.getType() == 12 && target instanceof Integer tier) {
            return Component.translatable(K + "research.book_tier", tier);
        }
        return Component.literal(String.valueOf(target));
    }

    /** Open the Necronomicon for the client player (invoked by the book item on right-click). */
    public static void open(int bookType) {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null) NecronomiconScreen.open(root(bookType, level.registryAccess()), bookType);
    }
}

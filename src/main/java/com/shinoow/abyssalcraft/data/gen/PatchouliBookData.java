package com.shinoow.abyssalcraft.data.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.platform.ResearchAdvancementCompat;
import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest.PageEntry;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest.PageType;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconRecipePages;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Generates the five cumulative Patchouli Necronomicon editions and their research advancements. */
public final class PatchouliBookData implements DataProvider {

    private static final String COMPONENT_CLASS =
        "com.shinoow.abyssalcraft.client.necronomicon.PatchouliActionComponent";
    private static final String MANIFEST_COMPONENT_CLASS =
        "com.shinoow.abyssalcraft.client.necronomicon.PatchouliManifestComponent";
    private static final String[] BOOKS = {
        "necronomicon", "abyssal_wasteland_necronomicon", "dreadlands_necronomicon",
        "omothol_necronomicon", "abyssalnomicon"
    };
    private static final String[] BOOK_TEXTURES = {
        "patchouli:textures/gui/book_brown.png", "patchouli:textures/gui/book_green.png",
        "patchouli:textures/gui/book_red.png", "patchouli:textures/gui/book_purple.png",
        "patchouli:textures/gui/book_gray.png"
    };

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public PatchouliBookData(DataGenCompat.Gen gen) {
        packOutput = gen.packOutput;
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookup.thenCompose(registries -> generate(output, registries));
    }

    @Override
    public String getName() {
        return "AbyssalCraft Patchouli Necronomicons";
    }

    private CompletableFuture<?> generate(CachedOutput output, HolderLookup.Provider registries) {
        KnowledgeContent.bootstrap();
        NecronomiconPageManifest.bootstrap(registries);
        Path dataRoot = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(AbyssalCraft.MODID);
        Path assetRoot = packOutput.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(AbyssalCraft.MODID);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<PageEntry> pages = NecronomiconPageManifest.pages();

        int emittedEntries = 0;
        for (int tier = 0; tier < BOOKS.length; tier++) {
            String book = BOOKS[tier];
            Map<String, String> titleOwners = new HashMap<>();
            Path dataBook = dataRoot.resolve("patchouli_books").resolve(book);
            Path assetBook = assetRoot.resolve("patchouli_books").resolve(book).resolve("en_us");
            futures.add(DataProvider.saveStable(output, bookJson(tier), dataBook.resolve("book.json")));
            futures.add(DataProvider.saveStable(output, actionTemplate(), assetBook.resolve("templates/action.json")));
            futures.add(DataProvider.saveStable(output, manifestTemplate(),
                assetBook.resolve("templates/manifest.json")));
            for (PageType type : PageType.values()) {
                String category = type.name().toLowerCase(Locale.ROOT);
                futures.add(DataProvider.saveStable(output, categoryJson(type, tier),
                    assetBook.resolve("categories").resolve(category + ".json")));
            }
            futures.add(DataProvider.saveStable(output, researchBookCategory(tier),
                assetBook.resolve("categories/research.json")));
            for (String category : List.of("biome", "dimension", "entity", "misc", "book")) {
                futures.add(DataProvider.saveStable(output, researchBookSubcategory(category),
                    assetBook.resolve("categories/research/" + category + ".json")));
            }
            futures.add(DataProvider.saveStable(output, spellbookEntry(tier),
                assetBook.resolve("entries/information/spellbook.json")));
            requireUniqueTitle(titleOwners, "container.abyssalcraft.spellbook", "spellbook", book);
            emittedEntries++;
            for (IResearchItem research : KnowledgeContent.researches()) {
                requireUniqueTitle(titleOwners, research.getName(),
                    "research/" + research.getID().getPath(), book);
                futures.add(DataProvider.saveStable(output, researchBookEntry(research),
                    assetBook.resolve("entries/research/" + research.getID().getPath() + ".json")));
                emittedEntries++;
            }

            for (PageEntry page : pages) {
                if (!NecronomiconPageManifest.isAvailableForBook(page, tier)) continue;
                String category = page.type().name().toLowerCase(Locale.ROOT);
                Path path = assetBook.resolve("entries").resolve(category)
                    .resolve(page.id().getPath() + ".json");
                JsonObject entry = entryJson(page, tier);
                validateEntry(page, entry);
                requireUniqueTitle(titleOwners, entry.get("name").getAsString(),
                    page.id().toString(), book);
                futures.add(DataProvider.saveStable(output, entry, path));
                emittedEntries++;
            }
        }

        futures.add(DataProvider.saveStable(output, researchRoot(false),
            dataRoot.resolve("advancements/research/root.json")));
        futures.add(DataProvider.saveStable(output, researchRoot(true),
            dataRoot.resolve("advancement/research/root.json")));
        futures.add(DataProvider.saveStable(output, permanentlyLockedAdvancement(false),
            dataRoot.resolve("advancements/research/always_locked.json")));
        futures.add(DataProvider.saveStable(output, permanentlyLockedAdvancement(true),
            dataRoot.resolve("advancement/research/always_locked.json")));
        for (String category : List.of("biome", "dimension", "entity", "misc", "book")) {
            futures.add(DataProvider.saveStable(output, researchCategory(category, false),
                dataRoot.resolve("advancements/research/category/" + category + ".json")));
            futures.add(DataProvider.saveStable(output, researchCategory(category, true),
                dataRoot.resolve("advancement/research/category/" + category + ".json")));
        }
        for (IResearchItem research : KnowledgeContent.researches()) {
            futures.add(DataProvider.saveStable(output, researchAdvancement(research, false),
                dataRoot.resolve("advancements/research/" + research.getID().getPath() + ".json")));
            futures.add(DataProvider.saveStable(output, researchAdvancement(research, true),
                dataRoot.resolve("advancement/research/" + research.getID().getPath() + ".json")));
        }

        System.out.println("RR_PATCHOULI_BOOKS_OK books=5 manifest=" + pages.size()
            + " entries=" + emittedEntries + " researchAdvancements=" + KnowledgeContent.researches().size()
            + " permanentLocks=1 actionComponents=2");
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static void requireUniqueTitle(Map<String, String> owners, String titleKey,
                                           String owner, String book) {
        String previous = owners.putIfAbsent(titleKey, owner);
        if (previous != null) {
            throw new IllegalStateException("Duplicate Patchouli title key in " + book + ": "
                + titleKey + " used by " + previous + " and " + owner);
        }
    }

    private static void validateEntry(PageEntry page, JsonObject entry) {
        JsonObject actionPage = entry.getAsJsonArray("pages").get(0).getAsJsonObject();
        if (page.legacyFields() == null && isManifestPage(page)) {
            if (!"abyssalcraft:manifest".equals(actionPage.get("type").getAsString())
                || !page.id().toString().equals(actionPage.get("page").getAsString())) {
                throw new IllegalStateException("Patchouli manifest page contract changed for " + page.id());
            }
        } else {
            boolean actionable = page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE;
            if (!"abyssalcraft:action".equals(actionPage.get("type").getAsString())
                || actionable != "study".equals(actionPage.get("action").getAsString())
                || actionable != page.id().toString().equals(actionPage.get("page").getAsString())) {
                throw new IllegalStateException("Patchouli page action contract changed for " + page.id());
            }
        }
        String advancement = entry.has("advancement") ? entry.get("advancement").getAsString() : null;
        String expected = page.researchId() == null
            ? null : "abyssalcraft:research/" + page.researchId().getPath();
        if (!java.util.Objects.equals(advancement, expected)) {
            throw new IllegalStateException("Patchouli research gate changed for " + page.id());
        }
        validateRecipeMappings(page, entry);
    }

    private static void validateRecipeMappings(PageEntry page, JsonObject entry) {
        if (page.legacyFields() == null || !page.content().kind().equals("recipe")
            || page.content().status() != NecronomiconPageManifest.OwnerStatus.ACTIVE) {
            if (entry.has("extra_recipe_mappings")) {
                throw new IllegalStateException("Unexpected Patchouli recipe mapping for " + page.id());
            }
            return;
        }
        NecronomiconRecipePages.Definition definition =
            NecronomiconRecipePages.definition(page.legacyFields().legacyId());
        JsonObject mappings = entry.getAsJsonObject("extra_recipe_mappings");
        if (definition == null || mappings == null
            || !mappings.keySet().equals(definition.resultIds().stream()
                .map(ResourceLocation::toString).collect(java.util.stream.Collectors.toSet()))) {
            throw new IllegalStateException("Patchouli recipe result mapping changed for " + page.id());
        }
        JsonArray pages = entry.getAsJsonArray("pages");
        for (String result : mappings.keySet()) {
            int pageIndex = mappings.get(result).getAsInt();
            if (pageIndex < 0 || pageIndex >= pages.size()
                || !"patchouli:crafting".equals(pages.get(pageIndex).getAsJsonObject()
                    .get("type").getAsString())) {
                throw new IllegalStateException("Patchouli recipe page index changed for " + page.id());
            }
        }
    }

    private static JsonObject bookJson(int tier) {
        JsonObject json = new JsonObject();
        json.addProperty("name", "item.abyssalcraft." + BOOKS[tier]);
        json.addProperty("landing_text", "gui.abyssalcraft.necronomicon.intro.text");
        json.addProperty("version", 1);
        json.addProperty("use_resource_pack", true);
        json.addProperty("i18n", true);
        json.addProperty("dont_generate_book", true);
        json.addProperty("custom_book_item", "abyssalcraft:" + BOOKS[tier]);
        json.addProperty("book_texture", BOOK_TEXTURES[tier]);
        json.addProperty("show_progress", true);
        json.addProperty("show_toasts", true);
        json.addProperty("advancements_tab", "abyssalcraft:root");
        json.addProperty("text_overflow_mode", "resize");
        return json;
    }

    private static JsonObject categoryJson(PageType type, int tier) {
        String category = type.name().toLowerCase(Locale.ROOT);
        JsonObject json = new JsonObject();
        json.addProperty("name", "gui.abyssalcraft.necronomicon.catalog." + category);
        json.addProperty("description", "gui.abyssalcraft.necronomicon.catalog." + category);
        json.addProperty("icon", "abyssalcraft:" + BOOKS[tier]);
        json.addProperty("sortnum", type.ordinal());
        return json;
    }

    private static JsonObject researchBookCategory(int tier) {
        JsonObject json = new JsonObject();
        json.addProperty("name", "gui.abyssalcraft.necronomicon.research.title");
        json.addProperty("description", "gui.abyssalcraft.necronomicon.patchouli.research.description");
        json.addProperty("icon", "minecraft:experience_bottle");
        json.addProperty("sortnum", PageType.values().length);
        return json;
    }

    private static JsonObject researchBookSubcategory(String category) {
        JsonObject json = new JsonObject();
        json.addProperty("name", "gui.abyssalcraft.necronomicon.research.category." + category);
        json.addProperty("description", "gui.abyssalcraft.necronomicon.patchouli.research.description");
        json.addProperty("icon", categoryIcon(category));
        json.addProperty("parent", "abyssalcraft:research");
        json.addProperty("sortnum", new ArrayList<>(List.of("biome", "dimension", "entity", "misc", "book"))
            .indexOf(category));
        return json;
    }

    private static JsonObject researchBookEntry(IResearchItem research) {
        String category = ResearchAdvancementCompat.category(research);
        String advancement = ResearchAdvancementCompat.advancementId(research).toString();
        JsonObject entry = new JsonObject();
        entry.addProperty("name", research.getName());
        entry.addProperty("category", "abyssalcraft:research/" + category);
        entry.addProperty("icon", categoryIcon(category));
        entry.addProperty("read_by_default", true);
        JsonArray pages = new JsonArray();
        JsonObject quest = new JsonObject();
        quest.addProperty("type", "patchouli:quest");
        quest.addProperty("trigger", advancement);
        quest.addProperty("title", research.getName());
        quest.addProperty("text", "gui.abyssalcraft.necronomicon.patchouli.research.description");
        pages.add(quest);
        JsonObject details = new JsonObject();
        details.addProperty("type", "abyssalcraft:manifest");
        details.addProperty("kind", "research");
        details.addProperty("id", research.getID().getPath());
        details.addProperty("page", "");
        pages.add(details);
        entry.add("pages", pages);
        return entry;
    }

    private static JsonObject actionTemplate() {
        JsonObject json = new JsonObject();
        JsonArray components = new JsonArray();
        components.add(component("patchouli:header", -1, -1, "text", "#title"));
        JsonObject text = component("patchouli:text", 0, 20, "text", "#text");
        text.addProperty("max_width", 100);
        components.add(text);
        JsonObject action = component("patchouli:custom", 0, 140, "class", COMPONENT_CLASS);
        action.addProperty("action", "#action");
        action.addProperty("page", "#page");
        components.add(action);
        json.add("components", components);
        return json;
    }

    private static JsonObject manifestTemplate() {
        JsonObject json = new JsonObject();
        JsonArray components = new JsonArray();
        JsonObject manifest = component("patchouli:custom", 0, 8, "class", MANIFEST_COMPONENT_CLASS);
        manifest.addProperty("kind", "#kind");
        manifest.addProperty("id", "#id");
        manifest.addProperty("page", "#page");
        components.add(manifest);
        json.add("components", components);
        return json;
    }

    private static JsonObject component(String type, int x, int y, String key, String value) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty(key, value);
        json.addProperty("x", x);
        json.addProperty("y", y);
        return json;
    }

    private static JsonObject spellbookEntry(int tier) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", "container.abyssalcraft.spellbook");
        entry.addProperty("category", "abyssalcraft:information");
        entry.addProperty("icon", "minecraft:paper");
        entry.addProperty("sortnum", -100);
        JsonArray pages = new JsonArray();
        JsonObject page = actionPage("container.abyssalcraft.spellbook",
            "gui.abyssalcraft.necronomicon.patchouli.spellbook", "spellbook", "");
        pages.add(page);
        entry.add("pages", pages);
        return entry;
    }

    private static JsonObject entryJson(PageEntry page, int tier) {
        String titleKey = entryTitleKey(page);
        JsonObject entry = new JsonObject();
        entry.addProperty("name", titleKey);
        entry.addProperty("category", "abyssalcraft:" + page.type().name().toLowerCase(Locale.ROOT));
        entry.addProperty("icon", icon(page, tier));
        entry.addProperty("sortnum", page.legacyFields() == null
            ? 10000 + page.id().getPath().hashCode() & 0x7FFFFFFF
            : page.legacyFields().sourceOrder());
        if (page.researchId() != null) {
            entry.addProperty("advancement", "abyssalcraft:research/" + page.researchId().getPath());
            if (page.researchId().equals(NecronomiconPageManifest.PERMANENTLY_LOCKED)) {
                entry.addProperty("secret", true);
            }
        }
        JsonArray pages = new JsonArray();
        if (page.legacyFields() == null && isManifestPage(page)) {
            pages.add(manifestPage(page));
        } else {
            boolean actionable = page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE;
            pages.add(actionPage(titleKey, body(page), actionable ? "study" : "",
                actionable ? page.id().toString() : ""));
            addVisualPages(pages, page, titleKey);
        }
        entry.add("pages", pages);
        addRecipeMappings(entry, page);
        return entry;
    }

    private static String entryTitleKey(PageEntry page) {
        if (page.legacyFields() == null) return page.titleKey();
        return "gui.abyssalcraft.necronomicon.entry."
            + page.legacyFields().legacyId().toLowerCase(Locale.ROOT) + ".title";
    }

    private static void addRecipeMappings(JsonObject entry, PageEntry page) {
        if (page.legacyFields() == null || !page.content().kind().equals("recipe")
            || page.content().status() != NecronomiconPageManifest.OwnerStatus.ACTIVE) {
            return;
        }
        NecronomiconRecipePages.Definition definition =
            NecronomiconRecipePages.definition(page.legacyFields().legacyId());
        if (definition == null || !definition.active()) return;
        JsonObject mappings = new JsonObject();
        for (int index = 0; index < definition.resultIds().size(); index++) {
            int pageIndex = definition.resultIds().size() == 1 ? 1 : 1 + index / 2;
            mappings.addProperty(definition.resultIds().get(index).toString(), pageIndex);
        }
        entry.add("extra_recipe_mappings", mappings);
    }

    private static boolean isManifestPage(PageEntry page) {
        return page.content().kind().equals("ritual") || page.content().kind().equals("spell")
            || page.content().kind().equals("place_of_power");
    }

    private static JsonObject manifestPage(PageEntry page) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "abyssalcraft:manifest");
        json.addProperty("kind", page.content().kind());
        String path = page.id().getPath();
        json.addProperty("id", path.substring(path.lastIndexOf('/') + 1));
        json.addProperty("page", page.id().toString());
        return json;
    }

    private static JsonObject actionPage(String title, String text, String action, String pageId) {
        JsonObject page = new JsonObject();
        page.addProperty("type", "abyssalcraft:action");
        page.addProperty("title", title);
        page.addProperty("text", text);
        page.addProperty("action", action);
        page.addProperty("page", pageId);
        return page;
    }

    private static void addVisualPages(JsonArray pages, PageEntry page, String titleKey) {
        if (page.image() != null) {
            JsonObject image = new JsonObject();
            image.addProperty("type", "patchouli:image");
            JsonArray images = new JsonArray();
            images.add(page.image().texture().toString());
            image.add("images", images);
            image.addProperty("title", titleKey);
            pages.add(image);
            return;
        }
        if (page.content().kind().equals("item")
            && page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) {
            JsonObject spotlight = new JsonObject();
            spotlight.addProperty("type", "patchouli:spotlight");
            spotlight.addProperty("item", page.content().reference());
            spotlight.addProperty("title", titleKey);
            pages.add(spotlight);
            return;
        }
        if (page.content().kind().equals("recipe")
            && page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) {
            String[] recipes = page.content().reference().split(",");
            for (int index = 0; index < recipes.length; index += 2) {
                JsonObject recipe = new JsonObject();
                recipe.addProperty("type", "patchouli:crafting");
                recipe.addProperty("recipe", recipes[index]);
                if (index + 1 < recipes.length) recipe.addProperty("recipe2", recipes[index + 1]);
                recipe.addProperty("title", titleKey);
                pages.add(recipe);
            }
        }
    }

    private static String body(PageEntry page) {
        if (page.textKey() != null && !page.textKey().isBlank()) return page.textKey();
        if (page.content().status() != NecronomiconPageManifest.OwnerStatus.ACTIVE) {
            return "gui.abyssalcraft.necronomicon.patchouli.unavailable";
        }
        return page.content().reference();
    }

    private static String icon(PageEntry page, int tier) {
        ItemStack icon = page.icon();
        if (!icon.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(icon.getItem());
            if (id != null) return id.toString();
        }
        return "abyssalcraft:" + BOOKS[tier];
    }

    private static JsonObject researchRoot(boolean modern) {
        JsonObject json = advancement("abyssalcraft:root", "abyssalcraft:necronomicon",
            "gui.abyssalcraft.necronomicon.research.title",
            "gui.abyssalcraft.necronomicon.patchouli.research.description", "task", modern);
        return json;
    }

    private static JsonObject researchCategory(String category, boolean modern) {
        return advancement("abyssalcraft:research/root", categoryIcon(category),
            "gui.abyssalcraft.necronomicon.research.category." + category,
            "gui.abyssalcraft.necronomicon.patchouli.research.description", "task", modern);
    }

    private static JsonObject permanentlyLockedAdvancement(boolean modern) {
        JsonObject json = advancement("abyssalcraft:research/root", "minecraft:barrier",
            "gui.abyssalcraft.necronomicon.patchouli.locked.title",
            "gui.abyssalcraft.necronomicon.patchouli.locked.description", "task", modern);
        JsonObject display = json.getAsJsonObject("display");
        display.addProperty("show_toast", false);
        display.addProperty("announce_to_chat", false);
        display.addProperty("hidden", true);
        return json;
    }

    private static JsonObject researchAdvancement(IResearchItem research, boolean modern) {
        String category = ResearchAdvancementCompat.category(research);
        JsonObject json = advancement("abyssalcraft:research/category/" + category,
            categoryIcon(category), research.getName(), null, "goal", modern);
        json.getAsJsonObject("display").add("description", researchDescription(research));
        return json;
    }

    private static JsonObject advancement(String parent, String icon, String title,
                                          String description, String frame, boolean modern) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        JsonObject display = new JsonObject();
        JsonObject iconJson = new JsonObject();
        iconJson.addProperty(modern ? "id" : "item", icon);
        display.add("icon", iconJson);
        display.add("title", translate(title));
        if (description != null) display.add("description", translate(description));
        display.addProperty("frame", frame);
        display.addProperty("show_toast", true);
        display.addProperty("announce_to_chat", true);
        display.addProperty("hidden", false);
        json.add("display", display);
        JsonObject criteria = new JsonObject();
        JsonObject granted = new JsonObject();
        granted.addProperty("trigger", "minecraft:impossible");
        criteria.add("granted", granted);
        json.add("criteria", criteria);
        return json;
    }

    private static JsonObject researchDescription(IResearchItem research) {
        JsonObject description = new JsonObject();
        description.addProperty("translate", ResearchAdvancementCompat.conditionTranslationKey(research));
        JsonArray with = new JsonArray();
        JsonObject target = new JsonObject();
        target.addProperty("text", ResearchAdvancementCompat.conditionTarget(research));
        with.add(target);
        description.add("with", with);
        return description;
    }

    private static JsonObject translate(String key) {
        JsonObject json = new JsonObject();
        json.addProperty("translate", key);
        return json;
    }

    private static String categoryIcon(String category) {
        return switch (category) {
            case "biome" -> "minecraft:grass_block";
            case "dimension" -> "minecraft:ender_eye";
            case "entity" -> "minecraft:rotten_flesh";
            case "book" -> "abyssalcraft:necronomicon";
            default -> "minecraft:experience_bottle";
        };
    }
}
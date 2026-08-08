package com.shinoow.abyssalcraft.system.knowledge;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.knowledge.condition.ConditionProcessorRegistry;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.KnowledgePredicate;
import com.shinoow.abyssalcraft.system.knowledge.condition.NecronomiconCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import com.shinoow.abyssalcraft.system.data.NecromancyData;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

/** Permanent datagen invariants for the RR-KNOWLEDGE catalog and store. */
public final class KnowledgeSystemSelfTest {

    private KnowledgeSystemSelfTest() {}

    public static void run(net.minecraft.core.HolderLookup.Provider registries) {
        KnowledgeContent.bootstrap();
        KnowledgeContent.bootstrapOfferings();
        require(KnowledgeContent.researches().size() == 42, "research catalog is not 42");
        require(KnowledgeContent.conditions().size() == 42, "condition catalog is not 42");

        Set<String> researchIds = new HashSet<>();
        for (IResearchItem research : KnowledgeContent.researches()) {
            require(researchIds.add(research.getID().toString()), "duplicate research " + research.getID());
            require(research.getRequiredLevel() == 0 && research.getPointsCost() == 0,
                "legacy research economy changed for " + research.getID());
            require(research.getUnlockConditions().length == 1,
                "research must have one catalog condition " + research.getID());
        }

        ConditionProcessorRegistry processors = ConditionProcessorRegistry.instance();
        for (IUnlockCondition condition : KnowledgeContent.conditions()) {
            if (condition.getType() >= 0 && condition.getType() != NecronomiconCondition.TYPE) {
                require(processors.hasProcessor(condition.getType()), "missing processor " + condition.getType());
            }
            validateEntityIds(condition);
        }

        int offerings = 0;
        for (KnowledgeType type : KnowledgeType.values()) {
            offerings += ResearchRegistry.instance().getOfferingsForType(type).size();
        }
        require(offerings == 11, "offering catalog is not 11");

        CompoundTag tag = new CompoundTag();
        NecroData data = new NecroData(tag);
        require(data.triggerEntityUnlock("abyssalcraft:ghoul"), "first mutation was ignored");
        require(!data.triggerEntityUnlock("abyssalcraft:ghoul"), "duplicate mutation changed store");
        require(data.triggerArtifactUnlock("abyssalcraft:artifact_fixture"), "artifact trigger was ignored");
        require(data.triggerPageUnlock("abyssalcraft:page_fixture"), "page trigger was ignored");
        require(data.triggerWhisperUnlock("book/4"), "whisper trigger was ignored");
        NecroData reconnected = new NecroData(tag.copy());
        require(reconnected.getArtifactTriggers().contains("abyssalcraft:artifact_fixture")
            && reconnected.getPageTriggers().contains("abyssalcraft:page_fixture")
            && reconnected.getWhisperTriggers().contains("book/4"), "knowledge NBT reconnect lost triggers");
        require(data.unlockAllKnowledge(true) && !data.unlockAllKnowledge(true), "boolean mutation is not idempotent");

        require(processors.hasProcessor(5) && processors.hasProcessor(6), "predicate processors are not permanent");
        require(!ConditionProcessorRegistry.matchesRegistered(KnowledgePredicate.DARKLANDS_BIOMES,
            "abyssalcraft:not_registered", BuiltInRegistries.ITEM), "stale type 5 registry id matched");
        require(!ConditionProcessorRegistry.matchesRegistered(KnowledgePredicate.DREAD_ENTITIES,
            "abyssalcraft:not_registered", BuiltInRegistries.ENTITY_TYPE), "stale type 6 entity matched registry");
        NecroData predicateData = new NecroData(new CompoundTag());
        predicateData.triggerEntityUnlock("abyssalcraft:dreadling");
        require(processors.getProcessor(6).processUnlock(
            new com.shinoow.abyssalcraft.system.knowledge.condition.EntityPredicateCondition(
                KnowledgePredicate.DREAD_ENTITIES), predicateData, null), "registered type 6 entity did not match");
        NecroData stalePredicateData = new NecroData(new CompoundTag());
        stalePredicateData.triggerEntityUnlock("abyssalcraft:not_registered");
        require(!processors.getProcessor(6).processUnlock(
            new com.shinoow.abyssalcraft.system.knowledge.condition.EntityPredicateCondition(
                KnowledgePredicate.DREAD_ENTITIES), stalePredicateData, null), "stale type 6 entity matched");
        require(NecronomiconActions.actionIds().equals(List.of("create_altar", "ritual", "place_of_power")),
            "Necronomicon action registry changed");

        NecromancyData snapshots = new NecromancyData();
        for (int i = 0; i < 21; i++) snapshots.storeData("mob-" + i, new CompoundTag(), i % 3);
        require(snapshots.getData().size() == 20, "necromancy snapshot cap is not 20");
        require(snapshots.getDataForName("mob-0") == null && snapshots.getDataForName("mob-20") != null,
            "necromancy snapshot eviction order changed");
        CompoundTag saved = snapshots.serialize();
        require(NecromancyData.load(saved).getData().size() == 20, "necromancy snapshot round-trip failed");
        require(NecromancyData.crystalSize(0.74F) == 0 && NecromancyData.crystalSize(0.75F) == 1
            && NecromancyData.crystalSize(1.5F) == 2, "necromancy crystal-size boundaries changed");
        require(java.util.Arrays.equals(ComplexConfig.parsePortalColor(java.util.List.of(255, 255, 255)),
            new int[] {255, 255, 255}),
            "portal color parser changed");
        require(java.util.Arrays.equals(ComplexConfig.parseCoraliumOre(java.util.List.of(12, 8, 40)),
            new int[] {12, 8, 40}),
            "coralium ore parser changed");
        require("CbA".equals(EffectHooks.invertName("AbC")), "anti-player name inversion changed");

        validateNecronomiconManifest(registries, researchIds);
        require(NecronomiconPageManifest.pages().size() == 401,
            "knowledge manifest is not 401 pages");

        System.out.printf("RR_KNOWLEDGE_SELF_TEST_OK research=%d conditions=%d offerings=%d pages=%d%n",
            KnowledgeContent.researches().size(), KnowledgeContent.conditions().size(), offerings,
            NecronomiconPageManifest.pages().size());
    }

    public static int manifestPageCount() {
        return NecronomiconPageManifest.pages().size();
    }

    private static void validateNecronomiconManifest(net.minecraft.core.HolderLookup.Provider registries,
            Set<String> researchIds) {
        NecronomiconPageManifest.bootstrap(registries);
        List<NecronomiconPageManifest.PageEntry> pages = NecronomiconPageManifest.pages();
        int expected = LegacyNecronomiconPageCatalog.ids().size() + RitualManifestCatalog.entries().size()
            + SpellManifestCatalog.entries().size() + EnergyStructures.ALL.size();
        require(pages.size() == expected, "Necronomicon manifest count changed: " + pages.size() + " != " + expected);

        Set<String> pageIds = new HashSet<>();
        Set<String> itemPageIds = new HashSet<>();
        Set<String> imagePageReferences = new HashSet<>();
        Set<String> imageTextures = new HashSet<>();
        int active = 0;
        int blocked = 0;
        int activeImageVisuals = 0;
        int activeItemVisuals = 0;
        int blockedItemVisuals = 0;
        Map<String, Long> legacyVisualCounts = LegacyNecronomiconPageManifest.pages().stream()
            .collect(Collectors.groupingBy(LegacyNecronomiconPageManifest.LegacyPage::visualKind,
                java.util.TreeMap::new, Collectors.counting()));
        Map<String, Long> blockedByOwner;
        int legacyIndex = 0;
        JsonObject english = readJsonResource("assets/abyssalcraft/lang/en_us.json");
        for (NecronomiconPageManifest.PageEntry page : pages) {
            require(pageIds.add(page.id().toString()), "duplicate Necronomicon page " + page.id());
            require(page.titleKey() != null && !page.titleKey().isBlank(), "blank title for " + page.id());
            require(page.type() != null && page.icon() != null && page.relatedPages() != null
                && page.content() != null,
                "incomplete Necronomicon page " + page.id());
            require(page.content().status() != NecronomiconPageManifest.OwnerStatus.MISSING,
                "missing content owner for " + page.id() + ": " + page.content().reason());
            if (page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) active++;
            if (page.content().status() == NecronomiconPageManifest.OwnerStatus.BLOCKED) blocked++;
            if (page.legacyFields() != null) {
                var legacy = page.legacyFields();
                require(legacy.sourceOrder() == legacyIndex + 1, "legacy source order changed for " + page.id());
                require(legacy.legacyId().equals(LegacyNecronomiconPageCatalog.ids().get(legacyIndex)),
                    "legacy page order changed for " + page.id());
                require(legacy.pageNumber() > 0 && legacy.bookType() >= 0 && legacy.bookType() <= 4,
                    "invalid legacy page coordinates for " + page.id());
                require(!legacy.titleReference().isBlank() && !legacy.textReference().isBlank()
                    && !legacy.visualKind().isBlank() && !legacy.researchReference().isBlank()
                    && !legacy.constructor().isBlank(), "incomplete legacy fields for " + page.id());
                LegacyNecronomiconPageManifest.LegacyPage source =
                    LegacyNecronomiconPageManifest.pages().get(legacyIndex);
                require(source.status() == page.content().status(),
                    "legacy JSON status differs from final manifest for " + page.id());
                require(source.owner().equals(page.content().owner()),
                    "legacy JSON owner differs from final manifest for " + page.id());
                require(page.textKey() != null && !page.textKey().isBlank(), "blank legacy text for " + page.id());
                if (legacy.visualKind().equals("NONE")) {
                    require(page.content().kind().equals("text")
                        && page.content().reference().equals(page.textKey()),
                        "text-only legacy page has no content reference " + page.id());
                }
                if (!page.textKey().startsWith("dynamic:")) {
                    require(english.has(page.textKey()), "missing legacy text lang key " + page.textKey());
                }
                require(english.has(page.titleKey()), "missing legacy title lang key " + page.titleKey());
                if ("IMAGE".equals(legacy.visualKind())) {
                    activeImageVisuals++;
                    require(page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE,
                        "legacy image visual is not active " + page.id());
                    require(page.content().owner().equals("necronomicon-image-renderer"),
                        "legacy image visual has the wrong owner " + page.id());
                    require(page.content().reference().equals(legacy.visualReference()),
                        "legacy image visual reference changed " + page.id());
                    require(imagePageReferences.add(page.id() + "=" + legacy.visualReference()),
                        "duplicate legacy image page reference " + page.id());
                    imageTextures.add(legacy.visualReference());
                    validateNecronomiconImage(page);
                } else {
                    require(page.image() == null, "non-image page exposed image content " + page.id());
                }
                if ("ITEM".equals(legacy.visualKind())) {
                    require(itemPageIds.add(page.id().toString()),
                        "duplicate legacy item visual owner " + page.id());
                    if (page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE) {
                        activeItemVisuals++;
                        require(!page.icon().isEmpty(), "active item visual has an empty stack " + page.id());
                        var itemId = ACRef.parse(page.content().reference());
                        require(BuiltInRegistries.ITEM.containsKey(itemId),
                            "active item visual is absent from registry " + page.id() + ": " + itemId);
                        require(BuiltInRegistries.ITEM.getKey(page.icon().getItem()).equals(itemId),
                            "item visual stack does not match canonical reference " + page.id());
                        validateEnchantedBook(page, registries);
                    } else {
                        blockedItemVisuals++;
                        require(page.icon().isEmpty(), "blocked item visual exposed a stack " + page.id());
                    }
                }
                legacyIndex++;
            } else {
                require(page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE,
                    "catalog content is not active for " + page.id());
            }
            if (page.researchId() != null) {
                require(page.researchId().equals(NecronomiconPageManifest.PERMANENTLY_LOCKED)
                    || researchIds.contains(page.researchId().toString()),
                    "missing page research " + page.researchId() + " for " + page.id());
            }
            for (var related : page.relatedPages()) {
                require(pageIds.contains(related.toString()) || pages.stream().anyMatch(other -> other.id().equals(related)),
                    "missing related page " + related + " for " + page.id());
            }
        }
        RitualManifestCatalog.entries().forEach(ritual -> require(
            pageIds.contains(ACRef.id(NecronomiconPageManifest.catalogId("ritual", ritual.id())).toString()),
            "missing ritual page " + ritual.id()));
        SpellManifestCatalog.entries().forEach(spell -> require(
            pageIds.contains(ACRef.id(NecronomiconPageManifest.catalogId("spell", spell.id())).toString()),
            "missing spell page " + spell.id()));
        EnergyStructures.ALL.forEach(structure -> require(
            pageIds.contains(ACRef.id(NecronomiconPageManifest.catalogId(
                "place_of_power", structure.getIdentifier())).toString()),
            "missing Place of Power page " + structure.getIdentifier()));
        require(legacyIndex == LegacyNecronomiconPageCatalog.ids().size(),
            "legacy manifest coverage changed: " + legacyIndex);
        long expectedImages = legacyVisualCounts.getOrDefault("IMAGE", 0L);
        long expectedItems = legacyVisualCounts.getOrDefault("ITEM", 0L);
        require(activeImageVisuals == expectedImages && imagePageReferences.size() == expectedImages,
            "legacy image visual closure changed: active=" + activeImageVisuals
                + " unique=" + imagePageReferences.size());
        require(imageTextures.size() == 75,
            "legacy image texture reference count changed: " + imageTextures.size());
        require(itemPageIds.size() == expectedItems, "legacy item visual count changed: " + itemPageIds.size());
        require(activeItemVisuals == expectedItems && blockedItemVisuals == 0,
            "legacy item visual closure changed: active=" + activeItemVisuals + " blocked=" + blockedItemVisuals);
        validateNecronomiconRecipes(pages);
        validateAkloContent(pages, english);
        blockedByOwner = pages.stream()
            .filter(page -> page.content().status() == NecronomiconPageManifest.OwnerStatus.BLOCKED)
            .collect(Collectors.groupingBy(page -> page.content().owner(), java.util.TreeMap::new,
                Collectors.counting()));
        require(active + blocked == pages.size(), "Necronomicon status accounting is incomplete");
        Map<String, Long> expectedBlockedByOwner = NecronomiconRecipePages.definitions().values().stream()
            .filter(definition -> !definition.active())
            .collect(Collectors.groupingBy(definition -> "necronomicon-recipe-renderer",
                java.util.TreeMap::new, Collectors.counting()));
        require(blockedByOwner.equals(expectedBlockedByOwner),
            "Necronomicon BLOCKED owner set changed: " + blockedByOwner);
        require(!blockedByOwner.containsKey("RR-CONTENT"),
            "Necronomicon still delegates user-owned item pages to RR-CONTENT");
        require(pages.stream().filter(page -> page.content().status() == NecronomiconPageManifest.OwnerStatus.BLOCKED)
            .noneMatch(page -> NecronomiconPageManifest.findActionable(page.id()).isPresent()),
            "BLOCKED Necronomicon page was counted as actionable");
        require(pages.stream().filter(page -> page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE)
            .allMatch(page -> NecronomiconPageManifest.findActionable(page.id()).isPresent()),
            "ACTIVE Necronomicon page is not actionable");
        validateNecronomiconTierGate(pages);
        Map<String, Map<NecronomiconPageManifest.OwnerStatus, Long>> ownerStatus = pages.stream()
            .filter(page -> (page.legacyFields() != null && Set.of("IMAGE", "RECIPE", "ITEM")
                .contains(page.legacyFields().visualKind())) || page.content().owner().equals("aklo-content"))
            .collect(Collectors.groupingBy(page -> page.content().owner(), java.util.TreeMap::new,
                Collectors.groupingBy(page -> page.content().status(), () ->
                    new java.util.EnumMap<>(NecronomiconPageManifest.OwnerStatus.class), Collectors.counting())));
        System.out.printf("RR_NECRO_PAGE_MANIFEST active=%d blocked=%d missing=0 owners=%s%n",
            active, blocked, ownerStatus);
    }

    private static void validateNecronomiconTierGate(List<NecronomiconPageManifest.PageEntry> pages) {
        int clientVisible = 0;
        int serverActionable = 0;
        for (NecronomiconPageManifest.PageEntry page : pages) {
            int expectedBookType = page.legacyFields() != null
                ? page.legacyFields().bookType()
                : catalogBookType(page);
            require(page.requiredBookType() == expectedBookType,
                "Necronomicon page tier differs from owner " + page.id());
            for (int bookType = 0; bookType < 5; bookType++) {
                int testedBookType = bookType;
                boolean expectedClient = bookType >= expectedBookType;
                boolean expectedServer = page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE
                    && expectedClient;
                boolean clientGate = NecronomiconPageManifest.isAvailableForBook(page, bookType);
                boolean serverGate = NecronomiconPageManifest.findActionable(page.id())
                    .filter(candidate -> NecronomiconPageManifest.isAvailableForBook(candidate, testedBookType))
                    .isPresent();
                require(clientGate == expectedClient,
                    "client tier gate changed for " + page.id() + " book=" + bookType);
                require(serverGate == expectedServer,
                    "server tier gate changed for " + page.id() + " book=" + bookType);
                if (clientGate) clientVisible++;
                if (serverGate) serverActionable++;
            }
        }
        require(pages.stream().allMatch(page -> !NecronomiconPageManifest.isAvailableForBook(page, -1)),
            "missing held Necronomicon did not fail closed");
        System.out.printf("RR_NECRO_TIER_GATE_OK books=5 client=%d server=%d pages=%d%n",
            clientVisible, serverActionable, pages.size());
    }

    private static int catalogBookType(NecronomiconPageManifest.PageEntry page) {
        String path = page.id().getPath();
        if (path.startsWith("ritual/")) {
            String id = path.substring("ritual/".length());
            return RitualManifestCatalog.entries().stream()
                .filter(entry -> entry.id().toLowerCase(java.util.Locale.ROOT).equals(id))
                .findFirst().orElseThrow(() -> new IllegalStateException("missing ritual owner for " + page.id()))
                .bookType();
        }
        if (path.startsWith("spell/")) {
            String id = path.substring("spell/".length());
            return SpellManifestCatalog.entries().stream()
                .filter(entry -> entry.id().toLowerCase(java.util.Locale.ROOT).equals(id))
                .findFirst().orElseThrow(() -> new IllegalStateException("missing spell owner for " + page.id()))
                .bookType();
        }
        if (path.startsWith("place_of_power/")) {
            String id = path.substring("place_of_power/".length());
            return EnergyStructures.ALL.stream()
                .filter(entry -> entry.getIdentifier().toLowerCase(java.util.Locale.ROOT).equals(id))
                .findFirst().orElseThrow(() -> new IllegalStateException("missing Place of Power owner for " + page.id()))
                .getBookType();
        }
        throw new IllegalStateException("unknown Necronomicon page owner " + page.id());
    }

    private static void validateNecronomiconRecipes(List<NecronomiconPageManifest.PageEntry> pages) {
        Set<String> legacyIds = LegacyNecronomiconPageManifest.pages().stream()
            .filter(page -> page.owner().equals("necronomicon-recipe-renderer"))
            .map(LegacyNecronomiconPageManifest.LegacyPage::legacyId).collect(Collectors.toSet());
        require(legacyIds.equals(NecronomiconRecipePages.definitions().keySet()),
            "Necronomicon recipe catalog does not close: legacy=" + legacyIds.size()
                + " catalog=" + NecronomiconRecipePages.definitions().size());
        long active = NecronomiconRecipePages.definitions().values().stream()
            .filter(NecronomiconRecipePages.Definition::active).count();
        long manifestActive = pages.stream().filter(page -> page.legacyFields() != null
            && "RECIPE".equals(page.legacyFields().visualKind())
            && page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE).count();
        require(active == manifestActive,
            "Necronomicon recipe status closure changed: definitions=" + active + " manifest=" + manifestActive);
        NecronomiconRecipePages.definitions().forEach((legacyId, definition) -> {
            var page = pages.stream().filter(candidate -> candidate.legacyFields() != null
                && candidate.legacyFields().legacyId().equals(legacyId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing recipe page " + legacyId));
            require(page.content().status() == (definition.active()
                ? NecronomiconPageManifest.OwnerStatus.ACTIVE : NecronomiconPageManifest.OwnerStatus.BLOCKED),
                "recipe page status differs from catalog " + legacyId);
            if (definition.active()) {
                require(!definition.recipeIds().isEmpty(), "active recipe page has no recipes " + legacyId);
                definition.recipeIds().forEach(id -> {
                    require(id.getNamespace().equals("abyssalcraft") && !id.getPath().isBlank(),
                        "invalid modern recipe id " + id);
                    String forge = "data/abyssalcraft/recipes/" + id.getPath() + ".json";
                    String neo = "data/abyssalcraft/recipe/" + id.getPath() + ".json";
                    require(resourceExists(forge) || resourceExists(neo),
                        "missing modern recipe resource " + id + " for " + legacyId);
                    validateRecipeResource(resourceExists(forge) ? forge : neo, legacyId);
                });
            } else {
                require(!definition.blockedReason().isBlank(), "BLOCKED recipe lacks dependency " + legacyId);
            }
        });
    }

        private static void validateEnchantedBook(NecronomiconPageManifest.PageEntry page,
            net.minecraft.core.HolderLookup.Provider registries) {
        Map<String, Map.Entry<net.minecraft.resources.ResourceLocation, Integer>> expected = Map.of(
            "ENCHANTMENT_LIGHT_PIERCE", Map.entry(ACRef.id("light_pierce"), 5),
            "ENCHANTMENT_IRON_WALL", Map.entry(ACRef.id("iron_wall"), 1),
            "ENCHANTMENT_SAPPING", Map.entry(ACRef.id("sapping"), 3),
            "ENCHANTMENT_MULTI_REND", Map.entry(ACRef.id("multi_rend"), 1),
            "ENCHANTMENT_BLINDING_LIGHT", Map.entry(ACRef.id("blinding_light"), 1));
        var enchantment = expected.get(page.legacyFields().legacyId());
        if (enchantment == null) return;
        require(page.icon().is(net.minecraft.world.item.Items.ENCHANTED_BOOK),
            "enchantment page is not an enchanted book " + page.id());
        Map<net.minecraft.resources.ResourceLocation, Integer> actual =
            com.shinoow.abyssalcraft.platform.EnchantmentDataCompat.read(page.icon());
        var key = net.minecraft.resources.ResourceKey.create(
            net.minecraft.core.registries.Registries.ENCHANTMENT, enchantment.getKey());
        if (com.shinoow.abyssalcraft.platform.EnchantmentCompat.hasEnchantment(registries, key)) {
            require(actual.equals(Map.of(enchantment.getKey(), enchantment.getValue())),
                "enchantment page has the wrong stored enchantment " + page.id() + ": " + actual);
        } else {
            require(actual.isEmpty(), "unbound datagen enchantment produced a fabricated component " + page.id());
            require(resourceExists("data/abyssalcraft/enchantment/" + enchantment.getKey().getPath() + ".json"),
                "unbound datagen enchantment has no datapack definition " + enchantment.getKey());
        }
    }

    private static boolean resourceExists(String path) {
        return KnowledgeSystemSelfTest.class.getClassLoader().getResource(path) != null;
    }

    private static void validateRecipeResource(String path, String legacyId) {
        JsonObject recipe = readJsonResource(path);
        boolean shaped = recipe.has("pattern") && recipe.get("pattern").isJsonArray()
            && !recipe.getAsJsonArray("pattern").isEmpty()
            && recipe.has("key") && recipe.get("key").isJsonObject()
            && !recipe.getAsJsonObject("key").entrySet().isEmpty();
        boolean shapeless = recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()
            && !recipe.getAsJsonArray("ingredients").isEmpty();
        require(shaped || shapeless, "recipe has no inputs " + path + " for " + legacyId);
        require(recipe.has("result") && recipe.get("result").isJsonObject(),
            "recipe has no output " + path + " for " + legacyId);
        JsonObject result = recipe.getAsJsonObject("result");
        String output = result.has("item") ? result.get("item").getAsString()
            : result.has("id") ? result.get("id").getAsString() : "";
        require(!output.isBlank(), "recipe has a blank output " + path + " for " + legacyId);
        requireRegisteredRecipeItem(output, "output", path, legacyId);
        if (shaped) {
            recipe.getAsJsonObject("key").entrySet().forEach(entry ->
                validateRecipeIngredient(entry.getValue(), path, legacyId));
        } else {
            recipe.getAsJsonArray("ingredients").forEach(ingredient ->
                validateRecipeIngredient(ingredient, path, legacyId));
        }
    }

    private static void validateRecipeIngredient(JsonElement element, String path, String legacyId) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(candidate -> validateRecipeIngredient(candidate, path, legacyId));
            return;
        }
        require(element.isJsonObject(), "recipe has a malformed input " + path + " for " + legacyId);
        JsonObject ingredient = element.getAsJsonObject();
        if (ingredient.has("item")) {
            requireRegisteredRecipeItem(ingredient.get("item").getAsString(), "input", path, legacyId);
        } else {
            require(ingredient.has("tag"), "recipe input has no item or tag " + path + " for " + legacyId);
        }
    }

    private static void requireRegisteredRecipeItem(String value, String role, String path, String legacyId) {
        var id = ACRef.parse(value);
        require(BuiltInRegistries.ITEM.containsKey(id),
            "recipe " + role + " is not registered " + id + " in " + path + " for " + legacyId);
    }

    private static void validateNecronomiconImage(NecronomiconPageManifest.PageEntry page) {
        NecronomiconPageManifest.ImageContent image = page.image();
        require(image != null, "active image page lacks image content " + page.id());
        require(image.texture().toString().equals(page.content().reference()),
            "image texture does not match content reference " + page.id());
        String resource = "assets/" + image.texture().getNamespace() + "/" + image.texture().getPath();
        try (InputStream stream = KnowledgeSystemSelfTest.class.getClassLoader().getResourceAsStream(resource)) {
            require(stream != null, "missing resource " + resource);
            BufferedImage decoded = ImageIO.read(stream);
            require(decoded != null, "undecodable Necronomicon PNG " + resource);
            require(decoded.getWidth() == image.textureWidth() && decoded.getHeight() == image.textureHeight(),
                "Necronomicon image dimensions changed " + resource);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to decode " + resource, exception);
        }
        require(image.u() == 0 && image.v() == 0
            && image.width() == Math.max(1, image.textureWidth() * 255 / 256)
            && image.height() == Math.max(1, image.textureHeight() * 192 / 256),
            "legacy Necronomicon fixed-UV image sampling changed " + page.id());
    }

    private static void validateAkloContent(List<NecronomiconPageManifest.PageEntry> pages, JsonObject english) {
        String textKey = "necronomicon.text.knowledge.aklo";
        List<NecronomiconPageManifest.PageEntry> akloPages = pages.stream()
            .filter(entry -> entry.content().owner().equals("aklo-content")).toList();
        require(akloPages.size() == 1, "expected exactly one Aklo Necronomicon page");
        NecronomiconPageManifest.PageEntry page = akloPages.get(0);
        require(page.content().status() == NecronomiconPageManifest.OwnerStatus.ACTIVE,
            "Aklo Necronomicon page is not ACTIVE");
        require(textKey.equals(page.textKey()), "Aklo Necronomicon page text key changed");

        JsonObject chinese = readJsonResource("assets/abyssalcraft/lang/zh_cn.json");
        String englishText = requireNonBlankLanguage(english, textKey, "en_us");
        String chineseText = requireNonBlankLanguage(chinese, textKey, "zh_cn");
        JsonObject font = readJsonResource("assets/abyssalcraft/font/aklo.json");
        JsonObject provider = font.getAsJsonArray("providers").get(0).getAsJsonObject();
        require("bitmap".equals(provider.get("type").getAsString()), "Aklo font is not a bitmap provider");
        require("abyssalcraft:font/aklo.png".equals(provider.get("file").getAsString()),
            "Aklo font bitmap reference changed");

        Set<Integer> glyphs = new LinkedHashSet<>();
        provider.getAsJsonArray("chars").forEach(row -> row.getAsString().codePoints()
            .filter(codePoint -> codePoint != 0).forEach(glyphs::add));
        for (int codePoint = 0x20; codePoint <= 0x7e; codePoint++) {
            require(glyphs.contains(codePoint), "Aklo font is missing printable ASCII glyph " + codePoint);
        }
        englishText.codePoints().forEach(codePoint -> require(glyphs.contains(codePoint),
            "Aklo en_us text uses missing glyph " + codePoint));
        chineseText.codePoints().forEach(codePoint -> require(glyphs.contains(codePoint),
            "Aklo zh_cn text uses missing glyph " + codePoint));

        byte[] png = readResource("assets/abyssalcraft/textures/font/aklo.png");
        require(png.length > 24 && png[0] == (byte) 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G',
            "Aklo font texture is not a PNG");
        require(readInt(png, 16) == 256 && readInt(png, 20) == 256,
            "Aklo font texture must be 256x256");
    }

    private static String requireNonBlankLanguage(JsonObject language, String key, String locale) {
        require(language.has(key), locale + " is missing Aklo text key " + key);
        String value = language.get(key).getAsString();
        require(!value.isBlank(), locale + " Aklo text is blank");
        return value;
    }

    private static int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) << 24 | (bytes[offset + 1] & 0xff) << 16
            | (bytes[offset + 2] & 0xff) << 8 | bytes[offset + 3] & 0xff;
    }

    private static byte[] readResource(String path) {
        try (InputStream stream = KnowledgeSystemSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return stream.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private static JsonObject readJsonResource(String path) {
        try (InputStream stream = KnowledgeSystemSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8))
                .getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private static void validateEntityIds(IUnlockCondition condition) {
        Object value = condition.getConditionObject();
        if (condition.getType() == 1) {
            requireEntity((String) value);
        } else if (condition.getType() == 4 || condition.getType() == 11) {
            for (String id : (String[]) value) {
                requireEntity(id);
            }
        } else if (condition.getType() == 6) {
            for (String id : ((KnowledgePredicate) value).ids()) {
                requireEntity(id);
            }
        }
    }

    private static void requireEntity(String id) {
        require(BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.parse(id)), "missing entity " + id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
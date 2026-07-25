package com.shinoow.abyssalcraft.system.ritual;

import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.BREEDING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.CLEANSING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.CORRUPTION;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.CREATION;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.CURING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.DREAD_SPAWN;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.HOUSE;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.INFESTING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.INFUSION;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.MASS_ENCHANTING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.PORTAL;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.POTION_AOE;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.PURGING;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.RESPAWN_JZAHAR;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.RESURRECTION;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.SUMMON;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.TRANSFORMATION;
import static com.shinoow.abyssalcraft.system.ritual.RitualManifest.Kind.WEATHER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Ordered, source-faithful catalog of the 62 rituals registered by AbyssalCraft 1.12.2. */
public final class RitualManifestCatalog {

    private static final RitualIngredient EMPTY = RitualIngredient.empty();
    private static final List<RitualManifest> ENTRIES = build();
    private static final Map<String, RitualManifest> BY_ID = index();

    private RitualManifestCatalog() {}

    public static List<RitualManifest> entries() {
        return ENTRIES;
    }

    public static RitualManifest get(String id) {
        return BY_ID.get(id);
    }

    private static List<RitualManifest> build() {
        List<RitualManifest> entries = new ArrayList<>(62);

        add(entries, ritual(1, "transmutation_gem", "transmutationGem", INFUSION, 0, null, 300)
            .center(ac("coralium_pearl")).result(acId("transmutation_gem"))
            .layout(mc("diamond"), mc("blaze_powder"), mc("ender_pearl"), mc("blaze_powder"),
                mc("diamond"), mc("blaze_powder"), mc("ender_pearl"), mc("blaze_powder")));
        add(entries, ritual(2, "oblivion_catalyst", "oblivionCatalyst", INFUSION, 0, null, 5000)
            .livingSacrifice().center(mc("ender_eye")).result(acId("oblivion_catalyst"))
            .layout(mc("redstone"), ac("shard_of_oblivion"), mc("redstone"), ac("shard_of_oblivion"),
                mc("redstone"), ac("shard_of_oblivion"), mc("redstone"), ac("shard_of_oblivion")));
        add(entries, ritual(3, "portal", "portal", PORTAL, 0, null, 1000)
            .center(acAny("gatewaykey", "gatewaykeydl", "gatewaykeyjzh"))
            .layout(ac("shadow_gem"), EMPTY, ac("shadow_gem"), EMPTY,
                ac("shadow_gem"), EMPTY, ac("shadow_gem"), EMPTY));
        add(entries, ritual(4, "summon_asorah", "summonAsorah", SUMMON, 1,
                ACDimensions.ABYSSAL_WASTELAND, 1000)
            .strictOfferings().targets(acId("dragonboss"))
            .layout(mc("gold_ingot"), ac("transmutation_gem"), mc("gold_ingot"),
                ac("liquid_coralium_bucket"), mc("gold_ingot"), mc("enchanting_table"),
                mc("gold_ingot"), EMPTY));
        add(entries, ritual(5, "dreadlands_infused_gateway_key", "dreadInfusedGatewayKey", INFUSION, 1,
                ACDimensions.ABYSSAL_WASTELAND, 10000)
            .center(ac("gatewaykey")).result(acId("gatewaykeydl"))
            .layout(ac("transmutation_gem"), EMPTY, ac("dreadlands_infused_powerstone"), EMPTY,
                ac("eye_of_the_abyss"), EMPTY, EMPTY, EMPTY));
        add(entries, ritual(6, "sealing_key", "sealingKey", INFUSION, 2,
                ACDimensions.DREADLANDS, 20000)
            .livingSacrifice().center(ac("gatewaykeydl")).result(acId("sealing_key"))
            .layout(ac("dread_cloth"), ac("crystal_abyssalnite"), ac("crystal_dreadium"), ac("elysian_stone"),
                ac("dread_cloth"), ac("elysian_stone"), ac("crystal_dreadium"), ac("crystal_abyssalnite")));
        add(entries, ritual(7, "interdimensional_cage", "interdimensionalcage", INFUSION, 2, null, 1000)
            .center(ac("shard_of_oblivion")).result(acId("interdimensional_cage"))
            .layout(repeat(mc("iron_bars"), 8)));
        add(entries, ritual(8, "respawn_jzahar", "respawnJzahar", RESPAWN_JZAHAR, 3,
                ACDimensions.OMOTHOL, 20000)
            .targets(acId("jzahar"))
            .layout(ac("omothol_essence"), ac("shard_of_oblivion"), ac("omothol_essence"), ac("shard_of_oblivion"),
                ac("omothol_essence"), ac("shard_of_oblivion"), ac("omothol_essence"), ac("shard_of_oblivion")));

        add(entries, depths(9, "depths_helmet", "depthsHelmet", "refined_coralium_helmet"));
        add(entries, depths(10, "depths_chestplate", "depthsChestplate", "refined_coralium_chestplate"));
        add(entries, depths(11, "depths_leggings", "depthsLeggings", "refined_coralium_leggings"));
        add(entries, depths(12, "depths_boots", "depthsBoots", "refined_coralium_boots"));

        add(entries, ritual(13, "breeding", "breeding", BREEDING, 0, null, 500)
            .layout(mc("wheat"), mc("potato"), mc("carrot"), mc("melon_slice"),
                mc("pumpkin"), mc("wheat_seeds"), mc("melon_seeds"), mc("pumpkin_seeds")));
        add(entries, ritual(14, "summon_sacthoth", "summonSacthoth", SUMMON, 1, null, 1000)
            .strictOfferings().targets(acId("shadowboss"))
            .layout(ac("oblivion_catalyst"), mc("obsidian"), ac("liquid_coralium_bucket"), mc("obsidian"),
                ac("liquid_antimatter_bucket"), mc("obsidian"), ac("odb_core"), mc("obsidian")));
        add(entries, ritual(15, "dread_spawn", "dreadSpawn", DREAD_SPAWN, 2, null, 500)
            .targets(acId("dreadspawn"), acId("greaterdreadspawn"), acId("lesserdreadbeast"))
            .layout(ac("dread_fragment"), ac("dreadstone"), ac("elysian_stone"), ac("dreaded_shoggoth_flesh"),
                mc("leather"), mc("rotten_flesh"), ac("dreaded_shard_of_abyssalnite"), ac("abyssalnite_ingot")));
        add(entries, potion(16, "coralium_plague_aoe", "corPotionAoE", 1,
            "coralium_plagued_flesh", "coralium_plague"));
        add(entries, potion(17, "dread_plague_aoe", "drePotionAoE", 2,
            "dread_fragment", "dread_plague"));
        add(entries, potion(18, "antimatter_aoe", "antiPotionAoE", 0,
            "rotten_anti_flesh", "antimatter"));
        add(entries, ritual(19, "dreadlands_infused_powerstone", "powerStone", INFUSION, 2,
                ACDimensions.DREADLANDS, 5000)
            .center(ac("coralium_infused_stone")).result(acId("dreadlands_infused_powerstone"))
            .layout(repeat(ac("dreadlands_essence"), 8)));
        add(entries, ritual(20, "resurrection", "resurrection", RESURRECTION, 2, null, 1000)
            .livingSacrifice().center(mc("name_tag"))
            .layout(crystal("carbon"), crystal("hydrogen"), crystal("nitrogen"), crystal("oxygen"),
                crystal("phosphorus"), crystal("sulfur"), EMPTY, EMPTY));
        add(entries, ritual(21, "book_of_many_faces", "facebook", INFUSION, 2, null, 2000)
            .center(mc("book")).result(acId("book_of_many_faces"))
            .layout(ac("crystal_carbon"), ac("crystal_hydrogen"), ac("crystal_nitrogen"), ac("crystal_oxygen"),
                ac("crystal_phosphorus"), ac("crystal_sulfur"), mc("feather"), tag("minecraft:dyes")));
        add(entries, ritual(22, "staff_of_the_gatekeeper", "jzaharStaff", INFUSION, 4,
                ACDimensions.OMOTHOL, 15000)
            .center(acAny("gatewaykey", "gatewaykeydl", "gatewaykeyjzh"))
            .result(acId("staff_of_the_gatekeeper"))
            .layout(ac("dreadlands_essence"), ac("omothol_essence"), ac("eldritch_scale"), ac("ethaxium_ingot"),
                ac("eldritch_scale"), ac("ethaxium_ingot"), ac("eldritch_scale"), ac("abyssal_wasteland_essence")));
        add(entries, ritual(23, "silver_key", "silverKey", INFUSION, 4, ACDimensions.OMOTHOL, 20000)
            .livingSacrifice().center(ac("gatewaykeyjzh")).result(acId("silver_key"))
            .layout(repeat(ac("ethaxium_ingot"), 8)));
        add(entries, ritual(24, "unchained_portal_anchor", "unchainedPortalAnchor", INFUSION, 4, null, 20000)
            .center(ac("unchained_portal_anchor")).result(acId("unchained_portal_anchor"))
            .layout(ac("ethaxium"), EMPTY, ac("ethaxium"), EMPTY, ac("ethaxium"), EMPTY, ac("ethaxium"), EMPTY));

        RitualIngredient decorativeStatue = acAny(
            "decorative_jzahar_statue", "decorative_cthulhu_statue", "decorative_hastur_statue",
            "decorative_azathoth_statue", "decorative_nyarlathotep_statue",
            "decorative_yog_sothoth_statue", "decorative_shub_niggurath_statue");
        RitualIngredient functionalStatue = acAny(
            "jzahar_statue", "cthulhu_statue", "hastur_statue", "azathoth_statue",
            "nyarlathotep_statue", "yog_sothoth_statue", "shub_niggurath_statue");
        add(entries, ritual(25, "cleansing", "cleansing", CLEANSING, 1, Level.OVERWORLD, 10000)
            .livingSacrifice().layout(repeat(decorativeStatue, 8)));
        add(entries, ritual(26, "corruption", "corruption", CORRUPTION, 1, Level.OVERWORLD, 10000)
            .livingSacrifice().layout(decorativeStatue, ac("darkstone"), decorativeStatue, ac("darkstone"),
                decorativeStatue, ac("darkstone"), decorativeStatue, ac("darkstone")));
        add(entries, ritual(27, "infesting", "infesting", INFESTING, 3, Level.OVERWORLD, 10000)
            .livingSacrifice().layout(functionalStatue, ac("coralium_stone"), functionalStatue, ac("coralium_stone"),
                functionalStatue, ac("coralium_stone"), functionalStatue, ac("coralium_stone")));
        add(entries, ritual(28, "curing", "curing", CURING, 4, null, 20000)
            .layout(ac("dread_antidote"), EMPTY, ac("dread_antidote"), EMPTY,
                ac("dread_antidote"), EMPTY, ac("dread_antidote"), EMPTY));
        add(entries, ritual(29, "purging", "purging", PURGING, 2, null, 10000)
            .layout(ac("calcium_crystal_cluster"), EMPTY, ac("calcium_crystal_cluster"), EMPTY,
                ac("calcium_crystal_cluster"), EMPTY, ac("calcium_crystal_cluster"), EMPTY));
        add(entries, ritual(30, "mass_enchanting", "massEnchantment", MASS_ENCHANTING, 4, null, 50000)
            .center(RitualIngredient.anyItem()).layout(repeat(mc("enchanted_book"), 8)));
        add(entries, ritual(31, "weather", "weather", WEATHER, 0, null, 100)
            .layout(repeat(mc("feather"), 8)));

        RitualIngredient spiritShard = acAny("spirit_tablet_shard_0", "spirit_tablet_shard_1",
            "spirit_tablet_shard_2", "spirit_tablet_shard_3");
        add(entries, ritual(32, "spirit_tablet", "spiritTablet", INFUSION, 1, null, 5000)
            .center(ac("shadow_gem")).result(acId("spirit_tablet"))
            .layout(EMPTY, spiritShard, EMPTY, spiritShard, EMPTY, spiritShard, EMPTY, spiritShard));
        add(entries, ritual(33, "spirit_altar", "spiritAltar", INFUSION, 1, null, 1000)
            .livingSacrifice().center(ac("shadow_gem")).result(acId("spirit_altar"))
            .layout(mc("gold_ingot"), mc("gold_ingot"), mc("gold_ingot"), ac("darkstone_cobblestone"),
                ac("darkstone_cobblestone"), ac("darkstone_cobblestone"), ac("darkstone_cobblestone"), ac("darkstone_cobblestone")));

        add(entries, statue(34, "cthulhu", repeat(ac("overworld_shoggoth_flesh"), 5)));
        add(entries, statue(35, "hastur", repeat(ac("abyssal_shoggoth_flesh"), 5)));
        add(entries, statue(36, "jzahar", repeat(ac("eldritch_scale"), 5)));
        add(entries, statue(37, "azathoth", ac("overworld_shoggoth_flesh"), ac("abyssal_shoggoth_flesh"),
            ac("dreaded_shoggoth_flesh"), ac("omothol_shoggoth_flesh"), ac("shadow_shoggoth_flesh")));
        add(entries, statue(38, "nyarlathotep", repeat(ac("dreaded_shoggoth_flesh"), 5)));
        add(entries, statue(39, "yog_sothoth", repeat(ac("omothol_shoggoth_flesh"), 5)));
        add(entries, statue(40, "shub_niggurath", repeat(ac("shadow_shoggoth_flesh"), 5)));

        add(entries, ritual(41, "ethaxium", "ethaxium", TRANSFORMATION, 3, ACDimensions.OMOTHOL, 10000)
            .result(acId("ethaxium")).layout(repeat(mc("soul_sand"), 8)));
        add(entries, ritual(42, "ethaxium_ingot", "ethaxiumIngot", INFUSION, 3, ACDimensions.OMOTHOL, 1000)
            .center(ac("life_crystal")).result(acId("ethaxium_ingot"))
            .layout(ac("ethaxium_brick"), EMPTY, ac("ethaxium_brick"), EMPTY,
                ac("ethaxium_brick"), EMPTY, ac("ethaxium_brick"), EMPTY));
        add(entries, ritual(43, "token_of_jzahar", "jzaharCoin", INFUSION, 3, null, 500)
            .center(ac("coin")).result(acId("token_of_jzahar"))
            .layout(ac("transmutation_gem"), EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY));

        add(entries, charm(44, "cthulhu", "legacy/dye_cyan"));
        add(entries, charm(45, "hastur", "legacy/dye_yellow"));
        add(entries, charm(46, "jzahar", "legacy/dye_gray"));
        add(entries, charm(47, "azathoth", "legacy/dye_purple"));
        add(entries, charm(48, "nyarlathotep", "legacy/dye_blue"));
        add(entries, charm(49, "yog_sothoth", "legacy/dye_orange"));
        add(entries, charm(50, "shub_niggurath", "legacy/dye_black"));

        add(entries, ritual(51, "energy_container", "energyContainer", INFUSION, 0, null, 100)
            .center(ac("energypedestal")).result(acId("energycontainer"))
            .copyCenterData("PotEnergy")
            .layout(ac("energycollector"), ac("shadow_shard"), ac("energycollector"), ac("shadow_shard"),
                ac("energycollector"), ac("shadow_shard"), ac("energycollector"), ac("shadow_shard")));
        add(entries, ritual(52, "overworld_ring", "overworld_ring", INFUSION, 0, null, 400)
            .center(ac("ring")).result(acId("ring_overworld"))
            .layout(ac("shadow_shard"), mc("cobblestone"), ac("coralium_gem"), mc("cobblestone"),
                ac("shadow_shard"), mc("cobblestone"), ac("coralium_gem"), mc("cobblestone")));
        add(entries, ritual(53, "abyssal_wasteland_ring", "abyssal_wasteland_ring", INFUSION, 1, null, 400)
            .center(acAny("ring", "ring_overworld")).result(acId("ring_abyssal_wasteland"))
            .layout(ac("shadow_shard"), ac("abyssal_cobblestone"), ac("coralium_gem"), ac("coralium_cobblestone"),
                ac("shadow_shard"), ac("abyssal_cobblestone"), ac("coralium_gem"), ac("coralium_cobblestone")));
        add(entries, ritual(54, "dreadlands_ring", "dreadlands_ring", INFUSION, 2, null, 400)
            .center(acAny("ring", "ring_overworld", "ring_abyssal_wasteland")).result(acId("ring_dreadlands"))
            .layout(ac("shadow_shard"), ac("dreadstone_cobblestone"), ac("coralium_gem"), ac("elysian_cobblestone"),
                ac("shadow_shard"), ac("dreadstone_cobblestone"), ac("coralium_gem"), ac("elysian_cobblestone")));
        add(entries, ritual(55, "omothol_ring", "omothol_ring", INFUSION, 3, null, 400)
            .center(acAny("ring", "ring_overworld", "ring_abyssal_wasteland", "ring_dreadlands"))
            .result(acId("ring_omothol"))
            .layout(ac("shadow_shard"), ac("ethaxium_brick"), ac("coralium_gem"), ac("dark_ethaxium_brick"),
                ac("shadow_shard"), ac("ethaxium_brick"), ac("coralium_gem"), ac("dark_ethaxium_brick")));

        Set<String> rendingData = Set.of("energyShadow", "energyAbyssal", "energyDread", "energyOmothol", "ench");
        add(entries, ritual(56, "abyssal_wasteland_staff_of_rending", "sorAWupgrade", INFUSION, 1,
                ACDimensions.ABYSSAL_WASTELAND, 1000)
            .center(ac("staff_of_rending")).result(acId("abyssal_wasteland_staff_of_rending"))
            .copyCenterData(rendingData)
            .layout(ac("shadow_gem"), ac("abyssal_stone"), ac("coralium_plagued_flesh"), ac("abyssal_stone"),
                ac("coralium_plagued_flesh"), ac("abyssal_stone"), ac("coralium_plagued_flesh"), ac("abyssal_stone")));
        add(entries, ritual(57, "dreadlands_staff_of_rending", "sorDLupgrade", INFUSION, 2,
                ACDimensions.DREADLANDS, 2000)
            .center(ac("abyssal_wasteland_staff_of_rending")).result(acId("dreadlands_staff_of_rending"))
            .copyCenterData(rendingData)
            .layout(ac("shadow_gem"), ac("dreadstone"), ac("dread_fragment"), ac("dreadstone"),
                ac("dread_fragment"), ac("dreadstone"), ac("dread_fragment"), ac("dreadstone")));
        add(entries, ritual(58, "omothol_staff_of_rending", "sorOMTupgrade", INFUSION, 3,
                ACDimensions.OMOTHOL, 3000)
            .center(ac("dreadlands_staff_of_rending")).result(acId("omothol_staff_of_rending"))
            .copyCenterData(rendingData)
            .layout(ac("shadow_gem"), ac("omothol_stone"), ac("omothol_ghoul_flesh"), ac("omothol_stone"),
                ac("omothol_ghoul_flesh"), ac("omothol_stone"), ac("omothol_ghoul_flesh"), ac("omothol_stone")));
        add(entries, ritual(59, "house", "house", HOUSE, 0, null, 0)
            .hidden().center(acAny("minecraft:oak_door", "minecraft:spruce_door", "minecraft:birch_door",
                "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door"))
            .targets(acId("house"))
            .layout(tag("minecraft:planks"), tag("minecraft:wooden_stairs"), tag("minecraft:planks"),
                tag("minecraft:planks"), tag("minecraft:planks"), tag("minecraft:planks"),
                tag("minecraft:planks"), tag("minecraft:wooden_stairs")));

        add(entries, ritual(60, "basic_scroll", "basicScroll", CREATION, 0, null, 100)
            .result(acId("basic_scroll"))
            .layout(mc("book"), EMPTY, mc("book"), EMPTY, mc("book"), EMPTY, mc("book"), EMPTY));
        add(entries, ritual(61, "lesser_scroll", "lesserScroll", CREATION, 1, null, 1000)
            .result(acId("lesser_scroll"))
            .layout(mc("book"), EMPTY, ac("wastelands_thorn"), EMPTY,
                mc("book"), EMPTY, ac("luminous_thistle"), EMPTY));
        add(entries, ritual(62, "moderate_scroll", "moderateScroll", CREATION, 2, null, 2000)
            .result(acId("moderate_scroll"))
            .layout(mc("book"), EMPTY, ac("dread_fragment"), EMPTY,
                mc("book"), EMPTY, ac("dreaded_shard_of_abyssalnite"), EMPTY));

        return List.copyOf(entries);
    }

    private static Definition depths(int order, String id, String legacyId, String center) {
        return ritual(order, id, legacyId, INFUSION, 1, ACDimensions.ABYSSAL_WASTELAND, 300)
            .strictOfferings().center(ac(center)).result(acId(id))
            .layout(ac("coralium_gem_cluster_9"), ac("coralium_gem_cluster_9"), ac("liquid_coralium_bucket"),
                mc("vine"), mc("lily_pad"), ac("transmutation_gem"), ac("coralium_plagued_flesh"), EMPTY);
    }

    private static Definition potion(int order, String id, String legacyId, int bookType,
                                     String reagent, String effect) {
        return ritual(order, id, legacyId, POTION_AOE, bookType, null, 300)
            .targets(acId(effect))
            .layout(ac(reagent), mc("potion"), ac(reagent), mc("potion"),
                ac(reagent), mc("potion"), ac(reagent), mc("gunpowder"));
    }

    private static Definition statue(int order, String deity, RitualIngredient... firstFive) {
        String legacy = switch (deity) {
            case "yog_sothoth" -> "yogsothothStatue";
            case "shub_niggurath" -> "shubniggurathStatue";
            default -> deity + "Statue";
        };
        RitualIngredient[] layout = new RitualIngredient[8];
        System.arraycopy(firstFive, 0, layout, 0, 5);
        layout[5] = ac("abyssal_wasteland_essence");
        layout[6] = ac("dreadlands_essence");
        layout[7] = ac("omothol_essence");
        return ritual(order, deity + "_statue", legacy, INFUSION, 4, null, 20000)
            .livingSacrifice().center(ac("monolith_stone")).result(acId(deity + "_statue"))
            .layout(layout);
    }

    private static Definition charm(int order, String deity, String dyeTag) {
        String compact = deity.replace("_", "");
        String legacy = compact + "Charm";
        return ritual(order, deity + "_charm", legacy, INFUSION, 4, null, 2000)
            .center(ac("charm")).result(acId(compact + "charm"))
            .layout(repeat(tag("abyssalcraft:" + dyeTag), 8));
    }

    private static RitualIngredient crystal(String element) {
        return acAny("crystal_shard_" + element, "crystal_" + element, element + "_crystal_cluster");
    }

    private static Definition ritual(int order, String id, String legacyId, RitualManifest.Kind kind,
                                     int bookType, ResourceKey<Level> dimension, float energy) {
        return new Definition(order, id, legacyId, kind, bookType, dimension, energy);
    }

    private static void add(List<RitualManifest> entries, Definition definition) {
        entries.add(definition.build());
    }

    private static RitualIngredient ac(String id) {
        return RitualIngredient.item(id.contains(":") ? id : "abyssalcraft:" + id);
    }

    private static RitualIngredient mc(String id) {
        return RitualIngredient.item("minecraft:" + id);
    }

    private static RitualIngredient acAny(String... ids) {
        return RitualIngredient.anyOf(Arrays.stream(ids)
            .map(id -> id.contains(":") ? id : "abyssalcraft:" + id)
            .toArray(String[]::new));
    }

    private static RitualIngredient tag(String id) {
        return RitualIngredient.tag(id);
    }

    private static RitualIngredient[] repeat(RitualIngredient ingredient, int count) {
        RitualIngredient[] result = new RitualIngredient[count];
        Arrays.fill(result, ingredient);
        return result;
    }

    private static ResourceLocation acId(String id) {
        return ACRef.id(id);
    }

    private static Map<String, RitualManifest> index() {
        Map<String, RitualManifest> index = new LinkedHashMap<>();
        for (RitualManifest manifest : ENTRIES) index.put(manifest.id(), manifest);
        return Map.copyOf(index);
    }

    private static final class Definition {

        private final int order;
        private final String id;
        private final String legacyId;
        private final RitualManifest.Kind kind;
        private final int bookType;
        private final ResourceKey<Level> dimension;
        private final float energy;
        private boolean livingSacrifice;
        private RitualIngredient center = EMPTY;
        private List<RitualIngredient> layout = List.of();
        private ResourceLocation result;
        private List<ResourceLocation> targets = List.of();
        private boolean strictOfferings;
        private boolean strictCenterData;
        private boolean copyCenterData;
        private Set<String> copiedDataKeys = Set.of();
        private ResourceLocation research;
        private boolean hidden;

        private Definition(int order, String id, String legacyId, RitualManifest.Kind kind, int bookType,
                           ResourceKey<Level> dimension, float energy) {
            this.order = order;
            this.id = id;
            this.legacyId = legacyId;
            this.kind = kind;
            this.bookType = bookType;
            this.dimension = dimension;
            this.energy = energy;
        }

        private Definition livingSacrifice() {
            livingSacrifice = true;
            return this;
        }

        private Definition center(RitualIngredient ingredient) {
            center = ingredient;
            return this;
        }

        private Definition result(ResourceLocation id) {
            result = id;
            return this;
        }

        private Definition targets(ResourceLocation... ids) {
            targets = List.of(ids);
            return this;
        }

        private Definition strictOfferings() {
            strictOfferings = true;
            return this;
        }

        @SuppressWarnings("unused")
        private Definition strictCenterData() {
            strictCenterData = true;
            return this;
        }

        private Definition copyCenterData(String... keys) {
            return copyCenterData(Set.of(keys));
        }

        private Definition copyCenterData(Set<String> keys) {
            copyCenterData = true;
            copiedDataKeys = Set.copyOf(keys);
            return this;
        }

        @SuppressWarnings("unused")
        private Definition research(ResourceLocation id) {
            research = id;
            return this;
        }

        private Definition hidden() {
            hidden = true;
            return this;
        }

        private Definition layout(RitualIngredient... ingredients) {
            layout = List.of(ingredients);
            return this;
        }

        private RitualManifest build() {
            return new RitualManifest(order, id, legacyId, kind, bookType, dimension, energy,
                livingSacrifice, center, layout, result, targets, strictOfferings, strictCenterData,
                copyCenterData, copiedDataKeys, research, hidden);
        }
    }
}
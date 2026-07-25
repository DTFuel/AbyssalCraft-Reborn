package com.shinoow.abyssalcraft.content.entity.legacy;

import java.util.Set;
import java.util.TreeSet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;

public final class EntityCatalogInvariant {

    private static final Set<String> CONTENT_ENTITIES = Set.of(
        "antizombie", "antiabyssalzombie", "anticreeper", "antiskeleton", "antispider", "antighoul",
        "antiplayer", "anticow", "antipig", "antichicken", "antibat",
        "demon_chicken", "demon_cow", "demon_pig", "demon_sheep",
        "evil_chicken", "evil_cow", "evil_pig", "evil_sheep",
        "ghoul", "depths_ghoul", "dreaded_ghoul", "omothol_ghoul", "shadow_ghoul",
        "lesser_shoggoth", "shoggoth", "greater_shoggoth",
        "abyssalzombie", "coraliumsquid", "dreadling", "dreadspawn", "greaterdreadspawn",
        "lesserdreadbeast", "shadowcreature", "shadowmonster", "shadowbeast",
        "chagaroth", "jzahar", "shadowboss", "dragonboss", "dreadguard", "gskeleton", "remnant",
        "shuboffspring", "jzaharminion", "chagarothfist", "chagarothspawn", "dragonminion",
        "acidprojectile", "dreadslug", "inkprojectile", "coraliumarrow", "dreadedcharge",
        "blackhole", "implosion", "primedodb", "primedodbcore", "compasstentacle", "powerstonetracker",
        "portal", "singleportal", "spirititem", "gatekeeperessence"
    );

    private static final Set<String> SPAWN_EGGS = Set.of(
        "antizombie_spawn_egg", "antiabyssalzombie_spawn_egg", "anticreeper_spawn_egg",
        "antiskeleton_spawn_egg", "antispider_spawn_egg", "antighoul_spawn_egg", "antiplayer_spawn_egg",
        "anticow_spawn_egg", "antipig_spawn_egg", "antichicken_spawn_egg", "antibat_spawn_egg",
        "demon_chicken_spawn_egg", "demon_cow_spawn_egg", "demon_pig_spawn_egg", "demon_sheep_spawn_egg",
        "evil_chicken_spawn_egg", "evil_cow_spawn_egg", "evil_pig_spawn_egg", "evil_sheep_spawn_egg",
        "ghoul_spawn_egg", "depths_ghoul_spawn_egg", "dreaded_ghoul_spawn_egg",
        "omothol_ghoul_spawn_egg", "shadow_ghoul_spawn_egg",
        "lesser_shoggoth_spawn_egg", "shoggoth_spawn_egg", "greater_shoggoth_spawn_egg",
        "abyssalzombie_spawn_egg", "coraliumsquid_spawn_egg", "dreadling_spawn_egg", "dreadspawn_spawn_egg",
        "greaterdreadspawn_spawn_egg", "lesserdreadbeast_spawn_egg", "shadowcreature_spawn_egg",
        "shadowmonster_spawn_egg", "shadowbeast_spawn_egg",
        "chagaroth_spawn_egg", "jzahar_spawn_egg", "shadowboss_spawn_egg", "dragonboss_spawn_egg",
        "dreadguard_spawn_egg", "gskeleton_spawn_egg", "remnant_spawn_egg", "shuboffspring_spawn_egg",
        "jzaharminion_spawn_egg", "chagarothfist_spawn_egg", "chagarothspawn_spawn_egg",
        "dragonminion_spawn_egg"
    );

    private static final Set<String> PLACEMENTS = Set.of(
        "abyssalzombie", "antiabyssalzombie", "antibat", "antichicken", "anticow", "anticreeper",
        "antighoul", "antipig", "antiplayer", "antiskeleton", "antispider", "antizombie",
        "chagarothfist", "chagarothspawn", "coraliumsquid",
        "demon_chicken", "demon_cow", "demon_pig", "demon_sheep",
        "depths_ghoul", "dreaded_ghoul", "dreadguard", "dreadling", "dreadspawn", "dragonminion",
        "evil_chicken", "evil_cow", "evil_pig", "evil_sheep", "ghoul", "greater_shoggoth",
        "greaterdreadspawn", "gskeleton", "jzaharminion", "lesser_shoggoth", "lesserdreadbeast",
        "omothol_ghoul", "remnant", "shadow_ghoul", "shadowbeast", "shadowcreature", "shadowmonster",
        "shoggoth", "shuboffspring"
    );

    private EntityCatalogInvariant() {}

    public static String validate() {
        Set<String> allEntities = entityIds();
        Set<String> contentEntities = new TreeSet<>(allEntities);
        contentEntities.remove("pilot_mob");
        requireExact("content entities", CONTENT_ENTITIES, contentEntities);

        Set<String> expectedAll = new TreeSet<>(CONTENT_ENTITIES);
        expectedAll.add("pilot_mob");
        requireExact("all AC entities", expectedAll, allEntities);
        requireExact("spawn eggs", SPAWN_EGGS, itemIds("_spawn_egg"));
        requireExact("placements", PLACEMENTS, SpawnPlacementCompat.registeredIds());

        String result = "RR_ENTITY_CATALOG_OK content=63 all_ac=64 eggs=48 placements=44";
        System.out.println(result);
        return result;
    }

    private static Set<String> entityIds() {
        Set<String> ids = new TreeSet<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (AbyssalCraft.MODID.equals(id.getNamespace())) ids.add(id.getPath());
        }
        return ids;
    }

    private static Set<String> itemIds(String suffix) {
        Set<String> ids = new TreeSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (AbyssalCraft.MODID.equals(id.getNamespace()) && id.getPath().endsWith(suffix)) ids.add(id.getPath());
        }
        return ids;
    }

    private static void requireExact(String label, Set<String> expected, Set<String> actual) {
        if (expected.equals(actual)) return;
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        Set<String> unexpected = new TreeSet<>(actual);
        unexpected.removeAll(expected);
        throw new IllegalStateException(label + " mismatch: missing=" + missing + ", unexpected=" + unexpected);
    }
}

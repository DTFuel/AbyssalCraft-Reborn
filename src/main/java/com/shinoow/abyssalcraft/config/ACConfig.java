package com.shinoow.abyssalcraft.config;

import java.util.function.Supplier;
import java.util.List;

import com.shinoow.abyssalcraft.platform.ConfigCompat;

/**
 * AbyssalCraft config skeleton (modern replacement for the 1.12.2 {@code lib/ACConfig}).
 *
 * <p>M0 skeleton only: a representative {@code COMMON} spec with a few keys per category, built
 * entirely through {@link ConfigCompat} so no loader-forked config API leaks into business code.
 * The full option set (and any {@code CLIENT} spec) is filled in later by PJ-2 (T8.2). Every option
 * is a plain {@link Supplier} - read the live value with {@code .get()}.
 *
 * <p>Wiring: {@link #register()} is invoked once during mod construction (see {@code AbyssalCraft}).
 */
public final class ACConfig {

    private ACConfig() {}

    // ---- general ----
    public static Supplier<Boolean> shouldSpread;
    public static Supplier<Boolean> shouldInfect;
    public static Supplier<Boolean> destroyOcean;
    public static Supplier<Boolean> hardcoreMode;
    public static Supplier<Boolean> antiItemDisintegration;
    public static Supplier<Boolean> smeltingRecipes;
    public static Supplier<Boolean> purgeMobSpawns;
    public static Supplier<Double> damageAmpl;
    public static Supplier<Boolean> mimicFire;
    public static Supplier<Boolean> armorPotionEffects;
    public static Supplier<Boolean> syncDataOnBookOpening;
    public static Supplier<Boolean> portalSpawnsNearPlayer;
    public static Supplier<Boolean> showBossDialogs;
    public static Supplier<Integer> knowledgeSyncDelay;
    public static Supplier<Boolean> lootTableContent;
    public static Supplier<Boolean> nightVisionEverywhere;
    public static Supplier<Boolean> no_potion_clouds;
    public static Supplier<Boolean> vanilla_handling;

    // ---- dimensions ----
    public static Supplier<Boolean> keepLoaded1;
    public static Supplier<Boolean> keepLoaded2;
    public static Supplier<Boolean> keepLoaded3;
    public static Supplier<Boolean> keepLoaded4;
    public static Supplier<Integer> portalCooldown;
    public static Supplier<Integer> startDimension;

    // ---- mobs ----
    public static Supplier<Boolean> demonAnimalFire;
    public static Supplier<Integer> evilAnimalSpawnWeight;
    public static Supplier<Integer> demonAnimalSpawnWeight;
    public static Supplier<Boolean> depthsGhoulBiomeDictSpawn;
    public static Supplier<Boolean> abyssalZombieBiomeDictSpawn;
    public static Supplier<Integer> darkOffspringSpawnWeight;
    public static Supplier<Boolean> demonAnimalsSpawnOnDeath;
    public static Supplier<Boolean> evilAnimalNewMoonSpawning;
    public static Supplier<Boolean> antiPlayersPickupLoot;
    public static Supplier<Integer> dreadSpawnSpawnLimit;
    public static Supplier<Integer> greaterDreadSpawnSpawnLimit;
    public static Supplier<Integer> jzaharHealingPace;
    public static Supplier<Integer> jzaharHealingAmount;
    public static Supplier<Integer> chagarothHealingPace;
    public static Supplier<Integer> chagarothHealingAmount;
    public static Supplier<Integer> sacthothHealingPace;
    public static Supplier<Integer> sacthothHealingAmount;

    // ---- client ----
    public static Supplier<Boolean> particleBlock;
    public static Supplier<Boolean> particleEntity;
    public static Supplier<Boolean> darkRealmSmokeParticles;
    public static Supplier<Double> depthsHelmetOverlayOpacity;

    // ---- rituals ----
    public static Supplier<Integer> corruptionRitualRange;
    public static Supplier<Integer> cleansingRitualRange;
    public static Supplier<Integer> purgingRitualRange;
    public static Supplier<Integer> enchantmentMaxLevel;
    public static Supplier<Boolean> enchantBooks;
    public static Supplier<Integer> curingRitualRange;
    public static Supplier<Integer> infestingRitualRange;
    public static Supplier<Boolean> enchantMergedBooks;

    // ---- shoggoth ----
    public static Supplier<Boolean> shoggothOoze;
    public static Supplier<Boolean> oozeExpire;
    public static Supplier<Boolean> consumeItems;
    public static Supplier<Boolean> shieldsBlockAcid;
    public static Supplier<Double> acidResistanceHardness;
    public static Supplier<Integer> acidSpitFrequency;
    public static Supplier<Integer> monolithBuildingCooldown;
    public static Supplier<Boolean> shoggothGlowingEyes;
    public static Supplier<Integer> biomassPlayerDistance;
    public static Supplier<Integer> biomassMaxSpawn;
    public static Supplier<Integer> biomassCooldown;
    public static Supplier<Integer> biomassShoggothDistance;

    // ---- worldgen ----
        public static Supplier<Integer> darklandsRegionWeight;
        public static Supplier<Integer> worldgenConfigMigrationVersion;
    public static Supplier<Boolean> generateDarklandsStructures;
    public static Supplier<Boolean> generateShoggothLairs;
    public static Supplier<Boolean> generateAbyssalWastelandPillars;
    public static Supplier<Boolean> generateAbyssalWastelandRuins;
    public static Supplier<Boolean> generateAntimatterLake;
    public static Supplier<Boolean> generateCoraliumLake;
    public static Supplier<Boolean> generateDreadlandsStalagmite;
    public static Supplier<Boolean> generateStatuesInLairs;
    public static Supplier<Boolean> generateGraveyards;
    public static Supplier<Boolean> generateOmotholStructures;
    public static Supplier<Boolean> useAmplifiedWorldType;
    public static Supplier<Boolean> generateCoraliumOre;
    public static Supplier<Boolean> generateNitreOre;
    public static Supplier<Boolean> generateAbyssalniteOre;
    public static Supplier<Boolean> generateAbyssalCoraliumOre;
    public static Supplier<Boolean> generateDreadlandsAbyssalniteOre;
    public static Supplier<Boolean> generateDreadedAbyssalniteOre;
    public static Supplier<Boolean> generateAbyssalIronOre;
    public static Supplier<Boolean> generateAbyssalGoldOre;
    public static Supplier<Boolean> generateAbyssalDiamondOre;
    public static Supplier<Boolean> generateAbyssalNitreOre;
    public static Supplier<Boolean> generatePearlescentCoraliumOre;
    public static Supplier<Boolean> generateLiquifiedCoraliumOre;
    public static Supplier<Integer> shoggothLairSpawnRate;
    public static Supplier<Integer> shoggothLairSpawnRateRivers;
    public static Supplier<Integer> shoggothLairGenerationDistance;
    public static Supplier<Integer> darkShrineSpawnRate;
    public static Supplier<Integer> darkRitualGroundsSpawnRate;
    public static Supplier<Integer> graveyardGenerationDistance;
    public static Supplier<Integer> graveyardGenerationChance;

    // ---- silly_settings ----
    public static Supplier<Boolean> breakLogic;
    public static Supplier<Boolean> nuclearAntimatterExplosions;
    public static Supplier<Boolean> jzaharBreaksFourthWall;
    public static Supplier<Integer> odbExplosionSize;
    public static Supplier<Integer> antimatterExplosionSize;

    // ---- wet_noodle ----
    public static Supplier<Boolean> no_dreadlands_spread;
    public static Supplier<Boolean> no_acid_breaking_blocks;
    public static Supplier<Boolean> no_spectral_dragons;
    public static Supplier<Boolean> no_projectile_damage_immunity;
    public static Supplier<Boolean> no_disruptions;
    public static Supplier<Boolean> no_black_holes;
    public static Supplier<Boolean> no_odb_explosions;

    // ---- mod_compat ----
    public static Supplier<Boolean> hcdarkness_aw;
    public static Supplier<Boolean> hcdarkness_dl;
    public static Supplier<Boolean> hcdarkness_omt;
    public static Supplier<Boolean> hcdarkness_dr;

    // ---- spells ----
    public static Supplier<Boolean> entropy_spell;
    public static Supplier<Boolean> life_drain_spell;
    public static Supplier<Boolean> mining_spell;
    public static Supplier<Boolean> grasp_of_cthulhu_spell;
    public static Supplier<Boolean> invisibility_spell;
    public static Supplier<Boolean> detachment_spell;
    public static Supplier<Boolean> steal_vigor_spell;
    public static Supplier<Boolean> sirens_song_spell;
    public static Supplier<Boolean> undeath_to_dust_spell;
    public static Supplier<Boolean> ooze_removal_spell;
    public static Supplier<Boolean> teleport_hostile_spell;
    public static Supplier<Boolean> floating_spell;
    public static Supplier<Boolean> teleport_home_spell;
    public static Supplier<Boolean> compass_spell;

    // ---- modules ----
    public static Supplier<Boolean> spirit_items;

    // ---- ghoul ----
    public static Supplier<Boolean> ghouls_burn;
    public static Supplier<Integer> tombstoneMaxSpawn;
    public static Supplier<Integer> tombstoneCooldown;
    public static Supplier<Integer> tombstoneGhoulDistance;

        // ---- non-scalar legacy configuration ----
        public static Supplier<List<? extends Integer>> startDimensionColors;
        public static Supplier<List<? extends String>> interdimensionalCageBlacklist;
        public static Supplier<List<? extends String>> itemTransportBlacklist;
        public static Supplier<List<? extends String>> mobItemPickupBlacklist;
        public static Supplier<List<? extends String>> dreadPlagueImmunityList;
        public static Supplier<List<? extends String>> dreadPlagueCarrierList;
        public static Supplier<List<? extends String>> coraliumPlagueImmunityList;
        public static Supplier<List<? extends String>> coraliumPlagueCarrierList;
        public static Supplier<List<? extends String>> demonAnimalTransformations;
        public static Supplier<List<? extends String>> dimensionBookTypeMappings;
        public static Supplier<List<? extends String>> blackHoleDimensionBlacklist;
        public static Supplier<List<? extends String>> oreGenerationDimensionBlacklist;
        public static Supplier<List<? extends String>> structureGenerationDimensionBlacklist;
        public static Supplier<List<? extends Integer>> coraliumOreGeneration;

    private static ConfigCompat.Built common;
    private static ConfigCompat.Built client;

    /** Build and register the COMMON and CLIENT configs. Call exactly once, during mod construction. */
    public static void register() {

        // ---- CLIENT spec: client-render options only ----
        ConfigCompat.Builder cb = ConfigCompat.builder();

        cb.push("client");
        particleBlock = cb.comment("Toggles whether blocks that emits particles should do so.")
                .defineBool("particle_block", true);
        particleEntity = cb.comment("Toggles whether entities that emits particles should do so.")
                .defineBool("particle_entity", true);
        darkRealmSmokeParticles = cb.comment("Toggles whether or not non-shadow entities will emit smoke particles inside the Dark Realm.")
                .defineBool("dark_realm_smoke_particles", true);
        depthsHelmetOverlayOpacity = cb.comment("Sets the opacity for the overlay shown when wearing the Visage of The Depths, reducing the value increases the transparency on the texture. Client Side only!\n[range: 0.5 ~ 1.0, default: 1.0]")
                .defineDouble("depths_helmet_overlay_opacity", 1.0D, 0.5D, 1.0D);
        cb.pop();

        client = cb.build();

        // ---- COMMON spec: everything else ----
        ConfigCompat.Builder b = ConfigCompat.builder();

        b.push("general");
        shouldSpread = b.comment("Set true for the Liquid Coralium to convert other liquids into itself and transmute blocks into their Abyssal Wasteland counterparts outside of the Abyssal Wasteland.")
                .defineBool("should_spread", true);
        shouldInfect = b.comment("Set true to allow the Coralium Plague to spread outside The Abyssal Wasteland.")
                .defineBool("should_infect", false);
        destroyOcean = b.comment("Set true to allow the Liquid Coralium to spread across oceans. WARNING: The game can crash from this.")
                .defineBool("destroy_ocean", false);
        hardcoreMode = b.comment("Toggles Hardcore mode. If set to true, all mobs (in the mod) will become tougher.")
                .defineBool("hardcore_mode", false);
        antiItemDisintegration = b.comment("Toggles whether or not Liquid Antimatter will disintegrate any items dropped into a pool of it.")
                .defineBool("anti_item_disintegration", true);
        smeltingRecipes = b.comment("Deprecated compatibility key. Armor recycling is now controlled by datapack recipes.")
                .defineBool("smelting_recipes", true);
        purgeMobSpawns = b.comment("Toggles whether or not to clear and repopulate the monster spawn list of all dimension biomes to ensure no mob from another mod got in there.")
                .defineBool("purge_mob_spawns", false);
        damageAmpl = b.comment("When Hardcore Mode is enabled, you can use this to amplify the armor-piercing damage mobs deal.\n[range: 1.0 ~ 10.0, default: 1.0]")
                .defineDouble("damage_ampl", 1.0D, 1.0D, 10.0D);
        mimicFire = b.comment("Toggles whether or not Demon Animals will spread Mimic Fire instead of regular Fire (regular Fire can affect performance)")
                .defineBool("mimic_fire", true);
        armorPotionEffects = b.comment("Toggles any interactions where armor sets either give certain Potion Effects, or dispell others. Useful if you have another mod installed that provides similar customization to any armor set.")
                .defineBool("armor_potion_effects", true);
        syncDataOnBookOpening = b.comment("Toggles whether or not the Necronomicon knowledge will sync from the server to the client each time a player opens their Necronomicon.")
                .defineBool("sync_data_on_book_opening", true);
        portalSpawnsNearPlayer = b.comment("Toggles whether or not portals require a player to be nearby in order for it to rarely spawn mobs. If this option is disabled they follow the same principle as Nether portals.")
                .defineBool("portal_spawns_near_player", true);
        showBossDialogs = b.comment("Toggles whether or not boss dialogs are displayed at any point during their fights (when they spawn, when they die,  etc)")
                .defineBool("show_boss_dialogs", true);
        knowledgeSyncDelay = b.comment("Delay in ticks until Knowledge is synced to the client upon changing dimensions. Higher numbers mean you might see item names re-locked for a few seconds when changing dimension, but might reduce load time for the dimension by a little (useful in larger modpacks).\n[range: 20 ~ 400, default: 60]")
                .defineInt("knowledge_sync_delay", 60, 20, 400);
        lootTableContent = b.comment("Toggles whether or not AbyssalCraft Items should be inserted into vanilla loot tables (dungeons, strongholds etc).")
                .defineBool("loot_table_content", true);
        nightVisionEverywhere = b.comment("Toggles whether or not the Night Vision buff from the Plated Coralium Helmet should be applied in all dimensions, rather than only Surface Worlds.")
                .defineBool("night_vision_everywhere", true);
        no_potion_clouds = b.comment("Toggles whether or not victims dying to the Coralium Plague and Dread Plague create potion clouds on death (can save performance if disabled).")
                .defineBool("no_potion_clouds", false);
        vanilla_handling = b.comment("Toggles if the Gateway Key should support creating portals to The Nether and The End along with performing rituals there.\n[Changes take effect after a Minecraft restart]")
                .defineBool("vanilla_handling", true);
        b.pop();

        b.push("dimensions");
        keepLoaded1 = b.comment("Set true to prevent The Abyssal Wasteland from automatically unloading (might affect performance)")
                .defineBool("keep_loaded1", false);
        keepLoaded2 = b.comment("Set true to prevent The Dreadlands from automatically unloading (might affect performance)")
                .defineBool("keep_loaded2", false);
        keepLoaded3 = b.comment("Set true to prevent Omothol from automatically unloading (might affect performance)")
                .defineBool("keep_loaded3", false);
        keepLoaded4 = b.comment("Set true to prevent The Dark Realm from automatically unloading (might affect performance)")
                .defineBool("keep_loaded4", false);
        portalCooldown = b.comment("Cooldown after using a portal, increasing the value increases the delay until you can teleport again. Measured in ticks (20 ticks = 1 second).\n[range: 10 ~ 300, default: 200]")
                .defineInt("portal_cooldown", 200, 10, 300);
        startDimension = b.comment("The dimension ID of the dimension where you make the portal to the Abyssal Wastelands.")
                .defineInt("start_dimension", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        b.pop();

        b.push("mobs");
        demonAnimalFire = b.comment("Set to false to prevent Demon Animals (Pigs, Cows, Chickens) from burning in the overworld.")
                .defineBool("demon_animal_fire", false);
        evilAnimalSpawnWeight = b.comment("Spawn weight for the Evil Animals (Pigs, Cows, Chickens), keep under 35 to avoid complete annihilation.\n[range: 0 ~ 100, default: 20]")
                .defineInt("evil_animal_spawn_weight", 15, 0, 100);
        demonAnimalSpawnWeight = b.comment("Spawn weight for the Demon Animals (Pigs, Cows, Chickens) spawning in the Nether.\n[range: 0 ~ 100, default: 15]")
                .defineInt("demon_animal_spawn_weight", 15, 0, 100);
        depthsGhoulBiomeDictSpawn = b.comment("Toggles whether or not Depths Ghouls should use the Biome Dictionary for finding biomes to spawn in (which might lead to them spawning in dimensions from other mods).")
                .defineBool("depths_ghoul_biome_dict_spawn", true);
        abyssalZombieBiomeDictSpawn = b.comment("Toggles whether or not Abyssal Zombies should use the Biome Dictionary for finding biomes to spawn in (which might lead to them spawning in dimensions from other mods).")
                .defineBool("abyssal_zombie_biome_dict_spawn", true);
        darkOffspringSpawnWeight = b.comment("Spawn weight for Dark Offspring. They spawn twice as likely in Darklands Forests and Roofed Forests.\n[range: 0 ~ 50, default: 5]")
                .defineInt("dark_offspring_spawn_weight", 5, 0, 50);
        demonAnimalsSpawnOnDeath = b.comment("Toggles whether or not an Evil Animal spawns a Demon Animal on death.")
                .defineBool("demon_animals_spawn_on_death", true);
        evilAnimalNewMoonSpawning = b.comment("Toggles whether or not Evil Animals only spawn at night during a new moon.")
                .defineBool("evil_animal_new_moon_spawning", true);
        antiPlayersPickupLoot = b.comment("Toggles whether or not Anti-Players can pick up loot. You really should just blacklist them in whatever mob spawner/duplicator instead.")
                .defineBool("anti_players_pickup_loot", true);
        dreadSpawnSpawnLimit = b.comment("Spawn limit on how many Dread Spawns can be spawned by other mobs capable of spawning them. The spawn limit for Cha'garoth is half of this.")
                .defineInt("dread_spawn_spawn_limit", 20, 0, 50);
        greaterDreadSpawnSpawnLimit = b.comment("Spawn limit on how many Greater Dread Spawns can be spawned by other mobs capable of spawning them. The spawn limit for Cha'garoth is half of this.")
                .defineInt("greater_dread_spawn_spawn_limit", 10, 0, 50);
        jzaharHealingPace = b.comment("The pace at which J'zahar regenerates health (in ticks)\n[range: 20 ~ 1200, default: 200]")
                .defineInt("jzahar_healing_pace", 200, 20, 1200);
        jzaharHealingAmount = b.comment("The amount of HP J'zahar heals when he regenerates health (set to 0 to disable healing)\n[range: 0 ~ 100, default: 1]")
                .defineInt("jzahar_healing_amount", 1, 0, 100);
        chagarothHealingPace = b.comment("The pace at which Cha'garoth regenerates health (in ticks)\n[range: 20 ~ 1200, default: 200]")
                .defineInt("chagaroth_healing_pace", 200, 20, 1200);
        chagarothHealingAmount = b.comment("The amount of HP Cha'garoth heals when he regenerates health (setto 0 to disable healing)\n[range: 0 ~ 100, default: 1]")
                .defineInt("chagaroth_healing_amount", 1, 0, 100);
        sacthothHealingPace = b.comment("The pace at which Sacthoth regenerates health (in ticks)\n[range: 20 ~ 1200, default: 200]")
                .defineInt("sacthoth_healing_pace", 200, 20, 1200);
        sacthothHealingAmount = b.comment("The amount of HP Sacthoth heals when he regenerates health (setto 0 to disable healing)\n[range: 0 ~ 100, default: 1]")
                .defineInt("sacthoth_healing_amount", 1, 0, 100);
        b.pop();

        b.push("rituals");
        corruptionRitualRange = b.comment("The range (in chunks) that will be affected by the Ritual of Corruption (on the x and z axis)\n[range: 3 ~ 100, default: 32]")
                .defineInt("corruption_ritual_range", 32, 3, 100);
        cleansingRitualRange = b.comment("The range (in chunks) that will be affected by the Ritual of Cleansing (on the x and z axis)\n[range: 3 ~ 100, default: 32]")
                .defineInt("cleansing_ritual_range", 32, 3, 100);
        purgingRitualRange = b.comment("The range (in chunks) that will be affected by the Ritual of Purging (on the x and z axis)\n[range: 3 ~ 100, default: 32]")
                .defineInt("purging_ritual_range", 32, 3, 100);
        enchantmentMaxLevel = b.comment("The combined max level a single enchantment applied through the Mass Enchantment ritual can have. For example, if the max level is 10 and you apply 8 Sharpness 5 books, you'd end up with Sharpness 10 on the Item, rather than 40.\n[range: 1 ~ 100, default: 10]")
                .defineInt("enchantment_max_level", 10, 1, 100);
        enchantBooks = b.comment("Toggles whether or not Books can be enchanted through the Mass Enchantment ritual.")
                .defineBool("enchant_books", true);
        curingRitualRange = b.comment("The range (in chunks) that will be affected by the Ritual of Curing (on the x and z axis)\n[range: 3 ~ 100, default: 32]")
                .defineInt("curing_ritual_range", 32, 3, 100);
        infestingRitualRange = b.comment("The range (in chunks) that will be affected by the Ritual of Infesting (on the x and z axis)\n[range: 3 ~ 100, default: 32]")
                .defineInt("infesting_ritual_range", 32, 3, 100);
        enchantMergedBooks = b.comment("Toggles whether or not you can use Enchanted Books that have been merged on an Anvil in the Mass Enchantment ritual.")
                .defineBool("enchant_merged_books", true);
        b.pop();

        b.push("shoggoth");
        shoggothOoze = b.comment("Toggles whether or not Lesser Shoggoths should spread their ooze when walking around.")
                .defineBool("shoggoth_ooze", true);
        oozeExpire = b.comment("Toggles whether or not Shoggoth Ooze slowly reverts to dirt after constant light exposure. Ooze blocks that aren't full blocks will shrink instead.")
                .defineBool("ooze_expire", true);
        consumeItems = b.comment("Toggles whether or not Lesser Shoggoths will consume any dropped item they run into.")
                .defineBool("consume_items", false);
        shieldsBlockAcid = b.comment("Toggles whether or not Shields can block the acid projectiles spat by Lesser Shoggoths.")
                .defineBool("shields_block_acid", true);
        acidResistanceHardness = b.comment("The minimum Block Hardness required for a Block to not be destroyed by Shoggoth Acid (some blocks are unaffected regardless of their hardness)\n[range: 2.1 ~ 51.0, default: 3.0]")
                .defineDouble("acid_resistance_hardness", 3.0D, 2.1D, 51.0D);
        acidSpitFrequency = b.comment("The frequency (in ticks) at which a Lesser Shoggoth can spit acid. Higher values increase the time between each spit attack, while lower values descrease the time (and 0 disables it).\n[range: 0 ~ 300, default: 100]")
                .defineInt("acid_spit_frequency", 120, 0, 300);
        monolithBuildingCooldown = b.comment("The cooldown (in ticks) between each attempt by a Lesser Shoggoth to construct a monolith. Higher values increase the time, while lower values decrease it (and 0 disables it).\n[range: 0 ~ 2400, default: 1800]")
                .defineInt("monolith_building_cooldown", 1500, 0, 2400);
        shoggothGlowingEyes = b.comment("Toggles whether or not the eyes of Lesser Shoggoths should glow. The glowing can be heavy on performance, so if you're dropping FPS noticeably while looking at Lesser Shoggoths, consider turning this off. Client Side only!")
                .defineBool("shoggoth_glowing_eyes", true);
        biomassPlayerDistance = b.comment("Max distance a player has to be from a Biomass for it to trigger Shoggoth spawning. Lower means you have to be closer.\n[range: 5 ~ 48, default: 16]")
                .defineInt("biomass_player_distance", 16, 5, 48);
        biomassMaxSpawn = b.comment("The amount of nearby Shoggoths (within 32 blocks) at which the Biomass will halt spawning any new ones.\n[range: 1 ~ 10, default: 6]")
                .defineInt("biomass_max_spawn", 6, 1, 10);
        biomassCooldown = b.comment("The amount of time (in ticks) it takes between every attempt to spawn a Shogggoth.\n[range: 40 ~ 1200, default: 400]")
                .defineInt("biomass_cooldown", 400, 40, 1200);
        biomassShoggothDistance = b.comment("Max distance to check for nearby Shoggoths before spawning more.\n[range: 5 ~ 48, default: 32]")
                .defineInt("biomass_shoggoth_distance", 32, 5, 48);
        b.pop();

        b.push("worldgen");
        darklandsRegionWeight = b.comment("TerraBlender region weight for the Darklands and Coralium biomes in the Overworld. Higher values make these regions more common.\n[range: 1 ~ 20, default: 1]")
                .defineInt("darklands_region_weight", 1, 1, 20);
        worldgenConfigMigrationVersion = b.comment("Internal world generation configuration migration version.")
                .defineInt("config_migration_version", 0, 0, 1);
        generateDarklandsStructures = b.comment("Toggles whether or not to generate random Darklands structures.")
                .defineBool("generate_darklands_structures", true);
        generateShoggothLairs = b.comment("Toggles whether or not to generate Shoggoth Lairs (however, they will still generate in Omothol).")
                .defineBool("generate_shoggoth_lairs", true);
        generateAbyssalWastelandPillars = b.comment("Toggles whether or not to generate Tall Obsidian Pillars in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_wasteland_pillars", true);
        generateAbyssalWastelandRuins = b.comment("Toggles whether or not to generate small ruins in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_wasteland_ruins", true);
        generateAntimatterLake = b.comment("Toggles whether or not to generate Liquid Antimatter Lakes in Coralium Infested Swamps.")
                .defineBool("generate_antimatter_lake", true);
        generateCoraliumLake = b.comment("Toggles whether or not to generate Liquid Coralium Lakes in the Abyssal Wasteland.")
                .defineBool("generate_coralium_lake", true);
        generateDreadlandsStalagmite = b.comment("Toggles whether or not to generate Stalagmites in Dreadlands and Purified Dreadlands biomes.")
                .defineBool("generate_dreadlands_stalagmite", true);
        generateStatuesInLairs = b.comment("Toggles whether or not statues have a chance of generating inside a Shoggoth Lair.")
                .defineBool("generate_statues_in_lairs", true);
        generateGraveyards = b.comment("Toggles whether or not Graveyards should generate.")
                .defineBool("generate_graveyards", true);
        generateOmotholStructures = b.comment("Toggles whether or not to generate the 'Temple City of Omothol' (Except the Temple of J'zahar).")
                .defineBool("generate_omothol_structures", true);
        useAmplifiedWorldType = b.comment("Applies an amplified terrain offset to the AbyssalCraft dimensions.")
                .defineBool("use_amplified_world_type", false);
        generateCoraliumOre = b.comment("Toggles whether or not to generate Coralium Ore in the Overworld.")
                .defineBool("generate_coralium_ore", true);
        generateNitreOre = b.comment("Toggles whether or not to generate Nitre Ore in the Overworld.")
                .defineBool("generate_nitre_ore", true);
        generateAbyssalniteOre = b.comment("Toggles wheter or not to generate Abyssalnite Ore in Darklands Biomes.")
                .defineBool("generate_abyssalnite_ore", true);
        generateAbyssalCoraliumOre = b.comment("Toggles whether or not to generate Coralium Ore in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_coralium_ore", true);
        generateDreadlandsAbyssalniteOre = b.comment("Toggles whether or not to generate Abyssalnite Ore in the Dreadlands.")
                .defineBool("generate_dreadlands_abyssalnite_ore", true);
        generateDreadedAbyssalniteOre = b.comment("Toggles whether or not to generate Dreaded Abyssalnite Ore in the Dreadlands.")
                .defineBool("generate_dreaded_abyssalnite_ore", true);
        generateAbyssalIronOre = b.comment("Toggles whether or not to generate Iron Ore in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_iron_ore", true);
        generateAbyssalGoldOre = b.comment("Toggles whether or not to generate Gold Ore in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_gold_ore", true);
        generateAbyssalDiamondOre = b.comment("Toggles whether or not to generate Diamond Ore in the Abyssal Wasteland")
                .defineBool("generate_abyssal_diamond_ore", true);
        generateAbyssalNitreOre = b.comment("Toggles whether or not to generate Nitre Ore in the Abyssal Wasteland.")
                .defineBool("generate_abyssal_nitre_ore", true);
        generatePearlescentCoraliumOre = b.comment("Toggles whether or not to generate Pearlescent Coralium Ore in the Abyssal Wasteland.")
                .defineBool("generate_pearlescent_coralium_ore", true);
        generateLiquifiedCoraliumOre = b.comment("Toggles whether or not to generate Liquified Coralium Ore in the Abyssal Wasteland.")
                .defineBool("generate_liquified_coralium_ore", true);
        shoggothLairSpawnRate = b.comment("Target chunk density for Shoggoth Lairs in swamp biomes. Higher numbers increase spacing; 0 disables this branch. Combined with the minimum-distance setting.\n[range: 0 ~ 1000, default: 35]")
                .defineInt("shoggoth_lair_spawn_rate", 35, 0, 1000);
        shoggothLairSpawnRateRivers = b.comment("Target chunk density for Shoggoth Lairs in river biomes. Higher numbers increase spacing; 0 disables this branch. Combined with the minimum-distance setting.\n[range: 0 ~ 1000, default: 30]")
                .defineInt("shoggoth_lair_spawn_rate_rivers", 30, 0, 1000);
        shoggothLairGenerationDistance = b.comment("The minimum distance at which two Shoggoth Lairs will generate from each other.\n[range: 40 ~ 1000, default: 100]")
                .defineInt("shoggoth_lair_generation_distance", 100, 40, 1000);
        darkShrineSpawnRate = b.comment("Generation chance of a Dark Shrine in applicable biomes. Higher numbers decrease the chance of a Shrine generating, while lower numbers increase the chance.\n[range: 0 ~ 100, default: 10]")
                .defineInt("dark_shrine_spawn_rate", 10, 0, 100);
        darkRitualGroundsSpawnRate = b.comment("Generation chance of Dark Ritual Grounds in applicable biomes. Higher numbers decrease the chance of a Shrine generating, while lower numbers increase the chance.\n[range: 0 ~ 100, default: 10]")
                .defineInt("dark_ritual_grounds_spawn_rate", 10, 0, 100);
        graveyardGenerationDistance = b.comment("The minimum distance from another Graveyard that a new one will generate.\n[range: 40 ~ 1000, default: 150]")
                .defineInt("graveyard_generation_distance", 150, 40, 1000);
        graveyardGenerationChance = b.comment("Generation chance of a Graveyard. Higher numbers decrease the chance of a Graveyard generating, while lower numbers increase the chance.\n[range: 0 ~ 1000, default: 50]")
                .defineInt("graveyard_generation_chance", 50, 0, 1000);
        b.pop();

        b.push("silly_settings");
        breakLogic = b.comment("Set true to allow the Liquid Coralium to break the laws of physics in terms of movement")
                .defineBool("break_logic", false);
        nuclearAntimatterExplosions = b.comment("Take a wild guess what this does... Done guessing? Yeah, makes the antimatter explosions more genuine by making them go all nuclear. Recommended to not enable unless you want chaos and destruction.")
                .defineBool("nuclear_antimatter_explosions", false);
        jzaharBreaksFourthWall = b.comment("Toggles whether or not J'zahar can break the fourth wall.")
                .defineBool("jzahar_breaks_fourth_wall", true);
        odbExplosionSize = b.comment("The explosion size of an ODB. 400 is the rough limit if running on 2GB of RAM, anything above that will require more allocated memory, and could crash the game or freeze it for longer periods of time.\n[range: 80 ~ 800, default: 160]")
                .defineInt("odb_explosion_size", 160, 80, 800);
        antimatterExplosionSize = b.comment("The explosion size of antimatter mobs colliding with their normal counterpart if Nucler Antimatter Explosions is enabled.\n[range: 40 ~ 200, default: 80]")
                .defineInt("antimatter_explosion_size", 80, 40, 200);
        b.pop();

        b.push("wet_noodle");
        no_dreadlands_spread = b.comment("Toggles whether or not the spreading of Dreadlands through the Dread Plague is disabled. Cha'garoth remains unaffected by this (because he doesn't naturally spawn outside of the dimension).")
                .defineBool("no_dreadlands_spread", false);
        no_acid_breaking_blocks = b.comment("Toggles whether or not the acid projectiles Lesser Shoggoths spit can break blocks.")
                .defineBool("no_acid_breaking_blocks", false);
        no_spectral_dragons = b.comment("Toggles whether or not Spectral Dragons should spawn in the Abyssal Wasteland.")
                .defineBool("no_spectral_dragons", false);
        no_projectile_damage_immunity = b.comment("Toggles whether or not Lesser Shoggoths are immune to projectile damage.")
                .defineBool("no_projectile_damage_immunity", false);
        no_disruptions = b.comment("Toggles whether or not statues or failing rituals will trigger disruptions.")
                .defineBool("no_disruptions", false);
        no_black_holes = b.comment("Toggles whether or not J'zahar can use his attack that creates a black hole.")
                .defineBool("no_black_holes", false);
        no_odb_explosions = b.comment("Toggles whether or not Oblivion Deathbombs (or ODB Cores) can explode.")
                .defineBool("no_odb_explosions", false);
        b.pop();

        b.push("mod_compat");
        hcdarkness_aw = b.comment("Toggles whether or not the Abyssal Wasteland should be darker if Hardcore Darkness is installed.")
                .defineBool("hcdarkness_aw", true);
        hcdarkness_dl = b.comment("Toggles whether or not the Dreadlands should be darker if Hardcore Darkness is installed.")
                .defineBool("hcdarkness_dl", true);
        hcdarkness_omt = b.comment("Toggles whether or not Omothol should be darker if Hardcore Darkness is installed.")
                .defineBool("hcdarkness_omt", true);
        hcdarkness_dr = b.comment("Toggles whether or not the Dark Realm should be darker if Hardcore Darkness is installed.")
                .defineBool("hcdarkness_dr", true);
        b.pop();

        b.push("spells");
        entropy_spell = b.comment("Set to false to disable the Entropy spell.")
                .defineBool("entropy_spell", true);
        life_drain_spell = b.comment("Set to false to disable the Life Drain spell.")
                .defineBool("life_drain_spell", true);
        mining_spell = b.comment("Set to false to disable the Mining spell.")
                .defineBool("mining_spell", true);
        grasp_of_cthulhu_spell = b.comment("Set to false to disable the Grasp of Cthulhu spell.")
                .defineBool("grasp_of_cthulhu_spell", true);
        invisibility_spell = b.comment("Set to false to disable the Hide from the Eye spell.")
                .defineBool("invisibility_spell", true);
        detachment_spell = b.comment("Set to false to disable the Detachment spell.")
                .defineBool("detachment_spell", true);
        steal_vigor_spell = b.comment("Set to false to disable the Steal Vigor spell.")
                .defineBool("steal_vigor_spell", true);
        sirens_song_spell = b.comment("Set to false to disable the Siren's Song spell.")
                .defineBool("sirens_song_spell", true);
        undeath_to_dust_spell = b.comment("Set to false to disable the Undeath to Dust spell.")
                .defineBool("undeath_to_dust_spell", true);
        ooze_removal_spell = b.comment("Set to false to disable the Ooze Removal spell.")
                .defineBool("ooze_removal_spell", true);
        teleport_hostile_spell = b.comment("Set to false to disable the Sacrificial Interdiction spell.")
                .defineBool("teleport_hostile_spell", true);
        floating_spell = b.comment("Set to false to disable the Floating spell.")
                .defineBool("floating_spell", true);
        teleport_home_spell = b.comment("Set to false to disable the Teleport Home spell.")
                .defineBool("teleport_home_spell", true);
        compass_spell = b.comment("Set to false to disable the Eldritch Directions spell.")
                .defineBool("compass_spell", true);
        b.pop();

        b.push("modules");
        spirit_items = b.comment("Set to false to disable Spirit Items. Items/spells/rituals remain, but item transfer stops.")
                .defineBool("spirit_items", true);
        b.pop();

        b.push("ghoul");
        ghouls_burn = b.comment("Set to toggle if Ghouls and Depths Ghouls should burn in sunlight.")
                .defineBool("ghouls_burn", false);
        tombstoneMaxSpawn = b.comment("The amount of nearby Ghouls (within 15 blocks) at which the Tombstone will halt spawning any new ones.\n[range: 1 ~ 10, default: 5]")
                .defineInt("tombstone_max_spawn", 5, 1, 10);
        tombstoneCooldown = b.comment("The amount of time (in ticks) it takes between every attempt to spawn a Ghoul.\n[range: 40 ~ 1200, default: 200]")
                .defineInt("tombstone_cooldown", 200, 40, 1200);
        tombstoneGhoulDistance = b.comment("Max distance to check for nearby Ghouls before spawning more.\n[range: 5 ~ 48, default: 15]")
                .defineInt("tombstone_ghoul_distance", 15, 5, 48);
        b.pop();

                b.push("complex");
                startDimensionColors = b.comment("First portal dimension RGB color.")
                        .defineIntList("start_dimension_colors", List.of(255, 255, 255));
                interdimensionalCageBlacklist = b.defineStringList("interdimensional_cage_blacklist", List.of());
                itemTransportBlacklist = b.defineStringList("item_transport_blacklist", List.of());
                mobItemPickupBlacklist = b.defineStringList("mob_item_pickup_blacklist", List.of(
                        "minecraft:rotten_flesh", "minecraft:bone", "abyssalcraft:anti_ghoul_flesh",
                        "abyssalcraft:coralium_plagued_flesh", "abyssalcraft:anti_plagued_flesh"));
                dreadPlagueImmunityList = b.defineStringList("dread_plague_immunity", List.of());
                dreadPlagueCarrierList = b.defineStringList("dread_plague_carriers", List.of());
                coraliumPlagueImmunityList = b.defineStringList("coralium_plague_immunity", List.of());
                coraliumPlagueCarrierList = b.defineStringList("coralium_plague_carriers", List.of());
                demonAnimalTransformations = b.comment("entity_id;demon_type(0-3);chance(0-1)")
                        .defineStringList("demon_animal_transformations", List.of());
                dimensionBookTypeMappings = b.comment("dimension_id;book_type;optional_name")
                        .defineStringList("dimension_book_type_mappings", List.of());
                blackHoleDimensionBlacklist = b.defineStringList("black_hole_dimension_blacklist", List.of());
                oreGenerationDimensionBlacklist = b.defineStringList("ore_generation_dimension_blacklist", List.of());
                structureGenerationDimensionBlacklist = b.defineStringList("structure_generation_dimension_blacklist", List.of());
                coraliumOreGeneration = b.comment("vein count, vein size, max height")
                        .defineIntList("coralium_ore_generation", List.of(12, 8, 40));
                b.pop();

        common = b.build();

        ConfigCompat.register(ConfigCompat.Type.COMMON, common);
        ConfigCompat.register(ConfigCompat.Type.CLIENT, client);
    }

        public static boolean isCommonConfig(Object spec) {
                return common != null && common.matches(spec);
        }
}

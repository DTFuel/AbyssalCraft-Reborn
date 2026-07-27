package com.shinoow.abyssalcraft.world;

import java.util.function.IntSupplier;

import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.structure.StructureKind;

/** Runtime configuration gates for data-defined worldgen objects. */
public final class WorldgenConfigGate {

    private static final ResourceLocation CORALIUM_LAKE = ACRef.id("lake_liquid_coralium");
    private static final ResourceLocation ANTIMATTER_LAKE = ACRef.id("lake_liquid_antimatter");
    private static final ResourceLocation ABYSSAL_WASTELAND_PILLARS = ACRef.id("abyssal_wasteland_pillars");

    private WorldgenConfigGate() {}

    public static boolean allowsStructure(StructureKind kind, int chunkX, int chunkZ,
                                          IntSupplier randomInt) {
        return switch (kind) {
            case GRAVEYARD -> ACConfig.generateGraveyards.get()
                && passesDistance(ACConfig.graveyardGenerationDistance.get(), chunkX, chunkZ)
                && passesChance(ACConfig.graveyardGenerationChance.get(), randomInt);
            case ABYRUIN -> ACConfig.generateAbyssalWastelandRuins.get();
            case DARK_SHRINE -> ACConfig.generateDarklandsStructures.get()
                && passesChance(ACConfig.darkShrineSpawnRate.get(), randomInt);
            case DARK_RITUAL_GROUNDS -> ACConfig.generateDarklandsStructures.get()
                && passesChance(ACConfig.darkRitualGroundsSpawnRate.get(), randomInt);
            case SHOGGOTH_PIT -> ACConfig.generateShoggothLairs.get()
                && passesDistance(ACConfig.shoggothLairGenerationDistance.get(), chunkX, chunkZ)
                && passesChance(ACConfig.shoggothLairSpawnRate.get(), randomInt);
            case SHOGGOTH_PIT_RIVER -> ACConfig.generateShoggothLairs.get()
                && passesDistance(ACConfig.shoggothLairGenerationDistance.get(), chunkX, chunkZ)
                && passesChance(ACConfig.shoggothLairSpawnRateRivers.get(), randomInt);
            case OMOTHOL_CITY, OMOTHOL_TEMPLE, OMOTHOL_TOWER, OMOTHOL_STORAGE, ETHAXIUM_HOUSE ->
                ACConfig.generateOmotholStructures.get();
            case CHAGAROTH_LAIR, JZAHAR_TEMPLE -> true;
        };
    }

    public static boolean allowsPlacedFeature(ResourceLocation id) {
        if (id == null) return false;
        if (CORALIUM_LAKE.equals(id)) {
            return ACConfig.generateCoraliumLake.get();
        }
        if (ANTIMATTER_LAKE.equals(id)) {
            return ACConfig.generateAntimatterLake.get();
        }
        if (ABYSSAL_WASTELAND_PILLARS.equals(id)) {
            return ACConfig.generateAbyssalWastelandPillars.get();
        }
        if (!AbyssalCraft.MODID.equals(id.getNamespace())) return true;
        return switch (id.getPath()) {
            case "ore_nitre" -> ACConfig.generateNitreOre.get();
            case "ore_abyssalnite" -> ACConfig.generateAbyssalniteOre.get();
            case "ore_abyssal_coralium" -> ACConfig.generateAbyssalCoraliumOre.get();
            case "ore_dreadlands_abyssalnite" -> ACConfig.generateDreadlandsAbyssalniteOre.get();
            case "ore_dreaded_abyssalnite" -> ACConfig.generateDreadedAbyssalniteOre.get();
            case "ore_abyssal_iron" -> ACConfig.generateAbyssalIronOre.get();
            case "ore_abyssal_gold" -> ACConfig.generateAbyssalGoldOre.get();
            case "ore_abyssal_diamond" -> ACConfig.generateAbyssalDiamondOre.get();
            case "ore_abyssal_nitre" -> ACConfig.generateAbyssalNitreOre.get();
            case "ore_pearlescent_coralium" -> ACConfig.generatePearlescentCoraliumOre.get();
            case "ore_liquified_coralium" -> ACConfig.generateLiquifiedCoraliumOre.get();
            default -> true;
        };
    }

    static boolean passesChance(int chance, IntSupplier randomInt) {
        return chance > 0 && Math.floorMod(randomInt.getAsInt(), chance) == 0;
    }

    static boolean passesDistance(int blocks, int chunkX, int chunkZ) {
        int chunkInterval = Math.max(1, (blocks + 15) / 16);
        return Math.floorMod(chunkX, chunkInterval) == 0
            && Math.floorMod(chunkZ, chunkInterval) == 0;
    }
}
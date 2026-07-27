package com.shinoow.abyssalcraft.data;

import net.minecraft.data.DataProvider;

import com.shinoow.abyssalcraft.platform.DataGenCompat;

/**
 * Datagen aggregator (relay file, owned by PA-4).
 *
 * <p>{@code runData} fires {@code GatherDataEvent}; {@link DataGenCompat} turns it into a neutral
 * {@link DataGenCompat.Gen} and calls {@link #gather}. Each content stage's {@code XxxData} provider
 * is appended here one line at a time at its Gate; parallel content tasks never edit this file.
 * Carries no loader-forked API - the {@code GatherDataEvent} fork lives in {@code platform/DataGenCompat}.
 */
public final class ACDataGenerators {

    private ACDataGenerators() {}

    /** Called during {@code runData}. Registers every mod {@code DataProvider} on the generator. */
    public static void gather(DataGenCompat.Gen gen) {
        com.shinoow.abyssalcraft.content.entity.legacy.EntityCatalogInvariant.validate();
        com.shinoow.abyssalcraft.world.WorldgenInvariant.validate();
        // Infrastructure smoke provider; real providers (models/blockstates/recipes/loot/lang/tags)
        // are appended here at each stage Gate.
        gen.generator.addProvider(gen.includeServer, (DataProvider.Factory<ACExampleProvider>) ACExampleProvider::new);
        // PB-3 building-material blocks: blockstates + block models + item models (client assets).
        gen.generator.addProvider(gen.includeClient, new com.shinoow.abyssalcraft.data.gen.BaseBlockData(gen));
        gen.generator.addProvider(gen.includeClient, new com.shinoow.abyssalcraft.data.gen.DecoBlockData(gen));
        gen.generator.addProvider(gen.includeClient, new com.shinoow.abyssalcraft.data.gen.EnergyBlockData(gen));
        gen.generator.addProvider(gen.includeClient, new com.shinoow.abyssalcraft.data.gen.EnergyItemData(gen));
        // PK-3 (Stage K) item models: generated/spawn-egg models for every registered item lacking one.
        gen.generator.addProvider(gen.includeClient, new com.shinoow.abyssalcraft.data.gen.ModelItemData(gen));
        // PK-5 (Stage K) block loot: self-drop table for every AC block that lacks one (skips the
        // ore loot already shipped by PB-4); faithful ore gem-drops for the few not yet covered.
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.ACBlockLoot(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.OreLootData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.EntityLootData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.LegacyCraftingRecipeData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.CookingRecipeData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.ACTagData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.CrystalClusterRecipeData(gen));
        // PC-9 (Stage C2b) machine recipes: crystallizer/transmutator/materializer example ProcessingRecipes.
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.MachineRecipeData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.MenuHostValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.StateTransformerValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.RendingPedestalValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.KnowledgeValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.R2GateValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.EnergyValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.PortalValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.RitualSpellPortalValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.SystemMatrixValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.EntityBehaviorValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.NetworkValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.ClientFxValidationData(gen));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.LangValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.AdvApiValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.ConfigValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.AssetBlockValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.AssetCatalogValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.JEIAutomationValidationData());
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.WorldgenValidationData(gen.packOutput));
        gen.generator.addProvider(gen.includeServer, new com.shinoow.abyssalcraft.data.gen.DatagenClosureValidationData(gen));
    }
}

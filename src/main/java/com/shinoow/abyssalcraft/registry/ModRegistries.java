package com.shinoow.abyssalcraft.registry;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.demon.DemonBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.ghoul.GhoulHeadBlocks;
import com.shinoow.abyssalcraft.content.block.ore.OreBlocks;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.portal.PortalBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;
import com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntities;
import com.shinoow.abyssalcraft.content.entity.anti.AntiEntities;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.ShoggothEntities;
import com.shinoow.abyssalcraft.content.item.armor.ArmorItems;
import com.shinoow.abyssalcraft.content.item.bag.CrystalBagItems;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.energy.EnergyItems;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.item.material.MachineContentItems;
import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.content.item.portal.PortalItems;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItems;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.item.tool.ToolItems;
import com.shinoow.abyssalcraft.content.item.weapon.SoulReaperItems;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStands;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformers;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.content.menu.base.MachineMenus;
import com.shinoow.abyssalcraft.content.recipe.base.ProcessingRecipes;
import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;
import com.shinoow.abyssalcraft.platform.ContentLootCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/**
 * Registry aggregator (relay file, owned by PA-2).
 *
 * <p>{@link #ALL} is the single collection point for every mod {@link ModRegistrar}. The main class
 * {@code init(IEventBus)} -- the one place allowed to touch loader-forked API -- attaches each entry
 * to the MOD event bus. Keeping the bus reference in the {@code @Mod} class means this relay (and the
 * per-module {@code Mod*} registrars) stay free of {@code //?} forks, honouring the compat rule that
 * loader/version divergence lives only in {@code platform/} plus the main class.
 *
 * <p>Stage-Gate integration appends one registrar per line to {@link #ALL}; parallel content tasks
 * never edit this file directly.
 */
public final class ModRegistries {

    private ModRegistries() {}

    /** Every mod DeferredRegister wrapper, in mount order. Extended one line at a time per Gate. */
    public static final List<ModRegistrar<?>> ALL = List.of(
        ModCreativeTabs.TABS,
        ContentLootCompat.MODIFIERS,
        // Machine framework (Gate P1): BE/menu/recipe types.
        MachineBlockEntities.BLOCK_ENTITIES,
        MachineMenus.MENUS,
        ProcessingRecipes.RECIPE_TYPES,
        ProcessingRecipes.RECIPE_SERIALIZERS,
        // Pilot Materializer machine (PP-3): block/item/BE/menu + materialization recipe.
        Materializers.BLOCKS,
        Materializers.ITEMS,
        Materializers.BLOCK_ENTITIES,
        Materializers.MENUS,
        // Pilot Crystallizer machine (PP-2): block/item/BE/menu + crystallization recipe.
        Crystallizers.BLOCKS,
        Crystallizers.ITEMS,
        Crystallizers.BLOCK_ENTITIES,
        Crystallizers.MENUS,
        // Pilot Transmutator machine (PP-4): block/item/BE/menu + transmutation recipe.
        Transmutators.BLOCKS,
        Transmutators.ITEMS,
        Transmutators.BLOCK_ENTITIES,
        Transmutators.MENUS,
        LiquidCoraliumCompat.FLUID_TYPES,
        LiquidCoraliumCompat.FLUIDS,
        LiquidCoraliumCompat.BLOCKS,
        LiquidCoraliumCompat.ITEMS,
        LiquidAntimatterCompat.FLUID_TYPES,
        LiquidAntimatterCompat.FLUIDS,
        LiquidAntimatterCompat.BLOCKS,
        LiquidAntimatterCompat.ITEMS,
        // Stage B1 content: food & miscellaneous items (PB-2).
        MiscItems.ITEMS,
        // Stage B1 building materials (PB-3): base stone/wood block family + their BlockItems.
        BaseBlocks.BLOCKS,
        BaseBlocks.ITEMS,
        // Stage B1 material tier (PB-1): material + crystal items (ingots/nuggets/gems/clusters/dusts/crystals/coin).
        MaterialItems.ITEMS,
        MachineContentItems.ITEMS,
        CrystalClusterBlocks.BLOCKS,
        CrystalClusterBlocks.ITEMS,
        // RR-MACHINE: four Crystal Bag tiers used by the faithful Materializer data contract.
        CrystalBagItems.ITEMS,
        // Stage B1 decorative/plain-function blocks (PB-5): statues/mural/tombstones/ingot blocks/ground/sand/glass/plants + BlockItems.
        DecoBlocks.BLOCKS,
        DecoBlocks.ITEMS,
        DecoBlocks.BLOCK_ENTITIES,
        DemonBlocks.BLOCKS,
        // Stage B2 tools (PB-6): 4 material tiers x {pickaxe, axe, shovel, hoe, sword}.
        ToolItems.ITEMS,
        SoulReaperItems.ITEMS,
        // Stage B2 ores (PB-4): 13 ore blocks + BlockItems (drops/tiers via loot + tags).
        OreBlocks.BLOCKS,
        OreBlocks.ITEMS,
        // Stage B2 armor (PB-7): 7 armor materials x {helmet, chestplate, leggings, boots}.
        ArmorItems.ITEMS,
        // Stage C1 menu framework (PC-3): canonical MenuType registry + example item-container menu.
        ModMenus.MENUS,
        // Stage C1 general block-entity framework (PC-1): reusable directional / inventory BE bases.
        ModBlockEntities.BLOCK_ENTITIES,
        // Stage C1 custom recipe framework (PC-2): anvil_forging + rending recipe types + serializers
        // (crystallization/materialization/transmutation stay pilot-machine-registered; see ModRecipes).
        ModRecipes.RECIPE_TYPES,
        ModRecipes.RECIPE_SERIALIZERS,
        // Stage C2a research table (PC-8): block/item/BE/menu (research/knowledge hook deferred to S-B).
        ResearchTables.BLOCKS,
        ResearchTables.ITEMS,
        ResearchTables.BLOCK_ENTITIES,
        ResearchTables.MENUS,
        // Stage C2a sequential brewing stand (PC-8): block/item/BE/menu (vanilla brewing + chain-to-neighbour).
        BrewingStands.BLOCKS,
        BrewingStands.ITEMS,
        BrewingStands.BLOCK_ENTITIES,
        BrewingStands.MENUS,
        StateTransformers.BLOCKS,
        StateTransformers.ITEMS,
        StateTransformers.BLOCK_ENTITIES,
        StateTransformers.MENUS,
        RendingPedestals.BLOCKS,
        RendingPedestals.ITEMS,
        RendingPedestals.BLOCK_ENTITIES,
        RendingPedestals.MENUS,
        LegacyEntities.ENTITIES,
        LegacyEntities.ITEMS,
        // Stage D2a demon/evil animals (PD-4): 4 demon + 4 evil hostile farm-animal EntityTypes.
        DemonEntities.ENTITIES,
        DemonEntities.ITEMS,
        // Stage D2a anti-matter family (PD-3): 11 anti EntityTypes + 11 spawn eggs.
        AntiEntities.ENTITIES,
        AntiEntities.ITEMS,
        // Stage D2a ghoul family (PD-5): 5 ghoul EntityTypes + 5 spawn eggs.
        GhoulEntities.ENTITIES,
        GhoulEntities.ITEMS,
        GhoulHeadBlocks.BLOCKS,
        GhoulHeadBlocks.ITEMS,
        // Stage D2a shoggoth family (PD-5): 3 shoggoth EntityTypes + 3 spawn eggs.
        ShoggothEntities.ENTITIES,
        ShoggothEntities.ITEMS,
        ShoggothBlocks.BLOCKS,
        ShoggothBlocks.ITEMS,
        StructureContent.BLOCKS,
        StructureContent.ITEMS,
        StructureContent.BLOCK_ENTITIES,
        // Stage D2a projectiles (PD-6): 5 projectile EntityTypes (acid/coralium arrow/dreaded charge/dread slug/ink).
        ProjectileEntities.ENTITIES,
        // Stage D2a misc entities (PD-6): 10 non-living EntityTypes (black hole/implosion/ODB/portal/spirit item/...).
        MiscEntities.ENTITIES,
        // Stage D2b bosses (PD-7): 12 boss EntityTypes (4 bar-bosses + 8 elites/minions) + 12 spawn eggs.
        BossEntities.ENTITIES,
        BossEntities.ITEMS,
        // Stage G0 worldgen framework (PG-0): code-defined Feature registry (mini_pillar vertical-slice example; PG-4 extends).
        ModWorldgen.DENSITY_FUNCTION_TYPES,
        ModWorldgen.FEATURES,
        // Stage S-A potion effects (PS-4): 5 MobEffects (coralium/dread plague, antimatter, 2 antidotes) + 7 brewable potions.
        ACEffects.EFFECTS,
        ACEffects.POTIONS,
        // Stage G1 structures (PG-5): programmatic StructureType + StructurePiece (graveyard/abyruin/dark_shrine via ACStructure kind).
        ModWorldgen.STRUCTURE_TYPES,
        ModWorldgen.STRUCTURE_PIECES,
        // Stage H1 sounds (PH-4): 45 SoundEvents (dreadguard/ghoul/shoggoth/jzahar/chant/... ids match sounds.json).
        ModSounds.SOUNDS,
        // Stage H1 particles (PH-3): particle types (abyssal_fx concrete; client sprite provider in ParticleCompat).
        ModParticles.PARTICLES,
        // Stage 6/7 unblock content: the Necronomicon books (open the PH-5 death-book GUI on right-click).
        BookItems.ITEMS,
        // Stage 6/7 unblock content: energy blocks (pilot deity statue = the PE source that fills energy items).
        EnergyBlocks.BLOCKS,
        EnergyBlocks.ITEMS,
        EnergyBlocks.BLOCK_ENTITIES,
        EnergyBlocks.MENUS,
        EnergyItems.ITEMS,
        // Stage 6/7 unblock content: ritual blocks (altar + pedestals = the PE-consuming ritual multiblock).
        RitualBlocks.BLOCKS,
        RitualBlocks.ITEMS,
        RitualBlocks.BLOCK_ENTITIES,
        PortalBlocks.BLOCKS,
        PortalBlocks.ITEMS,
        PortalBlocks.BLOCK_ENTITIES,
        PortalItems.ITEMS,
        RitualItems.ITEMS,
        ScrollItems.ITEMS,
        TabletItems.ITEMS,
        // RR-MENU-HOST: Spirit Tablet + Spirit Altar + their persistent menu/BE types.
        TransferContent.BLOCKS,
        TransferContent.ITEMS,
        TransferContent.BLOCK_ENTITIES,
        TransferContent.MENUS
        // ... appended here at each stage Gate.
    );
}

package com.shinoow.abyssalcraft.content.block.ore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Ore blocks (owned by PB-4, Stage B2).
 *
 * <p>The 13 AbyssalCraft ores ported from 1.12.2 {@code BlockACOre}: overworld coralium/abyssalnite/
 * nitre + the Abyssal Wasteland dimension ores (iron/gold/diamond/nitre/coralium variants) +
 * dreadlands abyssalnite. Every ore is a plain vanilla {@link Block} with
 * {@code requiresCorrectToolForDrops()} and the legacy hardness/resistance, so this business file
 * carries no loader fork; the forked {@code DeferredRegister} lives in {@link ModRegistrar}.
 *
 * <p>Drops and mining tier are data-driven: loot tables ({@code data/abyssalcraft/loot_table(s)/blocks})
 * and the {@code mineable/pickaxe} + {@code needs_iron_tool}/{@code needs_diamond_tool} block tags.
 * Models/textures use vanilla placeholders for now (faithful layered ore overlays deferred to PK).
 */
public final class OreBlocks {

    private OreBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Every ore block, in registration order (available for datagen / other consumers). */
    public static final List<Supplier<Block>> ALL = new ArrayList<>();

    /** Register an ore block + its {@link BlockItem}. {@code requiresCorrectToolForDrops} + legacy strength. */
    private static Supplier<Block> ore(String name, float hardness, float resistance) {
        Supplier<Block> block = BLOCKS.register(name, () ->
            new Block(BlockBehaviour.Properties.of().strength(hardness, resistance).requiresCorrectToolForDrops()));
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        ALL.add(block);
        return block;
    }

    public static final Supplier<Block> CORALIUM_ORE = ore("coralium_ore", 3.0F, 6.0F);
    public static final Supplier<Block> ABYSSALNITE_ORE = ore("abyssalnite_ore", 3.0F, 6.0F);
    public static final Supplier<Block> ABYSSAL_ABYSSALNITE_ORE = ore("abyssal_abyssalnite_ore", 3.0F, 6.0F);
    public static final Supplier<Block> DREADLANDS_ABYSSALNITE_ORE = ore("dreadlands_abyssalnite_ore", 2.5F, 20.0F);
    public static final Supplier<Block> DREADED_ABYSSALNITE_ORE = ore("dreaded_abyssalnite_ore", 2.5F, 20.0F);
    public static final Supplier<Block> NITRE_ORE = ore("nitre_ore", 3.0F, 6.0F);
    public static final Supplier<Block> ABYSSAL_CORALIUM_ORE = ore("abyssal_coralium_ore", 3.0F, 6.0F);
    public static final Supplier<Block> ABYSSAL_IRON_ORE = ore("abyssal_iron_ore", 3.0F, 6.0F);
    public static final Supplier<Block> ABYSSAL_GOLD_ORE = ore("abyssal_gold_ore", 5.0F, 10.0F);
    public static final Supplier<Block> ABYSSAL_DIAMOND_ORE = ore("abyssal_diamond_ore", 5.0F, 10.0F);
    public static final Supplier<Block> ABYSSAL_NITRE_ORE = ore("abyssal_nitre_ore", 3.0F, 6.0F);
    public static final Supplier<Block> PEARLESCENT_CORALIUM_ORE = ore("pearlescent_coralium_ore", 8.0F, 10.0F);
    public static final Supplier<Block> LIQUIFIED_CORALIUM_ORE = ore("liquified_coralium_ore", 10.0F, 12.0F);
}

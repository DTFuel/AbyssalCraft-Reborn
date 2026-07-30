package com.shinoow.abyssalcraft.content.block.ritual;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.ChatFormatting;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.item.ColoredBlockItem;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.BlockFactory;

/**
 * Ritual block content (owned by content/block/ritual): the ritual altar (this commit) and its ring
 * pedestals (added next). The altar consumes the held Necronomicon's PE (CR-58/59) + the pedestals'
 * offerings to run a {@link com.shinoow.abyssalcraft.system.ritual.Ritual} (PS-6). Registrars attach to
 * the MOD bus through {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class RitualBlocks {

    private RitualBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    public static final Supplier<Block> RITUAL_ALTAR = BLOCKS.register("ritual_altar", () ->
        new RitualAltarBlock(BlockBehaviour.Properties.of().strength(3.5F).noOcclusion()));

    public static final Supplier<BlockItem> RITUAL_ALTAR_ITEM = ITEMS.register("ritual_altar", () ->
        new BlockItem(RITUAL_ALTAR.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR_BE =
        BLOCK_ENTITIES.register("ritual_altar", () ->
            BlockEntityType.Builder.<RitualAltarBlockEntity>of(
                (pos, state) -> new RitualAltarBlockEntity(pos, state), RITUAL_ALTAR.get()).build(null));

    public static final Supplier<Block> RITUAL_PEDESTAL = BLOCKS.register("ritual_pedestal", () ->
        new RitualPedestalBlock(BlockBehaviour.Properties.of().strength(3.5F).noOcclusion()));

    public static final Supplier<BlockItem> RITUAL_PEDESTAL_ITEM = ITEMS.register("ritual_pedestal", () ->
        new BlockItem(RITUAL_PEDESTAL.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<RitualPedestalBlockEntity>> RITUAL_PEDESTAL_BE =
        BLOCK_ENTITIES.register("ritual_pedestal", () ->
            BlockEntityType.Builder.<RitualPedestalBlockEntity>of(
                (pos, state) -> new RitualPedestalBlockEntity(pos, state), RITUAL_PEDESTAL.get()).build(null));

    public static final Supplier<Block> CORALIUM_INFUSED_STONE = block("coralium_infused_stone", () ->
        BlockFactory.experienceBlock(BlockBehaviour.Properties.of()
            .strength(3.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), UniformInt.of(2, 5)));

    public static final Supplier<Block> DREADLANDS_INFUSED_POWERSTONE = block("dreadlands_infused_powerstone", () ->
        new DreadlandsPowerstoneBlock(BlockBehaviour.Properties.of()
            .strength(50.0F, 3000.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion()));

    public static final Supplier<Block> ODB_CORE = block("odb_core", () ->
        new OblivionDeathbombCoreBlock(BlockBehaviour.Properties.of()
            .strength(3.0F, 0.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()),
        ChatFormatting.DARK_RED);

    public static final Supplier<Block> OBLIVION_DEATHBOMB = block("oblivion_deathbomb", () ->
        new TntBlock(BlockBehaviour.Properties.of()
            .strength(3.0F, 0.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()),
        ChatFormatting.DARK_RED);

    private static Supplier<Block> block(String id, Supplier<? extends Block> factory) {
        return block(id, factory, null);
    }

    private static Supplier<Block> block(String id, Supplier<? extends Block> factory, ChatFormatting color) {
        Supplier<Block> block = BLOCKS.register(id, factory);
        ITEMS.register(id, () -> color == null
            ? new BlockItem(block.get(), new Item.Properties())
            : new ColoredBlockItem(block.get(), new Item.Properties(), color));
        return block;
    }
}

package com.shinoow.abyssalcraft.content.block.structure;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/** Concrete content hosts consumed by converted legacy structure markers. */
public final class StructureContent {

    private StructureContent() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    public static final Supplier<CrateBlock> CRATE = BLOCKS.register("crate", () ->
        new CrateBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.WOOD)));
    public static final Supplier<SealingLockBlock> SEALING_LOCK = BLOCKS.register("sealing_lock", () ->
        new SealingLockBlock(BlockBehaviour.Properties.of().strength(2.5F, 20.0F)
            .sound(SoundType.STONE).noOcclusion()));

    public static final Supplier<BlockEntityType<CrateBlockEntity>> CRATE_BE =
        BLOCK_ENTITIES.register("crate", () -> BlockEntityType.Builder
            .of(CrateBlockEntity::new, CRATE.get()).build(null));
    public static final Supplier<BlockEntityType<SealingLockBlockEntity>> SEALING_LOCK_BE =
        BLOCK_ENTITIES.register("sealing_lock", () -> BlockEntityType.Builder
            .of(SealingLockBlockEntity::new, SEALING_LOCK.get()).build(null));

    static {
        ITEMS.register("crate", () -> new BlockItem(CRATE.get(), new Item.Properties()));
        ITEMS.register("sealing_lock", () -> new BlockItem(SEALING_LOCK.get(), new Item.Properties()));
    }
}
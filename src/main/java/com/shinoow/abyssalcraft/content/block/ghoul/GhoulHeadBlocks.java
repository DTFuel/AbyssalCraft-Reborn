package com.shinoow.abyssalcraft.content.block.ghoul;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/** The four legacy Depths Ghoul head drops, kept under their original registry ids. */
public final class GhoulHeadBlocks {

    private GhoulHeadBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<Block> DEPTHS_GHOUL_HEAD = register("dghead");
    public static final Supplier<Block> PETE_HEAD = register("phead");
    public static final Supplier<Block> WILSON_HEAD = register("whead");
    public static final Supplier<Block> ORANGE_HEAD = register("ohead");

    public static final List<Supplier<Block>> ALL = List.of(
        DEPTHS_GHOUL_HEAD, PETE_HEAD, WILSON_HEAD, ORANGE_HEAD);

    private static Supplier<Block> register(String id) {
        Supplier<Block> block = BLOCKS.register(id, () -> new GhoulHeadBlock(
            BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).noOcclusion()));
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
package com.shinoow.abyssalcraft.content.block.shoggoth;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/** Blocks that host the Shoggoth ooze, biomass and monolith-building behavior. */
public final class ShoggothBlocks {

    private ShoggothBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final TagKey<Block> WORSHIP_TARGETS =
        TagKey.create(Registries.BLOCK, ACRef.id("shoggoth_worship_targets"));

    public static final Supplier<ShoggothOozeBlock> SHOGGOTH_OOZE = BLOCKS.register("shoggoth_ooze", () ->
        new ShoggothOozeBlock(BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.SLIME_BLOCK)
            .randomTicks().noOcclusion()));
    public static final Supplier<ShoggothBiomassBlock> SHOGGOTH_BIOMASS = BLOCKS.register("shoggoth_biomass", () ->
        new ShoggothBiomassBlock(BlockBehaviour.Properties.of().strength(1.0F, 18.0F).sound(SoundType.SAND)
            .lightLevel(state -> 8).noOcclusion()));
    public static final Supplier<BlockEntityType<ShoggothBiomassBlockEntity>> SHOGGOTH_BIOMASS_BE =
        BLOCK_ENTITIES.register("shoggoth_biomass", () -> BlockEntityType.Builder.of(
            ShoggothBiomassBlockEntity::new, SHOGGOTH_BIOMASS.get()).build(null));

    static {
        ITEMS.register("shoggoth_ooze", () -> new BlockItem(SHOGGOTH_OOZE.get(), new Item.Properties()));
    }
}
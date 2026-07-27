package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RendingPedestals {

    private RendingPedestals() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> RENDING_PEDESTAL = BLOCKS.register("rending_pedestal", () ->
        new RendingPedestalBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F)
            .sound(SoundType.STONE).noOcclusion()));
    public static final Supplier<RendingPedestalBlockItem> RENDING_PEDESTAL_ITEM =
        ITEMS.register("rending_pedestal", () ->
            new RendingPedestalBlockItem(RENDING_PEDESTAL.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RendingPedestalBlockEntity>> RENDING_PEDESTAL_BE =
        BLOCK_ENTITIES.register("rending_pedestal", () -> BlockEntityType.Builder
            .of(RendingPedestalBlockEntity::new, RENDING_PEDESTAL.get()).build(null));
    public static final Supplier<MenuType<RendingPedestalMenu>> RENDING_PEDESTAL_MENU =
        MENUS.register("rending_pedestal", () -> MenuCompat.create(RendingPedestalMenu::new));
}
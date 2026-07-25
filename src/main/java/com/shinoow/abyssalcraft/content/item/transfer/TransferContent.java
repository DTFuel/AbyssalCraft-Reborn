package com.shinoow.abyssalcraft.content.item.transfer;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.transfer.SpiritAltarBlock;
import com.shinoow.abyssalcraft.content.block.transfer.SpiritAltarBlockEntity;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

public final class TransferContent {

    private TransferContent() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<SpiritTabletItem> SPIRIT_TABLET = ITEMS.register("spirit_tablet", SpiritTabletItem::new);
    public static final Supplier<Block> SPIRIT_ALTAR = BLOCKS.register("spirit_altar", () ->
        new SpiritAltarBlock(BlockBehaviour.Properties.of().strength(2.5F, 20.0F).sound(SoundType.METAL).noOcclusion()));
    public static final Supplier<BlockItem> SPIRIT_ALTAR_ITEM = ITEMS.register("spirit_altar", () ->
        new BlockItem(SPIRIT_ALTAR.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<SpiritAltarBlockEntity>> SPIRIT_ALTAR_BE =
        BLOCK_ENTITIES.register("spirit_altar", () -> BlockEntityType.Builder.<SpiritAltarBlockEntity>of(
            SpiritAltarBlockEntity::new, SPIRIT_ALTAR.get()).build(null));
    public static final Supplier<MenuType<SpiritTabletMenu>> SPIRIT_TABLET_MENU = MENUS.register(
        "spirit_tablet", () -> MenuCompat.create(SpiritTabletMenu::new));
}
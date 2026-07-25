package com.shinoow.abyssalcraft.content.machine.crystallizer;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Crystallizer registration (owned by PP-2): block + block-item + BE type + menu type + the
 * {@code crystallization} recipe type/serializer. Reuses the PP-1 {@link MachineMenu} and
 * {@link ProcessingRecipe}; all registrars are attached to the MOD bus via {@code ModRegistries.ALL}.
 */
public final class Crystallizers {

    private Crystallizers() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> CRYSTALLIZER = BLOCKS.register("crystallizer", () ->
        new CrystallizerBlock(BlockBehaviour.Properties.of().strength(3.5F)));

    public static final Supplier<BlockItem> CRYSTALLIZER_ITEM = ITEMS.register("crystallizer", () ->
        new BlockItem(CRYSTALLIZER.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<CrystallizerBlockEntity>> CRYSTALLIZER_BE = BLOCK_ENTITIES.register("crystallizer", () ->
        BlockEntityType.Builder.<CrystallizerBlockEntity>of(
            (pos, state) -> new CrystallizerBlockEntity(pos, state), CRYSTALLIZER.get()).build(null));

    public static final Supplier<MenuType<CrystallizerMenu>> CRYSTALLIZER_MENU = MENUS.register("crystallizer", () ->
        MenuCompat.create(CrystallizerMenu::new));

}

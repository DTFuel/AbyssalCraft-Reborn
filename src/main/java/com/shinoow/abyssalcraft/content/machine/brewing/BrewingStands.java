package com.shinoow.abyssalcraft.content.machine.brewing;

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
 * Sequential Brewing Stand registration (owned by PC-8, Stage C2a): block + block-item + BE type +
 * menu type. Brewing uses vanilla potion recipes (via {@code PotionBrewingCompat}), so no custom
 * recipe type. Attached to the MOD bus via {@code ModRegistries.ALL}.
 */
public final class BrewingStands {

    private BrewingStands() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> BREWING_STAND = BLOCKS.register("sequential_brewing_stand", () ->
        new BrewingStandBlock(BlockBehaviour.Properties.of().strength(0.5F).noOcclusion()));

    public static final Supplier<BlockItem> BREWING_STAND_ITEM = ITEMS.register("sequential_brewing_stand", () ->
        new BlockItem(BREWING_STAND.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<BrewingStandBlockEntity>> BREWING_STAND_BE = BLOCK_ENTITIES.register("sequential_brewing_stand", () ->
        BlockEntityType.Builder.<BrewingStandBlockEntity>of(
            (pos, state) -> new BrewingStandBlockEntity(pos, state), BREWING_STAND.get()).build(null));

    public static final Supplier<MenuType<BrewingStandMenu>> BREWING_STAND_MENU = MENUS.register("sequential_brewing_stand", () ->
        MenuCompat.create((windowId, inventory, data) ->
            new BrewingStandMenu(BrewingStands.BREWING_STAND_MENU.get(), windowId, inventory)));
}

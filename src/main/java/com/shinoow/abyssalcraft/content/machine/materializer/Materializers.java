package com.shinoow.abyssalcraft.content.machine.materializer;

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
 * Materializer registration (owned by PP-3): block + block-item + BE type + menu type + the
 * {@code materialization} recipe type/serializer. Reuses the PP-1 {@link MachineMenu} and
 * {@link ProcessingRecipe}; all registrars are attached to the MOD bus via {@code ModRegistries.ALL}.
 */
public final class Materializers {

    private Materializers() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> MATERIALIZER = BLOCKS.register("materializer", () ->
        new MaterializerBlock(BlockBehaviour.Properties.of().strength(3.5F)));

    public static final Supplier<BlockItem> MATERIALIZER_ITEM = ITEMS.register("materializer", () ->
        new BlockItem(MATERIALIZER.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<MaterializerBlockEntity>> MATERIALIZER_BE = BLOCK_ENTITIES.register("materializer", () ->
        BlockEntityType.Builder.<MaterializerBlockEntity>of(
            (pos, state) -> new MaterializerBlockEntity(pos, state), MATERIALIZER.get()).build(null));

    public static final Supplier<MenuType<MaterializerMenu>> MATERIALIZER_MENU = MENUS.register("materializer", () ->
        MenuCompat.create(MaterializerMenu::new));

}

package com.shinoow.abyssalcraft.content.machine.researchtable;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Research Table registration (owned by PC-8, Stage C2a): block + block-item + BE type + menu type.
 * No recipe type (the research/knowledge hook is deferred to Stage S-B). Attached to the MOD bus via
 * {@code ModRegistries.ALL}.
 */
public final class ResearchTables {

    private ResearchTables() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> RESEARCH_TABLE = BLOCKS.register("research_table", () ->
        new ResearchTableBlock(BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.WOOD)
            .lightLevel(state -> 6).noOcclusion()));

    public static final Supplier<BlockItem> RESEARCH_TABLE_ITEM = ITEMS.register("research_table", () ->
        new BlockItem(RESEARCH_TABLE.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE_BE = BLOCK_ENTITIES.register("research_table", () ->
        BlockEntityType.Builder.<ResearchTableBlockEntity>of(
            (pos, state) -> new ResearchTableBlockEntity(pos, state), RESEARCH_TABLE.get()).build(null));

    public static final Supplier<MenuType<ResearchTableMenu>> RESEARCH_TABLE_MENU = MENUS.register("research_table", () ->
        MenuCompat.create((windowId, inventory, data) ->
            new ResearchTableMenu(ResearchTables.RESEARCH_TABLE_MENU.get(), windowId, inventory)));
}

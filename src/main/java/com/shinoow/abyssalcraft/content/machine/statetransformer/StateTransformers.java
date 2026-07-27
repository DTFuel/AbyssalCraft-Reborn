package com.shinoow.abyssalcraft.content.machine.statetransformer;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class StateTransformers {

    private StateTransformers() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> STATE_TRANSFORMER = BLOCKS.register("state_transformer", () ->
        new StateTransformerBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F)
            .sound(SoundType.STONE).noOcclusion()));
    public static final Supplier<BlockItem> STATE_TRANSFORMER_ITEM = ITEMS.register("state_transformer", () ->
        new BlockItem(STATE_TRANSFORMER.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<StateTransformerBlockEntity>> STATE_TRANSFORMER_BE =
        BLOCK_ENTITIES.register("state_transformer", () -> BlockEntityType.Builder
            .of(StateTransformerBlockEntity::new, STATE_TRANSFORMER.get()).build(null));
    public static final Supplier<MenuType<StateTransformerMenu>> STATE_TRANSFORMER_MENU =
        MENUS.register("state_transformer", () -> MenuCompat.create(StateTransformerMenu::new));
}
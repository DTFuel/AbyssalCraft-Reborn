package com.shinoow.abyssalcraft.content.machine.transmutator;

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
 * Transmutator registration (owned by PP-4): block + block-item + BE type + menu type + the
 * {@code transmutation} recipe type/serializer. Sibling of the PP-2 Crystallizer / PP-3 Materializer:
 * reuses the PP-1 {@link MachineMenu} and {@link ProcessingRecipe}; all registrars are attached to the
 * MOD bus via {@code ModRegistries.ALL}.
 */
public final class Transmutators {

    private Transmutators() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    public static final Supplier<Block> TRANSMUTATOR = BLOCKS.register("transmutator", () ->
        new TransmutatorBlock(BlockBehaviour.Properties.of().strength(3.5F)));

    public static final Supplier<BlockItem> TRANSMUTATOR_ITEM = ITEMS.register("transmutator", () ->
        new BlockItem(TRANSMUTATOR.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<TransmutatorBlockEntity>> TRANSMUTATOR_BE = BLOCK_ENTITIES.register("transmutator", () ->
        BlockEntityType.Builder.<TransmutatorBlockEntity>of(
            (pos, state) -> new TransmutatorBlockEntity(pos, state), TRANSMUTATOR.get()).build(null));

    public static final Supplier<MenuType<TransmutatorMenu>> TRANSMUTATOR_MENU = MENUS.register("transmutator", () ->
        MenuCompat.create(TransmutatorMenu::new));

}

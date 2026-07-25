package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.demo.DemoBlock;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Demo registrar -- the M0 minimal vertical slice (owned by PA-5).
 *
 * <p>Registers one placeable block (+ its {@link BlockItem}), one plain item, and a dedicated creative
 * tab holding both, exercising the full content path (block/item registration -&gt; assets -&gt;
 * creative menu) on top of the compat layer. Everything here is vanilla API shared by both loader
 * nodes, so this business-package file carries no {@code //?} fork; the loader-forked DeferredRegister
 * lives in {@link ModRegistrar}. The three registrars are attached to the MOD bus by the main class
 * through {@link ModRegistries#ALL}.
 */
public final class DemoRegistrar {

    private DemoRegistrar() {}

    /** {@code minecraft:block} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<Block> BLOCKS =
        ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<Item> ITEMS =
        ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** {@code minecraft:creative_mode_tab} registrar for the demo tab. */
    public static final ModRegistrar<CreativeModeTab> TABS =
        ModRegistrar.of(Registries.CREATIVE_MODE_TAB, AbyssalCraft.MODID);

    /** The placeable demo block. */
    public static final Supplier<DemoBlock> DEMO_BLOCK = BLOCKS.register("demo_block", () ->
        new DemoBlock(BlockBehaviour.Properties.of().strength(1.5F)));

    /** {@link BlockItem} for {@link #DEMO_BLOCK}. */
    public static final Supplier<BlockItem> DEMO_BLOCK_ITEM = ITEMS.register("demo_block", () ->
        new BlockItem(DEMO_BLOCK.get(), new Item.Properties()));

    /** A plain demo item. */
    public static final Supplier<Item> DEMO_ITEM = ITEMS.register("demo_item", () ->
        new Item(new Item.Properties()));

    /** Dedicated creative tab listing the demo block and item so they are visible and placeable. */
    public static final Supplier<CreativeModeTab> DEMO_TAB = TABS.register("demo", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.demo"))
            .icon(() -> new ItemStack(DEMO_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(DEMO_BLOCK_ITEM.get());
                output.accept(DEMO_ITEM.get());
            })
            .build());
}

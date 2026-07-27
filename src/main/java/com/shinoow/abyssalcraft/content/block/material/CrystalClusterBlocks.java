package com.shinoow.abyssalcraft.content.block.material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

public final class CrystalClusterBlocks {

    private CrystalClusterBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final List<Supplier<CrystalClusterBlock>> CLUSTERS = new ArrayList<>();
    public static final List<Supplier<CrystalClusterBlock>> MACHINE_COMPAT_CLUSTERS = new ArrayList<>();

    static {
        for (String element : MaterialItems.CRYSTAL_ELEMENTS) {
            CLUSTERS.add(reg(element));
        }
        for (String element : MaterialItems.MACHINE_COMPAT_ELEMENTS) {
            MACHINE_COMPAT_CLUSTERS.add(reg(element));
        }
    }

    private static Supplier<CrystalClusterBlock> reg(String element) {
        String name = element + "_crystal_cluster";
        Supplier<CrystalClusterBlock> block = BLOCKS.register(name, () -> new CrystalClusterBlock(
            BlockBehaviour.Properties.of().strength(4.0F, 8.0F).sound(SoundType.GLASS)
                .requiresCorrectToolForDrops().noOcclusion(), element));
        ITEMS.register(name, () -> new CrystalClusterItem(block.get(), new Item.Properties()));
        return block;
    }
}
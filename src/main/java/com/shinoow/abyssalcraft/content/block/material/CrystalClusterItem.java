package com.shinoow.abyssalcraft.content.block.material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class CrystalClusterItem extends BlockItem {

    private final CrystalClusterBlock cluster;

    public CrystalClusterItem(CrystalClusterBlock cluster, Properties properties) {
        super(cluster, properties);
        this.cluster = cluster;
    }

    @Override
    public Component getName(ItemStack stack) {
        return cluster.getName();
    }
}
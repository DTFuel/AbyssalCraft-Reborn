package com.shinoow.abyssalcraft.system.ritual;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Mutable altar surface exposed to ritual completion behaviors. */
public interface RitualHost {

    ItemStack ritualCenter();

    void setRitualCenter(ItemStack stack);

    List<ItemStack> ritualOfferingSnapshot();

    List<BlockPos> ritualPedestalPositions();

    void fillRitualPedestals(ItemStack stack);
}
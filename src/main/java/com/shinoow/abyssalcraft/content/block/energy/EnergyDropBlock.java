package com.shinoow.abyssalcraft.content.block.energy;

import java.util.List;

import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Shared self-drop behavior that carries a block entity's legacy {@code PotEnergy}. */
public abstract class EnergyDropBlock extends InteractiveBlockCompat {

    protected EnergyDropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Object blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        float energy = blockEntity instanceof IEnergyContainer container ? container.getContainedEnergy() : 0.0F;
        return List.of(stackWithEnergy(state, energy));
    }

    public static ItemStack stackWithEnergy(BlockState state, float energy) {
        ItemStack stack = new ItemStack(state.getBlock());
        if (energy > 0) {
            ItemDataCompat.putFloat(stack, EnergyBlockItem.ENERGY_KEY, energy);
        }
        return stack;
    }
}
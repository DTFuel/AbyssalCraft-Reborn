package com.shinoow.abyssalcraft.system.energy;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * An item that can hold Potential Energy (owned by PS-5), faithful to the 1.12.2
 * {@code api.energy.IEnergyContainerItem}. The stored PE lives in the stack's mod data (read/written
 * fork-free through {@link ItemDataCompat}), so every implementor only supplies its {@link #getMaxEnergy}.
 * This is the item-side counterpart to the block-entity {@link IEnergyContainer}, and the piece the
 * energy pedestal pulls from, the spell system drains, and the HUD PE meter reads.
 */
public interface IEnergyContainerItem {

    /** NBT / component key the stored PE lives under. */
    String ENERGY_KEY = "PotentialEnergy";

    /** The maximum Potential Energy this item can hold (e.g. by Necronomicon tier). */
    int getMaxEnergy(ItemStack stack);

    /** The Potential Energy currently stored in {@code stack}. */
    default float getContainedEnergy(ItemStack stack) {
        return ItemDataCompat.getFloat(stack, ENERGY_KEY);
    }

    /** Set the stored Potential Energy (clamped to {@code [0, maxEnergy]}). */
    default void setEnergy(ItemStack stack, float energy) {
        ItemDataCompat.putFloat(stack, ENERGY_KEY, Mth.clamp(energy, 0F, getMaxEnergy(stack)));
    }

    /** Add PE to {@code stack}; returns the overflow that did not fit. */
    default float addEnergy(ItemStack stack, float energy) {
        if (energy <= 0) {
            return 0;
        }
        float added = Math.min(getMaxEnergy(stack) - getContainedEnergy(stack), energy);
        if (added > 0) {
            setEnergy(stack, getContainedEnergy(stack) + added);
        }
        return energy - added;
    }

    /** Consume up to {@code energy} PE from {@code stack}; returns the amount actually consumed. */
    default float consumeEnergy(ItemStack stack, float energy) {
        if (energy <= 0) {
            return 0;
        }
        float consumed = Math.min(getContainedEnergy(stack), energy);
        if (consumed > 0) {
            setEnergy(stack, getContainedEnergy(stack) - consumed);
        }
        return consumed;
    }

    /** Whether this item has room for (and accepts) more PE. */
    default boolean canAcceptPE(ItemStack stack) {
        return getContainedEnergy(stack) < getMaxEnergy(stack);
    }

    /** Whether this item has PE it can transfer out. */
    default boolean canTransferPE(ItemStack stack) {
        return getContainedEnergy(stack) > 0;
    }
}

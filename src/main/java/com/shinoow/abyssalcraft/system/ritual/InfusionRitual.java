package com.shinoow.abyssalcraft.system.ritual;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An infusion ritual (owned by PS-6): consumes its offerings and produces {@code result} above the altar.
 * Faithful to the 1.12.2 {@code NecronomiconInfusionRitual} (transmutation gem / oblivion catalyst / gateway
 * keys / etc.), the most common ritual type. The result is a {@link Supplier} so it can reference an item
 * registered after ritual registration (mod items register on the bus, after the init that seeds rituals);
 * it is resolved lazily at completion. Fork-free item spawn (like PD-4's death substitute).
 */
public final class InfusionRitual extends Ritual {

    private final Supplier<ItemStack> result;

    public InfusionRitual(String name, int bookType, ResourceKey<Level> dimension, float requiredEnergy,
                          Supplier<ItemStack> result, ItemStack... offerings) {
        super(name, bookType, dimension, requiredEnergy, false, offerings);
        this.result = result;
    }

    public InfusionRitual(String name, int bookType, ResourceKey<Level> dimension, float requiredEnergy,
                          boolean requiresSacrifice, RitualIngredient center, Supplier<ItemStack> result,
                          RitualIngredient... offerings) {
        super(name, bookType, dimension, requiredEnergy, requiresSacrifice, center, offerings);
        this.result = result;
    }

    public ItemStack result() {
        return result.get();
    }

    @Override
    public void complete(Level level, BlockPos altar, Player player) {
        if (level.isClientSide) {
            return;
        }
        ItemEntity drop = new ItemEntity(level, altar.getX() + 0.5, altar.getY() + 1.2, altar.getZ() + 0.5, result.get().copy());
        drop.setDeltaMovement(0, 0.1, 0);
        level.addFreshEntity(drop);
    }
}

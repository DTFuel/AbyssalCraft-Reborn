package com.shinoow.abyssalcraft.content.item.crystal;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Pilot crystal items -- the minimal item set for the pilot machine subsystem (owned by PP-1).
 *
 * <p>A single crystallization product and a single machine fuel: enough for the P2 machines to have a
 * real input/output/fuel to reference. Plain vanilla {@link Item}s (no custom behaviour yet), so this
 * business file carries no loader fork; the forked DeferredRegister lives in {@link ModRegistrar}.
 * Models/textures are deferred to the asset stage (PK); registration does not require them.
 */
public final class CrystalItems {

    private CrystalItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for pilot items. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Pilot crystallization product. */
    public static final Supplier<Item> PILOT_CRYSTAL = ITEMS.register("pilot_crystal", () -> new Item(new Item.Properties()));

    /** Pilot machine fuel. */
    public static final Supplier<Item> PILOT_FUEL = ITEMS.register("pilot_fuel", () -> new Item(new Item.Properties()));
}

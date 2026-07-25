package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

/**
 * Compat: deferred spawn-egg item construction (loader axis).
 *
 * <p>Vanilla {@code SpawnEggItem} demands the {@link EntityType} up front, which a DeferredRegister
 * cannot provide at item-construction time. Both loaders ship a supplier-based variant with the same
 * constructor shape but a different class -- Forge {@code net.minecraftforge.common.ForgeSpawnEggItem}
 * vs NeoForge {@code net.neoforged.neoforge.common.DeferredSpawnEggItem}. Business code
 * (registry/AntiEntities and future family registrars) funnels through here to stay fork-free.
 */
public final class SpawnEggCompat {

    private SpawnEggCompat() {}

    /** Build a spawn egg for {@code type} with the given background / highlight tint colours. */
    public static Item create(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight) {
        //? if forge {
        return new net.minecraftforge.common.ForgeSpawnEggItem(type, background, highlight, new Item.Properties());
        //?} else {
        /*return new net.neoforged.neoforge.common.DeferredSpawnEggItem(type, background, highlight, new Item.Properties());
        *///?}
    }
}

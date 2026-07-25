package com.shinoow.abyssalcraft.system.enchant;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Stable references to AbyssalCraft's five enchantments (owned by PS-3), faithful to the 1.12.2
 * {@code AbyssalCraftAPI} set: {@code blinding_light}, {@code iron_wall}, {@code light_pierce},
 * {@code multi_rend}, {@code sapping}.
 *
 * <p>The enchantment <em>definitions</em> are loader-forked (1.20.1 registers {@code Enchantment}
 * instances via {@code platform/EnchantmentCompat}; 1.21 loads them as datapack JSON under
 * {@code data/abyssalcraft/enchantment/}), but the {@link ResourceKey} identity is version-stable, so
 * business + effect code references an enchantment through these keys on both loaders. The enchantment
 * <em>effects</em> (iron-wall knockback lock, light-pierce bonus vs shadow, blinding light) land with
 * the event-handler subsystem (PS-11); {@code multi_rend}/{@code sapping} drive the not-yet-ported Staff
 * of Rending.
 */
public final class ACEnchantments {

    private ACEnchantments() {}

    public static final ResourceKey<Enchantment> BLINDING_LIGHT = key("blinding_light");
    public static final ResourceKey<Enchantment> IRON_WALL = key("iron_wall");
    public static final ResourceKey<Enchantment> LIGHT_PIERCE = key("light_pierce");
    public static final ResourceKey<Enchantment> MULTI_REND = key("multi_rend");
    public static final ResourceKey<Enchantment> SAPPING = key("sapping");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ACRef.id(name));
    }
}

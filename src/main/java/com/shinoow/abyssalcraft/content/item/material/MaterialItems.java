package com.shinoow.abyssalcraft.content.item.material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Material and crystal items (owned by PB-1, Stage B1).
 *
 * <p>The AbyssalCraft crafting-material tier ported from 1.12.2: ingots/nuggets/gems/gem-clusters/
 * chunks/bricks/dusts/plates/misc materials + {@code coin}, plus the elemental crystal system (26
 * elements &times; crystal / shard / fragment = 78 items). Every entry is a plain vanilla {@link Item}
 * (no custom behaviour yet), so this business file carries no loader fork; the forked
 * {@code DeferredRegister} lives in {@link ModRegistrar}.
 *
 * <p>The crystals are grayscale textures tinted per element at render time -- the colour table
 * {@link #CRYSTAL_COLORS} is applied through {@code platform/ClientColorCompat} (client-only, queued by
 * {@code client/ACClientSetup}). {@link #ALL} is the flat list the creative tab pulls from.
 */
public final class MaterialItems {

    private MaterialItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for the material tier. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Every registered material/crystal item, in registration order (creative tab pulls from this). */
    public static final List<Supplier<Item>> ALL = new ArrayList<>();

    /** The plain crafting materials only (no crystals) -- the Gate B "items" creative tab pulls from this. */
    public static final List<Supplier<Item>> BASICS = new ArrayList<>();

    /** Elemental crystal types (index = 1.12.2 {@code Crystals} ordinal), used for names + colours. */
    public static final String[] CRYSTAL_ELEMENTS = {
        "iron", "gold", "sulfur", "carbon", "oxygen", "hydrogen", "nitrogen", "phosphorus",
        "potassium", "nitrate", "methane", "redstone", "abyssalnite", "coralium", "dreadium",
        "blaze", "silicon", "magnesium", "aluminium", "silica", "alumina", "magnesia", "zinc",
        "calcium", "beryllium", "beryl"
    };

    /** Per-element tint colour (RGB), parallel to {@link #CRYSTAL_ELEMENTS} (from 1.12.2 ACClientVars). */
    public static final int[] CRYSTAL_COLORS = {
        0xD9D9D9, 0xF3CC3E, 0xF6FF00, 0x3D3D36, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0x996A18,
        0xD9D9D9, 0x1500FF, 0x19FC00, 0xFF0000, 0x4A1C89, 0x00FFEE, 0x880101, 0xFFCC00,
        0xD9D9D9, 0xD9D9D9, 0xD9D9D9, 0xFFFFFF, 0xD9D8D9, 0xFFFFFF, 0xD7D8D9, 0xD7D8D9,
        0xD9D9D9, 0xFFFFFF
    };

    /** Crystal / shard / fragment item per element, parallel to {@link #CRYSTAL_ELEMENTS} (for tinting). */
    public static final List<Supplier<Item>> CRYSTALS = new ArrayList<>();
    public static final List<Supplier<Item>> CRYSTAL_SHARDS = new ArrayList<>();
    public static final List<Supplier<Item>> CRYSTAL_FRAGMENTS = new ArrayList<>();

    /** Legacy machine-recipe compatibility crystals, kept outside the ordinal-indexed 26-element set. */
    public static final String[] MACHINE_COMPAT_ELEMENTS = {"copper", "tin"};
    public static final int[] MACHINE_COMPAT_COLORS = {0xD67C55, 0xD9D9D9};
    public static final List<Supplier<Item>> MACHINE_COMPAT_CRYSTALS = new ArrayList<>();
    public static final List<Supplier<Item>> MACHINE_COMPAT_SHARDS = new ArrayList<>();

    /** Plain crafting materials (registry id == texture id). */
    private static final String[] BASIC = {
        "abyssalnite_ingot", "refined_coralium_ingot", "dreadium_ingot", "ethaxium_ingot",
        "abyssalnite_nugget", "refined_coralium_nugget", "dreadium_nugget", "ethaxium_nugget",
        "coralium_gem", "coralium_pearl", "chunk_of_coralium", "dreaded_shard_of_abyssalnite",
        "coralium_brick", "ethaxium_brick", "charcoal", "methane", "nitre", "sulfur",
        "coralium_plate", "dreadium_plate", "shadow_fragment", "shadow_shard", "shadow_gem",
        "shard_of_oblivion", "dread_fragment", "carbon_cluster", "dense_carbon_cluster",
        "dread_cloth", "life_crystal", "eldritch_scale", "coin",
        "coralium_gem_cluster_2", "coralium_gem_cluster_3", "coralium_gem_cluster_4",
        "coralium_gem_cluster_5", "coralium_gem_cluster_6", "coralium_gem_cluster_7",
        "coralium_gem_cluster_8", "coralium_gem_cluster_9"
    };

    private static Supplier<Item> reg(String name) {
        Supplier<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties()));
        ALL.add(item);
        return item;
    }

    static {
        for (String name : BASIC) {
            BASICS.add(reg(name));
        }
        for (String element : CRYSTAL_ELEMENTS) {
            CRYSTALS.add(reg("crystal_" + element));
            CRYSTAL_SHARDS.add(reg("crystal_shard_" + element));
            CRYSTAL_FRAGMENTS.add(reg("crystal_fragment_" + element));
        }
        for (String element : MACHINE_COMPAT_ELEMENTS) {
            MACHINE_COMPAT_CRYSTALS.add(reg("crystal_" + element));
            MACHINE_COMPAT_SHARDS.add(reg("crystal_shard_" + element));
        }
    }
}

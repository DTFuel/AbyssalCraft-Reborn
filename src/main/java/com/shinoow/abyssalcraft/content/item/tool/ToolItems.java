package com.shinoow.abyssalcraft.content.item.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.ToolCompat;

/**
 * Tool items (owned by PB-6): 4 AbyssalCraft material tiers x {pickaxe, axe, shovel, hoe, sword} = 20.
 *
 * <p>Tier stats (uses/speed/attack/enchantability/harvest level) are the 1.12.2
 * {@code AbyssalCraftAPI} values; per-tool attack/speed modifiers follow vanilla conventions. All the
 * loader/version divergence (Tier harvest field, tool item constructors) lives in
 * {@link ToolCompat}, so this business file carries no {@code //?}. Special-effect tools (longbow,
 * cudgel, katana, soul reaper, staves of rending) are deferred to their systems (M7).
 *
 * <p>Repair ingredients are the tier's ingot from PB-1, resolved lazily by registry id (so this task
 * only <em>reads</em> PB-1 and never touches its files). Models/textures ship in the asset stage (PK);
 * creative-tab placement is a Gate B action. The registrar is attached to the MOD bus via
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class ToolItems {

    private ToolItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for tools. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Every registered tool, in registration order (for datagen / creative fill). */
    public static final List<Supplier<Item>> ALL = new ArrayList<>();

    // Tiers: (harvest level, uses, speed, attack bonus, enchantment value, repair ingot) -- 1.12.2 AbyssalCraftAPI.
    private static final Tier ABYSSALNITE = ToolCompat.tier("abyssalnite", 4, 1261, 10.0F, 4.0F, 12, repair("abyssalnite_ingot"));
    private static final Tier CORALIUM = ToolCompat.tier("refined_coralium", 5, 1800, 12.0F, 5.0F, 13, repair("refined_coralium_ingot"));
    private static final Tier DREADIUM = ToolCompat.tier("dreadium", 6, 2300, 14.0F, 6.0F, 14, repair("dreadium_ingot"));
    private static final Tier ETHAXIUM = ToolCompat.tier("ethaxium", 8, 2800, 16.0F, 8.0F, 20, repair("ethaxium_ingot"));

    static {
        registerTier("abyssalnite", ABYSSALNITE);
        registerTier("refined_coralium", CORALIUM);
        registerTier("dreadium", DREADIUM);
        registerTier("ethaxium", ETHAXIUM);
    }

    public static final Supplier<Item> CUDGEL = special("cudgel", () ->
        ToolCompat.cudgel(new Item.Properties().durability(1500)));

    /** Register the five standard tools for one tier (vanilla-convention attack/speed modifiers). */
    private static void registerTier(String prefix, Tier tier) {
        ALL.add(ITEMS.register(prefix + "_pickaxe", () -> ToolCompat.pickaxe(tier, 1, -2.8F, new Item.Properties())));
        ALL.add(ITEMS.register(prefix + "_axe", () -> ToolCompat.axe(tier, 6.0F, -3.0F, new Item.Properties())));
        ALL.add(ITEMS.register(prefix + "_shovel", () -> ToolCompat.shovel(tier, 1.5F, -3.0F, new Item.Properties())));
        ALL.add(ITEMS.register(prefix + "_hoe", () -> ToolCompat.hoe(tier, -3, 0.0F, new Item.Properties())));
        ALL.add(ITEMS.register(prefix + "_sword", () -> ToolCompat.sword(tier, 3, -2.4F, new Item.Properties())));
    }

    private static Supplier<Item> special(String id, Supplier<Item> factory) {
        Supplier<Item> item = ITEMS.register(id, factory);
        ALL.add(item);
        return item;
    }

    /** Lazy repair ingredient resolved by registry id (the material ingots are registered by PB-1). */
    private static Supplier<Ingredient> repair(String ingotId) {
        return () -> Ingredient.of(BuiltInRegistries.ITEM.get(ACRef.id(ingotId)));
    }
}

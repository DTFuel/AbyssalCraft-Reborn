package com.shinoow.abyssalcraft.content.item.armor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Armor items (owned by PB-7): 7 AbyssalCraft armor materials x {helmet, chestplate, leggings, boots} = 28.
 *
 * <p>Material stats (durability multiplier, per-slot defense, enchantability, toughness) are the
 * 1.12.2 {@code AbyssalCraftAPI} values; knockback resistance is 0 (a 1.9+ concept absent from the
 * legacy materials). All the loader/version divergence ({@code ArmorMaterial} interface vs record +
 * {@code Holder}, durability placement) lives in {@link ArmorCompat}, so this business file carries no
 * {@code //?}.
 *
 * <p>Repair ingredients are the material's ingot/plate from PB-1, resolved lazily by registry id (so
 * this task only <em>reads</em> PB-1 and never touches its files). Worn-armor layer textures ship in
 * the render stage; item models/textures in the asset stage (PK); creative-tab placement is a Gate B
 * action. The registrar is attached to the MOD bus via
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class ArmorItems {

    private ArmorItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for armor. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Every registered armor piece, in registration order (for datagen / creative fill). */
    public static final List<Supplier<Item>> ALL = new ArrayList<>();

    // Materials: (name, durabilityMultiplier, defense{boots, legs, chest, helmet}, enchantValue, toughness, knockback, repair) -- 1.12.2 AbyssalCraftAPI.
    private static final ArmorCompat.Material ABYSSALNITE = ArmorCompat.material("abyssalnite", 35, 3, 6, 8, 3, 13, 1.0F, 0.0F, repair("abyssalnite_ingot"));
    private static final ArmorCompat.Material REFINED_CORALIUM = ArmorCompat.material("refined_coralium", 37, 3, 6, 8, 3, 14, 1.0F, 0.0F, repair("refined_coralium_ingot"));
    private static final ArmorCompat.Material PLATED_CORALIUM = ArmorCompat.material("plated_coralium", 55, 4, 7, 9, 4, 14, 3.0F, 0.0F, repair("coralium_plate"));
    private static final ArmorCompat.Material DEPTHS = ArmorCompat.material("depths", 33, 3, 6, 8, 3, 25, 1.5F, 0.0F, repair("refined_coralium_ingot"));
    private static final ArmorCompat.Material DREADIUM = ArmorCompat.material("dreadium", 40, 3, 6, 8, 3, 15, 1.0F, 0.0F, repair("dreadium_ingot"));
    private static final ArmorCompat.Material DREADIUM_SAMURAI = ArmorCompat.material("dreadium_samurai", 45, 3, 6, 8, 3, 20, 1.5F, 0.0F, repair("dreadium_ingot"));
    private static final ArmorCompat.Material ETHAXIUM = ArmorCompat.material("ethaxium", 50, 3, 6, 8, 3, 25, 2.0F, 0.0F, repair("ethaxium_ingot"));

    static {
        registerSet("abyssalnite", ABYSSALNITE);
        registerSet("refined_coralium", REFINED_CORALIUM);
        registerSet("plated_coralium", PLATED_CORALIUM);
        registerSet("depths", DEPTHS);
        registerSet("dreadium", DREADIUM);
        registerSet("dreadium_samurai", DREADIUM_SAMURAI);
        registerSet("ethaxium", ETHAXIUM);
    }

    /** Register the four armor pieces for one material. */
    private static void registerSet(String prefix, ArmorCompat.Material material) {
        ArmorCompat.Visual visual = prefix.equals("dreadium_samurai") ? ArmorCompat.Visual.DREADIUM_SAMURAI
            : prefix.equals("depths") ? ArmorCompat.Visual.DEPTHS : ArmorCompat.Visual.STANDARD;
        ALL.add(ITEMS.register(prefix + "_helmet", () -> ArmorCompat.piece(material, ArmorItem.Type.HELMET, new Item.Properties(), visual)));
        ALL.add(ITEMS.register(prefix + "_chestplate", () -> ArmorCompat.piece(material, ArmorItem.Type.CHESTPLATE, new Item.Properties(), visual)));
        ALL.add(ITEMS.register(prefix + "_leggings", () -> ArmorCompat.piece(material, ArmorItem.Type.LEGGINGS, new Item.Properties(), visual)));
        ALL.add(ITEMS.register(prefix + "_boots", () -> ArmorCompat.piece(material, ArmorItem.Type.BOOTS, new Item.Properties(), visual)));
    }

    /** Lazy repair ingredient resolved by registry id (the material ingots/plates are registered by PB-1). */
    private static Supplier<Ingredient> repair(String itemId) {
        return () -> Ingredient.of(BuiltInRegistries.ITEM.get(ACRef.id(itemId)));
    }
}

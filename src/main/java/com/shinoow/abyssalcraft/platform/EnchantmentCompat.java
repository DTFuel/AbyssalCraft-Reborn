package com.shinoow.abyssalcraft.platform;

//? if forge {
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import com.shinoow.abyssalcraft.AbyssalCraft;
//?}

/**
 * Compat: enchantment registration (the deepest 1.20&harr;1.21 divergence after networking).
 *
 * <p>1.20.1 enchantments are code ({@code Enchantment} subclasses in a registry); 1.21 made them
 * fully datapack-driven (JSON under {@code data/<ns>/enchantment/}). This class owns the Forge-side
 * registration of AbyssalCraft's five enchantments; on NeoForge it is a no-op because the same five
 * live as datapack JSON (business references them uniformly through {@code system/enchant/ACEnchantments}
 * {@link net.minecraft.resources.ResourceKey}s). Faithful 1.12.2 metadata (rarity / cost / level /
 * slot); enchantment <em>effects</em> land with the event-handler subsystem (PS-11).
 *
 * <p>Call {@link #bootstrap(Object)} once from the mod constructor with the MOD event bus.
 */
public final class EnchantmentCompat {

    private EnchantmentCompat() {}

    //? if forge {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, AbyssalCraft.MODID);

    /** Custom category matching the (not-yet-ported) Staff of Rending; matches nothing for now. */
    private static final EnchantmentCategory STAFF_OF_RENDING =
            EnchantmentCategory.create("abyssalcraft_staff_of_rending", item -> false);

    static {
        // (rarity, category, slot, minCostBase, minCostPerLevel, costRange, maxLevel) -- faithful 1.12.2.
        register("blinding_light", Enchantment.Rarity.COMMON, EnchantmentCategory.BREAKABLE, EquipmentSlot.OFFHAND, 14, 0, 30, 1);
        register("iron_wall", Enchantment.Rarity.UNCOMMON, EnchantmentCategory.ARMOR_CHEST, EquipmentSlot.CHEST, 14, 0, 30, 1);
        register("light_pierce", Enchantment.Rarity.COMMON, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, 5, 8, 20, 5);
        register("multi_rend", Enchantment.Rarity.RARE, STAFF_OF_RENDING, EquipmentSlot.MAINHAND, 20, 0, 20, 1);
        register("sapping", Enchantment.Rarity.UNCOMMON, STAFF_OF_RENDING, EquipmentSlot.MAINHAND, 12, 8, 20, 3);
    }

    private static void register(String name, Enchantment.Rarity rarity, EnchantmentCategory cat,
            EquipmentSlot slot, int minBase, int minPerLevel, int costRange, int maxLevel) {
        ENCHANTMENTS.register(name,
                () -> new AbyssalEnchantment(rarity, cat, new EquipmentSlot[] {slot}, minBase, minPerLevel, costRange, maxLevel));
    }

    public static void bootstrap(Object modBus) {
        ENCHANTMENTS.register((IEventBus) modBus);
    }

    /** Generic faithful-metadata enchantment; effects live in the event-handler subsystem (PS-11). */
    private static final class AbyssalEnchantment extends Enchantment {
        private final int minBase;
        private final int minPerLevel;
        private final int costRange;
        private final int maxLvl;

        AbyssalEnchantment(Rarity rarity, EnchantmentCategory cat, EquipmentSlot[] slots,
                int minBase, int minPerLevel, int costRange, int maxLvl) {
            super(rarity, cat, slots);
            this.minBase = minBase;
            this.minPerLevel = minPerLevel;
            this.costRange = costRange;
            this.maxLvl = maxLvl;
        }

        @Override
        public int getMinCost(int level) {
            return minBase + (level - 1) * minPerLevel;
        }

        @Override
        public int getMaxCost(int level) {
            return getMinCost(level) + costRange;
        }

        @Override
        public int getMaxLevel() {
            return maxLvl;
        }
    }
    //?} else {
    /*public static void bootstrap(Object modBus) {
        // NeoForge 1.21: enchantments are datapack-driven (data/abyssalcraft/enchantment/*.json).
        // Nothing to register in code; the ResourceKeys in ACEnchantments reference the loaded entries.
    }
    *///?}
}

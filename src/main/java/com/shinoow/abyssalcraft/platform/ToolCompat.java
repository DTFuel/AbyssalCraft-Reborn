package com.shinoow.abyssalcraft.platform;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Compat: tool tiers + tool items (vanilla axis).
 *
 * <p>1.21 reworked tools: {@code Tier.getLevel()} (int harvest level) became
 * {@code getIncorrectBlocksForDrops()} (a block tag), and the tool item constructors dropped their
 * {@code (damage, speed)} arguments in favour of attribute components ({@code createAttributes(...)}
 * on each tool class, applied through {@code Item.Properties.attributes(...)}). Business tool code
 * builds its tiers and items through this class so it stays fork-free; the durability comes from the
 * tier's {@code getUses()} (the tool item sets it, as in vanilla).
 */
public final class ToolCompat {

    private ToolCompat() {}

    /**
     * Build a tool {@link Tier} from the classic 1.12.2 parameters. Forge registers the four AC tiers
     * after netherite in its sorting registry; 1.21 resolves the same order through each tier's
     * {@code incorrect_for_*} tag. {@code repair} is resolved lazily so it can reference material items.
     */
    public static Tier tier(String name, int level, int uses, float speed, float attackBonus, int enchantValue,
                            Supplier<Ingredient> repair) {
        Tier tier = new Tier() {
            @Override public int getUses() { return uses; }
            @Override public float getSpeed() { return speed; }
            @Override public float getAttackDamageBonus() { return attackBonus; }
            @Override public int getEnchantmentValue() { return enchantValue; }
            @Override public Ingredient getRepairIngredient() { return repair.get(); }
            //? if >=1.21 {
            /*@Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
                return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    ACRef.id("incorrect_for_" + name + "_tool"));
            }
            *///?} else {
            @Override public int getLevel() { return level; }
            @Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getTag() {
                if (name.equals("abyssalnite")) return null;
                return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    ACRef.id("requires_" + name + "_tool"));
            }
            //?}
        };
        //? if forge {
        Object previous = switch (name) {
            case "abyssalnite" -> net.minecraft.world.item.Tiers.NETHERITE;
            case "refined_coralium" -> ACRef.id("abyssalnite");
            case "dreadium" -> ACRef.id("refined_coralium");
            case "ethaxium" -> ACRef.id("dreadium");
            default -> throw new IllegalArgumentException("Unknown AbyssalCraft tool tier " + name);
        };
        return net.minecraftforge.common.TierSortingRegistry.registerTier(
            tier, ACRef.id(name), List.of(previous), List.of());
        //?} else {
        /*return tier;
        *///?}
    }

    /** Sword ({@code dmg} is an int, matching the vanilla sword constructor/createAttributes). */
    public static Item sword(Tier tier, int dmg, float speed, Item.Properties props) {
        //? if >=1.21 {
        /*return new SwordItem(tier, props.attributes(SwordItem.createAttributes(tier, dmg, speed)));
        *///?} else {
        return new SwordItem(tier, dmg, speed, props);
        //?}
    }

    public static Item soulReaper(Item.Properties props) {
        Tier tier = new Tier() {
            @Override public int getUses() { return 2000; }
            @Override public float getSpeed() { return 1.0F; }
            @Override public float getAttackDamageBonus() { return 26.0F; }
            @Override public int getEnchantmentValue() { return 0; }
            @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
            //? if >=1.21 {
            /*@Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
                return net.minecraft.tags.BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            }
            *///?} else {
            @Override public int getLevel() { return 0; }
            @Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getTag() { return null; }
            //?}
        };
        //? if >=1.21 {
        /*return new SwordItem(tier, props.attributes(SwordItem.createAttributes(tier, 3, -2.4F))) {
            @Override public boolean isEnchantable(ItemStack stack) { return false; }
        };
        *///?} else {
        return new SwordItem(tier, 3, -2.4F, props) {
            @Override public boolean isEnchantable(ItemStack stack) { return false; }
        };
        //?}
    }

    /** The legacy Cudgel: 1500 durability, +19 attack, -2.4 speed, bone repair and no enchanting. */
    public static Item cudgel(Item.Properties props) {
        Tier tier = new Tier() {
            @Override public int getUses() { return 1500; }
            @Override public float getSpeed() { return 1.0F; }
            @Override public float getAttackDamageBonus() { return 0.0F; }
            @Override public int getEnchantmentValue() { return 0; }
            @Override public Ingredient getRepairIngredient() { return Ingredient.of(net.minecraft.world.item.Items.BONE); }
            //? if >=1.21 {
            /*@Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
                return net.minecraft.tags.BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            }
            *///?} else {
            @Override public int getLevel() { return 0; }
            @Override public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getTag() { return null; }
            //?}
        };
        //? if >=1.21 {
        /*return new SwordItem(tier, props.attributes(SwordItem.createAttributes(tier, 19, -2.4F))) {
            @Override public boolean isEnchantable(ItemStack stack) { return false; }
        };
        *///?} else {
        return new SwordItem(tier, 19, -2.4F, props) {
            @Override public boolean isEnchantable(ItemStack stack) { return false; }
        };
        //?}
    }

    /** Pickaxe ({@code dmg} is an int). */
    public static Item pickaxe(Tier tier, int dmg, float speed, Item.Properties props) {
        //? if >=1.21 {
        /*return new PickaxeItem(tier, props.attributes(PickaxeItem.createAttributes(tier, dmg, speed)));
        *///?} else {
        return new PickaxeItem(tier, dmg, speed, props);
        //?}
    }

    /** Axe ({@code dmg} is a float). */
    public static Item axe(Tier tier, float dmg, float speed, Item.Properties props) {
        //? if >=1.21 {
        /*return new AxeItem(tier, props.attributes(AxeItem.createAttributes(tier, dmg, speed)));
        *///?} else {
        return new AxeItem(tier, dmg, speed, props);
        //?}
    }

    /** Shovel ({@code dmg} is a float). */
    public static Item shovel(Tier tier, float dmg, float speed, Item.Properties props) {
        //? if >=1.21 {
        /*return new ShovelItem(tier, props.attributes(ShovelItem.createAttributes(tier, dmg, speed)));
        *///?} else {
        return new ShovelItem(tier, dmg, speed, props);
        //?}
    }

    /** Hoe ({@code dmg} is an int). */
    public static Item hoe(Tier tier, int dmg, float speed, Item.Properties props) {
        //? if >=1.21 {
        /*return new HoeItem(tier, props.attributes(HoeItem.createAttributes(tier, dmg, speed)));
        *///?} else {
        return new HoeItem(tier, dmg, speed, props);
        //?}
    }
}

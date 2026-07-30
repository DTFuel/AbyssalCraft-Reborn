package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

//? if >=1.21 {
/*import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;

/**
 * Compat: armor materials + armor items (vanilla axis).
 *
 * <p>1.21 reworked armor: {@code ArmorMaterial} changed from an interface (1.20.1, anonymously
 * implemented) into a {@code record} referenced through a {@code Holder<ArmorMaterial>}, and per-piece
 * durability moved from the material ({@code getDurabilityForType}) onto the item's
 * {@code Item.Properties.durability(type.getDurability(factor))}. Business armor code builds its
 * materials and pieces through this class so it stays fork-free.
 *
 * <p>Defense values follow the classic 1.12.2 {@code EnumHelper.addArmorMaterial} slot order
 * {@code {boots, leggings, chestplate, helmet}} (the {@code EntityEquipmentSlot} index order). Worn
 * armor layer textures (PE-5) resolve to {@code abyssalcraft:textures/models/armor/<name>_layer_{1,2}.png}:
 * 1.20.1 through the namespaced {@code getName()} (Forge patches the armor-location split), 1.21 through
 * a single-entry {@code Layer} list. The item icon still comes from the item model (PK).
 */
public final class ArmorCompat {

    public enum Visual {
        STANDARD,
        DEPTHS,
        DREADIUM_SAMURAI
    }

    private ArmorCompat() {}

    /** Neutral armor-material descriptor, built once per set and converted per piece by {@link #piece}. */
    public static final class Material {
        final String name;
        final int durabilityMultiplier;
        final int boots, legs, chest, helmet;
        final int enchantValue;
        final float toughness;
        final float knockbackResistance;
        final Supplier<Ingredient> repair;

        Material(String name, int durabilityMultiplier, int boots, int legs, int chest, int helmet,
                 int enchantValue, float toughness, float knockbackResistance, Supplier<Ingredient> repair) {
            this.name = name;
            this.durabilityMultiplier = durabilityMultiplier;
            this.boots = boots;
            this.legs = legs;
            this.chest = chest;
            this.helmet = helmet;
            this.enchantValue = enchantValue;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repair = repair;
        }

        int defense(ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> helmet;
                case CHESTPLATE -> chest;
                case LEGGINGS -> legs;
                case BOOTS -> boots;
                default -> 0;
            };
        }
    }

    /**
     * Build an armor material from the classic 1.12.2 {@code AbyssalCraftAPI} parameters. Defense is
     * given in {@code {boots, leggings, chestplate, helmet}} slot order.
     */
    public static Material material(String name, int durabilityMultiplier, int boots, int legs, int chest, int helmet,
                                    int enchantValue, float toughness, float knockbackResistance, Supplier<Ingredient> repair) {
        return new Material(name, durabilityMultiplier, boots, legs, chest, helmet, enchantValue, toughness, knockbackResistance, repair);
    }

    /** Per-slot base durability (vanilla {@code HEALTH_PER_SLOT}), shared by both loaders. */
    private static int baseDurability(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
            default -> 13;
        };
    }

    /** Build one armor piece ({@code type} = HELMET / CHESTPLATE / LEGGINGS / BOOTS) for a material. */
    public static Item piece(Material m, ArmorItem.Type type, Item.Properties props) {
        return piece(m, type, props, Visual.STANDARD);
    }

    public static Item piece(Material m, ArmorItem.Type type, Item.Properties props, Visual visual) {
        //? if >=1.21 {
        /*ArmorMaterial material = new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, m.helmet, ArmorItem.Type.CHESTPLATE, m.chest,
                   ArmorItem.Type.LEGGINGS, m.legs, ArmorItem.Type.BOOTS, m.boots),
            m.enchantValue, SoundEvents.ARMOR_EQUIP_IRON, m.repair,
            List.of(new ArmorMaterial.Layer(ACRef.id(m.name))), m.toughness, m.knockbackResistance);
        return new VisualArmorItem(Holder.direct(material), type,
            props.durability(type.getDurability(m.durabilityMultiplier)), visual);
        *///?} else {
        ArmorMaterial material = new ArmorMaterial() {
            @Override public int getDurabilityForType(ArmorItem.Type t) { return baseDurability(t) * m.durabilityMultiplier; }
            @Override public int getDefenseForType(ArmorItem.Type t) { return m.defense(t); }
            @Override public int getEnchantmentValue() { return m.enchantValue; }
            @Override public net.minecraft.sounds.SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
            @Override public Ingredient getRepairIngredient() { return m.repair.get(); }
            @Override public String getName() { return AbyssalCraft.MODID + ":" + m.name; }
            @Override public float getToughness() { return m.toughness; }
            @Override public float getKnockbackResistance() { return m.knockbackResistance; }
        };
        return visual == Visual.DREADIUM_SAMURAI
            ? new SamuraiArmorItem(material, type, props)
            : new VisualArmorItem(material, type, props, visual);
        //?}
    }

    public static final class VisualArmorItem extends ArmorItem {
        private final Visual visual;

        //? if >=1.21 {
        /*VisualArmorItem(net.minecraft.core.Holder<ArmorMaterial> material, Type type,
                        Item.Properties properties, Visual visual) {
            super(material, type, properties);
            this.visual = visual;
        }
        *///?} else {
        VisualArmorItem(ArmorMaterial material, Type type, Item.Properties properties, Visual visual) {
            super(material, type, properties);
            this.visual = visual;
        }
        //?}

        public Visual visual() {
            return visual;
        }
    }

    //? if <1.21 {
    public static final class SamuraiArmorItem extends ArmorItem {

        SamuraiArmorItem(ArmorMaterial material, Type type, Item.Properties properties) {
            super(material, type, properties);
        }

        @Override
        public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
            consumer.accept(ArmorClientCompat.samuraiExtension());
        }
    }
    //?}
}

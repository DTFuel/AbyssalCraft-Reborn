package com.shinoow.abyssalcraft.content.item.misc;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.FoodCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import net.minecraft.world.effect.MobEffects;

/**
 * Food and miscellaneous items (owned by PB-2).
 *
 * <p>Ports the 1.12.2 "Food" section plus the plain miscellaneous item shells (catalyst, tokens,
 * essences, spirit-tablet shards). Every entry is a vanilla {@link Item}; food entries carry a
 * {@link net.minecraft.world.food.FoodProperties} built through {@link FoodCompat} (the only
 * version-forked bit). This business file therefore has no {@code //?} fork -- divergence lives in
 * the compat layer, per the frozen §2 rule.
 *
 * <p>Registry ids use the clean snake_case names (matching the design's naming examples), not the old
 * abbreviated ids. Deliberately out of scope here (deferred to the owning stage):
 * <ul>
 *   <li>Food eat-time potion effects incl. the custom coralium/dread plague -&gt; effect system (T7.10).</li>
 *   <li>Item models/textures -&gt; asset stage (PK); this task only registers + names the items.</li>
 *   <li>Creative-tab placement -&gt; Gate B integration on {@link ModCreativeTabs} (the relay file).</li>
 * </ul>
 * The registrar is attached to the MOD bus by the main class through {@link ModRegistries#ALL}.
 */
public final class MiscItems {

    private MiscItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for food/misc items. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    // --- Food (nutrition / saturation preserved from 1.12.2 ItemHandler; eat-time effects deferred to T7.10) ---
    public static final Supplier<Item> CORALIUM_PLAGUED_FLESH = effectFood("coralium_plagued_flesh", 2, 0.1F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 600, 1));
        if (!EffectHooks.isCoraliumImmune(entity)) {
            entity.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0));
        }
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION, 600, 0));
    });
    public static final Supplier<Item> ANTI_BEEF = food("anti_beef", 0, 0.0F);
    public static final Supplier<Item> ANTI_CHICKEN = food("anti_chicken", 0, 0.0F);
    public static final Supplier<Item> ANTI_PORK = food("anti_pork", 0, 0.0F);
    public static final Supplier<Item> ROTTEN_ANTI_FLESH = food("rotten_anti_flesh", 0, 0.0F);
    public static final Supplier<Item> ANTI_SPIDER_EYE = food("anti_spider_eye", 0, 0.0F);
    public static final Supplier<Item> ANTI_PLAGUED_FLESH = effectFood("anti_plagued_flesh", 0, 0.0F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.SATURATION, 600, 1));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.REGENERATION, 600, 0));
    });
    public static final Supplier<Item> GENERIC_MEAT = food("generic_meat", 4, 0.4F);
    public static final Supplier<Item> COOKED_GENERIC_MEAT = food("cooked_generic_meat", 9, 0.9F);
    public static final Supplier<Item> GHOUL_FLESH = effectFood("ghoul_flesh", 2, 0.1F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 600, 1));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION, 600, 0));
    });
    public static final Supplier<Item> ABYSSAL_GHOUL_FLESH = effectFood("abyssal_ghoul_flesh", 2, 0.1F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 600, 1));
        if (!EffectHooks.isCoraliumImmune(entity)) {
            entity.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0));
        }
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION, 600, 0));
    });
    public static final Supplier<Item> DREADED_GHOUL_FLESH = effectFood("dreaded_ghoul_flesh", 2, 0.1F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 600, 1));
        entity.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 600, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION, 600, 0));
    });
    public static final Supplier<Item> OMOTHOL_GHOUL_FLESH = effectFood("omothol_ghoul_flesh", 3, 0.3F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.WEAKNESS, 100, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER,
            EffectHooks.isCoraliumImmune(entity) ? 300 : 400, 1));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION,
            EffectHooks.isCoraliumImmune(entity) ? 200 : 300, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.BLINDNESS, 40, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.NIGHT_VISION, 40, 0));
    });
    public static final Supplier<Item> SHADOW_GHOUL_FLESH = effectFood("shadow_ghoul_flesh", 2, 0.1F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 600, 1));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.CONFUSION, 600, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.BLINDNESS, 600, 0));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.NIGHT_VISION, 600, 0));
    });
    public static final Supplier<Item> ANTI_GHOUL_FLESH = effectFood("anti_ghoul_flesh", 0, 0.0F, (level, entity) -> {
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.SATURATION, 600, 1));
        entity.addEffect(MobEffectCompat.vanillaEffect(MobEffects.REGENERATION, 600, 0));
    });

    public static final Supplier<Item> CORALIUM_ANTIDOTE = ITEMS.register("coralium_antidote",
        () -> new AntidoteItem(ACEffects.CORALIUM_PLAGUE, ACEffects.CORALIUM_ANTIDOTE));
    public static final Supplier<Item> DREAD_ANTIDOTE = ITEMS.register("dread_antidote",
        () -> new AntidoteItem(ACEffects.DREAD_PLAGUE, ACEffects.DREAD_ANTIDOTE));

    public static final Supplier<Item> SKIN_OF_THE_ABYSSAL_WASTELAND = plain("skin_of_the_abyssal_wasteland");
    public static final Supplier<Item> SKIN_OF_THE_DREADLANDS = plain("skin_of_the_dreadlands");
    public static final Supplier<Item> SKIN_OF_OMOTHOL = plain("skin_of_omothol");

    // --- Shoggoth flesh (non-edible drops; 1.12.2 registered these as plain ItemACBasic). Added by PD-5
    //     to complete the flesh roster next to the ghoul fleshes; consumed by the shoggoth loot tables. ---
    public static final Supplier<Item> OVERWORLD_SHOGGOTH_FLESH = plain("overworld_shoggoth_flesh");
    public static final Supplier<Item> ABYSSAL_SHOGGOTH_FLESH = plain("abyssal_shoggoth_flesh");
    public static final Supplier<Item> DREADED_SHOGGOTH_FLESH = plain("dreaded_shoggoth_flesh");
    public static final Supplier<Item> OMOTHOL_SHOGGOTH_FLESH = plain("omothol_shoggoth_flesh");
    public static final Supplier<Item> SHADOW_SHOGGOTH_FLESH = plain("shadow_shoggoth_flesh");

    // --- Miscellaneous item shells (plain items; system behaviour added by the owning stage) ---
    public static final Supplier<Item> OBLIVION_CATALYST = plain("oblivion_catalyst");
    public static final Supplier<Item> ANTI_BONE = plain("anti_bone");
    public static final Supplier<Item> POWERSTONE_TRACKER =
        ITEMS.register("powerstone_tracker", PowerstoneTrackerItem::new);
    public static final Supplier<Item> EYE_OF_THE_ABYSS =
        ITEMS.register("eye_of_the_abyss", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ESSENCE_OF_THE_GATEKEEPER = plain("essence_of_the_gatekeeper");
    public static final Supplier<Item> TOKEN_OF_JZAHAR = plain("token_of_jzahar");
    public static final Supplier<Item> SPIRIT_TABLET_SHARD_0 = plain("spirit_tablet_shard_0");
    public static final Supplier<Item> SPIRIT_TABLET_SHARD_1 = plain("spirit_tablet_shard_1");
    public static final Supplier<Item> SPIRIT_TABLET_SHARD_2 = plain("spirit_tablet_shard_2");
    public static final Supplier<Item> SPIRIT_TABLET_SHARD_3 = plain("spirit_tablet_shard_3");

    /** Register an edible item with the given hunger/saturation (built via the version-safe compat). */
    private static Supplier<Item> food(String name, int nutrition, float saturation) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().food(FoodCompat.food(nutrition, saturation))));
    }

    private static Supplier<Item> effectFood(String name, int nutrition, float saturation,
            java.util.function.BiConsumer<net.minecraft.world.level.Level, net.minecraft.world.entity.LivingEntity> effects) {
        return ITEMS.register(name, () -> new EffectFoodItem(
            new Item.Properties().food(FoodCompat.food(nutrition, saturation)), effects));
    }

    /** Register a plain, behaviourless item. */
    private static Supplier<Item> plain(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}

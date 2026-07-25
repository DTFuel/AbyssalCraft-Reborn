package com.shinoow.abyssalcraft.system.effect;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat.ACMobEffect;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.alchemy.Potion;

/**
 * AbyssalCraft's potion effects + brewable potions (owned by PS-4), faithful to the 1.12.2
 * {@code MiscHandler} / {@code PotionBuilder} set: the coralium &amp; dread plagues, the antimatter
 * effect, and the two antidotes, plus their seven brewable potion variants.
 *
 * <p>Registered fork-free through {@link ModRegistrar}; the {@code MobEffect} tick + the
 * {@code MobEffectInstance} construction forks live in {@code platform/MobEffectCompat}. The tick here
 * is the faithful <em>core</em> (the plagues / antimatter hurt the afflicted); the signature spread /
 * immunity lists / antimatter&rarr;anti-entity conversion / antidote cure land with the event-handler
 * subsystem (PS-11), and brewing recipes with the brewing stand (PC-8 {@code PotionBrewingCompat}).
 */
public final class ACEffects {

    private ACEffects() {}

    public static final ModRegistrar<MobEffect> EFFECTS = ModRegistrar.of(Registries.MOB_EFFECT, AbyssalCraft.MODID);
    public static final ModRegistrar<Potion> POTIONS = ModRegistrar.of(Registries.POTION, AbyssalCraft.MODID);

    // --- mob effects (colours faithful to 1.12.2 ACClientVars) ---

    public static final Supplier<MobEffect> CORALIUM_PLAGUE = EFFECTS.register("coralium_plague",
            () -> new ACMobEffect(MobEffectCategory.HARMFUL, 0x00FFFF,
                    EffectHooks::coraliumTick));

    public static final Supplier<MobEffect> DREAD_PLAGUE = EFFECTS.register("dread_plague",
            () -> new ACMobEffect(MobEffectCategory.HARMFUL, 0xAD1313,
                    EffectHooks::dreadTick));

    public static final Supplier<MobEffect> ANTIMATTER = EFFECTS.register("antimatter",
            () -> new ACMobEffect(MobEffectCategory.HARMFUL, 0xFFFFFF,
                    EffectHooks::antimatterTick));

    // Antidotes: beneficial markers here; the plague-cure behaviour lands with the event handlers (PS-11).
    public static final Supplier<MobEffect> CORALIUM_ANTIDOTE = EFFECTS.register("coralium_antidote",
            () -> new ACMobEffect(MobEffectCategory.BENEFICIAL, 0x00FF06, EffectHooks::coraliumAntidoteTick));

    public static final Supplier<MobEffect> DREAD_ANTIDOTE = EFFECTS.register("dread_antidote",
            () -> new ACMobEffect(MobEffectCategory.BENEFICIAL, 0x00FF06, EffectHooks::dreadAntidoteTick));

    // --- brewable potions (durations faithful to 1.12.2 MiscHandler) ---

    public static final Supplier<Potion> CPLAGUE = POTIONS.register("cplague",
            () -> new Potion("cplague", MobEffectCompat.effectInstance(CORALIUM_PLAGUE, 3600, 0)));
    public static final Supplier<Potion> CPLAGUE_LONG = POTIONS.register("cplague_long",
            () -> new Potion("cplague", MobEffectCompat.effectInstance(CORALIUM_PLAGUE, 9600, 0)));
    public static final Supplier<Potion> DPLAGUE = POTIONS.register("dplague",
            () -> new Potion("dplague", MobEffectCompat.effectInstance(DREAD_PLAGUE, 3600, 0)));
    public static final Supplier<Potion> DPLAGUE_LONG = POTIONS.register("dplague_long",
            () -> new Potion("dplague", MobEffectCompat.effectInstance(DREAD_PLAGUE, 9600, 0)));
    public static final Supplier<Potion> DPLAGUE_STRONG = POTIONS.register("dplague_strong",
            () -> new Potion("dplague", MobEffectCompat.effectInstance(DREAD_PLAGUE, 432, 1)));
    public static final Supplier<Potion> ANTIMATTER_POTION = POTIONS.register("antimatter",
            () -> new Potion("antimatter", MobEffectCompat.effectInstance(ANTIMATTER, 3600, 0)));
    public static final Supplier<Potion> ANTIMATTER_LONG = POTIONS.register("antimatter_long",
            () -> new Potion("antimatter", MobEffectCompat.effectInstance(ANTIMATTER, 9600, 0)));
}

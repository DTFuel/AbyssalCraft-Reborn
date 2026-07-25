package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Compat: mob-effect (potion effect) ticking + instance construction (loader axis).
 *
 * <p>{@code MobEffect} stays code-based on both loaders (unlike 1.21 enchantments), and its
 * constructor {@code (MobEffectCategory, int color)} is version-stable, so registration goes through
 * the normal {@code ModRegistrar}. Two small forks live here: the per-tick hook renamed/returns
 * differently (1.20.1 {@code void applyEffectTick} + {@code isDurationEffectTick} / 1.21
 * {@code boolean applyEffectTick} + {@code shouldApplyEffectTickThisTick}), and
 * {@code MobEffectInstance} takes a raw {@code MobEffect} (1.20.1) vs a {@code Holder<MobEffect>}
 * (1.21). Business ({@code system/effect/ACEffects}) supplies a fork-free {@link Tick} callback and
 * builds potions via {@link #effectInstance}.
 */
public final class MobEffectCompat {

    private MobEffectCompat() {}

    /** Fork-free per-tick action for an {@link ACMobEffect}. */
    public interface Tick {
        void apply(LivingEntity entity, int amplifier);
    }

    /** A mob effect whose per-tick behaviour is a fork-free callback; ticks every tick. */
    public static final class ACMobEffect extends MobEffect {

        private final Tick tick;

        public ACMobEffect(MobEffectCategory category, int color, Tick tick) {
            super(category, color);
            this.tick = tick;
        }

        //? if forge {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            tick.apply(entity, amplifier);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
        //?} else {
        /*@Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            tick.apply(entity, amplifier);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
        *///?}
    }

    /** Build a {@link MobEffectInstance} for {@code effect} (loader fork: raw effect / Holder). */
    public static MobEffectInstance effectInstance(Supplier<MobEffect> effect, int duration, int amplifier) {
        //? if forge {
        return new MobEffectInstance(effect.get(), duration, amplifier);
        //?} else {
        /*return new MobEffectInstance(
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()), duration, amplifier);
        *///?}
    }

    public static boolean hasEffect(LivingEntity entity, Supplier<MobEffect> effect) {
        //? if forge {
        return entity.hasEffect(effect.get());
        //?} else {
        /*return entity.hasEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()));
        *///?}
    }

    public static MobEffectInstance getEffect(LivingEntity entity, Supplier<MobEffect> effect) {
        //? if forge {
        return entity.getEffect(effect.get());
        //?} else {
        /*return entity.getEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()));
        *///?}
    }

    public static boolean removeEffect(LivingEntity entity, Supplier<MobEffect> effect) {
        //? if forge {
        return entity.removeEffect(effect.get());
        //?} else {
        /*return entity.removeEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()));
        *///?}
    }

    /** Build an instance from a vanilla effect constant (raw value on 1.20, holder on 1.21). */
    public static MobEffectInstance vanillaEffect(
            //? if >=1.21 {
            /*Holder<MobEffect> effect,
            *///?} else {
            MobEffect effect,
            //?}
            int duration, int amplifier) {
        //? if forge {
        return new MobEffectInstance(effect, duration, amplifier);
        //?} else {
        /*return new MobEffectInstance(effect, duration, amplifier);
        *///?}
    }
}

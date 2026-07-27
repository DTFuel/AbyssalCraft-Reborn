package com.shinoow.abyssalcraft.system.rending;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.content.entity.boss.ACBossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.content.entity.behavior.EldritchEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;

/** Frozen four-ledger contract shared by Staffs and the Rending Pedestal. */
public enum RendingEnergyType {
    SHADOW("Shadow", "energyShadow", 200, "shadow_gem"),
    ABYSSAL("Abyssal", "energyAbyssal", 100, "abyssal_wasteland_essence"),
    DREAD("Dread", "energyDread", 100, "dreadlands_essence"),
    OMOTHOL("Omothol", "energyOmothol", 100, "omothol_essence");

    private final String recipeName;
    private final String dataKey;
    private final int threshold;
    private final String outputPath;

    RendingEnergyType(String recipeName, String dataKey, int threshold, String outputPath) {
        this.recipeName = recipeName;
        this.dataKey = dataKey;
        this.threshold = threshold;
        this.outputPath = outputPath;
    }

    public String recipeName() {
        return recipeName;
    }

    public String dataKey() {
        return dataKey;
    }

    public int threshold() {
        return threshold;
    }

    public String translationKey() {
        return "rending.abyssalcraft." + name().toLowerCase(Locale.ROOT);
    }

    public int get(ItemStack stack) {
        return Math.max(0, ItemDataCompat.getInt(stack, dataKey, 0));
    }

    public void set(ItemStack stack, int amount) {
        ItemDataCompat.putInt(stack, dataKey, Math.max(0, amount));
    }

    public void add(ItemStack stack, int amount) {
        if (amount > 0) set(stack, get(stack) + amount);
    }

    public boolean matches(LivingEntity target) {
        if (!target.isAlive() || isBoss(target)) return false;
        return switch (this) {
            case SHADOW -> isShadow(target);
            case ABYSSAL -> target.level().dimension() == ACDimensions.ABYSSAL_WASTELAND
                && EffectHooks.isCoraliumCarrier(target);
            case DREAD -> target.level().dimension() == ACDimensions.DREADLANDS
                && EffectHooks.isDreadCarrier(target);
            case OMOTHOL -> target.level().dimension() == ACDimensions.OMOTHOL
                && isOmothol(target) && !isShadow(target);
        };
    }

    public boolean validates(RendingRecipe recipe) {
        ResourceLocation output = net.minecraft.core.registries.BuiltInRegistries.ITEM
            .getKey(recipe.result().getItem());
        return recipe.maxEnergy() == threshold
            && output.getNamespace().equals("abyssalcraft") && output.getPath().equals(outputPath);
    }

    public static Optional<RendingEnergyType> fromRecipe(RendingRecipe recipe) {
        return Arrays.stream(values())
            .filter(type -> type.recipeName.equalsIgnoreCase(recipe.energyName()))
            .findFirst();
    }

    public static boolean isBoss(LivingEntity target) {
        return target instanceof ACBossMob || target instanceof EliteMob
            || target instanceof WitherBoss || target instanceof EnderDragon;
    }

    private static boolean isShadow(LivingEntity target) {
        if (target.getType() == GhoulEntities.SHADOW_GHOUL.get()
            || target.getType() == LegacyEntities.SHADOW_CREATURE.get()
            || target.getType() == LegacyEntities.SHADOW_MONSTER.get()
            || target.getType() == LegacyEntities.SHADOW_BEAST.get()) return true;
        return target instanceof AbstractShoggoth shoggoth && shoggoth.getShoggothType() == 4;
    }

    private static boolean isOmothol(LivingEntity target) {
        return EldritchEntities.isEldritch(target);
    }
}
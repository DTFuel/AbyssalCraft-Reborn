package com.shinoow.abyssalcraft.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ContentConfigMatrix;

/** Server-authoritative runtime gate for the reloadable legacy armor recycling recipes. */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerCompatMixin {

    //? if <1.21 {
    @Inject(method = "getRecipeFor", at = @At("RETURN"), cancellable = true)
    private <C extends net.minecraft.world.Container, T extends Recipe<C>> void abyssalcraft$gateArmorSmelting(
            RecipeType<T> type, C input, Level level, CallbackInfoReturnable<Optional<T>> callback) {
        if (!ContentConfigMatrix.smeltingRecipes() && callback.getReturnValue().isPresent()
                && isArmorRecycling(callback.getReturnValue().get().getId().getPath())) {
            callback.setReturnValue(Optional.empty());
        }
    }
    //?} else {
    /*@Inject(method = "getRecipeFor", at = @At("RETURN"), cancellable = true)
    private <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> void abyssalcraft$gateArmorSmelting(
            RecipeType<T> type, I input, Level level,
            CallbackInfoReturnable<Optional<net.minecraft.world.item.crafting.RecipeHolder<T>>> callback) {
        if (!ContentConfigMatrix.smeltingRecipes() && callback.getReturnValue().isPresent()
                && isArmorRecycling(callback.getReturnValue().get().id().getPath())) {
            callback.setReturnValue(Optional.empty());
        }
    }
    *///?}

    private static boolean isArmorRecycling(String path) {
        return path.startsWith("smelting_") && path.endsWith("_recycling");
    }
}
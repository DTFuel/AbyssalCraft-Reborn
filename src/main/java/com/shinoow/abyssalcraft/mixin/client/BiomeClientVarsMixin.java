package com.shinoow.abyssalcraft.mixin.client;

import com.shinoow.abyssalcraft.client.ClientVarsConsumers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeClientVarsMixin {

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void abyssalcraft$grass(double x, double z, CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(color("grass", callback.getReturnValue()));
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void abyssalcraft$foliage(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(color("foliage", callback.getReturnValue()));
    }

    @Inject(method = "getWaterColor", at = @At("RETURN"), cancellable = true)
    private void abyssalcraft$water(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(color("water", callback.getReturnValue()));
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void abyssalcraft$sky(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(color("sky", callback.getReturnValue()));
    }

    private int color(String channel, int fallback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return fallback;
        ResourceLocation id = minecraft.level.registryAccess().registryOrThrow(Registries.BIOME)
            .getKey((Biome) (Object) this);
        return ClientVarsConsumers.biomeColor(id, channel, fallback);
    }
}
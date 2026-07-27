package com.shinoow.abyssalcraft.mixin.client;

import com.shinoow.abyssalcraft.client.ClientVarsConsumers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffect.class)
public abstract class MobEffectClientVarsMixin {

    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void abyssalcraft$color(CallbackInfoReturnable<Integer> callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        ResourceLocation id = minecraft.level.registryAccess().registryOrThrow(Registries.MOB_EFFECT)
            .getKey((MobEffect) (Object) this);
        callback.setReturnValue(ClientVarsConsumers.effectColor(id, callback.getReturnValue()));
    }
}
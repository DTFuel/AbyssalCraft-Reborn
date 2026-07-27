package com.shinoow.abyssalcraft.client;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientHooksCompat;
import com.shinoow.abyssalcraft.system.enchant.EnchantmentEffects;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Runtime consumers for Dark Realm smoke and the Visage of the Depths overlay. */
public final class ClientFxEffects {

    private ClientFxEffects() {}

    public static void register() {
        ClientHooksCompat.queueClientTick(ClientFxEffects::tickDarkRealmSmoke);
        ClientHooksCompat.queueOverlay("depths_helmet", ClientFxEffects::renderDepthsHelmetOverlay);
    }

    private static void tickDarkRealmSmoke() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.level().dimension() != ACDimensions.DARK_REALM
                || !ClientFxConfig.darkRealmSmokeParticles()) return;
        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(32.0D), LivingEntity::isAlive)) {
            if (entity instanceof Player || EnchantmentEffects.isShadow(entity)) continue;
            entity.level().addParticle(ParticleTypes.SMOKE,
                entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth(),
                entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight(),
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth(),
                0.0D, 0.0D, 0.0D);
        }
    }

    private static void renderDepthsHelmetOverlay(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()
                || !ACRef.id("depths_helmet").equals(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(
                        player.getItemBySlot(EquipmentSlot.HEAD).getItem()))) return;
        ClientHooksCompat.blitFullscreen(graphics, ACRef.id("textures/misc/coraliumblur.png"),
            width, height, ClientFxConfig.depthsHelmetOverlayOpacity());
    }
}
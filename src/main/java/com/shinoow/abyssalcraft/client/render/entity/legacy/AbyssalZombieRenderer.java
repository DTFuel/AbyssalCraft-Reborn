package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public final class AbyssalZombieRenderer extends ZombieRenderer {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/abyssal_zombie.png");
    private static final ResourceLocation EYES = ACRef.id("textures/model/abyssal_zombie_eyes.png");

    public AbyssalZombieRenderer(EntityRendererProvider.Context context) {
        super(context, ModModelLayers.ABYSSAL_ZOMBIE,
            ModModelLayers.ABYSSAL_ZOMBIE_INNER, ModModelLayers.ABYSSAL_ZOMBIE_OUTER);
        addLayer(new SimpleEyesLayer<>(this, EYES));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }
}
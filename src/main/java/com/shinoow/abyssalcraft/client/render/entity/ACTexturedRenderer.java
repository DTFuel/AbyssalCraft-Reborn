package com.shinoow.abyssalcraft.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Generic textured mob renderer (owned by PE-2, Stage E2): a vanilla {@link EntityModel} plus a fixed
 * AbyssalCraft texture.
 *
 * <p>Faithful successor to the 1.12.2 anti/demon renders ({@code RenderLiving}/{@code RenderBiped} +
 * {@code getEntityTexture}), which reused vanilla models with an AC texture. Used for the mobs that
 * extend {@code ACMob} (so they cannot reuse a vanilla renderer that expects the vanilla entity type);
 * the model just animates via {@code LivingEntity} params, so any vanilla model fits.
 */
public class ACTexturedRenderer<T extends Mob> extends MobRenderer<T, EntityModel<T>> {

    private final ResourceLocation texture;

    public ACTexturedRenderer(EntityRendererProvider.Context context, EntityModel<T> model, float shadowRadius,
                              ResourceLocation texture) {
        super(context, model, shadowRadius);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}

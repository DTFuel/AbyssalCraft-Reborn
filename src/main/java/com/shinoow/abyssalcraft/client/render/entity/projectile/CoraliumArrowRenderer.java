package com.shinoow.abyssalcraft.client.render.entity.projectile;

import com.shinoow.abyssalcraft.content.entity.projectile.CoraliumArrow;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Faithful renderer for the Coralium Arrow (owned by PE-4, Stage E2): the vanilla {@link ArrowRenderer}
 * geometry with the 1.12.2 {@code corarrow} texture -- the direct successor to 1.12.2
 * {@code RenderCoraliumArrow}. Fork-free (the arrow renderer API is identical across 1.20.1 / 1.21.1).
 */
public class CoraliumArrowRenderer extends ArrowRenderer<CoraliumArrow> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/corarrow.png");

    public CoraliumArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CoraliumArrow entity) {
        return TEXTURE;
    }
}

package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class FixedItemRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ItemRenderer itemRenderer;
    private final ItemStack item;
    private final float scale;
    private final boolean fullBright;

    public FixedItemRenderer(EntityRendererProvider.Context context, ItemStack item, float scale, boolean fullBright) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.item = item;
        this.scale = scale;
        this.fullBright = fullBright;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(item, ItemDisplayContext.GROUND, fullBright ? 15728880 : packedLight,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, poseStack, buffer,
            entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
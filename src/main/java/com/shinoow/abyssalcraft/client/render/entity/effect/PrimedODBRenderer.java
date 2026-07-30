package com.shinoow.abyssalcraft.client.render.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shinoow.abyssalcraft.content.entity.misc.PrimedODB;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class PrimedODBRenderer extends EntityRenderer<PrimedODB> {

    private final BlockRenderDispatcher blockRenderer;
    private final BlockState blockState;

    public PrimedODBRenderer(EntityRendererProvider.Context context, BlockState blockState) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.blockState = blockState;
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(PrimedODB entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float remaining = entity.getFuse() - partialTicks + 1.0F;
        float scale = 1.0F;
        if (remaining < 10.0F) {
            float pulse = 1.0F - remaining / 10.0F;
            pulse = Math.max(0.0F, Math.min(1.0F, pulse));
            pulse *= pulse;
            pulse *= pulse;
            scale += pulse * 0.3F;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        TntMinecartRenderer.renderWhiteSolidBlock(blockRenderer, blockState, poseStack, buffer,
            packedLight, entity.getFuse() / 5 % 2 == 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedODB entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
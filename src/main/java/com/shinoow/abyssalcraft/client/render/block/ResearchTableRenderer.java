package com.shinoow.abyssalcraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTableBlock;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTableBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {

    private static final ItemStack FEATHER = new ItemStack(Items.FEATHER);
    private final ItemRenderer itemRenderer;

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ResearchTableBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float rotationY = switch (table.getBlockState().getValue(ResearchTableBlock.FACING)) {
            case EAST -> -135.0F;
            case NORTH -> -45.0F;
            case SOUTH -> 135.0F;
            case WEST -> 45.0F;
            default -> 135.0F;
        };
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.4F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        poseStack.translate(-0.32F, -0.5F, -0.45F);
        itemRenderer.renderStatic(FEATHER, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, table.getLevel(), 0);
        poseStack.popPose();
    }
}
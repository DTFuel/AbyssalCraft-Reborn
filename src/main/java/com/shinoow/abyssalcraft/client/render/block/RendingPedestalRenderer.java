package com.shinoow.abyssalcraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestalBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RendingPedestalRenderer implements BlockEntityRenderer<RendingPedestalBlockEntity> {

    private final ItemRenderer itemRenderer;

    public RendingPedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RendingPedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack staff = pedestal.getItem(RendingPedestalBlockEntity.SLOT_STAFF);
        if (staff.isEmpty()) return;
        float height = staff.getItem() instanceof BlockItem ? 0.56F : 0.37F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -height, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        poseStack.translate(0.16F, 0.05F, 0.0F);
        itemRenderer.renderStatic(staff, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, pedestal.getLevel(), 0);
        poseStack.popPose();
    }
}
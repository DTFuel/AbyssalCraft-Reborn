package com.shinoow.abyssalcraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.block.ritual.RitualAltarBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the ritual's center item using the legacy altar display pose. */
public final class RitualAltarRenderer implements BlockEntityRenderer<RitualAltarBlockEntity> {

    private final ItemRenderer itemRenderer;

    public RitualAltarRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RitualAltarBlockEntity altar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack center = altar.getCenterItem();
        if (center.isEmpty()) return;

        float age = altar.getLevel() == null ? partialTick : altar.getLevel().getGameTime() + partialTick;
        float height = center.getItem() instanceof BlockItem ? 0.62F : 0.43F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -height, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age));
        itemRenderer.renderStatic(center, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, altar.getLevel(), 0);
        poseStack.popPose();
    }
}
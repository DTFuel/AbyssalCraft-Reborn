package com.shinoow.abyssalcraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.shinoow.abyssalcraft.content.block.energy.SacrificialAltarBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the Sacrificial Altar's singleton energy item in the legacy top pose. */
public final class SacrificialAltarRenderer implements BlockEntityRenderer<SacrificialAltarBlockEntity> {

    private final ItemRenderer itemRenderer;

    public SacrificialAltarRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SacrificialAltarBlockEntity altar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stored = altar.getStoredItem();
        if (stored.isEmpty()) return;
        float age = altar.getLevel() == null ? partialTick : altar.getLevel().getGameTime() + partialTick;
        float height = stored.getItem() instanceof BlockItem ? 0.50F : 0.31F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.38F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -height, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age));
        itemRenderer.renderStatic(stored, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, altar.getLevel(), 0);
        poseStack.popPose();
    }
}

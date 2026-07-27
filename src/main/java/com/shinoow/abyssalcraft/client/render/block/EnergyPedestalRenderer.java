package com.shinoow.abyssalcraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.block.energy.EnergyPedestalBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;

/**
 * Faithful 1.12.2 {@code TileEntitySingletonInventoryBlockRenderer} for energy pedestals -- renders
 * the stored energy item floating and rotating above the pedestal. Part of the PE energy network
 * (Stage S / RR-ENERGY).
 *
 * <p><b>Registration:</b> {@link ACBlockEntityRenderers} relay (owned by Gate Integrator) calls
 * {@code renderers.registerBlockEntity(EnergyBlocks.ENERGY_PEDESTAL_BE.get(), EnergyPedestalRenderer::new)}.
 */
public final class EnergyPedestalRenderer implements BlockEntityRenderer<EnergyPedestalBlockEntity> {

    private final ItemRenderer itemRenderer;

    public EnergyPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EnergyPedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stored = pedestal.getStoredItem();
        if (stored.isEmpty()) return;

        float age = pedestal.getLevel() == null ? partialTick : pedestal.getLevel().getGameTime() + partialTick;
        float height = stored.getItem() instanceof BlockItem ? 0.56F : 0.37F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -height, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age));
        itemRenderer.renderStatic(stored, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, pedestal.getLevel(), 0);
        poseStack.popPose();
    }
}

package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.client.model.entity.DemonSheepModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class DemonSheepWoolLayer<T extends Mob> extends RenderLayer<T, DemonSheepModel<T>> {

    private final DemonSheepModel<T> wool;
    private final ResourceLocation texture;

    public DemonSheepWoolLayer(RenderLayerParent<T, DemonSheepModel<T>> parent,
                               DemonSheepModel<T> wool, ResourceLocation texture) {
        super(parent);
        this.wool = wool;
        this.texture = texture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        //? if forge {
        coloredCutoutModelCopyLayerRender(getParentModel(), wool, texture, poseStack, buffer, packedLight,
            entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick,
            1.0F, 1.0F, 1.0F);
        //?} else {
        /*coloredCutoutModelCopyLayerRender(getParentModel(), wool, texture, poseStack, buffer, packedLight,
            entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick, -1);
        *///?}
    }
}
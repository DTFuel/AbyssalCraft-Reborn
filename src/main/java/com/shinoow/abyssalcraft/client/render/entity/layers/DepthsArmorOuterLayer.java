package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.render.armor.ACArmorVisuals;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public final class DepthsArmorOuterLayer<T extends LivingEntity, M extends HumanoidModel<T>>
    extends RenderLayer<T, M> {

    private static final ResourceLocation LAYER_1 =
        ACRef.id("textures/models/armor/depths_layer_1_outer.png");
    private static final ResourceLocation LAYER_2 =
        ACRef.id("textures/models/armor/depths_layer_2_outer.png");

    private final HumanoidModel<T> inner;
    private final HumanoidModel<T> outer;
    private final HumanoidModel<T> armorStandInner;
    private final HumanoidModel<T> armorStandOuter;

    @SuppressWarnings("unchecked")
    public DepthsArmorOuterLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        this(parent, models, false);
    }

    @SuppressWarnings("unchecked")
    private DepthsArmorOuterLayer(RenderLayerParent<T, M> parent, EntityModelSet models,
                                  boolean skeleton) {
        super(parent);
        this.inner = new HumanoidModel<>(models.bakeLayer(skeleton
            ? ModModelLayers.DEPTHS_SKELETON_INNER : ModModelLayers.DEPTHS_INNER));
        this.outer = new HumanoidModel<>(models.bakeLayer(skeleton
            ? ModModelLayers.DEPTHS_SKELETON_OUTER : ModModelLayers.DEPTHS_OUTER));
        this.armorStandInner = (HumanoidModel<T>) (HumanoidModel<?>) new ArmorStandArmorModel(
            models.bakeLayer(ModModelLayers.DEPTHS_ARMOR_STAND_INNER));
        this.armorStandOuter = (HumanoidModel<T>) (HumanoidModel<?>) new ArmorStandArmorModel(
            models.bakeLayer(ModModelLayers.DEPTHS_ARMOR_STAND_OUTER));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void attach(LivingEntityRenderer<?, ?> renderer, EntityModelSet models) {
        if (renderer.getModel() instanceof HumanoidModel<?>) {
            ((LivingEntityRenderer) renderer).addLayer(new DepthsArmorOuterLayer(renderer, models));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void attachSkeleton(LivingEntityRenderer<?, ?> renderer, EntityModelSet models) {
        if (renderer.getModel() instanceof HumanoidModel<?>) {
            ((LivingEntityRenderer) renderer).addLayer(new DepthsArmorOuterLayer(renderer, models, true));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.CHEST);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.LEGS);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.FEET);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.HEAD);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                             T entity, EquipmentSlot slot) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (!ACArmorVisuals.hasVisual(stack, ArmorCompat.Visual.DEPTHS)) return;

        boolean armorStand = entity instanceof ArmorStand;
        HumanoidModel<T> model = slot == EquipmentSlot.LEGS
            ? armorStand ? armorStandInner : inner
            : armorStand ? armorStandOuter : outer;
        getParentModel().copyPropertiesTo(model);
        ACArmorVisuals.setVisible(model, slot);
        ResourceLocation texture = slot == EquipmentSlot.LEGS ? LAYER_2 : LAYER_1;
        RenderType renderType = RenderType.armorCutoutNoCull(texture);
        //? if >=1.21 {
        /*VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, renderType, stack.hasFoil());
        *///?} else {
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, renderType, false, stack.hasFoil());
        //?}
        //? if >=1.21 {
        /*model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        *///?} else {
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
            1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }
}
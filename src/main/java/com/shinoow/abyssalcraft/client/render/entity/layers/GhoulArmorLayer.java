package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.model.entity.GhoulArmorModel;
import com.shinoow.abyssalcraft.client.model.entity.GhoulModel;
import com.shinoow.abyssalcraft.client.render.armor.MonsterArmorVisuals;
import com.shinoow.abyssalcraft.platform.ArmorRenderCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class GhoulArmorLayer<T extends Mob> extends RenderLayer<T, GhoulModel<T>> {

    private final GhoulArmorModel<T> inner;
    private final GhoulArmorModel<T> outer;

    public GhoulArmorLayer(RenderLayerParent<T, GhoulModel<T>> parent, EntityModelSet models) {
        super(parent);
        this.inner = new GhoulArmorModel<>(models.bakeLayer(ModModelLayers.GHOUL_ARMOR_INNER));
        this.outer = new GhoulArmorModel<>(models.bakeLayer(ModModelLayers.GHOUL_ARMOR_OUTER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T ghoul, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        renderPiece(poseStack, buffers, packedLight, ghoul, limbSwing, limbSwingAmount,
            ageInTicks, netHeadYaw, headPitch, EquipmentSlot.CHEST);
        renderPiece(poseStack, buffers, packedLight, ghoul, limbSwing, limbSwingAmount,
            ageInTicks, netHeadYaw, headPitch, EquipmentSlot.LEGS);
        renderPiece(poseStack, buffers, packedLight, ghoul, limbSwing, limbSwingAmount,
            ageInTicks, netHeadYaw, headPitch, EquipmentSlot.FEET);
        renderPiece(poseStack, buffers, packedLight, ghoul, limbSwing, limbSwingAmount,
            ageInTicks, netHeadYaw, headPitch, EquipmentSlot.HEAD);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                             T ghoul, float limbSwing, float limbSwingAmount,
                             float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlot slot) {
        ItemStack stack = ghoul.getItemBySlot(slot);
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getType().getSlot() != slot) return;

        GhoulArmorModel<T> model = slot == EquipmentSlot.LEGS ? inner : outer;
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(ghoul, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.setSlotVisible(slot);

        MonsterArmorVisuals.LayerData visual = MonsterArmorVisuals.resolve("ghoul", stack, slot);
        RenderType baseType = RenderType.armorCutoutNoCull(visual.texture());
        VertexConsumer base = ArmorRenderCompat.armorBuffer(buffers, baseType, stack.hasFoil());
        ArmorRenderCompat.render(model, poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, visual.color());
        if (visual.overlay() != null) {
            VertexConsumer overlay = buffers.getBuffer(RenderType.armorCutoutNoCull(visual.overlay()));
            ArmorRenderCompat.render(model, poseStack, overlay, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF);
        }
    }
}
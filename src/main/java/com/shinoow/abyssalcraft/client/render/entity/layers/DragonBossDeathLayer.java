package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.model.entity.DragonModel;
import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorRenderCompat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class DragonBossDeathLayer<T extends Mob> extends RenderLayer<T, DragonModel<T>> {

    private static final ResourceLocation EXPLODING =
        ACRef.id("textures/model/boss/dragonboss_exploding.png");
    private static final ResourceLocation BODY =
        ACRef.id("textures/model/boss/dragonboss.png");

    public DragonBossDeathLayer(RenderLayerParent<T, DragonModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof BossMob boss) || boss.kind() != BossKind.DRAGON_BOSS
                || boss.getACDeathTime() <= 0) return;

        VertexConsumer exploding = buffers.getBuffer(RenderType.dragonExplosionAlpha(EXPLODING));
        float progress = (boss.getACDeathTime() + partialTick) / 200.0F;
        ArmorRenderCompat.render(getParentModel(), poseStack, exploding,
            packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, progress);
        VertexConsumer decal = buffers.getBuffer(RenderType.entityDecal(BODY));
        ArmorRenderCompat.render(getParentModel(), poseStack, decal,
            packedLight, OverlayTexture.pack(0.0F, false), 0xFFFFFF);
    }
}
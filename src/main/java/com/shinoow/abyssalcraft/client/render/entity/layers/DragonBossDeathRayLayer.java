package com.shinoow.abyssalcraft.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import com.shinoow.abyssalcraft.client.model.entity.DragonModel;
import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.platform.DeathRayRenderCompat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Mob;

public final class DragonBossDeathRayLayer<T extends Mob> extends RenderLayer<T, DragonModel<T>> {

    public DragonBossDeathRayLayer(RenderLayerParent<T, DragonModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof BossMob boss) || boss.kind() != BossKind.DRAGON_BOSS
                || boss.getACDeathTime() <= 0) return;

        float progress = (boss.getACDeathTime() + partialTick) / 200.0F;
        poseStack.pushPose();
        poseStack.translate(0.0F, -1.0F, -2.0F);
        DeathRayRenderCompat.render(poseStack, buffers, progress, 60,
            0, 255, 255, 10.0F, 2.0F, true);
        poseStack.popPose();
    }
}
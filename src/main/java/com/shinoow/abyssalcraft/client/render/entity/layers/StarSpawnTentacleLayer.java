package com.shinoow.abyssalcraft.client.render.entity.layers;

import java.util.Set;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.shinoow.abyssalcraft.client.model.entity.StarSpawnTentacleModel;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class StarSpawnTentacleLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/model/tentacles.png");
    private static final Set<UUID> STAR_SPAWN = Set.of(
        UUID.fromString("a5d8abca-0979-4bb0-825a-f1ccda0b350b"),
        UUID.fromString("08f3211c-d425-47fd-afd8-f0e7f94152c4"));

    private final StarSpawnTentacleModel model;

    public StarSpawnTentacleLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                                  net.minecraft.client.model.geom.EntityModelSet models) {
        super(parent);
        this.model = new StarSpawnTentacleModel(models.bakeLayer(ModModelLayers.STAR_SPAWN_TENTACLES));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible() || !isStarSpawn(player)) return;

        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        poseStack.pushPose();
        if (player.isCrouching()) poseStack.translate(0.0F, 0.24F, 0.0F);
        getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.22F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.root().render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static boolean isStarSpawn(AbstractClientPlayer player) {
        return STAR_SPAWN.contains(player.getUUID());
    }
}
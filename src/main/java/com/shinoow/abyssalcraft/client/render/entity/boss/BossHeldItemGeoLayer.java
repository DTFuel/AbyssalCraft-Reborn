package com.shinoow.abyssalcraft.client.render.entity.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.shinoow.abyssalcraft.content.item.weapon.SoulReaperItems;
import com.shinoow.abyssalcraft.content.entity.boss.BossKind;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
//? if <1.21 {
import software.bernie.geckolib.core.animatable.GeoAnimatable;
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
*///?}

public final class BossHeldItemGeoLayer<T extends LivingEntity & GeoAnimatable>
    extends BlockAndItemGeoLayer<T> {

    public enum Mode {
        SACTHOTH,
        SKELETON_GOLIATH
    }

    private final Mode mode;

    public BossHeldItemGeoLayer(GeoRenderer<T> renderer, Mode mode) {
        super(renderer);
        this.mode = mode;
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, T entity) {
        if (entity.isInvisible()) return null;
        if (mode == Mode.SACTHOTH && entity instanceof BossMob boss
            && boss.kind() == BossKind.SACTHOTH && "leftarm1".equals(bone.getName())) {
            return boss.isAlive() && boss.getACDeathTime() <= 0
                ? SoulReaperItems.SOUL_REAPER_BLADE.get().getDefaultInstance() : null;
        }
        if (mode == Mode.SKELETON_GOLIATH && "rightarm".equals(bone.getName())) {
            return entity.getMainHandItem().isEmpty() ? null : entity.getMainHandItem();
        }
        if (mode == Mode.SKELETON_GOLIATH && "leftarm".equals(bone.getName())) {
            return entity.getOffhandItem().isEmpty() ? null : entity.getOffhandItem();
        }
        return null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T entity) {
        // The bone matrix + XP(-90)/YP(180) below already orient the item fully; always render as the
        // right hand so vanilla's left-hand mirror doesn't double-flip the off-hand (reversed shield).
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T entity,
                                      MultiBufferSource buffers, float partialTick,
                                      int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        if (mode == Mode.SACTHOTH) {
            poseStack.scale(1.2F, 1.2F, 1.2F);
            poseStack.translate(0.16F, 0.325F, -0.825F);
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.9F, 0.9F, 0.9F);
            poseStack.translate("leftarm".equals(bone.getName()) ? 0.16F : -0.16F,
                0.14F, 0.71F);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack,
            getTransformTypeForStack(bone, stack, entity), false,
            poseStack, buffers, entity.level(), packedLight, packedOverlay, entity.getId());
        poseStack.popPose();
    }
}

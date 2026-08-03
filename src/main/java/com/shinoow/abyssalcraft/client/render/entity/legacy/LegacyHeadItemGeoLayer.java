package com.shinoow.abyssalcraft.client.render.entity.legacy;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

final class LegacyHeadItemGeoLayer extends BlockAndItemGeoLayer<LegacyHostileMob> {

    private final String headBone;

    LegacyHeadItemGeoLayer(GeoRenderer<LegacyHostileMob> renderer, String headBone) {
        super(renderer);
        this.headBone = headBone;
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, LegacyHostileMob entity) {
        if (entity.isInvisible() || !headBone.equals(bone.getName())) return null;
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
        return stack.isEmpty() ? null : stack;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack,
                                                           LegacyHostileMob entity) {
        return ItemDisplayContext.HEAD;
    }
}
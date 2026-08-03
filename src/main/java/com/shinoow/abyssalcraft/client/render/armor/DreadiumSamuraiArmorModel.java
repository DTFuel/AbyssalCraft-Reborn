package com.shinoow.abyssalcraft.client.render.armor;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ArmorCompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

import software.bernie.geckolib.model.GeoModel;

public final class DreadiumSamuraiArmorModel extends GeoModel<ArmorCompat.SamuraiArmorItem> {

    private static final ResourceLocation INNER_MODEL = ACRef.id("geo/armor/dreadium_samurai_inner.geo.json");
    private static final ResourceLocation OUTER_MODEL = ACRef.id("geo/armor/dreadium_samurai_outer.geo.json");
    private static final ResourceLocation INNER_TEXTURE = ACRef.id("textures/models/armor/dreadium_samurai_layer_2.png");
    private static final ResourceLocation OUTER_TEXTURE = ACRef.id("textures/models/armor/dreadium_samurai_layer_1.png");
    private static final ResourceLocation ANIMATION = ACRef.id("animations/entity/empty.animation.json");

    @Override
    public ResourceLocation getModelResource(ArmorCompat.SamuraiArmorItem animatable) {
        return animatable.getType() == ArmorItem.Type.HELMET || animatable.getType() == ArmorItem.Type.LEGGINGS
            ? INNER_MODEL
            : OUTER_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ArmorCompat.SamuraiArmorItem animatable) {
        return animatable.getType() == ArmorItem.Type.LEGGINGS ? INNER_TEXTURE : OUTER_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ArmorCompat.SamuraiArmorItem animatable) {
        return ANIMATION;
    }
}
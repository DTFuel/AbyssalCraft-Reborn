package com.shinoow.abyssalcraft.client.render.entity.effect;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.util.Mth;

import software.bernie.geckolib.cache.object.GeoBone;

public final class DreadTentacleAnimations {

    private DreadTentacleAnimations() {}

    public static void apply(Function<String, Optional<GeoBone>> bones, float ageInTicks,
                             float limbSwing, float limbSwingAmount, boolean pointing,
                             float anchorX, float anchorY, float anchorZ) {
        float movement = Mth.sin((limbSwing * 0.4F + 2.0F) * 1.5F) * 0.09F * limbSwingAmount;
        float flap = Mth.sin(ageInTicks * 0.2F) * 0.3F;
        float flap2 = Mth.cos(ageInTicks * 0.2F) * 0.4F;

        GeoBone base = required(bones, "base");
        base.setPosX(-anchorX);
        base.setPosY(anchorY - 10.0F);
        base.setPosZ(anchorZ);
        setRotation(base, pointing ? (float) Math.PI / 2.0F : 0.7853982F,
            pointing ? 0.0F : 0.7853982F, 0.0F);

        setRotation(required(bones, "tentacle_1"),
            pointing ? -0.6396263F : -0.1396263F - flap2 * 0.1F - movement * 0.4F,
            flap * 0.1F + movement * 0.4F, 0.0F);
        setRotation(required(bones, "tentacle_1_1"), 0.0F, 0.0F, 0.0F);
        setRotation(required(bones, "tentacle_2"), -0.418879F - flap2 * 0.75F, 0.0F, 0.0F);
        setRotation(required(bones, "tentacle_2_1"), 0.0F, 0.0F, 0.0F);
        setRotation(required(bones, "tentacle_3"), -0.1396263F - flap2, 0.0F, 0.0F);
        setRotation(required(bones, "tentacle_3_1"), 0.0F, 0.0F, 0.0F);
    }

    private static GeoBone required(Function<String, Optional<GeoBone>> bones, String name) {
        return bones.apply(name).orElseThrow();
    }

    private static void setRotation(GeoBone bone, float xRot, float yRot, float zRot) {
        bone.setRotX(-xRot);
        bone.setRotY(-yRot);
        bone.setRotZ(zRot);
    }
}
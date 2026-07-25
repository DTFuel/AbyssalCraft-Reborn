package com.shinoow.abyssalcraft.client.render.entity.boss;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.content.entity.boss.BossKind;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
//? if forge {
import software.bernie.geckolib.core.object.Color;
//?} else {
/*import software.bernie.geckolib.util.Color;
*///?}

/**
 * GeckoLib renderer for the Jzahar / Sacthoth / Abyssal Dragon bar-bosses (owned by PE-4b), driven by
 * {@link BossGeoModel} (per-id mesh + texture). Registered by {@code BossRenderers} for those three
 * {@code EntityType}s; Chagaroth uses {@link ChagarothGeoRenderer}. Fork-free (the {@code (Context,
 * GeoModel)} constructor is identical across both GeckoLib builds).
 */
public class BossGeoRenderer extends GeoEntityRenderer<BossMob> {

    public BossGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new BossGeoModel());
        addRenderLayer(new BossGlowingGeoLayer(this));
        addRenderLayer(new BossDeathRayGeoLayer(this));
        addRenderLayer(new BossHeldItemGeoLayer<>(this, BossHeldItemGeoLayer.Mode.SACTHOTH));
    }

    @Override
    public RenderType getRenderType(BossMob entity, ResourceLocation texture,
                                    MultiBufferSource buffers, float partialTick) {
        if (entity.kind() == BossKind.SACTHOTH) return RenderType.entityTranslucent(texture);
        return super.getRenderType(entity, texture, buffers, partialTick);
    }

    @Override
    public Color getRenderColor(BossMob entity, float partialTick, int packedLight) {
        float alpha = entity.kind() == BossKind.SACTHOTH
            ? entity.getLightLevelDependentMagicValue() : 1.0F;
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, alpha);
    }

    @Override
    protected float getDeathMaxRotation(BossMob entity) {
        return 0.0F;
    }
}

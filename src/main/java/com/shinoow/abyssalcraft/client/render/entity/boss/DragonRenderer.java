package com.shinoow.abyssalcraft.client.render.entity.boss;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import com.shinoow.abyssalcraft.client.model.entity.DragonModel;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.registry.ModModelLayers;
import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DragonBossDeathLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DragonBossDeathRayLayer;

/**
 * Java-model renderer for the Abyssal Dragon + Dragon Minion (owned by PE-4b). Uses the
 * {@link DragonModel} {@code HierarchicalModel} (a posed port of the 1.12.2 procedural ender-dragon model)
 * rather than a GeckoLib static mesh, which cannot reproduce the dragon's render-time pose. One renderer
 * serves both dragons (different 256x256 textures). Fork-free ({@code MobRenderer} + {@code HierarchicalModel}
 * are identical across 1.20.1 / 1.21.1).
 */
public class DragonRenderer<T extends Mob> extends MobRenderer<T, DragonModel<T>> {

    private final ResourceLocation texture;

    public DragonRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyes) {
        super(context, new DragonModel<>(context.bakeLayer(ModModelLayers.DRAGON)), 1.5F);
        this.texture = texture;
        addLayer(new DragonBossDeathLayer<>(this));
        if (eyes != null) addLayer(new SimpleEyesLayer<>(this, eyes));
        addLayer(new DragonBossDeathRayLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected RenderType getRenderType(T entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (entity instanceof BossMob boss && boss.getACDeathTime() > 0) return null;
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    @Override
    protected float getFlipDegrees(T entity) {
        return entity instanceof BossMob ? 0.0F : super.getFlipDegrees(entity);
    }
}

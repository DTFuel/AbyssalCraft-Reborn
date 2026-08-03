package com.shinoow.abyssalcraft.client.render.entity;

import java.util.Set;

import com.shinoow.abyssalcraft.client.model.entity.GhoulModel;
import com.shinoow.abyssalcraft.client.model.entity.GhoulArmorModel;
import com.shinoow.abyssalcraft.content.entity.ghoul.AbstractGhoul;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.ShoggothEntities;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * Faithful renderers for the ghoul (PD-5) + shoggoth (PD-5) families (owned by PE-3, Stage E2).
 *
 * <p>Unlike the anti/demon families (PE-2, which reuse vanilla models), the ghoul and shoggoth textures
 * are UV-mapped to their bespoke 1.12.2 meshes. Ghouls use {@link GhoulModel}, while shoggoths use an
 * audited Gecko geometry resource with exact procedural animation. All eight register through the fork-free
 * {@link EntityRendererCompat.Renderers} sink and are added to {@code handled} so {@code ACEntityRenderers}
 * skips them in the E1 placeholder pass; the ghoul meshes are registered via {@link #registerLayers}.
 */
public final class GhoulShoggothRenderers {

    private GhoulShoggothRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        ghoul(renderers, handled, GhoulEntities.GHOUL.get(), "ghoul", "ghoul_eyes", false, false);
        ghoul(renderers, handled, GhoulEntities.DEPTHS_GHOUL.get(), "depths_ghoul", "depths_ghoul_eyes", false, false);
        ghoul(renderers, handled, GhoulEntities.DREADED_GHOUL.get(), "dreaded_ghoul", "dreaded_ghoul_eyes", false, true);
        ghoul(renderers, handled, GhoulEntities.OMOTHOL_GHOUL.get(), "omothol_ghoul", null, false, false);
        ghoul(renderers, handled, GhoulEntities.SHADOW_GHOUL.get(), "shadow_ghoul", "shadow_ghoul_eyes", true, false);
        shoggoth(renderers, handled, ShoggothEntities.LESSER_SHOGGOTH.get(), 0.75F);
        shoggoth(renderers, handled, ShoggothEntities.SHOGGOTH.get(), 1.0F);
        shoggoth(renderers, handled, ShoggothEntities.GREATER_SHOGGOTH.get(), 1.5F);
    }

    /** The two custom family meshes (vanilla-model families need no layer registration). */
    public static void registerLayers(EntityRendererCompat.Layers layers) {
        layers.register(ModModelLayers.GHOUL, GhoulModel::createBodyLayer);
        layers.register(ModModelLayers.GHOUL_ARMOR_INNER, () -> GhoulArmorModel.createBodyLayer(0.5F));
        layers.register(ModModelLayers.GHOUL_ARMOR_OUTER, () -> GhoulArmorModel.createBodyLayer(1.0F));
    }

    private static void ghoul(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                              EntityType<?> type, String texture, String eyes, boolean brightnessAlpha,
                              boolean dreadCarrier) {
        ghoul(renderers, handled, type, ACRef.id("textures/model/ghoul/" + texture + ".png"),
            eyes, brightnessAlpha, dreadCarrier);
        }

        private static void ghoul(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                  EntityType<?> type, ResourceLocation texture, String eyes, boolean brightnessAlpha,
                                  boolean dreadCarrier) {
        ResourceLocation e = eyes == null ? null : ACRef.id("textures/model/ghoul/" + eyes + ".png");
        EntityRendererProvider<AbstractGhoul> provider =
            ctx -> new GhoulRenderer<>(ctx, texture, e, brightnessAlpha, dreadCarrier);
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void shoggoth(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                 EntityType<?> type, float modelScale) {
        EntityRendererProvider<AbstractShoggoth> provider = ctx -> new ShoggothGeoRenderer(ctx, modelScale);
        renderers.register(type, provider);
        handled.add(type);
    }
}

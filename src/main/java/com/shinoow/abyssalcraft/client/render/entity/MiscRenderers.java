package com.shinoow.abyssalcraft.client.render.entity;

import java.util.Set;

import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.client.render.entity.effect.BillboardRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.CompassTentacleRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.ImplosionRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.PrimedODBRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.PortalRenderer;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Faithful-enough renderers for the misc non-living family (PD-6) -- owned by PE-4, Stage E2, following
 * the E2 dispatch idiom (register real renderers, mark their {@code EntityType}s handled so the E1
 * placeholder skips them).
 *
 * <p>The two item entities (spirit item / gatekeeper essence) reuse the vanilla {@link ItemEntityRenderer}
 * (faithful successor to 1.12.2 {@code RenderEntityItem}). The effect/logic bodies (black hole, implosion,
 * compass tentacle, the two portals, the two primed ODBs, the tracker) use dedicated billboard, model,
 * or fuse-aware renderers; no stand-in renderer remains in this family.
 */
public final class MiscRenderers {

    private MiscRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        billboard(renderers, handled, MiscEntities.BLACK_HOLE.get(), ACRef.id("textures/model/black_hole.png"), 4.0F, 1.0F, 2.0F, true);
        dedicated(renderers, handled, MiscEntities.IMPLOSION.get(), ImplosionRenderer::new);
        dedicated(renderers, handled, MiscEntities.COMPASS_TENTACLE.get(), CompassTentacleRenderer::new);
        dedicated(renderers, handled, MiscEntities.PORTAL.get(), PortalRenderer::new);
        dedicated(renderers, handled, MiscEntities.SINGLE_PORTAL.get(), PortalRenderer::new);
        dedicated(renderers, handled, MiscEntities.PRIMED_ODB.get(),
            ctx -> new PrimedODBRenderer(ctx, ACRef.vanilla("textures/block/tnt_side.png")));
        dedicated(renderers, handled, MiscEntities.PRIMED_ODB_CORE.get(),
            ctx -> new PrimedODBRenderer(ctx, ACRef.id("textures/block/odb_core.png")));
        billboard(renderers, handled, MiscEntities.POWERSTONE_TRACKER.get(),
            ACRef.id("textures/model/powerstone_tracker.png"), 0.5F, 1.0F, 0.0F, false);
        item(renderers, handled, MiscEntities.SPIRIT_ITEM.get());
        item(renderers, handled, MiscEntities.GATEKEEPER_ESSENCE.get());
    }

    private static void billboard(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                  EntityType<?> type, ResourceLocation texture, float scale,
                                  float verticalScale, float rotationSpeed, boolean wobble) {
        EntityRendererProvider<Entity> provider = ctx -> new BillboardRenderer<>(ctx, texture, scale,
            verticalScale, rotationSpeed, wobble);
        renderers.register(type, provider);
        handled.add(type);
    }

    private static <T extends Entity> void dedicated(EntityRendererCompat.Renderers renderers,
                                                      Set<EntityType<?>> handled, EntityType<T> type,
                                                      EntityRendererProvider<T> provider) {
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void item(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                             EntityType<?> type) {
        EntityRendererProvider<ItemEntity> provider = ItemEntityRenderer::new;
        renderers.register(type, provider);
        handled.add(type);
    }
}

package com.shinoow.abyssalcraft.client.render.entity;

import java.util.Set;

import com.shinoow.abyssalcraft.client.render.entity.projectile.CoraliumArrowRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.BillboardRenderer;
import com.shinoow.abyssalcraft.client.render.entity.effect.FixedItemRenderer;
import com.shinoow.abyssalcraft.content.entity.projectile.CoraliumArrow;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Faithful renderers for the projectile family (PD-6) -- owned by PE-4, Stage E2, following the E2
 * dispatch idiom (register real renderers, mark their {@code EntityType}s handled so the E1 placeholder
 * skips them).
 *
 * <p>The Coralium Arrow reuses the vanilla arrow geometry with its 1.12.2 texture. Acid and Dreaded
 * Charge use camera-facing sprites, while Dread Slug and Ink use fixed item renderers. No stand-in
 * renderer remains in this family.
 */
public final class ProjectileRenderers {

    private ProjectileRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        // Faithful: vanilla arrow geometry + AC texture.
        EntityType<?> arrow = ProjectileEntities.CORALIUM_ARROW.get();
        EntityRendererProvider<CoraliumArrow> arrowProvider = CoraliumArrowRenderer::new;
        renderers.register(arrow, arrowProvider);
        handled.add(arrow);

        // Faithful sprite/item equivalents of the 1.12.2 projectile renderers.
        billboard(renderers, handled, ProjectileEntities.ACID_PROJECTILE.get(),
            ACRef.id("textures/model/coralium_fireball.png"), 0.75F);
        fixedItem(renderers, handled, ProjectileEntities.DREAD_SLUG.get(),
            new ItemStack(BuiltInRegistries.ITEM.get(ACRef.id("dread_fragment"))), 0.5F);
        fixedItem(renderers, handled, ProjectileEntities.INK_PROJECTILE.get(), new ItemStack(Items.INK_SAC), 0.5F);
        billboard(renderers, handled, ProjectileEntities.DREADED_CHARGE.get(),
            ACRef.id("textures/model/dreaded_fireball.png"), 2.0F);
    }

    private static void billboard(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                  EntityType<?> type, ResourceLocation texture, float scale) {
        EntityRendererProvider<Entity> provider = ctx -> new BillboardRenderer<>(ctx, texture, scale,
            1.0F, 0.0F, false);
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void fixedItem(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                  EntityType<?> type, ItemStack item, float scale) {
        EntityRendererProvider<Entity> provider = ctx -> new FixedItemRenderer<>(ctx, item, scale, true);
        renderers.register(type, provider);
        handled.add(type);
    }
}

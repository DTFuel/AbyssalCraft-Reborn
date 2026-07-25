package com.shinoow.abyssalcraft.client.render.entity;

import java.util.Set;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import com.shinoow.abyssalcraft.client.render.entity.boss.BossGeoRenderer;
import com.shinoow.abyssalcraft.client.render.entity.boss.ChagarothGeoRenderer;
import com.shinoow.abyssalcraft.client.render.entity.boss.DragonRenderer;
import com.shinoow.abyssalcraft.client.render.entity.boss.EliteGeoRenderer;
import com.shinoow.abyssalcraft.client.render.entity.boss.EliteArmorGeoLayer;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.boss.BossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;

/**
 * Boss family renderers (owned by PE-4 / PE-4b, Stage E2), following the E2 dispatch idiom (register
 * faithful renderers, mark their {@code EntityType}s handled so the E1 placeholder skips them).
 *
 * <p>Ten bosses render via GeckoLib meshes ({@code geo/entity/<id>.geo.json}): Chagaroth via
 * {@link ChagarothGeoRenderer}, two bar-bosses (Jzahar / Sacthoth) via {@link BossGeoRenderer}, and seven
 * elites via {@link EliteGeoRenderer}. Nine meshes are converter-generated; the Dreadguard uses a
 * hand-written standard-biped geo (its 1.12.2 render reused a vanilla {@code ModelZombie}).
 *
 * <p>The two ender-dragon-derived bosses (Abyssal Dragon / Dragon Minion) instead use {@link DragonRenderer}
 * with a Java {@code HierarchicalModel} ({@link com.shinoow.abyssalcraft.client.model.entity.DragonModel}):
 * their 1.12.2 model is procedural (neck/tail splines) and cannot be a static mesh.
 * See {@code docs/spec/geckolib-model-porting.md}.
 */
public final class BossRenderers {

    private BossRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        // Three bar-bosses (BossMob) -> GeckoLib meshes.
        boss(renderers, handled, BossEntities.CHAGAROTH.get(), ChagarothGeoRenderer::new);
        boss(renderers, handled, BossEntities.JZAHAR.get(), BossGeoRenderer::new);
        boss(renderers, handled, BossEntities.SACTHOTH.get(), BossGeoRenderer::new);

        // Seven elites (EliteMob) -> GeckoLib meshes (resolved per id by EliteGeoModel). Six converter-
        // generated; the Dreadguard via a hand-written standard-biped geo (1.12.2 reused a vanilla ModelZombie).
        elite(renderers, handled, BossEntities.SKELETON_GOLIATH.get(), false,
            EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.REMNANT.get(), false, EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.SHUB_OFFSPRING.get(), true, EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.GATEKEEPER_MINION.get(), true, EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.CHAGAROTH_FIST.get(), false, EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.CHAGAROTH_SPAWN.get(), false, EliteArmorGeoLayer.Mode.NONE, false, 0.0F);
        elite(renderers, handled, BossEntities.DREADGUARD.get(), false, EliteArmorGeoLayer.Mode.DREADGUARD, true, 32.0F);

        // The two ender-dragon-derived bosses use a Java HierarchicalModel (DragonModel), NOT GeckoLib:
        // their 1.12.2 model is procedural (neck/tail rendered as multi-segment splines) and does not
        // export as a coherent static mesh.
        dragon(renderers, handled, BossEntities.DRAGON_BOSS.get(), "textures/model/boss/dragonboss.png",
            "textures/model/boss/dragonboss_eyes.png");
        dragon(renderers, handled, BossEntities.DRAGON_MINION.get(), "textures/model/elite/dragonminion.png",
            "textures/model/elite/dragonminion_eyes.png");
    }

    private static void boss(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                             EntityType<?> type, EntityRendererProvider<BossMob> provider) {
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void elite(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                              EntityType<?> type, boolean glowingEyes, EliteArmorGeoLayer.Mode armorMode,
                              boolean dreadCarrier, float carrierY) {
        boolean skeletonGoliath = type == BossEntities.SKELETON_GOLIATH.get();
        EntityRendererProvider<EliteMob> provider =
            context -> new EliteGeoRenderer(context, glowingEyes, armorMode, skeletonGoliath,
                dreadCarrier, carrierY);
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void dragon(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                               EntityType<?> type, String texturePath, String eyesPath) {
        ResourceLocation texture = ACRef.id(texturePath);
        ResourceLocation eyes = ACRef.id(eyesPath);
        EntityRendererProvider<Mob> provider = ctx -> new DragonRenderer<>(ctx, texture, eyes);
        renderers.register(type, provider);
        handled.add(type);
    }
}

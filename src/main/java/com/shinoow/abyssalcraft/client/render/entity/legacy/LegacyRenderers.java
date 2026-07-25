package com.shinoow.abyssalcraft.client.render.entity.legacy;

import java.util.Set;

import com.shinoow.abyssalcraft.client.model.entity.LegacyDreadbeastModel;
import com.shinoow.abyssalcraft.client.model.entity.LegacyDreadlingModel;
import com.shinoow.abyssalcraft.client.model.entity.LegacyDreadSpawnModel;
import com.shinoow.abyssalcraft.client.model.entity.LegacyShadowBeastModel;
import com.shinoow.abyssalcraft.client.model.entity.LegacyShadowCreatureModel;
import com.shinoow.abyssalcraft.client.model.entity.LegacyShadowMonsterModel;
import com.shinoow.abyssalcraft.content.entity.legacy.CoraliumSquid;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

public final class LegacyRenderers {

    private LegacyRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        EntityType<?> zombie = LegacyEntities.ABYSSAL_ZOMBIE.get();
        EntityRendererProvider<Zombie> zombieRenderer = AbyssalZombieRenderer::new;
        renderers.register(zombie, zombieRenderer);
        handled.add(zombie);

        EntityType<?> squid = LegacyEntities.CORALIUM_SQUID.get();
        EntityRendererProvider<CoraliumSquid> squidRenderer = CoraliumSquidRenderer::new;
        renderers.register(squid, squidRenderer);
        handled.add(squid);

        dread(renderers, handled, LegacyEntities.DREADLING.get(), ctx -> new LegacyDreadRenderer<>(ctx,
            new LegacyDreadlingModel(ctx.bakeLayer(ModModelLayers.LEGACY_DREADLING)),
            ACRef.id("textures/model/dreadling.png"), 0.5F, 1.0F, 0.0F, 3.0F, 0.0F));
        dread(renderers, handled, LegacyEntities.DREAD_SPAWN.get(), ctx -> new LegacyDreadRenderer<>(ctx,
            new LegacyDreadSpawnModel(ctx.bakeLayer(ModModelLayers.LEGACY_DREAD_SPAWN)),
            ACRef.id("textures/model/dread_spawn.png"), 0.5F, 1.0F, 0.0F, 22.0F, 0.0F));
        dread(renderers, handled, LegacyEntities.GREATER_DREAD_SPAWN.get(), ctx -> new LegacyDreadRenderer<>(ctx,
            new LegacyDreadSpawnModel(ctx.bakeLayer(ModModelLayers.LEGACY_DREAD_SPAWN)),
            ACRef.id("textures/model/greater_dread_spawn.png"), 0.5F, 2.0F, 0.0F, 22.0F, 0.0F));
        dread(renderers, handled, LegacyEntities.LESSER_DREADBEAST.get(), ctx -> new LegacyDreadRenderer<>(ctx,
            new LegacyDreadbeastModel(ctx.bakeLayer(ModModelLayers.LEGACY_DREADBEAST)),
            ACRef.id("textures/model/elite/lesser_dreadbeast.png"), 0.5F, 3.0F, 0.0F, 22.0F, 0.0F));

        shadow(renderers, handled, LegacyEntities.SHADOW_CREATURE.get(), ctx -> new LegacyShadowRenderer<>(ctx,
            new LegacyShadowCreatureModel(ctx.bakeLayer(ModModelLayers.LEGACY_SHADOW_CREATURE)),
            ACRef.id("textures/model/shadowcreature.png"),
            ACRef.id("textures/model/shadowcreature_eyes.png")));
        shadow(renderers, handled, LegacyEntities.SHADOW_MONSTER.get(), ctx -> new LegacyShadowRenderer<>(ctx,
            new LegacyShadowMonsterModel(ctx.bakeLayer(ModModelLayers.LEGACY_SHADOW_MONSTER)),
            ACRef.id("textures/model/shadowmonster.png"),
            ACRef.id("textures/model/shadowmonster_eyes.png")));
        shadow(renderers, handled, LegacyEntities.SHADOW_BEAST.get(), ctx -> new LegacyShadowRenderer<>(ctx,
            new LegacyShadowBeastModel(ctx.bakeLayer(ModModelLayers.LEGACY_SHADOW_BEAST)),
            ACRef.id("textures/model/elite/shadowbeast.png"),
            ACRef.id("textures/model/elite/shadowbeast_eyes.png")));
    }

    public static void registerLayers(EntityRendererCompat.Layers layers) {
        layers.register(ModModelLayers.LEGACY_DREADLING, LegacyDreadlingModel::createBodyLayer);
        layers.register(ModModelLayers.LEGACY_DREAD_SPAWN, LegacyDreadSpawnModel::createBodyLayer);
        layers.register(ModModelLayers.LEGACY_DREADBEAST, LegacyDreadbeastModel::createBodyLayer);
        layers.register(ModModelLayers.LEGACY_SHADOW_CREATURE, LegacyShadowCreatureModel::createBodyLayer);
        layers.register(ModModelLayers.LEGACY_SHADOW_MONSTER, LegacyShadowMonsterModel::createBodyLayer);
        layers.register(ModModelLayers.LEGACY_SHADOW_BEAST, LegacyShadowBeastModel::createBodyLayer);
    }

    private static void dread(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                              EntityType<?> type, EntityRendererProvider<LegacyHostileMob> provider) {
        renderers.register(type, provider);
        handled.add(type);
    }

    private static void shadow(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                               EntityType<?> type, EntityRendererProvider<LegacyHostileMob> provider) {
        renderers.register(type, provider);
        handled.add(type);
    }
}
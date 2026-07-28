package com.shinoow.abyssalcraft.client.render;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.client.model.entity.DragonModel;
import com.shinoow.abyssalcraft.client.model.entity.DreadTentacleModel;
import com.shinoow.abyssalcraft.client.model.entity.StarSpawnTentacleModel;
import com.shinoow.abyssalcraft.client.model.entity.DreadiumSamuraiArmorModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.SheepFurModel;
import com.shinoow.abyssalcraft.client.render.block.ACBlockEntityRenderers;
import com.shinoow.abyssalcraft.client.render.entity.ACPlaceholderRenderer;
import com.shinoow.abyssalcraft.client.render.entity.AntiDemonRenderers;
import com.shinoow.abyssalcraft.client.render.entity.BossRenderers;
import com.shinoow.abyssalcraft.client.render.entity.GhoulShoggothRenderers;
import com.shinoow.abyssalcraft.client.render.entity.MiscRenderers;
import com.shinoow.abyssalcraft.client.render.entity.ProjectileRenderers;
import com.shinoow.abyssalcraft.client.render.entity.legacy.LegacyRenderers;
import com.shinoow.abyssalcraft.client.render.entity.layers.StarSpawnTentacleLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DepthsArmorOuterLayer;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.entity.anti.AntiEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;
import com.shinoow.abyssalcraft.registry.ModModelLayers;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Entity renderer / model-layer dispatch (owned by PE-1, Stage E1). <b>Client-only</b> relay.
 *
 * <p>The single point where AbyssalCraft entity renderers and model layers are registered, wired to the
 * MOD bus by the main class through {@link EntityRendererCompat#attach} (inside {@code runWhenClient}).
 *
 * <p>Stage E1 registers the {@link ACPlaceholderRenderer} for <b>every</b> AbyssalCraft entity type
 * (discovered by namespace from the frozen registry -- auto-covering all D2a/D2b families incl. future
 * ones) so {@code runClient} no longer crashes on the 54 missing renderers. Stage E2 (PE-2..6) edits
 * this relay to register faithful per-family renderers + their {@code ModModelLayers} meshes.
 */
public final class ACEntityRenderers {

    private ACEntityRenderers() {}

    /** Register faithful E2 family renderers, then the E1 placeholder for every other AC entity. */
    public static void registerRenderers(EntityRendererCompat.Renderers renderers) {
        Set<EntityType<?>> registeredEntities = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Set<BlockEntityType<?>> registeredBlockEntities =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        EntityRendererCompat.Renderers audited = new EntityRendererCompat.Renderers() {
            @Override
            public void register(EntityType<?> type, EntityRendererProvider<?> provider) {
                if (!registeredEntities.add(type)) {
                    throw new IllegalStateException("Duplicate entity renderer: "
                        + BuiltInRegistries.ENTITY_TYPE.getKey(type));
                }
                renderers.register(type, provider);
            }

            @Override
            public <T extends BlockEntity> void registerBlockEntity(BlockEntityType<T> type,
                                                                    BlockEntityRendererProvider<T> provider) {
                if (!registeredBlockEntities.add(type)) {
                    throw new IllegalStateException("Duplicate block entity renderer: "
                        + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
                }
                renderers.registerBlockEntity(type, provider);
            }
        };
        Set<EntityType<?>> handled = new HashSet<>();
        // Stage E2 faithful family renderers override the E1 placeholder; each E2 task adds its call here.
        AntiDemonRenderers.register(audited, handled);
        GhoulShoggothRenderers.register(audited, handled);
        BossRenderers.register(audited, handled);
        MiscRenderers.register(audited, handled);
        ProjectileRenderers.register(audited, handled);
        LegacyRenderers.register(audited, handled);
        // BlockEntity renderers ride the same RegisterRenderers event. The four modern hosts that
        // need dynamic world rendering are kept exact by the gate below.
        ACBlockEntityRenderers.register(audited);
        // E1 default: placeholder for every AC entity not yet handled by an E2 family renderer.
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (AbyssalCraft.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace()) && !handled.contains(type)) {
                audited.register(type, ACPlaceholderRenderer::new);
            }
        }
        long expectedEntities = BuiltInRegistries.ENTITY_TYPE.stream()
            .filter(type -> AbyssalCraft.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace()))
            .count();
        Set<BlockEntityType<?>> expectedBlockEntities = Set.of(
            ResearchTables.RESEARCH_TABLE_BE.get(),
            RitualBlocks.RITUAL_PEDESTAL_BE.get(),
            RendingPedestals.RENDING_PEDESTAL_BE.get(),
            EnergyBlocks.ENERGY_PEDESTAL_BE.get());
        if (registeredEntities.size() != expectedEntities
            || !registeredBlockEntities.equals(expectedBlockEntities)) {
            throw new IllegalStateException("R4 renderer relay coverage changed: entities="
                + registeredEntities.size() + "/" + expectedEntities + ", blockEntities="
                + registeredBlockEntities.size() + "/" + expectedBlockEntities.size());
        }
        AbyssalCraft.LOGGER.info(
            "RR_BER_HOST_CLOSURE_OK registered={} research={} ritual={} rending={} energy={}",
            registeredBlockEntities.size(),
            registeredBlockEntities.contains(ResearchTables.RESEARCH_TABLE_BE.get()) ? 1 : 0,
            registeredBlockEntities.contains(RitualBlocks.RITUAL_PEDESTAL_BE.get()) ? 1 : 0,
            registeredBlockEntities.contains(RendingPedestals.RENDING_PEDESTAL_BE.get()) ? 1 : 0,
            registeredBlockEntities.contains(EnergyBlocks.ENERGY_PEDESTAL_BE.get()) ? 1 : 0);
    }

    /** Register model-layer definitions (placeholder cube in E1; E2 appends faithful meshes). */
    public static void registerLayers(EntityRendererCompat.Layers layers) {
        Set<ModelLayerLocation> registered = new HashSet<>();
        EntityRendererCompat.Layers audited = (location, definition) -> {
            if (!registered.add(location)) {
                throw new IllegalStateException("Duplicate model layer: " + location);
            }
            layers.register(location, definition);
        };
        audited.register(ModModelLayers.PLACEHOLDER, ModModelLayers::placeholder);
        audited.register(ModModelLayers.BILLBOARD, ModModelLayers::billboard);
        audited.register(ModModelLayers.ODB_CUBE, ModModelLayers::odbCube);
        audited.register(ModModelLayers.DREAD_TENTACLE, DreadTentacleModel::createBodyLayer);
        audited.register(ModModelLayers.STAR_SPAWN_TENTACLES, StarSpawnTentacleModel::createBodyLayer);
        audited.register(ModModelLayers.DEMON_SHEEP, SheepModel::createBodyLayer);
        audited.register(ModModelLayers.DEMON_SHEEP_FUR, SheepFurModel::createFurLayer);
        audited.register(ModModelLayers.SAMURAI_INNER, () -> DreadiumSamuraiArmorModel.createBodyLayer(0.5F));
        audited.register(ModModelLayers.SAMURAI_OUTER, () -> DreadiumSamuraiArmorModel.createBodyLayer(1.0F));
        audited.register(ModModelLayers.DEPTHS_INNER, () -> ModModelLayers.humanoidArmor(0.75F));
        audited.register(ModModelLayers.DEPTHS_OUTER, () -> ModModelLayers.humanoidArmor(1.25F));
        audited.register(ModModelLayers.DEPTHS_SKELETON_INNER, () -> ModModelLayers.skeletonArmor(0.75F));
        audited.register(ModModelLayers.DEPTHS_SKELETON_OUTER, () -> ModModelLayers.skeletonArmor(1.25F));
        audited.register(ModModelLayers.DEPTHS_ARMOR_STAND_INNER, () -> ModModelLayers.armorStandArmor(0.75F));
        audited.register(ModModelLayers.DEPTHS_ARMOR_STAND_OUTER, () -> ModModelLayers.armorStandArmor(1.25F));
        audited.register(ModModelLayers.ABYSSAL_ZOMBIE, () -> ModModelLayers.classicHumanoid(0.0F));
        audited.register(ModModelLayers.ABYSSAL_ZOMBIE_INNER, () -> ModModelLayers.classicHumanoid(0.5F));
        audited.register(ModModelLayers.ABYSSAL_ZOMBIE_OUTER, () -> ModModelLayers.classicHumanoid(1.0F));
        audited.register(ModModelLayers.DRAGON, DragonModel::createBodyLayer);
        GhoulShoggothRenderers.registerLayers(audited);
        LegacyRenderers.registerLayers(audited);
        if (!registered.contains(ModModelLayers.PLACEHOLDER)
            || !registered.contains(ModModelLayers.DRAGON)
            || !registered.contains(ModModelLayers.ABYSSAL_ZOMBIE)) {
            throw new IllegalStateException("R2 model layer relay is incomplete");
        }
        AbyssalCraft.LOGGER.info("R2_GATE_CLIENT_LAYERS_OK layers={}", registered.size());
    }

    /** Dynamic layers attached after vanilla player renderers exist. */
    public static void registerPlayerLayers(EntityRendererCompat.LayerTargets targets) {
        targets.forEachPlayer((renderer, models) -> {
            renderer.addLayer(new StarSpawnTentacleLayer(renderer, models));
            renderer.addLayer(new DepthsArmorOuterLayer(renderer, models));
        });
        targets.forHumanoid(EntityType.ARMOR_STAND, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.ZOMBIE, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.HUSK, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.DROWNED, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.SKELETON, DepthsArmorOuterLayer::attachSkeleton);
        targets.forHumanoid(EntityType.STRAY, DepthsArmorOuterLayer::attachSkeleton);
        targets.forHumanoid(EntityType.WITHER_SKELETON, DepthsArmorOuterLayer::attachSkeleton);
        targets.forHumanoid(EntityType.PIGLIN, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.PIGLIN_BRUTE, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(EntityType.ZOMBIFIED_PIGLIN, DepthsArmorOuterLayer::attach);
        targets.forHumanoid(LegacyEntities.ABYSSAL_ZOMBIE.get(), DepthsArmorOuterLayer::attach);
        targets.forHumanoid(AntiEntities.ANTI_ZOMBIE.get(), DepthsArmorOuterLayer::attach);
        targets.forHumanoid(AntiEntities.ANTI_ABYSSAL_ZOMBIE.get(), DepthsArmorOuterLayer::attach);
        targets.forHumanoid(AntiEntities.ANTI_SKELETON.get(), DepthsArmorOuterLayer::attachSkeleton);
        targets.forHumanoid(AntiEntities.ANTI_PLAYER.get(), DepthsArmorOuterLayer::attach);
    }
}

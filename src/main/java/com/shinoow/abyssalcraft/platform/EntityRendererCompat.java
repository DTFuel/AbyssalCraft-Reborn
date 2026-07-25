package com.shinoow.abyssalcraft.platform;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.EntityRenderersEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
*///?}

/**
 * Compat: entity-renderer + model-layer registration dispatch (loader axis). <b>Client-only</b>.
 *
 * <p>Both loaders publish the same {@code EntityRenderersEvent} (nested {@code RegisterRenderers} /
 * {@code RegisterLayerDefinitions}, with identical {@code registerEntityRenderer} /
 * {@code registerLayerDefinition} methods) on the MOD bus; only the package differs (Forge
 * {@code net.minecraftforge.client.event} vs NeoForge {@code net.neoforged.neoforge.client.event}).
 * So only the import forks -- the body is loader-neutral. Business relays ({@code ACEntityRenderers})
 * receive the fork-free {@link Renderers}/{@link Layers} sinks and never import the loader event.
 *
 * <p>Never referenced on a dedicated server: the main class attaches it inside
 * {@link SideExecutor#runWhenClient}, so this class (and the client-render packages) never load there.
 */
public final class EntityRendererCompat {

    private EntityRendererCompat() {}

    /** Loader-neutral entity-renderer sink (E1 registers a placeholder for all; E2 overrides per family). */
    public interface Renderers {
        void register(EntityType<?> type, EntityRendererProvider<?> provider);

        /** BlockEntity-renderer sink (PE-6, Stage E2): registered in the same {@code RegisterRenderers} event. */
        <T extends BlockEntity> void registerBlockEntity(BlockEntityType<T> type,
                                                         BlockEntityRendererProvider<T> provider);
    }

    /** Loader-neutral model-layer sink. */
    public interface Layers {
        void register(ModelLayerLocation location, Supplier<LayerDefinition> definition);
    }

    /** Loader-neutral access to the default + slim player renderers during AddLayers. */
    public interface LayerTargets {
        void forEachPlayer(PlayerLayerConsumer consumer);

        void forHumanoid(EntityType<?> type, HumanoidLayerConsumer consumer);
    }

    @FunctionalInterface
    public interface PlayerLayerConsumer {
        void accept(PlayerRenderer renderer, EntityModelSet models);
    }

    @FunctionalInterface
    public interface HumanoidLayerConsumer {
        void accept(LivingEntityRenderer<?, ?> renderer, EntityModelSet models);
    }

    /**
     * Attach both client renderer-registration listeners to the MOD bus. The callbacks fire inside the
     * matching {@code EntityRenderersEvent} (registry frozen) with a fork-free sink.
     */
    public static void attach(IEventBus modBus, Consumer<Renderers> renderers, Consumer<Layers> layers,
                              Consumer<LayerTargets> layerTargets) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
            renderers.accept(new Renderers() {
                @Override
                @SuppressWarnings({"unchecked", "rawtypes"})
                public void register(EntityType<?> type, EntityRendererProvider<?> provider) {
                    event.registerEntityRenderer((EntityType) type, (EntityRendererProvider) provider);
                }

                @Override
                public <T extends BlockEntity> void registerBlockEntity(BlockEntityType<T> type,
                                                                        BlockEntityRendererProvider<T> provider) {
                    event.registerBlockEntityRenderer(type, provider);
                }
            }));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
            layers.accept(event::registerLayerDefinition));
        modBus.addListener((EntityRenderersEvent.AddLayers event) ->
            layerTargets.accept(new LayerTargets() {
                @Override
                public void forEachPlayer(PlayerLayerConsumer consumer) {
                //? if forge {
                    for (String skin : event.getSkins()) {
                        if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                            consumer.accept(renderer, event.getEntityModels());
                        }
                    }
                //?} else {
                    /*for (net.minecraft.client.resources.PlayerSkin.Model skin : event.getSkins()) {
                        if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                            consumer.accept(renderer, event.getEntityModels());
                        }
                    }
                    *///?}
                }

                @Override
                @SuppressWarnings({"rawtypes", "unchecked"})
                public void forHumanoid(EntityType<?> type, HumanoidLayerConsumer consumer) {
                    Object renderer = event.getRenderer((EntityType) type);
                    if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                        consumer.accept(livingRenderer, event.getEntityModels());
                    }
                }
            }));
    }
}

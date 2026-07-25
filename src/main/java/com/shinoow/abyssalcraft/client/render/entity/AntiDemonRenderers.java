package com.shinoow.abyssalcraft.client.render.entity;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.content.entity.anti.AntiEntities;
import com.shinoow.abyssalcraft.content.entity.anti.AntiGhoul;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonAnimal;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;
import com.shinoow.abyssalcraft.client.render.entity.layers.SimpleEyesLayer;
import com.shinoow.abyssalcraft.client.render.entity.layers.DreadCarrierTentacleLayer;

import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * Faithful renderers for the anti (PD-3) + demon (PD-4) families (owned by PE-2, Stage E2).
 *
 * <p>Reproduces the 1.12.2 approach: a vanilla model + the AC anti/demon texture (from
 * {@code textures/model/}). {@code evil_*} mobs look like normal animals, so they use the vanilla
 * texture (faithful to {@code RenderEvilCow}). All 19 register through the fork-free
 * {@link EntityRendererCompat.Renderers} sink and are added to {@code handled} so {@code ACEntityRenderers}
 * skips them in the E1 placeholder pass.
 *
 * <p>Deferred (noted): the custom {@code ModelGhoul} (anti-ghoul approximated with the biped model),
 * the sheep wool layer, the anti-spider glowing-eyes layer, armor layers (&rarr; PE-5), and the
 * creeper charge layer -- faithful visual polish that the follow-up layer work adds.
 */
public final class AntiDemonRenderers {

    private AntiDemonRenderers() {}

    public static void register(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled) {
        // --- anti: vanilla model + anti texture (textures/model/anti/*.png) ---
        regArmored(renderers, handled, AntiEntities.ANTI_ZOMBIE, 0.5F, ac("anti/zombie.png"),
            ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
        regArmored(renderers, handled, AntiEntities.ANTI_ABYSSAL_ZOMBIE, 0.5F, ac("anti/abyssal_zombie.png"),
            ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
        regArmored(renderers, handled, AntiEntities.ANTI_SKELETON, 0.5F, ac("anti/skeleton.png"),
            ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
        regArmored(renderers, handled, AntiEntities.ANTI_PLAYER, 0.5F, ac("anti/steve.png"),
            ModelLayers.PLAYER, ModelLayers.PLAYER_INNER_ARMOR, ModelLayers.PLAYER_OUTER_ARMOR);
        EntityType<?> antiGhoul = AntiEntities.ANTI_GHOUL.get();
        EntityRendererProvider<AntiGhoul> antiGhoulRenderer = ctx ->
            new GhoulRenderer<>(ctx, ac("anti/depths_ghoul.png"), null, false, false);
        renderers.register(antiGhoul, antiGhoulRenderer);
        handled.add(antiGhoul);
        reg(renderers, handled, AntiEntities.ANTI_CREEPER, 0.5F, ac("anti/creeper.png"),
            ctx -> new CreeperModel<>(ctx.bakeLayer(ModelLayers.CREEPER)));
        EntityType<?> antiSpider = AntiEntities.ANTI_SPIDER.get();
        renderers.register(antiSpider, ctx -> {
            SpiderModel model = new SpiderModel<>(ctx.bakeLayer(ModelLayers.SPIDER));
            ACTexturedRenderer renderer = new ACTexturedRenderer(ctx, model, 1.0F, ac("anti/spider.png"));
            renderer.addLayer(new SimpleEyesLayer(renderer, ac("anti/spider_eyes.png")));
            return renderer;
        });
        handled.add(antiSpider);
        reg(renderers, handled, AntiEntities.ANTI_COW, 0.7F, ac("anti/cow.png"),
            ctx -> new CowModel<>(ctx.bakeLayer(ModelLayers.COW)));
        reg(renderers, handled, AntiEntities.ANTI_PIG, 0.7F, ac("anti/pig.png"),
            ctx -> new PigModel<>(ctx.bakeLayer(ModelLayers.PIG)));
        reg(renderers, handled, AntiEntities.ANTI_CHICKEN, 0.3F, ac("anti/chicken.png"),
            ctx -> new ChickenModel<>(ctx.bakeLayer(ModelLayers.CHICKEN)));
        reg(renderers, handled, AntiEntities.ANTI_BAT, 0.25F, ac("anti/bat.png"),
            ctx -> new BatModel(ctx.bakeLayer(ModelLayers.BAT)));
        // --- demon: vanilla animal model + demon texture (textures/model/demon_*.png) ---
        regCarrier(renderers, handled, DemonEntities.DEMON_CHICKEN, 0.3F, ac("demon_chicken.png"),
            ctx -> new ChickenModel<>(ctx.bakeLayer(ModelLayers.CHICKEN)));
        regCarrier(renderers, handled, DemonEntities.DEMON_COW, 0.7F, ac("demon_cow.png"),
            ctx -> new CowModel<>(ctx.bakeLayer(ModelLayers.COW)));
        regCarrier(renderers, handled, DemonEntities.DEMON_PIG, 0.7F, ac("demon_pig.png"),
            ctx -> new PigModel<>(ctx.bakeLayer(ModelLayers.PIG)));
        EntityType<?> demonSheep = DemonEntities.DEMON_SHEEP.get();
        EntityRendererProvider<DemonAnimal> demonSheepRenderer =
            ctx -> new DemonSheepRenderer(ctx, ac("demon_sheep.png"), ac("demon_sheep_fur.png"));
        renderers.register(demonSheep, demonSheepRenderer);
        handled.add(demonSheep);
        // --- evil: vanilla animal model + VANILLA texture (evil animals look normal) ---
        reg(renderers, handled, DemonEntities.EVIL_CHICKEN, 0.3F, ACRef.vanilla("textures/entity/chicken.png"),
            ctx -> new ChickenModel<>(ctx.bakeLayer(ModelLayers.CHICKEN)));
        reg(renderers, handled, DemonEntities.EVIL_COW, 0.7F, ACRef.vanilla("textures/entity/cow/cow.png"),
            ctx -> new CowModel<>(ctx.bakeLayer(ModelLayers.COW)));
        reg(renderers, handled, DemonEntities.EVIL_PIG, 0.7F, ACRef.vanilla("textures/entity/pig/pig.png"),
            ctx -> new PigModel<>(ctx.bakeLayer(ModelLayers.PIG)));
        EntityType<?> evilSheep = DemonEntities.EVIL_SHEEP.get();
        EntityRendererProvider<EvilAnimal> evilSheepRenderer = EvilSheepRenderer::new;
        renderers.register(evilSheep, evilSheepRenderer);
        handled.add(evilSheep);
    }

    private static ResourceLocation ac(String path) {
        return ACRef.id("textures/model/" + path);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void reg(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                            Supplier<? extends EntityType<?>> typeSup, float shadow, ResourceLocation texture,
                            Function<EntityRendererProvider.Context, EntityModel<?>> modelFn) {
        EntityType<?> type = typeSup.get();
        handled.add(type);
        renderers.register(type, ctx -> new ACTexturedRenderer(ctx, modelFn.apply(ctx), shadow, texture));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void regCarrier(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                   Supplier<? extends EntityType<?>> typeSup, float shadow,
                                   ResourceLocation texture,
                                   Function<EntityRendererProvider.Context, EntityModel<?>> modelFn) {
        EntityType<?> type = typeSup.get();
        handled.add(type);
        renderers.register(type, ctx -> {
            ACTexturedRenderer renderer = new ACTexturedRenderer(ctx, modelFn.apply(ctx), shadow, texture);
            renderer.addLayer(new DreadCarrierTentacleLayer(renderer, ctx.getModelSet(), 0.0F, 9.0F, 2.0F));
            return renderer;
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void regArmored(EntityRendererCompat.Renderers renderers, Set<EntityType<?>> handled,
                                   Supplier<? extends EntityType<?>> typeSup, float shadow,
                                   ResourceLocation texture, ModelLayerLocation bodyLayer,
                                   ModelLayerLocation innerArmor, ModelLayerLocation outerArmor) {
        EntityType<?> type = typeSup.get();
        handled.add(type);
        renderers.register(type, ctx -> {
            HumanoidModel model = new HumanoidModel(ctx.bakeLayer(bodyLayer));
            ACTexturedRenderer renderer = new ACTexturedRenderer(ctx, model, shadow, texture);
            renderer.addLayer(new HumanoidArmorLayer(renderer,
                new HumanoidModel(ctx.bakeLayer(innerArmor)),
                new HumanoidModel(ctx.bakeLayer(outerArmor)), ctx.getModelManager()));
            return renderer;
        });
    }
}

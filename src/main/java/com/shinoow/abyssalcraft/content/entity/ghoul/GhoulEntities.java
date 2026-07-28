package com.shinoow.abyssalcraft.content.entity.ghoul;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.SpawnEggCompat;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;

/**
 * Ghoul family registry (owned by PD-5, Stage D2a).
 *
 * <p>Registers the five ghoul {@link EntityType}s + their spawn eggs, publishes each mob's attribute
 * supplier (via {@link EntityAttributeCompat}) and its ground/darkness spawn-placement rule (via
 * {@link SpawnPlacementCompat}). Both registrars are attached to the MOD bus through
 * {@code ModRegistries.ALL}; the attribute + placement events are hooked once from the main class.
 *
 * <p>Natural spawning is data-driven: dimension-owned ghouls are listed directly by their biome,
 * while loader-specific biome modifiers handle cross-biome additions. Loot lives in
 * {@code loot_tables/entities/*} (dual-pathed for 1.20.1 / 1.21).
 */
public final class GhoulEntities {

    private GhoulEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace for the ghoul family. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    /** {@code minecraft:item} registrar for the ghoul spawn eggs. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<EntityType<Ghoul>> GHOUL = ENTITIES.register("ghoul", () ->
        EntityType.Builder.<Ghoul>of(Ghoul::new, MobCategory.MONSTER).sized(0.9F, 1.7F).build("ghoul"));
    public static final Supplier<EntityType<DepthsGhoul>> DEPTHS_GHOUL = ENTITIES.register("depths_ghoul", () ->
        EntityType.Builder.<DepthsGhoul>of(DepthsGhoul::new, MobCategory.MONSTER).sized(0.9F, 1.7F).build("depths_ghoul"));
    public static final Supplier<EntityType<DreadedGhoul>> DREADED_GHOUL = ENTITIES.register("dreaded_ghoul", () ->
        EntityType.Builder.<DreadedGhoul>of(DreadedGhoul::new, MobCategory.MONSTER).sized(0.9F, 1.7F).build("dreaded_ghoul"));
    public static final Supplier<EntityType<OmotholGhoul>> OMOTHOL_GHOUL = ENTITIES.register("omothol_ghoul", () ->
        EntityType.Builder.<OmotholGhoul>of(OmotholGhoul::new, MobCategory.MONSTER).sized(1.3F, 2.7F).build("omothol_ghoul"));
    public static final Supplier<EntityType<ShadowGhoul>> SHADOW_GHOUL = ENTITIES.register("shadow_ghoul", () ->
        EntityType.Builder.<ShadowGhoul>of(ShadowGhoul::new, MobCategory.MONSTER).sized(0.9F, 1.7F).build("shadow_ghoul"));

    public static final Supplier<Item> GHOUL_SPAWN_EGG =
        ITEMS.register("ghoul_spawn_egg", () -> SpawnEggCompat.create(GHOUL, 0xA1A766, 0x40460C));
    public static final Supplier<Item> DEPTHS_GHOUL_SPAWN_EGG =
        ITEMS.register("depths_ghoul_spawn_egg", () -> SpawnEggCompat.create(DEPTHS_GHOUL, 0x36A880, 0x012626));
    public static final Supplier<Item> DREADED_GHOUL_SPAWN_EGG =
        ITEMS.register("dreaded_ghoul_spawn_egg", () -> SpawnEggCompat.create(DREADED_GHOUL, 0xE60000, 0xCC0000));
    public static final Supplier<Item> OMOTHOL_GHOUL_SPAWN_EGG =
        ITEMS.register("omothol_ghoul_spawn_egg", () -> SpawnEggCompat.create(OMOTHOL_GHOUL, 0x133133, 0x342122));
    public static final Supplier<Item> SHADOW_GHOUL_SPAWN_EGG =
        ITEMS.register("shadow_ghoul_spawn_egg", () -> SpawnEggCompat.create(SHADOW_GHOUL, 0x000000, 0xFFFFFF));

    static {
        // Attribute suppliers -> mod-bus EntityAttributeCreationEvent (loaded via ModRegistries.ALL).
        EntityAttributeCompat.register(GHOUL, Ghoul::createAttributes);
        EntityAttributeCompat.register(DEPTHS_GHOUL, DepthsGhoul::createAttributes);
        EntityAttributeCompat.register(DREADED_GHOUL, DreadedGhoul::createAttributes);
        EntityAttributeCompat.register(OMOTHOL_GHOUL, OmotholGhoul::createAttributes);
        EntityAttributeCompat.register(SHADOW_GHOUL, ShadowGhoul::createAttributes);
        // Ground + monster (darkness) spawn placement, matching vanilla hostile mobs.
        placement(GHOUL);
        placement(DEPTHS_GHOUL);
        placement(DREADED_GHOUL);
        placement(OMOTHOL_GHOUL);
        placement(SHADOW_GHOUL);
    }

    private static <T extends Monster> void placement(Supplier<EntityType<T>> type) {
        SpawnPlacementCompat.registerGroundMonster(type);
    }
}

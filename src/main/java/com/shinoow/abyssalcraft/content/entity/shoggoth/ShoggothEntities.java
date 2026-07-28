package com.shinoow.abyssalcraft.content.entity.shoggoth;

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
 * Shoggoth family registry (owned by PD-5, Stage D2a).
 *
 * <p>Registers the three shoggoth {@link EntityType}s + their spawn eggs, publishes each mob's
 * attribute supplier (via {@link EntityAttributeCompat}) and its ground/darkness spawn-placement rule
 * (via {@link SpawnPlacementCompat}). Both registrars attach to the MOD bus through
 * {@code ModRegistries.ALL}.
 *
 * <p>Natural spawning is data-driven: Abyssal Wasteland biomes own their spawner entries directly,
 * while loader-specific {@code biome_modifier/spawn_shoggoth.json} files add the family to the other
 * dimensions. Loot lives in {@code loot_tables/entities/*} (dual-pathed for 1.20.1 / 1.21).
 */
public final class ShoggothEntities {

    private ShoggothEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace for the shoggoth family. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    /** {@code minecraft:item} registrar for the shoggoth spawn eggs. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<EntityType<LesserShoggoth>> LESSER_SHOGGOTH = ENTITIES.register("lesser_shoggoth", () ->
        EntityType.Builder.<LesserShoggoth>of(LesserShoggoth::new, MobCategory.MONSTER).sized(0.9F, 1.3F).build("lesser_shoggoth"));
    public static final Supplier<EntityType<Shoggoth>> SHOGGOTH = ENTITIES.register("shoggoth", () ->
        EntityType.Builder.<Shoggoth>of(Shoggoth::new, MobCategory.MONSTER).sized(1.2F, 1.8F).build("shoggoth"));
    public static final Supplier<EntityType<GreaterShoggoth>> GREATER_SHOGGOTH = ENTITIES.register("greater_shoggoth", () ->
        EntityType.Builder.<GreaterShoggoth>of(GreaterShoggoth::new, MobCategory.MONSTER).sized(1.8F, 2.6F).build("greater_shoggoth"));

    public static final Supplier<Item> LESSER_SHOGGOTH_SPAWN_EGG =
        ITEMS.register("lesser_shoggoth_spawn_egg", () -> SpawnEggCompat.create(LESSER_SHOGGOTH, 0x133133, 0x342122));
    public static final Supplier<Item> SHOGGOTH_SPAWN_EGG =
        ITEMS.register("shoggoth_spawn_egg", () -> SpawnEggCompat.create(SHOGGOTH, 0x133133, 0x342122));
    public static final Supplier<Item> GREATER_SHOGGOTH_SPAWN_EGG =
        ITEMS.register("greater_shoggoth_spawn_egg", () -> SpawnEggCompat.create(GREATER_SHOGGOTH, 0x133133, 0x342122));

    static {
        EntityAttributeCompat.register(LESSER_SHOGGOTH, LesserShoggoth::createAttributes);
        EntityAttributeCompat.register(SHOGGOTH, Shoggoth::createAttributes);
        EntityAttributeCompat.register(GREATER_SHOGGOTH, GreaterShoggoth::createAttributes);
        placement(LESSER_SHOGGOTH);
        placement(SHOGGOTH);
        placement(GREATER_SHOGGOTH);
    }

    private static <T extends Monster> void placement(Supplier<EntityType<T>> type) {
        SpawnPlacementCompat.registerGroundMonster(type);
    }
}

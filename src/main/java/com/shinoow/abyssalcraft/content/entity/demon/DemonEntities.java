package com.shinoow.abyssalcraft.content.entity.demon;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.SpawnEggCompat;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;

/**
 * Demon / evil animal registrations (owned by PD-4, Stage D2a).
 *
 * <p>Registers the four demon and four evil animal {@link EntityType}s (over the single {@link DemonAnimal}
 * / {@link EvilAnimal} classes, with the species {@link AnimalKind} captured in each factory), plus their
 * base attribute suppliers via {@link EntityAttributeCompat}. Attached to the MOD bus through
 * {@code ModRegistries.ALL}; the attribute event is hooked once from the main class (PD-1). Fire immunity
 * is baked into each type. Verified with {@code /summon} on a dedicated server (renderers are Stage E).
 */
public final class DemonEntities {

    private DemonEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    /** Species -> demon type, so {@link EvilAnimal} can spawn its matching demon on death. */
    private static final Map<AnimalKind, Supplier<EntityType<DemonAnimal>>> DEMON_TYPES = new EnumMap<>(AnimalKind.class);

    public static final Supplier<EntityType<DemonAnimal>> DEMON_CHICKEN = demon(AnimalKind.CHICKEN);
    public static final Supplier<EntityType<DemonAnimal>> DEMON_COW = demon(AnimalKind.COW);
    public static final Supplier<EntityType<DemonAnimal>> DEMON_PIG = demon(AnimalKind.PIG);
    public static final Supplier<EntityType<DemonAnimal>> DEMON_SHEEP = demon(AnimalKind.SHEEP);

    public static final Supplier<EntityType<EvilAnimal>> EVIL_CHICKEN = evil(AnimalKind.CHICKEN);
    public static final Supplier<EntityType<EvilAnimal>> EVIL_COW = evil(AnimalKind.COW);
    public static final Supplier<EntityType<EvilAnimal>> EVIL_PIG = evil(AnimalKind.PIG);
    public static final Supplier<EntityType<EvilAnimal>> EVIL_SHEEP = evil(AnimalKind.SHEEP);

    public static final Supplier<Item> DEMON_CHICKEN_SPAWN_EGG = egg("demon_chicken", DEMON_CHICKEN, 0xA1A1A1, 0xFF0000);
    public static final Supplier<Item> DEMON_COW_SPAWN_EGG = egg("demon_cow", DEMON_COW, 0x443626, 0xA1A1A1);
    public static final Supplier<Item> DEMON_PIG_SPAWN_EGG = egg("demon_pig", DEMON_PIG, 0xF0A5A2, 0xDB635F);
    public static final Supplier<Item> DEMON_SHEEP_SPAWN_EGG = egg("demon_sheep", DEMON_SHEEP, 0xE7E7E7, 0xFFB5B5);
    public static final Supplier<Item> EVIL_CHICKEN_SPAWN_EGG = egg("evil_chicken", EVIL_CHICKEN, 0xA1A1A1, 0xFF0000);
    public static final Supplier<Item> EVIL_COW_SPAWN_EGG = egg("evil_cow", EVIL_COW, 0x443626, 0xA1A1A1);
    public static final Supplier<Item> EVIL_PIG_SPAWN_EGG = egg("evil_pig", EVIL_PIG, 0xF0A5A2, 0xDB635F);
    public static final Supplier<Item> EVIL_SHEEP_SPAWN_EGG = egg("evil_sheep", EVIL_SHEEP, 0xE7E7E7, 0xFFB5B5);

    private static Supplier<EntityType<DemonAnimal>> demon(AnimalKind kind) {
        String name = "demon_" + kind.id();
        Supplier<EntityType<DemonAnimal>> type = ENTITIES.register(name, () ->
            EntityType.Builder.<DemonAnimal>of((t, l) -> new DemonAnimal(t, l, kind), MobCategory.MONSTER)
                .sized(kind.width(), kind.height())
                .fireImmune()
                .build(name));
        DEMON_TYPES.put(kind, type);
        EntityAttributeCompat.register(type, () -> DemonAnimal.createAttributes(kind.health()));
        SpawnPlacementCompat.registerGroundMonster(type);
        return type;
    }

    private static Supplier<EntityType<EvilAnimal>> evil(AnimalKind kind) {
        String name = "evil_" + kind.id();
        Supplier<EntityType<EvilAnimal>> type = ENTITIES.register(name, () ->
            EntityType.Builder.<EvilAnimal>of((t, l) -> new EvilAnimal(t, l, kind), MobCategory.MONSTER)
                .sized(kind.width(), kind.height())
                .fireImmune()
                .build(name));
        EntityAttributeCompat.register(type, () -> DemonAnimal.createAttributes(kind.health()));
        SpawnPlacementCompat.registerGroundMonster(type);
        return type;
    }

    private static Supplier<Item> egg(String id, Supplier<? extends EntityType<? extends net.minecraft.world.entity.Mob>> type,
                                      int background, int highlight) {
        return ITEMS.register(id + "_spawn_egg", () -> SpawnEggCompat.create(type, background, highlight));
    }

    /** The demon {@link EntityType} for a species (used by {@link EvilAnimal}'s death transform). */
    public static Supplier<EntityType<DemonAnimal>> demonType(AnimalKind kind) {
        return DEMON_TYPES.get(kind);
    }
}

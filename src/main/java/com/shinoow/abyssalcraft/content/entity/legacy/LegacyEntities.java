package com.shinoow.abyssalcraft.content.entity.legacy;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.SpawnEggCompat;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;

public final class LegacyEntities {

    private LegacyEntities() {}

    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<EntityType<AbyssalZombie>> ABYSSAL_ZOMBIE = ENTITIES.register("abyssalzombie", () ->
        EntityType.Builder.<AbyssalZombie>of(AbyssalZombie::new, MobCategory.MONSTER)
            .sized(0.6F, 1.8F).build("abyssalzombie"));
    public static final Supplier<EntityType<CoraliumSquid>> CORALIUM_SQUID = ENTITIES.register("coraliumsquid", () ->
        EntityType.Builder.<CoraliumSquid>of(CoraliumSquid::new, MobCategory.WATER_CREATURE)
            .sized(0.8F, 0.8F).build("coraliumsquid"));
    public static final Supplier<EntityType<LegacyHostileMob>> DREADLING = hostile(LegacyMobKind.DREADLING);
    public static final Supplier<EntityType<LegacyHostileMob>> DREAD_SPAWN = hostile(LegacyMobKind.DREAD_SPAWN);
    public static final Supplier<EntityType<LegacyHostileMob>> GREATER_DREAD_SPAWN = hostile(LegacyMobKind.GREATER_DREAD_SPAWN);
    public static final Supplier<EntityType<LegacyHostileMob>> LESSER_DREADBEAST = hostile(LegacyMobKind.LESSER_DREADBEAST);
    public static final Supplier<EntityType<LegacyHostileMob>> SHADOW_CREATURE = hostile(LegacyMobKind.SHADOW_CREATURE);
    public static final Supplier<EntityType<LegacyHostileMob>> SHADOW_MONSTER = hostile(LegacyMobKind.SHADOW_MONSTER);
    public static final Supplier<EntityType<LegacyHostileMob>> SHADOW_BEAST = hostile(LegacyMobKind.SHADOW_BEAST);

    public static final Supplier<Item> ABYSSAL_ZOMBIE_EGG = egg("abyssalzombie", ABYSSAL_ZOMBIE, 0x36A880, 0x052824);
    public static final Supplier<Item> CORALIUM_SQUID_EGG = egg("coraliumsquid", CORALIUM_SQUID, 0x014E43, 0x148F7E);
    public static final Supplier<Item> DREADLING_EGG = egg("dreadling", DREADLING, 0xE60000, 0xCC0000);
    public static final Supplier<Item> DREAD_SPAWN_EGG = egg("dreadspawn", DREAD_SPAWN, 0xE60000, 0xCC0000);
    public static final Supplier<Item> GREATER_DREAD_SPAWN_EGG = egg("greaterdreadspawn", GREATER_DREAD_SPAWN, 0xE60000, 0xCC0000);
    public static final Supplier<Item> LESSER_DREADBEAST_EGG = egg("lesserdreadbeast", LESSER_DREADBEAST, 0xE60000, 0xCC0000);
    public static final Supplier<Item> SHADOW_CREATURE_EGG = egg("shadowcreature", SHADOW_CREATURE, 0x000000, 0xFFFFFF);
    public static final Supplier<Item> SHADOW_MONSTER_EGG = egg("shadowmonster", SHADOW_MONSTER, 0x000000, 0xFFFFFF);
    public static final Supplier<Item> SHADOW_BEAST_EGG = egg("shadowbeast", SHADOW_BEAST, 0x000000, 0xFFFFFF);

    static {
        EntityAttributeCompat.register(ABYSSAL_ZOMBIE, AbyssalZombie::createAttributes);
        SpawnPlacementCompat.registerGroundMonster(ABYSSAL_ZOMBIE);
        EntityAttributeCompat.register(CORALIUM_SQUID, Squid::createAttributes);
        SpawnPlacementCompat.registerWaterAnimal(CORALIUM_SQUID);
    }

    public static boolean isCoralium(net.minecraft.world.entity.LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == ABYSSAL_ZOMBIE.get() || type == CORALIUM_SQUID.get()
            || type == com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities.DEPTHS_GHOUL.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.DRAGON_MINION.get();
    }

    public static boolean isDread(net.minecraft.world.entity.LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == DREADLING.get() || type == DREAD_SPAWN.get() || type == GREATER_DREAD_SPAWN.get()
            || type == LESSER_DREADBEAST.get()
            || type == com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities.DREADED_GHOUL.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.CHAGAROTH.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.DREADGUARD.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.CHAGAROTH_FIST.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.CHAGAROTH_SPAWN.get()
            || type == com.shinoow.abyssalcraft.content.entity.demon.DemonEntities.DEMON_CHICKEN.get()
            || type == com.shinoow.abyssalcraft.content.entity.demon.DemonEntities.DEMON_COW.get()
            || type == com.shinoow.abyssalcraft.content.entity.demon.DemonEntities.DEMON_PIG.get()
            || type == com.shinoow.abyssalcraft.content.entity.demon.DemonEntities.DEMON_SHEEP.get();
    }

    private static Supplier<EntityType<LegacyHostileMob>> hostile(LegacyMobKind kind) {
        Supplier<EntityType<LegacyHostileMob>> type = ENTITIES.register(kind.id, () -> {
            EntityType.Builder<LegacyHostileMob> builder = EntityType.Builder
                .<LegacyHostileMob>of((entityType, level) -> new LegacyHostileMob(entityType, level, kind), MobCategory.MONSTER)
                .sized(kind.width, kind.height);
            if (kind.fireImmune) builder.fireImmune();
            return builder.build(kind.id);
        });
        EntityAttributeCompat.register(type, () -> LegacyHostileMob.createAttributes(kind));
        SpawnPlacementCompat.registerGroundMonster(type);
        return type;
    }

    private static Supplier<Item> egg(String id, Supplier<? extends EntityType<? extends Mob>> type,
                                      int background, int highlight) {
        return ITEMS.register(id + "_spawn_egg", () -> SpawnEggCompat.create(type, background, highlight));
    }
}
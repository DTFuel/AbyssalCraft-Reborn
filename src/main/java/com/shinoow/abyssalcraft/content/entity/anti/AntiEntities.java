package com.shinoow.abyssalcraft.content.entity.anti;

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
 * Anti-matter entity family registry (owned by PD-3, Stage D2a).
 *
 * <p>Registers the eleven anti mobs (over vanilla bases per {@link AntiEntity} subclasses) plus a spawn
 * egg each, and publishes their attribute suppliers to the mod-bus creation event through
 * {@link EntityAttributeCompat} (already attached once by the main class from PD-1). Attached to the MOD
 * bus via {@code ModRegistries.ALL}.
 *
 * <p>Egg tint is faithful white/white (1.12.2 {@code registerEntityWithEgg(..., 0xFFFFFF, 0xFFFFFF)}).
 * Drops resolve through the per-type default loot table {@code abyssalcraft:entities/<id>} (dual-written
 * under {@code loot_table(s)/}).
 */
public final class AntiEntities {

    private AntiEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace for the anti family. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    /** {@code minecraft:item} registrar for the anti spawn eggs. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    // --- Entity types (faithful 1.12.2 ids + sizes; hostiles MONSTER, farm animals CREATURE, bat AMBIENT) ---
    public static final Supplier<EntityType<AntiZombie>> ANTI_ZOMBIE = ENTITIES.register("antizombie", () ->
        EntityType.Builder.<AntiZombie>of(AntiZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("antizombie"));
    public static final Supplier<EntityType<AntiAbyssalZombie>> ANTI_ABYSSAL_ZOMBIE = ENTITIES.register("antiabyssalzombie", () ->
        EntityType.Builder.<AntiAbyssalZombie>of(AntiAbyssalZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("antiabyssalzombie"));
    public static final Supplier<EntityType<AntiCreeper>> ANTI_CREEPER = ENTITIES.register("anticreeper", () ->
        EntityType.Builder.<AntiCreeper>of(AntiCreeper::new, MobCategory.MONSTER).sized(0.6F, 1.7F).build("anticreeper"));
    public static final Supplier<EntityType<AntiSkeleton>> ANTI_SKELETON = ENTITIES.register("antiskeleton", () ->
        EntityType.Builder.<AntiSkeleton>of(AntiSkeleton::new, MobCategory.MONSTER).sized(0.6F, 1.99F).build("antiskeleton"));
    public static final Supplier<EntityType<AntiSpider>> ANTI_SPIDER = ENTITIES.register("antispider", () ->
        EntityType.Builder.<AntiSpider>of(AntiSpider::new, MobCategory.MONSTER).sized(1.4F, 0.9F).build("antispider"));
    public static final Supplier<EntityType<AntiGhoul>> ANTI_GHOUL = ENTITIES.register("antighoul", () ->
        EntityType.Builder.<AntiGhoul>of(AntiGhoul::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("antighoul"));
    public static final Supplier<EntityType<AntiPlayer>> ANTI_PLAYER = ENTITIES.register("antiplayer", () ->
        EntityType.Builder.<AntiPlayer>of(AntiPlayer::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("antiplayer"));
    public static final Supplier<EntityType<AntiCow>> ANTI_COW = ENTITIES.register("anticow", () ->
        EntityType.Builder.<AntiCow>of(AntiCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).build("anticow"));
    public static final Supplier<EntityType<AntiPig>> ANTI_PIG = ENTITIES.register("antipig", () ->
        EntityType.Builder.<AntiPig>of(AntiPig::new, MobCategory.CREATURE).sized(0.9F, 0.9F).build("antipig"));
    public static final Supplier<EntityType<AntiChicken>> ANTI_CHICKEN = ENTITIES.register("antichicken", () ->
        EntityType.Builder.<AntiChicken>of(AntiChicken::new, MobCategory.CREATURE).sized(0.4F, 0.7F).build("antichicken"));
    public static final Supplier<EntityType<AntiBat>> ANTI_BAT = ENTITIES.register("antibat", () ->
        EntityType.Builder.<AntiBat>of(AntiBat::new, MobCategory.AMBIENT).sized(0.5F, 0.9F).build("antibat"));

    // --- Spawn eggs (faithful white/white tint; models deferred to asset stage PK) ---
    public static final Supplier<Item> ANTI_ZOMBIE_EGG = egg("antizombie_spawn_egg", ANTI_ZOMBIE);
    public static final Supplier<Item> ANTI_ABYSSAL_ZOMBIE_EGG = egg("antiabyssalzombie_spawn_egg", ANTI_ABYSSAL_ZOMBIE);
    public static final Supplier<Item> ANTI_CREEPER_EGG = egg("anticreeper_spawn_egg", ANTI_CREEPER);
    public static final Supplier<Item> ANTI_SKELETON_EGG = egg("antiskeleton_spawn_egg", ANTI_SKELETON);
    public static final Supplier<Item> ANTI_SPIDER_EGG = egg("antispider_spawn_egg", ANTI_SPIDER);
    public static final Supplier<Item> ANTI_GHOUL_EGG = egg("antighoul_spawn_egg", ANTI_GHOUL);
    public static final Supplier<Item> ANTI_PLAYER_EGG = egg("antiplayer_spawn_egg", ANTI_PLAYER);
    public static final Supplier<Item> ANTI_COW_EGG = egg("anticow_spawn_egg", ANTI_COW);
    public static final Supplier<Item> ANTI_PIG_EGG = egg("antipig_spawn_egg", ANTI_PIG);
    public static final Supplier<Item> ANTI_CHICKEN_EGG = egg("antichicken_spawn_egg", ANTI_CHICKEN);
    public static final Supplier<Item> ANTI_BAT_EGG = egg("antibat_spawn_egg", ANTI_BAT);

    static {
        // Publish base attributes to the mod-bus creation event (attach() already wired by the main class).
        EntityAttributeCompat.register(ANTI_ZOMBIE, AntiZombie::createAttributes);
        EntityAttributeCompat.register(ANTI_ABYSSAL_ZOMBIE, AntiAbyssalZombie::createAttributes);
        EntityAttributeCompat.register(ANTI_CREEPER, AntiCreeper::createAttributes);
        EntityAttributeCompat.register(ANTI_SKELETON, AntiSkeleton::createAttributes);
        EntityAttributeCompat.register(ANTI_SPIDER, AntiSpider::createAttributes);
        EntityAttributeCompat.register(ANTI_GHOUL, AntiGhoul::createAttributes);
        EntityAttributeCompat.register(ANTI_PLAYER, AntiPlayer::createAttributes);
        EntityAttributeCompat.register(ANTI_COW, AntiCow::createAttributes);
        EntityAttributeCompat.register(ANTI_PIG, AntiPig::createAttributes);
        EntityAttributeCompat.register(ANTI_CHICKEN, AntiChicken::createAttributes);
        EntityAttributeCompat.register(ANTI_BAT, AntiBat::createAttributes);
        SpawnPlacementCompat.registerGroundMonster(ANTI_ZOMBIE);
        SpawnPlacementCompat.registerGroundMonster(ANTI_ABYSSAL_ZOMBIE);
        SpawnPlacementCompat.registerGroundMonster(ANTI_CREEPER);
        SpawnPlacementCompat.registerGroundMonster(ANTI_SKELETON);
        SpawnPlacementCompat.registerGroundMonster(ANTI_SPIDER);
        SpawnPlacementCompat.registerGroundMonster(ANTI_GHOUL);
        SpawnPlacementCompat.registerGroundMonster(ANTI_PLAYER);
        SpawnPlacementCompat.registerGroundAnimal(ANTI_COW);
        SpawnPlacementCompat.registerGroundAnimal(ANTI_PIG);
        SpawnPlacementCompat.registerGroundAnimal(ANTI_CHICKEN);
        SpawnPlacementCompat.registerAmbientBat(ANTI_BAT);
    }

    private static Supplier<Item> egg(String name, Supplier<? extends EntityType<? extends net.minecraft.world.entity.Mob>> type) {
        return ITEMS.register(name, () -> SpawnEggCompat.create(type, 0xFFFFFF, 0xFFFFFF));
    }
}

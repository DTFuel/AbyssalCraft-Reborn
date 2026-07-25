package com.shinoow.abyssalcraft.content.entity.boss;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.SpawnEggCompat;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;

/**
 * Boss family registry (owned by PD-7, Stage D2b, Maps T3.6).
 *
 * <p>Registers the twelve boss {@link EntityType}s (four bar-bosses over {@link BossMob}/{@link BossKind}
 * + eight elites/minions over {@link EliteMob}/{@link EliteKind}, PD-4 collapse idiom) + their twelve
 * spawn eggs, and publishes each mob's attribute supplier via {@link EntityAttributeCompat}. Both
 * registrars attach to the MOD bus through {@code ModRegistries.ALL}; the attribute event is hooked once
 * from the main class (PD-1). Fire immunity is baked per {@code EntityType}.
 *
 * <p>Unlike the D2a families, bosses are <b>not</b> naturally spawned (they are ritual/structure summoned
 * in AbyssalCraft), so no {@code SpawnPlacementCompat} rule is registered. Boss loot depends on
 * not-yet-ported items and is deferred. Renderers are Stage E, so these are verified with {@code /summon}
 * on a dedicated server (the boss health bar shows for {@link BossMob}).
 */
public final class BossEntities {

    private BossEntities() {}

    /** {@code minecraft:entity_type} registrar for the boss family. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);
    /** {@code minecraft:item} registrar for the boss spawn eggs. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    // --- Four bar bosses (BossMob + ServerBossEvent health bar) ---
    public static final Supplier<EntityType<BossMob>> CHAGAROTH = boss(BossKind.CHAGAROTH);
    public static final Supplier<EntityType<BossMob>> JZAHAR = boss(BossKind.JZAHAR);
    public static final Supplier<EntityType<BossMob>> SACTHOTH = boss(BossKind.SACTHOTH);
    public static final Supplier<EntityType<BossMob>> DRAGON_BOSS = boss(BossKind.DRAGON_BOSS);

    // --- Eight elite bosses / boss minions (no health bar) ---
    public static final Supplier<EntityType<EliteMob>> DREADGUARD = elite(EliteKind.DREADGUARD);
    public static final Supplier<EntityType<EliteMob>> SKELETON_GOLIATH = elite(EliteKind.SKELETON_GOLIATH);
    public static final Supplier<EntityType<EliteMob>> REMNANT = elite(EliteKind.REMNANT);
    public static final Supplier<EntityType<EliteMob>> SHUB_OFFSPRING = elite(EliteKind.SHUB_OFFSPRING);
    public static final Supplier<EntityType<EliteMob>> GATEKEEPER_MINION = elite(EliteKind.GATEKEEPER_MINION);
    public static final Supplier<EntityType<EliteMob>> CHAGAROTH_FIST = elite(EliteKind.CHAGAROTH_FIST);
    public static final Supplier<EntityType<EliteMob>> CHAGAROTH_SPAWN = elite(EliteKind.CHAGAROTH_SPAWN);
    public static final Supplier<EntityType<EliteMob>> DRAGON_MINION = elite(EliteKind.DRAGON_MINION);

    // --- Twelve spawn eggs (faithful 1.12.2 colours) ---
    public static final Supplier<Item> CHAGAROTH_SPAWN_EGG =
        egg(BossKind.CHAGAROTH.id(), BossKind.CHAGAROTH.eggPrimary(), BossKind.CHAGAROTH.eggSecondary(), CHAGAROTH);
    public static final Supplier<Item> JZAHAR_SPAWN_EGG =
        egg(BossKind.JZAHAR.id(), BossKind.JZAHAR.eggPrimary(), BossKind.JZAHAR.eggSecondary(), JZAHAR);
    public static final Supplier<Item> SACTHOTH_SPAWN_EGG =
        egg(BossKind.SACTHOTH.id(), BossKind.SACTHOTH.eggPrimary(), BossKind.SACTHOTH.eggSecondary(), SACTHOTH);
    public static final Supplier<Item> DRAGON_BOSS_SPAWN_EGG =
        egg(BossKind.DRAGON_BOSS.id(), BossKind.DRAGON_BOSS.eggPrimary(), BossKind.DRAGON_BOSS.eggSecondary(), DRAGON_BOSS);
    public static final Supplier<Item> DREADGUARD_SPAWN_EGG =
        egg(EliteKind.DREADGUARD.id(), EliteKind.DREADGUARD.eggPrimary(), EliteKind.DREADGUARD.eggSecondary(), DREADGUARD);
    public static final Supplier<Item> SKELETON_GOLIATH_SPAWN_EGG =
        egg(EliteKind.SKELETON_GOLIATH.id(), EliteKind.SKELETON_GOLIATH.eggPrimary(), EliteKind.SKELETON_GOLIATH.eggSecondary(), SKELETON_GOLIATH);
    public static final Supplier<Item> REMNANT_SPAWN_EGG =
        egg(EliteKind.REMNANT.id(), EliteKind.REMNANT.eggPrimary(), EliteKind.REMNANT.eggSecondary(), REMNANT);
    public static final Supplier<Item> SHUB_OFFSPRING_SPAWN_EGG =
        egg(EliteKind.SHUB_OFFSPRING.id(), EliteKind.SHUB_OFFSPRING.eggPrimary(), EliteKind.SHUB_OFFSPRING.eggSecondary(), SHUB_OFFSPRING);
    public static final Supplier<Item> GATEKEEPER_MINION_SPAWN_EGG =
        egg(EliteKind.GATEKEEPER_MINION.id(), EliteKind.GATEKEEPER_MINION.eggPrimary(), EliteKind.GATEKEEPER_MINION.eggSecondary(), GATEKEEPER_MINION);
    public static final Supplier<Item> CHAGAROTH_FIST_SPAWN_EGG =
        egg(EliteKind.CHAGAROTH_FIST.id(), EliteKind.CHAGAROTH_FIST.eggPrimary(), EliteKind.CHAGAROTH_FIST.eggSecondary(), CHAGAROTH_FIST);
    public static final Supplier<Item> CHAGAROTH_SPAWN_SPAWN_EGG =
        egg(EliteKind.CHAGAROTH_SPAWN.id(), EliteKind.CHAGAROTH_SPAWN.eggPrimary(), EliteKind.CHAGAROTH_SPAWN.eggSecondary(), CHAGAROTH_SPAWN);
    public static final Supplier<Item> DRAGON_MINION_SPAWN_EGG =
        egg(EliteKind.DRAGON_MINION.id(), EliteKind.DRAGON_MINION.eggPrimary(), EliteKind.DRAGON_MINION.eggSecondary(), DRAGON_MINION);

    private static Supplier<EntityType<BossMob>> boss(BossKind kind) {
        Supplier<EntityType<BossMob>> type = ENTITIES.register(kind.id(), () -> {
            EntityType.Builder<BossMob> b =
                EntityType.Builder.<BossMob>of((t, l) -> kind == BossKind.CHAGAROTH
                    ? new ChagarothBoss(t, l) : kind == BossKind.JZAHAR
                        ? new JzaharBoss(t, l) : kind == BossKind.SACTHOTH
                            ? new SacthothBoss(t, l) : kind == BossKind.DRAGON_BOSS
                                ? new DragonBoss(t, l) : new BossMob(t, l, kind), MobCategory.MONSTER)
                    .sized(kind.width(), kind.height());
            if (kind.fireImmune()) {
                b.fireImmune();
            }
            return b.build(kind.id());
        });
        EntityAttributeCompat.register(type, () -> BossMob.createAttributes(kind));
        return type;
    }

    private static Supplier<EntityType<EliteMob>> elite(EliteKind kind) {
        Supplier<EntityType<EliteMob>> type = ENTITIES.register(kind.id(), () -> {
            EntityType.Builder<EliteMob> b =
                EntityType.Builder.<EliteMob>of((t, l) -> kind == EliteKind.DRAGON_MINION
                    ? new DragonMinion(t, l) : switch (kind) {
                        case DREADGUARD -> new DreadguardMob(t, l);
                        case SKELETON_GOLIATH -> new SkeletonGoliathMob(t, l);
                        case REMNANT -> new RemnantMob(t, l);
                        case GATEKEEPER_MINION -> new GatekeeperMinionMob(t, l);
                        case CHAGAROTH_SPAWN -> new ChagarothSpawnMob(t, l);
                        default -> new EliteMob(t, l, kind);
                    }, MobCategory.MONSTER)
                    .sized(kind.width(), kind.height());
            if (kind.fireImmune()) {
                b.fireImmune();
            }
            return b.build(kind.id());
        });
        EntityAttributeCompat.register(type, () -> EliteMob.createAttributes(kind));
        SpawnPlacementCompat.registerGroundMonster(type);
        return type;
    }

    private static Supplier<Item> egg(String id, int background, int highlight,
                                      Supplier<? extends EntityType<? extends Mob>> type) {
        return ITEMS.register(id + "_spawn_egg", () -> SpawnEggCompat.create(type, background, highlight));
    }
}

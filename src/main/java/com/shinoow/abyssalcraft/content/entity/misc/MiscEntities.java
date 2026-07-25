package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Miscellaneous non-living entity registrations (owned by PD-6, Stage D2a, Maps T3.5).
 *
 * <p>Registers the ten misc {@link EntityType}s under their faithful 1.12.2 ids. The ODB variants share
 * {@link PrimedODB} and the two portals share {@link DimensionPortal}, with the variant baked into each
 * factory. All are non-living, so no attribute suppliers, loot tables or spawn eggs are needed. Attached
 * to the MOD bus via {@code ModRegistries.ALL}; renderers are Stage E, so verification is {@code /summon}.
 */
public final class MiscEntities {

    private MiscEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);

    public static final Supplier<EntityType<BlackHole>> BLACK_HOLE = ENTITIES.register("blackhole", () ->
        EntityType.Builder.<BlackHole>of(BlackHole::new, MobCategory.MISC).sized(2.0F, 2.0F).build("blackhole"));

    public static final Supplier<EntityType<Implosion>> IMPLOSION = ENTITIES.register("implosion", () ->
        EntityType.Builder.<Implosion>of(Implosion::new, MobCategory.MISC).sized(2.0F, 2.0F).build("implosion"));

    public static final Supplier<EntityType<PrimedODB>> PRIMED_ODB = ENTITIES.register("primedodb", () ->
        EntityType.Builder.<PrimedODB>of((t, l) -> new PrimedODB(t, l, false), MobCategory.MISC).sized(0.98F, 0.98F).build("primedodb"));

    public static final Supplier<EntityType<PrimedODB>> PRIMED_ODB_CORE = ENTITIES.register("primedodbcore", () ->
        EntityType.Builder.<PrimedODB>of((t, l) -> new PrimedODB(t, l, true), MobCategory.MISC).sized(0.98F, 0.98F).build("primedodbcore"));

    public static final Supplier<EntityType<CompassTentacle>> COMPASS_TENTACLE = ENTITIES.register("compasstentacle", () ->
        EntityType.Builder.<CompassTentacle>of(CompassTentacle::new, MobCategory.MISC).sized(0.5F, 0.5F).build("compasstentacle"));

    public static final Supplier<EntityType<PSDLTracker>> POWERSTONE_TRACKER = ENTITIES.register("powerstonetracker", () ->
        EntityType.Builder.<PSDLTracker>of(PSDLTracker::new, MobCategory.MISC).sized(0.25F, 0.25F).build("powerstonetracker"));

    public static final Supplier<EntityType<DimensionPortal>> PORTAL = ENTITIES.register("portal", () ->
        EntityType.Builder.<DimensionPortal>of((t, l) -> new DimensionPortal(t, l, false), MobCategory.MISC).sized(1.0F, 2.0F).build("portal"));

    public static final Supplier<EntityType<DimensionPortal>> SINGLE_PORTAL = ENTITIES.register("singleportal", () ->
        EntityType.Builder.<DimensionPortal>of((t, l) -> new DimensionPortal(t, l, true), MobCategory.MISC).sized(1.0F, 2.0F).build("singleportal"));

    public static final Supplier<EntityType<SpiritItem>> SPIRIT_ITEM = ENTITIES.register("spirititem", () ->
        EntityType.Builder.<SpiritItem>of(SpiritItem::new, MobCategory.MISC).sized(0.25F, 0.25F).build("spirititem"));

    public static final Supplier<EntityType<GatekeeperEssence>> GATEKEEPER_ESSENCE = ENTITIES.register("gatekeeperessence", () ->
        EntityType.Builder.<GatekeeperEssence>of(GatekeeperEssence::new, MobCategory.MISC).sized(0.25F, 0.25F).build("gatekeeperessence"));
}

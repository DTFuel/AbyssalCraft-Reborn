package com.shinoow.abyssalcraft.content.entity.projectile;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Projectile entity registrations (owned by PD-6, Stage D2a, Maps T3.5).
 *
 * <p>Registers the five AbyssalCraft projectile {@link EntityType}s under their faithful 1.12.2 ids.
 * These are non-living, so they need no attribute suppliers, loot tables or spawn eggs; they are
 * spawned from code (mob attacks / dispensed items) rather than naturally. Attached to the MOD bus via
 * {@code ModRegistries.ALL}. Renderers are Stage E, so verification is {@code /summon} on a server.
 */
public final class ProjectileEntities {

    private ProjectileEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);

    public static final Supplier<EntityType<AcidProjectile>> ACID_PROJECTILE = ENTITIES.register("acidprojectile", () ->
        EntityType.Builder.<AcidProjectile>of(AcidProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).build("acidprojectile"));

    public static final Supplier<EntityType<DreadSlug>> DREAD_SLUG = ENTITIES.register("dreadslug", () ->
        EntityType.Builder.<DreadSlug>of(DreadSlug::new, MobCategory.MISC).sized(0.25F, 0.25F).build("dreadslug"));

    public static final Supplier<EntityType<InkProjectile>> INK_PROJECTILE = ENTITIES.register("inkprojectile", () ->
        EntityType.Builder.<InkProjectile>of(InkProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).build("inkprojectile"));

    public static final Supplier<EntityType<CoraliumArrow>> CORALIUM_ARROW = ENTITIES.register("coraliumarrow", () ->
        EntityType.Builder.<CoraliumArrow>of(CoraliumArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).build("coraliumarrow"));

    public static final Supplier<EntityType<DreadedCharge>> DREADED_CHARGE = ENTITIES.register("dreadedcharge", () ->
        EntityType.Builder.<DreadedCharge>of(DreadedCharge::new, MobCategory.MISC).sized(1.0F, 1.0F).build("dreadedcharge"));
}

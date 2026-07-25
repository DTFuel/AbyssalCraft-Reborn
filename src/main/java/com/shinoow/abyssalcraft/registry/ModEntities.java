package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Entity type registry + attribute wiring (owned by PD-1, Stage D1).
 *
 * <p>Aggregation point for {@code minecraft:entity_type} registrations and the mod-bus attribute
 * suppliers that back them. The concrete mob families (anti / demon / ghoul / shoggoth / bosses)
 * register their own {@link EntityType}s here in Stage D2a, each over an {@link ACMob} subclass.
 * Attached to the MOD bus via {@link ModRegistries#ALL}; the attribute event is hooked once from the
 * main class ({@link EntityAttributeCompat#attach}).
 *
 * <p>The {@code pilot_mob} below is a framework smoke test (mirroring the PC-1 block-entity bases):
 * the concrete {@link ACMob} base registered directly with a vanilla-monster attribute supplier, so
 * {@code /summon abyssalcraft:pilot_mob} spawns a living, ticking, non-crashing entity before any
 * real mob exists. Rendering is intentionally absent (Stage E owns entity renderers), so it is
 * verified on a dedicated server; a client would reject the missing renderer at startup.
 */
public final class ModEntities {

    private ModEntities() {}

    /** {@code minecraft:entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<EntityType<?>> ENTITIES =
        ModRegistrar.of(Registries.ENTITY_TYPE, AbyssalCraft.MODID);

    /** Example base mob proving the framework: {@code /summon}-able, has attributes, ticks with AI. */
    public static final Supplier<EntityType<ACMob>> PILOT_MOB = ENTITIES.register("pilot_mob", () ->
        EntityType.Builder.<ACMob>of(ACMob::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .build("pilot_mob"));

    static {
        // Publish the pilot mob's base attributes to the mod-bus creation event. Runs at class-init,
        // which ModRegistries.ALL forces before the main class calls EntityAttributeCompat.attach().
        EntityAttributeCompat.register(PILOT_MOB, ACMob::createAttributes);
    }
}

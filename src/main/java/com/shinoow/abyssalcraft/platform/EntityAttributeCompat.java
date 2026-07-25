package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
*///?}

/**
 * Compat: registering per-{@link EntityType} attribute suppliers (loader axis).
 *
 * <p>Both loaders fire a MOD-bus event during setup that collects the base {@link AttributeSupplier}
 * for every living entity type: Forge {@code net.minecraftforge.event.entity.EntityAttributeCreationEvent}
 * vs NeoForge {@code net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent}. Only the import
 * differs; both expose {@code put(EntityType, AttributeSupplier)}. Business code (registry/ModEntities)
 * stays fork-free by funnelling through here.
 *
 * <p>Usage: modules call {@link #register} (typically from their registrar's static init, guaranteed
 * loaded via {@code ModRegistries.ALL}); the main class calls {@link #attach} once to hook the event.
 * A living entity {@code /summon}ed without a registered attribute supplier crashes on spawn, so this
 * is mandatory framework wiring, not an optional extra.
 */
public final class EntityAttributeCompat {

    private EntityAttributeCompat() {}

    private record Entry(Supplier<? extends EntityType<? extends LivingEntity>> type,
                         Supplier<AttributeSupplier.Builder> attributes) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    /**
     * Record an attribute supplier to publish when the creation event fires. Both suppliers are
     * resolved lazily (inside the event), so this is safe to call before registries are frozen.
     */
    public static void register(Supplier<? extends EntityType<? extends LivingEntity>> type,
                                Supplier<AttributeSupplier.Builder> attributes) {
        ENTRIES.add(new Entry(type, attributes));
    }

    /** Attach the mod-bus listener that publishes every {@link #register}ed supplier. */
    public static void attach(IEventBus modBus) {
        modBus.addListener(EntityAttributeCompat::onCreate);
    }

    private static void onCreate(EntityAttributeCreationEvent event) {
        for (Entry e : ENTRIES) {
            event.put(e.type().get(), e.attributes().get().build());
        }
    }
}

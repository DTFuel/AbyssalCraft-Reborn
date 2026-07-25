package com.shinoow.abyssalcraft.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * Dimension key contract (relay file ※, owned by PG-0 / Stage G0).
 *
 * <p>Holds the {@link ResourceKey}{@code <Level>} handle for every AbyssalCraft dimension, so
 * teleporters, portal blocks, entity dimension checks and knowledge conditions can reference a
 * dimension type-safely instead of re-parsing a {@code ResourceLocation}. The datapack
 * {@code data/abyssalcraft/dimension/<id>.json} files own the actual world-generation wiring; these
 * keys only name the dimensions for the Java side.
 *
 * <p><b>Frozen worldgen ID contract (Gate G0, §5 of the parallel-task plan).</b> The four real
 * dimension ids are frozen here for the Stage G1 tasks (PG-1..3) to reference read-only; each G1
 * dimension task authors its own {@code dimension}/{@code dimension_type}/{@code noise_settings}/
 * {@code biome} JSON under these ids without depending on the others. {@link #MINI} is the Stage G0
 * vertical-slice example dimension (a minimal data-driven dimension proving the whole pipeline:
 * dimension_type + noise_settings + surface_rule + biome + fixed biome source + a custom feature).
 *
 * <p>Marked ※: this relay is pre-authorised to carry loader/version {@code //?} forks if a future
 * teleport helper needs forked API. Today the key construction ({@code Registries.DIMENSION} +
 * {@code ResourceKey.create}) is identical on both loaders/versions, so it stays fork-free.
 */
public final class ACDimensions {

    private ACDimensions() {}

    /** Stage G0 vertical-slice example dimension ({@code abyssalcraft:mini}). */
    public static final ResourceKey<Level> MINI = key("mini");

    /** The Abyssal Wasteland ({@code abyssalcraft:abyssal_wasteland}; 1.12.2 dim id 50). Filled by PG-1. */
    public static final ResourceKey<Level> ABYSSAL_WASTELAND = key("abyssal_wasteland");
    /** The Dreadlands ({@code abyssalcraft:dreadlands}; 1.12.2 dim id 51). Filled by PG-2. */
    public static final ResourceKey<Level> DREADLANDS = key("dreadlands");
    /** Omothol ({@code abyssalcraft:omothol}; 1.12.2 dim id 52). Filled by PG-3. */
    public static final ResourceKey<Level> OMOTHOL = key("omothol");
    /** The Dark Realm ({@code abyssalcraft:dark_realm}; 1.12.2 dim id 53). Filled by PG-3. */
    public static final ResourceKey<Level> DARK_REALM = key("dark_realm");

    private static ResourceKey<Level> key(String path) {
        return ResourceKey.create(Registries.DIMENSION, ACRef.id(path));
    }
}

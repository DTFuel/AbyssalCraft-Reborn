package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;
import java.util.List;

//? if forge {
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
//?} else {
/*import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
*///?}

/**
 * Compat: mod config spec building + registration (loader axis).
 *
 * <p>Forge uses {@code ForgeConfigSpec}; NeoForge {@code ModConfigSpec}. The two share an identical
 * builder surface and their value handles both implement {@link Supplier}. Business code (the
 * {@code config/} package) builds specs through this shim and stores options as plain
 * {@code Supplier<T>}, so no forked config API leaks outside {@code platform/}.
 */
public final class ConfigCompat {

    private ConfigCompat() {}

    /** Which config file a spec is registered under. */
    public enum Type { COMMON, CLIENT, SERVER }

    /** Opaque handle to a finished spec (wraps the loader-specific spec type). */
    public static final class Built {
        //? if forge {
        final ForgeConfigSpec spec;
        private Built(ForgeConfigSpec spec) { this.spec = spec; }
        //?} else {
        /*final ModConfigSpec spec;
        private Built(ModConfigSpec spec) { this.spec = spec; }
        *///?}

        public boolean matches(Object candidate) {
            return spec == candidate;
        }
    }

    /** Start building a new spec. */
    public static Builder builder() {
        return new Builder();
    }

    /** Register a finished spec with the active mod container. Call during mod construction. */
    // Forge's ModLoadingContext.get() is deprecated-for-removal on 47.x but fully functional on
    // 1.20.1 (same treatment as the main class); NeoForge's equivalent is not deprecated.
    @SuppressWarnings("removal")
    public static void register(Type type, Built built) {
        ModConfig.Type t = switch (type) {
            case CLIENT -> ModConfig.Type.CLIENT;
            case SERVER -> ModConfig.Type.SERVER;
            default -> ModConfig.Type.COMMON;
        };
        //? if forge {
        ModLoadingContext.get().registerConfig(t, built.spec);
        //?} else {
        /*ModLoadingContext.get().getActiveContainer().registerConfig(t, built.spec);
        *///?}
    }

    /** Thin wrapper over the loader's config-spec builder; exposes only non-forked types. */
    public static final class Builder {
        //? if forge {
        private final ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        //?} else {
        /*private final ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        *///?}

        /** Attach a comment to the next defined value (or the pushed category). */
        public Builder comment(String comment) {
            b.comment(comment);
            return this;
        }

        /** Enter a config category. */
        public Builder push(String category) {
            b.push(category);
            return this;
        }

        /** Leave the current config category. */
        public Builder pop() {
            b.pop();
            return this;
        }

        /** Define a boolean option; the returned Supplier reads the live value. */
        public Supplier<Boolean> defineBool(String path, boolean defaultValue) {
            return b.define(path, defaultValue);
        }

        /** Define an int option clamped to {@code [min, max]}. */
        public Supplier<Integer> defineInt(String path, int defaultValue, int min, int max) {
            return b.defineInRange(path, defaultValue, min, max);
        }

        /** Define a double option clamped to {@code [min, max]}. */
        public Supplier<Double> defineDouble(String path, double defaultValue, double min, double max) {
            return b.defineInRange(path, defaultValue, min, max);
        }

        @SuppressWarnings("unchecked")
        public Supplier<List<? extends String>> defineStringList(String path, List<String> defaults) {
            return (Supplier<List<? extends String>>) (Supplier<?>) b.defineListAllowEmpty(
                path, defaults, value -> value instanceof String);
        }

        @SuppressWarnings("unchecked")
        public Supplier<List<? extends Integer>> defineIntList(String path, List<Integer> defaults) {
            return (Supplier<List<? extends Integer>>) (Supplier<?>) b.defineListAllowEmpty(
                path, defaults, value -> value instanceof Integer);
        }

        /** Finish building the spec. */
        public Built build() {
            return new Built(b.build());
        }
    }
}

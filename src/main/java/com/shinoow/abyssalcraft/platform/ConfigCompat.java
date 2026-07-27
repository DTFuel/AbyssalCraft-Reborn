package com.shinoow.abyssalcraft.platform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.function.Consumer;
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

    public enum ValueType { BOOLEAN, INTEGER, DOUBLE, STRING_LIST, INTEGER_LIST }

    public static final class Entry<T> implements Supplier<T> {
        private Type configType = Type.COMMON;
        private final String category;
        private final String name;
        private final String comment;
        private final ValueType valueType;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final T defaultValue;
        private final double minimum;
        private final double maximum;

        private Entry(String category, String name, String comment, ValueType valueType,
            Supplier<T> getter, Consumer<T> setter, T defaultValue, double minimum, double maximum) {
            this.category = category;
            this.name = name;
            this.comment = comment;
            this.valueType = valueType;
            this.getter = getter;
            this.setter = setter;
            this.defaultValue = defaultValue;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override public T get() { return getter.get(); }
        public Type configType() { return configType; }
        public String category() { return category; }
        public String name() { return name; }
        public String path() { return category.isEmpty() ? name : category + "." + name; }
        public String comment() { return comment; }
        public ValueType valueType() { return valueType; }
        public T defaultValue() { return defaultValue; }

        public String formattedValue() {
            Object value = get();
            if (value instanceof List<?> list) return String.join(", ", list.stream().map(String::valueOf).toList());
            return String.valueOf(value);
        }

        public Object parse(String text) {
            try {
                return switch (valueType) {
                    case BOOLEAN -> parseBoolean(text);
                    case INTEGER -> checkedNumber(Integer.parseInt(text.trim()));
                    case DOUBLE -> checkedNumber(Double.parseDouble(text.trim()));
                    case STRING_LIST -> text.isBlank() ? List.of() : List.of(text.split("\\s*,\\s*"));
                    case INTEGER_LIST -> parseIntegerList(text);
                };
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Expected a number");
            }
        }

        @SuppressWarnings("unchecked")
        public void setParsed(Object value) {
            setter.accept((T) value);
        }

        private Number checkedNumber(Number value) {
            double number = value.doubleValue();
            if (number < minimum || number > maximum) {
                throw new IllegalArgumentException("Expected " + minimum + " to " + maximum);
            }
            return value;
        }

        private static Boolean parseBoolean(String text) {
            if ("true".equalsIgnoreCase(text.trim())) return true;
            if ("false".equalsIgnoreCase(text.trim())) return false;
            throw new IllegalArgumentException("Expected true or false");
        }

        private static List<Integer> parseIntegerList(String text) {
            if (text.isBlank()) return List.of();
            try {
                return java.util.Arrays.stream(text.split("\\s*,\\s*")).map(Integer::valueOf).toList();
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Expected comma-separated integers");
            }
        }
    }

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();
    private static final List<Built> BUILT_SPECS = new ArrayList<>();

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

        private final List<Entry<?>> entries = new ArrayList<>();

        public boolean matches(Object candidate) {
            return spec == candidate;
        }

        private void save() {
            spec.save();
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
        built.entries.forEach(entry -> entry.configType = type);
        ENTRIES.addAll(built.entries);
        BUILT_SPECS.add(built);
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

    public static List<Entry<?>> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static void saveAll() {
        BUILT_SPECS.forEach(Built::save);
    }

    /** Thin wrapper over the loader's config-spec builder; exposes only non-forked types. */
    public static final class Builder {
        //? if forge {
        private final ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        //?} else {
        /*private final ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        *///?}
        private final Deque<String> categories = new ArrayDeque<>();
        private final List<Entry<?>> entries = new ArrayList<>();
        private String pendingComment = "";

        /** Attach a comment to the next defined value (or the pushed category). */
        public Builder comment(String comment) {
            b.comment(comment);
            pendingComment = comment;
            return this;
        }

        /** Enter a config category. */
        public Builder push(String category) {
            b.push(category);
            categories.addLast(category);
            return this;
        }

        /** Leave the current config category. */
        public Builder pop() {
            b.pop();
            categories.removeLast();
            return this;
        }

        /** Define a boolean option; the returned Supplier reads the live value. */
        public Supplier<Boolean> defineBool(String path, boolean defaultValue) {
            var value = b.define(path, defaultValue);
            return add(path, ValueType.BOOLEAN, value, value::set, defaultValue, 0, 1);
        }

        /** Define an int option clamped to {@code [min, max]}. */
        public Supplier<Integer> defineInt(String path, int defaultValue, int min, int max) {
            var value = b.defineInRange(path, defaultValue, min, max);
            return add(path, ValueType.INTEGER, value, value::set, defaultValue, min, max);
        }

        /** Define a double option clamped to {@code [min, max]}. */
        public Supplier<Double> defineDouble(String path, double defaultValue, double min, double max) {
            var value = b.defineInRange(path, defaultValue, min, max);
            return add(path, ValueType.DOUBLE, value, value::set, defaultValue, min, max);
        }

        @SuppressWarnings("unchecked")
        public Supplier<List<? extends String>> defineStringList(String path, List<String> defaults) {
            var value = b.defineListAllowEmpty(path, defaults, element -> element instanceof String);
            return (Supplier<List<? extends String>>) (Supplier<?>) add(path, ValueType.STRING_LIST,
                value, value::set, List.copyOf(defaults), 0, 0);
        }

        @SuppressWarnings("unchecked")
        public Supplier<List<? extends Integer>> defineIntList(String path, List<Integer> defaults) {
            var value = b.defineListAllowEmpty(path, defaults, element -> element instanceof Integer);
            return (Supplier<List<? extends Integer>>) (Supplier<?>) add(path, ValueType.INTEGER_LIST,
                value, value::set, List.copyOf(defaults), 0, 0);
        }

        private <T> Entry<T> add(String name, ValueType valueType, Supplier<T> getter,
                Consumer<T> setter, T defaultValue, double minimum, double maximum) {
            Entry<T> entry = new Entry<>(String.join(".", categories), name, pendingComment,
                valueType, getter, setter, defaultValue, minimum, maximum);
            pendingComment = "";
            entries.add(entry);
            return entry;
        }

        /** Finish building the spec. */
        public Built build() {
            Built built = new Built(b.build());
            built.entries.addAll(entries);
            return built;
        }
    }
}

package com.shinoow.abyssalcraft.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shinoow.abyssalcraft.platform.ConfigCompat;

/** Loader-neutral draft, validation, save and reload contract used by the config screen. */
public final class ConfigEditorModel {

    private final List<ConfigCompat.Entry<?>> entries;
    private final Map<String, String> drafts = new LinkedHashMap<>();
    private final Map<String, Object> headlessValues;

    public ConfigEditorModel() {
        this(false);
    }

    private ConfigEditorModel(boolean headless) {
        entries = ConfigCompat.entries();
        headlessValues = headless ? new LinkedHashMap<>() : null;
        if (headless) entries.forEach(entry -> headlessValues.put(entry.path(), entry.defaultValue()));
        reload();
    }

    static ConfigEditorModel headless() {
        return new ConfigEditorModel(true);
    }

    public List<ConfigCompat.Entry<?>> entries() {
        return entries;
    }

    public String value(String path) {
        return drafts.get(path);
    }

    public void setValue(String path, String value) {
        if (!drafts.containsKey(path)) throw new IllegalArgumentException("Unknown config path: " + path);
        drafts.put(path, value);
    }

    public String validate(String path) {
        ConfigCompat.Entry<?> entry = entry(path);
        try {
            entry.parse(drafts.get(path));
            return "";
        } catch (IllegalArgumentException ex) {
            return ex.getMessage() == null ? "Invalid value" : ex.getMessage();
        }
    }

    public String save() {
        Map<ConfigCompat.Entry<?>, Object> parsed = new LinkedHashMap<>();
        for (ConfigCompat.Entry<?> entry : entries) {
            try {
                parsed.put(entry, entry.parse(drafts.get(entry.path())));
            } catch (IllegalArgumentException ex) {
                return entry.path() + ": " + (ex.getMessage() == null ? "Invalid value" : ex.getMessage());
            }
        }
        if (headlessValues != null) {
            parsed.forEach((entry, value) -> headlessValues.put(entry.path(), value));
        } else {
            parsed.forEach(ConfigCompat.Entry::setParsed);
            ConfigCompat.saveAll();
            ComplexConfig.reload();
        }
        reload();
        return "";
    }

    public void reload() {
        drafts.clear();
        entries.forEach(entry -> drafts.put(entry.path(), headlessValues == null
            ? entry.formattedValue() : format(headlessValues.get(entry.path()))));
    }

    private static String format(Object value) {
        if (value instanceof List<?> list) return String.join(", ", list.stream().map(String::valueOf).toList());
        return String.valueOf(value);
    }

    private ConfigCompat.Entry<?> entry(String path) {
        return entries.stream().filter(entry -> entry.path().equals(path)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown config path: " + path));
    }
}
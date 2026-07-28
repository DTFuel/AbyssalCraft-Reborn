package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.AbyssalCraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

/** Datagen entry point for permanent R6 RR-ASSET-LANG invariants. */
public final class LangValidationData implements DataProvider {

    private static final List<String> LANGUAGES = List.of(
        "en_us", "es_es", "fr_fr", "ja_jp", "ko_kr", "ru_ru", "zh_cn", "zh_tw");
    private static final Pattern FORMAT_PLACEHOLDER = Pattern.compile(
        "%(?:(?:\\d+)\\$)?[-#+ 0,(<]*\\d*(?:\\.\\d+)?[tT]?[a-zA-Z%]");

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<String, String> reference = readLanguage("en_us");
        RegistryCoverage coverage = validateRegisteredDescriptions(reference);
        for (String language : LANGUAGES) {
            Map<String, String> translations = readLanguage(language);
            require(translations.keySet().equals(reference.keySet()),
                language + " keyset differs from en_us; missing="
                    + difference(reference, translations) + ", extra=" + difference(translations, reference));

            for (Map.Entry<String, String> entry : translations.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                require(!value.isBlank(), language + " has an empty value for " + key);
                require(value.indexOf('\uFFFD') < 0, language + " has a replacement character in " + key);
                require(placeholders(value).equals(placeholders(reference.get(key))),
                    language + " has mismatched format placeholders for " + key);
            }
        }
        System.out.printf("RR_ASSET_LANG_AUDIT_OK languages=%d keys=%d blocks=%d items=%d%n",
            LANGUAGES.size(), reference.size(), coverage.blocks(), coverage.items());
        return CompletableFuture.completedFuture(null);
    }

    private static RegistryCoverage validateRegisteredDescriptions(Map<String, String> reference) {
        TreeSet<String> missing = new TreeSet<>();
        int blocks = 0;
        int items = 0;
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            if (!id.getNamespace().equals(AbyssalCraft.MODID)) continue;
            blocks++;
            String key = BuiltInRegistries.BLOCK.get(id).getDescriptionId();
            if (!reference.containsKey(key)) missing.add(key + " <- block " + id);
        }
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (!id.getNamespace().equals(AbyssalCraft.MODID)) continue;
            items++;
            String key = BuiltInRegistries.ITEM.get(id).getDescriptionId();
            if (!reference.containsKey(key)) missing.add(key + " <- item " + id);
        }
        require(missing.isEmpty(), "registered content is missing en_us descriptions: " + missing);
        return new RegistryCoverage(blocks, items);
    }

    private static Map<String, String> readLanguage(String language) {
        String resource = "assets/abyssalcraft/lang/" + language + ".json";
        try (InputStream stream = LangValidationData.class.getClassLoader().getResourceAsStream(resource)) {
            require(stream != null, "missing language resource " + resource);
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, String> translations = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                require(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString(),
                    language + " has a non-string value for " + entry.getKey());
                translations.put(entry.getKey(), entry.getValue().getAsString());
            }
            return translations;
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read " + resource, exception);
        }
    }

    private static List<String> difference(Map<String, String> left, Map<String, String> right) {
        return left.keySet().stream().filter(key -> !right.containsKey(key)).sorted().toList();
    }

    private static List<String> placeholders(String value) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = FORMAT_PLACEHOLDER.matcher(value);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR-ASSET-LANG: " + message);
    }

    private record RegistryCoverage(int blocks, int items) {}

    @Override
    public String getName() {
        return "AbyssalCraft RR-ASSET-LANG Validation";
    }
}
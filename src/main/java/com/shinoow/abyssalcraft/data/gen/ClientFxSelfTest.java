package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;

/**
 * Permanent classpath-resource invariants for RR-CLIENT-FX (sky / particle / sound).
 *
 * <p>Asserts the three dimension skybox textures + twelve {@code clientvars.json} tint channels, the two
 * custom particle descriptors ({@code abyssal_fx}, {@code blue_flame}) with their sprites, every registered
 * sound event's {@code sounds.json} entry + referenced {@code .ogg} + AbyssalCraft subtitle key, and that
 * each ritual manifest still exposes an eight-slot offering layout (the ItemRitual particle display source).
 * PEStream is delivered by RR-NET ({@code net/**} + {@code ClientNetworkEffects.peStream}) and validated
 * there, so it is intentionally out of this gate.
 */
public final class ClientFxSelfTest {

    /** The three tinted skybox textures (Dark Realm reuses Omothol's), matching {@code ACDimensionSkies}. */
    private static final List<String> SKY_TEXTURES = List.of(
        "abyssal_wasteland_sky", "dreadlands_sky", "omothol_sky");

    /** The twelve per-dimension skybox tint channels (four dimensions x RGB) in {@code clientvars.json}. */
    private static final List<String> CLIENTVARS_KEYS = List.of(
        "abyssalWastelandR", "abyssalWastelandG", "abyssalWastelandB",
        "dreadlandsR", "dreadlandsG", "dreadlandsB",
        "omotholR", "omotholG", "omotholB",
        "darkRealmR", "darkRealmG", "darkRealmB");

    /** The two custom particle types (each needs a descriptor + sprite texture). */
    private static final List<String> PARTICLES = List.of("abyssal_fx", "blue_flame");

    private ClientFxSelfTest() {}

    public static void run() {
        for (String texture : SKY_TEXTURES) {
            requireResource("assets/abyssalcraft/textures/environment/" + texture + ".png");
        }
        JsonObject clientvars = requireJson("assets/abyssalcraft/clientvars.json");
        for (String key : CLIENTVARS_KEYS) {
            require(clientvars.has(key), "clientvars.json is missing skybox tint channel " + key);
        }

        for (String particle : PARTICLES) {
            JsonObject descriptor = requireJson("assets/abyssalcraft/particles/" + particle + ".json");
            JsonArray textures = descriptor.getAsJsonArray("textures");
            require(textures != null && !textures.isEmpty(), "particle " + particle + " declares no textures");
            for (JsonElement element : textures) {
                String sprite = element.getAsString();
                require(sprite.startsWith("abyssalcraft:"), "particle sprite must be namespaced: " + sprite);
                requireResource("assets/abyssalcraft/textures/particle/"
                    + sprite.substring("abyssalcraft:".length()) + ".png");
            }
        }

        require(ModSounds.EVENTS.size() == 45, "expected 45 sound events, found " + ModSounds.EVENTS.size());
        require(ModSounds.EVENTS.containsKey("shoggoth.step"), "shoggoth.step sound event is missing");
        JsonObject sounds = requireJson("assets/abyssalcraft/sounds.json");
        JsonObject language = requireJson("assets/abyssalcraft/lang/en_us.json");
        Set<String> oggPaths = new LinkedHashSet<>();
        int subtitleKeys = 0;
        for (String id : ModSounds.EVENTS.keySet()) {
            require(sounds.has(id), "sounds.json is missing sound event " + id);
            JsonObject entry = sounds.getAsJsonObject(id);
            JsonArray soundList = entry.getAsJsonArray("sounds");
            require(soundList != null && !soundList.isEmpty(), "sound event " + id + " has no sound variants");
            for (JsonElement element : soundList) {
                String path = soundPath(element);
                if (path.startsWith("abyssalcraft:")) {
                    oggPaths.add(path.substring("abyssalcraft:".length()));
                }
            }
            if (entry.has("subtitle")) {
                String subtitle = entry.get("subtitle").getAsString();
                if (subtitle.startsWith("subtitle.abyssalcraft.")) {
                    require(language.has(subtitle), "en_us.json is missing subtitle key " + subtitle);
                    subtitleKeys++;
                }
            }
        }
        for (String ogg : oggPaths) {
            requireResource("assets/abyssalcraft/sounds/" + ogg + ".ogg");
        }

        for (RitualManifest manifest : RitualManifestCatalog.entries()) {
            require(manifest.offeringLayout().size() == RitualManifest.PEDESTAL_COUNT,
                "ritual " + manifest.id() + " no longer exposes an eight-slot ItemRitual display layout");
        }

        System.out.printf(
            "RR_CLIENT_FX_SELF_TEST_OK skies=%d particles=%d sounds=45 ogg=%d subtitles=%d rituals=%d%n",
            SKY_TEXTURES.size(), PARTICLES.size(), oggPaths.size(), subtitleKeys,
            RitualManifestCatalog.entries().size());
    }

    private static String soundPath(JsonElement element) {
        return element.isJsonObject() ? element.getAsJsonObject().get("name").getAsString() : element.getAsString();
    }

    private static JsonObject requireJson(String path) {
        return JsonParser.parseString(read(path)).getAsJsonObject();
    }

    private static void requireResource(String path) {
        try (InputStream stream = ClientFxSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close resource " + path, exception);
        }
    }

    private static String read(String path) {
        try (InputStream stream = ClientFxSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read resource " + path, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("RR client-fx self-test failed: " + message);
        }
    }
}

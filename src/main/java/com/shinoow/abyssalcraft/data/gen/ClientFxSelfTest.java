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
import com.shinoow.abyssalcraft.client.necronomicon.ACNecronomicon;
import com.shinoow.abyssalcraft.client.necronomicon.NecronomiconEntry;
import com.shinoow.abyssalcraft.client.ClientFxConfig;
import com.shinoow.abyssalcraft.platform.ConfigCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
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

    public static void run(net.minecraft.core.HolderLookup.Provider registries) {
        com.shinoow.abyssalcraft.system.client.ClientInputContract.validate();
        com.shinoow.abyssalcraft.client.ClientVarsConsumers.ConsumerStats consumerStats =
            com.shinoow.abyssalcraft.client.ClientVarsConsumers.validate();
        require(consumerStats.defined() == 94 && consumerStats.consumed() == 94
            && consumerStats.blocked().isEmpty(), "clientvars consumer closure is incomplete: " + consumerStats);
        com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.info(
            "RR_CLIENT_HUD_GATE_OK overlays=3 keybinds=5 tabletAuthority=server dimension=live");
        com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.info(
            "RR_CLIENTVARS_CONSUMER_GATE_OK defined={} consumed={} blocked={}",
            consumerStats.defined(), consumerStats.consumed(), consumerStats.blocked().size());
        validateClientFxConfigContract();
        validateClientVarsContract();
        requireResource("assets/abyssalcraft/textures/misc/coraliumblur.png");
        requireResource("assets/abyssalcraft/textures/misc/coraliumblur.png.mcmeta");
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

        Set<String> uiEntries = new LinkedHashSet<>();
        collectEntryIds(ACNecronomicon.root(4, registries), uiEntries);
        for (NecronomiconPageManifest.PageEntry page : NecronomiconPageManifest.pages()) {
            require(uiEntries.contains(page.id().getPath()), "Necronomicon UI is missing manifest page " + page.id());
        }

        System.out.printf(
            "RR_CLIENT_FX_SELF_TEST_OK skies=%d particles=%d sounds=45 ogg=%d subtitles=%d rituals=%d%n",
            SKY_TEXTURES.size(), PARTICLES.size(), oggPaths.size(), subtitleKeys,
            RitualManifestCatalog.entries().size());
    }

    private static void validateClientFxConfigContract() {
        require(config("client.dark_realm_smoke_particles").equals(Boolean.TRUE),
            "darkRealmSmokeParticles default must be true");
        require(config("client.depths_helmet_overlay_opacity").equals(1.0D),
            "depthsHelmetOverlayOpacity default must be 1.0");
        require(config("mod_compat.hcdarkness_aw").equals(Boolean.TRUE), "hcdarkness_aw default must be true");
        require(config("mod_compat.hcdarkness_dl").equals(Boolean.TRUE), "hcdarkness_dl default must be true");
        require(config("mod_compat.hcdarkness_omt").equals(Boolean.TRUE), "hcdarkness_omt default must be true");
        require(config("mod_compat.hcdarkness_dr").equals(Boolean.TRUE), "hcdarkness_dr default must be true");

        require(ClientFxConfig.clampOpacity(0.0D) == 0.5F, "overlay opacity lower clamp changed");
        require(ClientFxConfig.clampOpacity(0.75D) == 0.75F, "overlay opacity pass-through changed");
        require(ClientFxConfig.clampOpacity(2.0D) == 1.0F, "overlay opacity upper clamp changed");
        require(ClientFxConfig.Dimension.ABYSSAL_WASTELAND.legacyMinimumLight() == 0.25F,
            "Abyssal Wasteland legacy light profile changed");
        require(ClientFxConfig.Dimension.DREADLANDS.legacyMinimumLight() == 0.35F,
            "Dreadlands legacy light profile changed");
        require(ClientFxConfig.Dimension.OMOTHOL.legacyMinimumLight() == 0.25F,
            "Omothol legacy light profile changed");
        require(ClientFxConfig.Dimension.DARK_REALM.legacyMinimumLight() == 0.10F,
            "Dark Realm legacy light profile changed");
        require(!mapped(ClientFxConfig.Dimension.ABYSSAL_WASTELAND, false, true, true, true, true),
            "hardcore darkness must be disabled when the compatibility mod is absent");
        for (ClientFxConfig.Dimension dimension : ClientFxConfig.Dimension.values()) {
            require(mapped(dimension, true,
                dimension == ClientFxConfig.Dimension.ABYSSAL_WASTELAND,
                dimension == ClientFxConfig.Dimension.DREADLANDS,
                dimension == ClientFxConfig.Dimension.OMOTHOL,
                dimension == ClientFxConfig.Dimension.DARK_REALM),
                "hardcore darkness config is not mapped to " + dimension);
        }
        require(ClientFxConfig.defaults(() -> true, () -> 1.0D,
            () -> true, () -> true, () -> true, () -> true), "client FX defaults changed");
    }

    private static Object config(String path) {
        return ConfigCompat.entries().stream().filter(entry -> entry.path().equals(path)).findFirst()
            .orElseThrow(() -> new IllegalStateException("RR client-fx config path is missing: " + path))
            .defaultValue();
    }

    private static boolean mapped(ClientFxConfig.Dimension dimension, boolean loaded,
                                  boolean wasteland, boolean dreadlands, boolean omothol, boolean darkRealm) {
        return ClientFxConfig.hardcoreDarkness(dimension, () -> loaded, () -> wasteland, () -> dreadlands,
            () -> omothol, () -> darkRealm);
    }

    private static void collectEntryIds(NecronomiconEntry entry, Set<String> ids) {
        require(ids.add(entry.id()), "duplicate Necronomicon UI entry " + entry.id());
        entry.children().forEach(child -> collectEntryIds(child, ids));
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

    /**
     * Validates {@code clientvars.json} against the complete {@link com.shinoow.abyssalcraft.client.hud.ClientVars}
     * contract: ensures all 94 fields (plus version) are present, parseable, and valid.
     */
    private static void validateClientVarsContract() {
        JsonObject clientvars = requireJson("assets/abyssalcraft/clientvars.json");
        require(clientvars.has("version"), "clientvars.json is missing version field");
        require(clientvars.get("version").getAsInt() == com.shinoow.abyssalcraft.client.hud.ClientVars.VERSION,
            "clientvars.json version mismatch");

        com.shinoow.abyssalcraft.client.hud.ClientVars.ContractStats stats =
            com.shinoow.abyssalcraft.client.hud.ClientVars.contractStats();
        require(stats.fields() == com.shinoow.abyssalcraft.client.hud.ClientVars.FIELD_COUNT,
            "ClientVars contract changed: expected " + com.shinoow.abyssalcraft.client.hud.ClientVars.FIELD_COUNT
            + " fields but found " + stats.fields());

        // Parse to trigger full validation (color decoding, range checks, etc.)
        com.shinoow.abyssalcraft.client.hud.ClientVars parsed =
            com.shinoow.abyssalcraft.client.hud.ClientVars.parse(clientvars);
        require(parsed.crystalColors().length == com.shinoow.abyssalcraft.client.hud.ClientVars.CRYSTAL_COLOR_COUNT,
            "crystalColors must contain exactly " + com.shinoow.abyssalcraft.client.hud.ClientVars.CRYSTAL_COLOR_COUNT
            + " entries");

        // Smoke-test a few getters
        parsed.abyssalWastelandR();
        parsed.color("darklandsGrassColor");
        parsed.crystalColor(0);
        parsed.asorahDeathColor();
    }
}

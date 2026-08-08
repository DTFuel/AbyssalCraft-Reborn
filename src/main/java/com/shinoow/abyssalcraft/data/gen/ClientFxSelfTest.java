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
import com.shinoow.abyssalcraft.client.network.ClientNetworkEffects;
import com.shinoow.abyssalcraft.client.necronomicon.PatchouliNecronomicon;
import com.shinoow.abyssalcraft.client.ClientFxConfig;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientColorCompat;
import com.shinoow.abyssalcraft.platform.ConfigCompat;
import com.shinoow.abyssalcraft.registry.ModParticles;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconPageManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Permanent classpath-resource invariants for RR-CLIENT-FX (sky / particle / sound).
 *
 * <p>Asserts the three dimension skybox textures + twelve {@code clientvars.json} tint channels, the three
 * custom particle descriptors with their sprites, every registered
 * sound event's {@code sounds.json} entry + referenced {@code .ogg} + AbyssalCraft subtitle key, and that
 * each ritual manifest still exposes an eight-slot offering layout (the ItemRitual particle display source).
 * PEStream delivery remains owned by RR-NET; its dedicated particle resource and legacy line density are
 * validated here.
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

    /** The three custom particle types (each needs a descriptor and resolvable sprites). */
    private static final List<String> PARTICLES = List.of("abyssal_fx", "blue_flame", "pe_stream");
    private static final List<String> LINE_SHADER_ATTRIBUTES = List.of("Position", "UV0", "Color");

    private ClientFxSelfTest() {}

    public static void run(net.minecraft.core.HolderLookup.Provider registries) {
        com.shinoow.abyssalcraft.system.client.ClientInputContract.validate();
        com.shinoow.abyssalcraft.client.ClientVarsConsumers.ConsumerStats consumerStats =
            com.shinoow.abyssalcraft.client.ClientVarsConsumers.validate();
        require(consumerStats.defined() == 94 && consumerStats.consumed() == 94
            && consumerStats.blocked().isEmpty(), "clientvars consumer closure is incomplete: " + consumerStats);
        com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.info(
            "RR_CLIENT_HUD_GATE_OK overlays=2 keybinds=5 tabletAuthority=server");
        com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.info(
            "RR_CLIENTVARS_CONSUMER_GATE_OK defined={} consumed={} blocked={}",
            consumerStats.defined(), consumerStats.consumed(), consumerStats.blocked().size());
        validateClientFxConfigContract();
        validateClientVarsContract();
        //? if >=1.21 {
        /*require(ClientColorCompat.opaque(0x123456) == 0xFF123456,
            "1.21 item/block tint colors must carry an opaque alpha channel");
        *///?} else {
        require(ClientColorCompat.opaque(0x123456) == 0x123456,
            "1.20 tint colors must remain RGB values");
        //?}
        requireResource("assets/abyssalcraft/textures/misc/coraliumblur.png");
        requireResource("assets/abyssalcraft/textures/misc/coraliumblur.png.mcmeta");
        validateLineShader();
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
                require(sprite.contains(":"), "particle sprite must be namespaced: " + sprite);
                if (sprite.startsWith("abyssalcraft:")) {
                    requireResource("assets/abyssalcraft/textures/particle/"
                        + sprite.substring("abyssalcraft:".length()) + ".png");
                } else {
                    require(sprite.matches("minecraft:generic_[0-7]"),
                        "PE stream references an unexpected vanilla sprite " + sprite);
                }
            }
            if (particle.equals("pe_stream")) {
                require(textures.size() == 8, "PE stream must retain eight generic animation frames");
                for (int frame = 0; frame < 8; frame++) {
                    require(("minecraft:generic_" + (7 - frame)).equals(textures.get(frame).getAsString()),
                        "PE stream frame order changed at " + frame);
                }
            }
        }
        require(ACRef.id("pe_stream").equals(BuiltInRegistries.PARTICLE_TYPE.getKey(ModParticles.PE_STREAM.get())),
            "PE stream particle registry id changed");
        BlockPos streamEnd = new BlockPos(3, 0, 4);
        require(ClientNetworkEffects.peStreamSampleCount(BlockPos.ZERO, streamEnd, 1) == 75
            && ClientNetworkEffects.peStreamSampleCount(BlockPos.ZERO, streamEnd, 2) == 38
            && ClientNetworkEffects.peStreamSampleCount(BlockPos.ZERO, streamEnd, 3) == 25,
            "PE stream no longer follows the legacy 15-samples-per-block density");

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

        require(PatchouliNecronomicon.books().size() == 5,
            "Patchouli Necronomicon edition count changed");
        Set<String> patchouliEntries = new LinkedHashSet<>();
        int previousEntries = 0;
        for (int bookType = 0; bookType < 5; bookType++) {
            int entries = 1;
            for (NecronomiconPageManifest.PageEntry page : NecronomiconPageManifest.pages()) {
                if (NecronomiconPageManifest.isAvailableForBook(page, bookType)) {
                    require(patchouliEntries.add(bookType + ":" + page.id()),
                        "duplicate Patchouli entry in tier " + bookType + ": " + page.id());
                    entries++;
                }
            }
            require(entries >= previousEntries, "Patchouli Necronomicon tiers are not cumulative");
            previousEntries = entries;
        }
        require(previousEntries == NecronomiconPageManifest.pages().size() + 1,
            "Abyssalnomicon does not expose the complete Patchouli manifest");

        System.out.printf(
            "RR_CLIENT_FX_SELF_TEST_OK skies=%d particles=%d sounds=45 ogg=%d subtitles=%d rituals=%d%n",
            SKY_TEXTURES.size(), PARTICLES.size(), oggPaths.size(), subtitleKeys,
            RitualManifestCatalog.entries().size());
    }

    private static void validateLineShader() {
        String root = "assets/abyssalcraft/shaders/core/rendertype_line";
        JsonObject descriptor = requireJson(root + ".json");
        require("abyssalcraft:rendertype_line".equals(descriptor.get("vertex").getAsString())
            && "abyssalcraft:rendertype_line".equals(descriptor.get("fragment").getAsString()),
            "line shader programs are not namespaced correctly");
        JsonArray attributes = descriptor.getAsJsonArray("attributes");
        require(attributes.size() == LINE_SHADER_ATTRIBUTES.size(), "line shader vertex format changed");
        for (int index = 0; index < LINE_SHADER_ATTRIBUTES.size(); index++) {
            require(LINE_SHADER_ATTRIBUTES.get(index).equals(attributes.get(index).getAsString()),
                "line shader attribute order changed at " + index);
        }
        String vertex = read(root + ".vsh");
        String fragment = read(root + ".fsh");
        require(vertex.contains("ProjMat * ModelViewMat")
            && fragment.contains("vertexColor * ColorModulator")
            && !fragment.contains("lengthFade") && !fragment.contains("edgeFade"),
            "line shader lost its transform or solid color-gradient contract");
        System.out.println("RR_LINE_SHADER_SELF_TEST_OK attributes=3 solidGradient=1");
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

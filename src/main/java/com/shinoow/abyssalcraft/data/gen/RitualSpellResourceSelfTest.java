package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

/** Permanent classpath-resource invariants for the ritual and spell content. */
public final class RitualSpellResourceSelfTest {

    private static final List<String> ITEM_MODELS = List.of(
        "sealing_key", "interdimensional_cage", "book_of_many_faces", "staff_of_the_gatekeeper",
        "ring", "ring_overworld", "ring_abyssal_wasteland", "ring_dreadlands", "ring_omothol",
        "staff_of_rending", "abyssal_wasteland_staff_of_rending", "dreadlands_staff_of_rending",
        "omothol_staff_of_rending", "abyssal_wasteland_essence", "dreadlands_essence",
        "omothol_essence", "basic_scroll", "lesser_scroll", "moderate_scroll", "greater_scroll",
        "antimatter_scroll", "oblivion_scroll", "coralium_infused_stone",
        "dreadlands_infused_powerstone", "odb_core", "basic_scroll_inscribed",
        "lesser_scroll_inscribed", "moderate_scroll_inscribed", "greater_scroll_inscribed");
    private static final List<String> BLOCKS = List.of(
        "coralium_infused_stone", "dreadlands_infused_powerstone", "odb_core");
    private static final List<String> DIRECT_ITEM_TEXTURES = List.of(
        "sealing_key", "interdimensional_cage", "book_of_many_faces", "staff_of_the_gatekeeper",
        "staff_of_rending", "abyssal_wasteland_staff_of_rending", "dreadlands_staff_of_rending",
        "omothol_staff_of_rending", "abyssal_wasteland_essence", "dreadlands_essence",
        "omothol_essence", "basic_scroll", "lesser_scroll", "moderate_scroll", "greater_scroll",
        "antimatter_scroll", "oblivion_scroll");
    private static final List<String> RITUAL_MESSAGES = List.of(
        "busy", "locked", "invalid", "no_sacrifice", "started", "failed",
        "no_structure", "no_ritual", "no_energy", "success");

    private RitualSpellResourceSelfTest() {}

    public static void run() {
        Set<String> visitedModels = new HashSet<>();
        for (String id : ITEM_MODELS) {
            validateModel("assets/abyssalcraft/models/item/" + id + ".json", visitedModels);
        }
        validateModel("assets/abyssalcraft/models/item/ring_model.json", visitedModels);
        for (String id : BLOCKS) {
            requireJson("assets/abyssalcraft/blockstates/" + id + ".json");
            validateModel("assets/abyssalcraft/models/block/" + id + ".json", visitedModels);
            requireResource("assets/abyssalcraft/textures/block/" + id + ".png");
        }
        requireResource("assets/abyssalcraft/textures/block/odb_core_end.png");
        requireResource("assets/abyssalcraft/textures/block/ring_metal.png");
        requireResource("assets/abyssalcraft/textures/item/spell_overlay.png");
        for (String id : DIRECT_ITEM_TEXTURES) {
            requireResource("assets/abyssalcraft/textures/item/" + id + ".png");
        }

        JsonObject language = requireJson("assets/abyssalcraft/lang/en_us.json");
        for (String id : ITEM_MODELS.subList(0, 25)) {
            String prefix = BLOCKS.contains(id) ? "block." : "item.";
            require(language.has(prefix + "abyssalcraft." + id), "missing language key for " + id);
        }
        for (var spell : SpellManifestCatalog.entries()) {
            require(language.has("ac.spell." + spell.id()), "missing spell language key " + spell.id());
        }
        for (String message : RITUAL_MESSAGES) {
            require(language.has("message.abyssalcraft.ritual." + message),
                "missing ritual message " + message);
        }

        JsonObject damageType = requireJson("data/abyssalcraft/damage_type/spell.json");
        require("spell".equals(damageType.get("message_id").getAsString()),
            "spell damage type message id changed");
        for (String tag : List.of("bypasses_armor", "bypasses_resistance",
                "bypasses_enchantments", "bypasses_effects")) {
            require(anyResourceContains("data/minecraft/tags/damage_type/" + tag + ".json",
                "values", "abyssalcraft:spell"), "spell damage is missing from " + tag);
        }

        System.out.println("RR_RITUAL_SPELL_RESOURCES_OK itemModels=29 blockSets=3 spells=14 damageTags=4");
    }

    private static void validateModel(String path, Set<String> visited) {
        if (!visited.add(path)) return;
        String source = read(path);
        require(!source.contains("abyssalcraft:items/") && !source.contains("abyssalcraft:blocks/"),
            "legacy resource path in " + path);
        require(!source.contains("\"#-1\""), "undefined texture reference in " + path);
        JsonObject model = JsonParser.parseString(source).getAsJsonObject();
        JsonElement parent = model.get("parent");
        if (parent != null) validateModelReference(parent.getAsString(), visited);
        JsonObject textures = model.getAsJsonObject("textures");
        if (textures == null) return;
        for (JsonElement texture : textures.asMap().values()) {
            validateTextureReference(texture.getAsString());
        }
    }

    private static void validateModelReference(String reference, Set<String> visited) {
        if (!reference.startsWith("abyssalcraft:")) return;
        validateModel("assets/abyssalcraft/models/" + reference.substring("abyssalcraft:".length()) + ".json",
            visited);
    }

    private static void validateTextureReference(String reference) {
        if (reference.startsWith("#") || !reference.startsWith("abyssalcraft:")) return;
        requireResource("assets/abyssalcraft/textures/"
            + reference.substring("abyssalcraft:".length()) + ".png");
    }

    private static JsonObject requireJson(String path) {
        return JsonParser.parseString(read(path)).getAsJsonObject();
    }

    private static boolean anyResourceContains(String path, String array, String expected) {
        try {
            Enumeration<URL> resources = RitualSpellResourceSelfTest.class.getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                try (InputStream stream = resources.nextElement().openStream()) {
                    JsonObject json = JsonParser.parseString(
                        new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    if (json.has(array) && json.getAsJsonArray(array).asList().stream()
                            .anyMatch(value -> expected.equals(value.getAsString()))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to enumerate resources " + path, exception);
        }
    }

    private static void requireResource(String path) {
        try (InputStream stream = RitualSpellResourceSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close resource " + path, exception);
        }
    }

    private static String read(String path) {
        try (InputStream stream = RitualSpellResourceSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read resource " + path, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR ritual/spell resource self-test failed: " + message);
    }
}
package com.shinoow.abyssalcraft.system.advancement;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** Permanent datagen invariants for RR-ADV-API advancement resources and knowledge storage. */
public final class AdvApiSelfTest {

    private AdvApiSelfTest() {}

    public static void run() {
        require(AdvancementKnowledge.ENTRIES.size() == 9, "advancement catalog is not 9");
        Set<ResourceLocation> ids = new HashSet<>();
        for (AdvancementKnowledge.Entry entry : AdvancementKnowledge.ENTRIES) {
            require(ids.add(entry.id()), "duplicate advancement id " + entry.id());
            require(BuiltInRegistries.ITEM.containsKey(entry.icon()), "missing icon item " + entry.icon());
            entry.criterionItems().forEach(item ->
                require(BuiltInRegistries.ITEM.containsKey(item), "missing criterion item " + item));
            validateResource(entry, "advancements", false);
            validateResource(entry, "advancement", true);
        }

        CompoundTag tag = new CompoundTag();
        NecroData data = new NecroData(tag);
        String root = ACRef.id("root").toString();
        require(data.triggerAdvancementUnlock(root), "first advancement mutation was ignored");
        require(!data.triggerAdvancementUnlock(root), "duplicate advancement mutation changed store");
        require(data.getAdvancementTriggers().equals(List.of(root)), "advancement store round-trip changed");

        require(com.shinoow.abyssalcraft.platform.IMCCompat.retainedKeyCount() == 5,
            "retained IMC key count changed");
        require(com.shinoow.abyssalcraft.platform.IMCCompat.retiredKeyCount() == 13,
            "retired IMC key count changed");
        com.shinoow.abyssalcraft.integration.api.ACPluginSelfTest.run();
        com.shinoow.abyssalcraft.system.command.CommandSelfTest.run();

        System.out.println("RR_ADV_API_SELF_TEST_OK advancements=9 schemas=2 retainedImc=5 retiredImc=13 pluginLifecycle=ok commandToggle=ok");
    }

    private static void validateResource(AdvancementKnowledge.Entry entry, String directory, boolean modern) {
        String path = "data/abyssalcraft/" + directory + "/" + entry.id().getPath() + ".json";
        JsonObject json = readJson(path);
        JsonObject display = json.getAsJsonObject("display");
        JsonObject icon = display.getAsJsonObject("icon");
        String iconKey = modern ? "id" : "item";
        require(icon.has(iconKey) && entry.icon().toString().equals(icon.get(iconKey).getAsString()),
            "wrong icon in " + path);
        require(!icon.has(modern ? "item" : "id"), "cross-version icon key in " + path);
        if (entry.parent() == null) {
            require("abyssalcraft:textures/block/darkstone.png".equals(display.get("background").getAsString()),
                "wrong root background in " + path);
            requireResource("assets/abyssalcraft/textures/block/darkstone.png");
        }

        JsonElement parent = json.get("parent");
        if (entry.parent() == null) {
            require(parent == null, "unexpected parent in " + path);
        } else {
            require(parent != null && entry.parent().toString().equals(parent.getAsString()),
                "wrong parent in " + path);
        }

        Set<ResourceLocation> items = new LinkedHashSet<>();
        JsonObject criteria = json.getAsJsonObject("criteria");
        for (JsonElement criterionElement : criteria.asMap().values()) {
            JsonArray predicates = criterionElement.getAsJsonObject()
                .getAsJsonObject("conditions").getAsJsonArray("items");
            for (JsonElement predicate : predicates) {
                require(predicate.isJsonObject(), "item predicate is not an object in " + path);
                JsonArray predicateItems = predicate.getAsJsonObject().getAsJsonArray("items");
                require(predicateItems != null && predicateItems.size() == 1,
                    "item predicate must contain one item in " + path);
                items.add(ACRef.parse(predicateItems.get(0).getAsString()));
            }
        }
        require(items.equals(new LinkedHashSet<>(entry.criterionItems())), "criterion items changed in " + path);

        if (criteria.size() > 1) {
            JsonArray requirements = json.getAsJsonArray("requirements");
            require(requirements != null && requirements.size() == 1
                && requirements.get(0).getAsJsonArray().size() == criteria.size(),
                "multi-criterion advancement is not OR-linked in " + path);
        }
    }

    private static JsonObject readJson(String path) {
        try (InputStream stream = AdvApiSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8))
                .getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private static void requireResource(String path) {
        try (InputStream stream = AdvApiSelfTest.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "missing resource " + path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close " + path, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("RR-ADV-API self-test failed: " + message);
        }
    }
}
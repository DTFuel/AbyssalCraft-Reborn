package com.shinoow.abyssalcraft.data.gen;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.AbyssalCraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

/** Permanent complete R6 RR-ASSET registry-to-resource closure audit. */
public final class AssetCatalogSelfTest {

    private static final String ROOT = "assets/" + AbyssalCraft.MODID + "/";

    private AssetCatalogSelfTest() {}

    public static void run() {
        Set<String> models = new HashSet<>();
        Set<String> textures = new HashSet<>();
        int blocks = auditBlocks(models, textures);
        int items = auditItems(models, textures);
        int particles = auditParticles(textures);
        int sounds = auditSounds();
        auditFont(textures);
        System.out.printf("RR_ASSET_AUDIT_OK missing=0 blocks=%d items=%d models=%d textures=%d particles=%d sounds=%d fonts=1%n",
            blocks, items, models.size(), textures.size(), particles, sounds);
    }

    private static int auditBlocks(Set<String> models, Set<String> textures) {
        int count = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) continue;
            count++;
            collectModels(requireJson(ROOT + "blockstates/" + id.getPath() + ".json"), models, textures);
        }
        require(count > 0, "no registered blocks");
        return count;
    }

    private static int auditItems(Set<String> models, Set<String> textures) {
        int count = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) continue;
            count++;
            String model = "item/" + id.getPath();
            if (resource(ROOT + "models/" + model + ".json") != null) {
                validateModel(AbyssalCraft.MODID + ":" + model, models, textures);
            } else if (item instanceof BlockItem) {
                require(BuiltInRegistries.BLOCK.containsKey(id), "block item without block " + id);
            } else {
                require(item instanceof SpawnEggItem, "item without model " + id);
            }
        }
        require(count > 0, "no registered items");
        return count;
    }

    private static int auditParticles(Set<String> textures) {
        int count = 0;
        for (var particle : BuiltInRegistries.PARTICLE_TYPE) {
            ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(particle);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) continue;
            count++;
            JsonObject definition = requireJson(ROOT + "particles/" + id.getPath() + ".json");
            for (JsonElement texture : definition.getAsJsonArray("textures")) {
                String[] textureId = split(texture.getAsString(), "minecraft");
                if (AbyssalCraft.MODID.equals(textureId[0])) {
                    String reference = "particle/" + textureId[1];
                    validateTexture(AbyssalCraft.MODID + ":" + reference, textures);
                }
            }
        }
        return count;
    }

    private static int auditSounds() {
        JsonObject definitions = requireJson(ROOT + "sounds.json");
        int count = 0;
        for (var sound : BuiltInRegistries.SOUND_EVENT) {
            ResourceLocation id = BuiltInRegistries.SOUND_EVENT.getKey(sound);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) continue;
            count++;
            require(definitions.has(id.getPath()), "sound without definition " + id);
            collectSoundFiles(definitions.get(id.getPath()));
        }
        return count;
    }

    private static void collectSoundFiles(JsonElement element) {
        if (element.isJsonPrimitive()) {
            validateSound(element.getAsString());
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) collectSoundFiles(child);
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("name".equals(entry.getKey())) validateSound(entry.getValue().getAsString());
                else if (!"subtitle".equals(entry.getKey())) collectSoundFiles(entry.getValue());
            }
        }
    }

    private static void validateSound(String reference) {
        String[] id = split(reference, "minecraft");
        if (AbyssalCraft.MODID.equals(id[0])) requireResource(ROOT + "sounds/" + id[1] + ".ogg");
    }

    private static void auditFont(Set<String> textures) {
        JsonObject font = requireJson(ROOT + "font/aklo.json");
        for (JsonElement provider : font.getAsJsonArray("providers")) {
            JsonObject object = provider.getAsJsonObject();
            if (object.has("file")) validateTexture(object.get("file").getAsString(), textures);
        }
    }

    private static void collectModels(JsonElement element, Set<String> models, Set<String> textures) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("model".equals(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
                    validateModel(entry.getValue().getAsString(), models, textures);
                } else collectModels(entry.getValue(), models, textures);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) collectModels(child, models, textures);
        }
    }

    private static void validateModel(String reference, Set<String> models, Set<String> textures) {
        String[] id = split(reference, AbyssalCraft.MODID);
        if (!AbyssalCraft.MODID.equals(id[0]) || !models.add(reference)) return;
        JsonObject model = requireJson(ROOT + "models/" + id[1] + ".json");
        if (model.has("parent")) {
            String parent = model.get("parent").getAsString();
            validateModel(parent.indexOf(':') < 0 ? "minecraft:" + parent : parent, models, textures);
        }
        if (model.has("textures")) {
            for (JsonElement texture : model.getAsJsonObject("textures").asMap().values()) {
                if (!texture.getAsString().startsWith("#")) validateTexture(texture.getAsString(), textures);
            }
        }
    }

    private static void validateTexture(String reference, Set<String> textures) {
        String[] id = split(reference, "minecraft");
        if (!AbyssalCraft.MODID.equals(id[0]) || !textures.add(reference)) return;
        String path = ROOT + "textures/" + id[1] + (id[1].endsWith(".png") ? "" : ".png");
        try (InputStream stream = requireResource(path)) {
            BufferedImage image = ImageIO.read(stream);
            require(image != null && image.getWidth() > 0 && image.getHeight() > 0, "undecodable texture " + path);
        } catch (IOException exception) {
            throw new IllegalStateException("RR-ASSET unable to decode " + path, exception);
        }
    }

    private static JsonObject requireJson(String path) {
        try (InputStream stream = requireResource(path)) {
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("RR-ASSET invalid JSON " + path, exception);
        }
    }

    private static InputStream requireResource(String path) {
        InputStream stream = resource(path);
        require(stream != null, "missing resource " + path);
        return stream;
    }

    private static InputStream resource(String path) {
        return AssetCatalogSelfTest.class.getClassLoader().getResourceAsStream(path);
    }

    private static String[] split(String reference, String defaultNamespace) {
        int separator = reference.indexOf(':');
        return separator < 0 ? new String[] { defaultNamespace, reference }
            : new String[] { reference.substring(0, separator), reference.substring(separator + 1) };
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR-ASSET audit failed: " + message);
    }
}

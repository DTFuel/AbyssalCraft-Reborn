package com.shinoow.abyssalcraft.data.gen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shinoow.abyssalcraft.AbyssalCraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Permanent RR-ASSET-BLOCK closure audit for registered block client resources. */
public final class AssetBlockSelfTest {

    private static final String ASSET_ROOT = "assets/" + AbyssalCraft.MODID + "/";
    private static final Map<String, Integer> FACING_ROTATIONS = Map.of(
        "north", 0, "east", 90, "south", 180, "west", 270);

    private AssetBlockSelfTest() {}

    public static void run() {
        Set<String> visitedModels = new HashSet<>();
        int blocks = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) {
                continue;
            }
            blocks++;
            JsonObject state = requireJson(ASSET_ROOT + "blockstates/" + id.getPath() + ".json");
            collectModels(state, visitedModels);
        }
        require(blocks > 0, "no registered AbyssalCraft blocks were audited");
        validateMachineStates();
        System.out.printf("RR_ASSET_BLOCK_AUDIT_OK blocks=%d models=%d machines=3%n", blocks, visitedModels.size());
    }

    private static void collectModels(JsonElement element, Set<String> visitedModels) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("model".equals(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
                    validateModel(entry.getValue().getAsString(), visitedModels);
                } else {
                    collectModels(entry.getValue(), visitedModels);
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectModels(child, visitedModels);
            }
        }
    }

    private static void validateModel(String reference, Set<String> visitedModels) {
        String[] id = split(reference, AbyssalCraft.MODID);
        if (!AbyssalCraft.MODID.equals(id[0])) {
            return;
        }
        if (!visitedModels.add(id[0] + ":" + id[1])) {
            return;
        }
        JsonObject model = requireJson("assets/" + id[0] + "/models/" + id[1] + ".json");
        if (model.has("parent")) {
            validateModel(model.get("parent").getAsString(), visitedModels);
        }
        if (!model.has("textures")) {
            return;
        }
        for (Map.Entry<String, JsonElement> texture : model.getAsJsonObject("textures").entrySet()) {
            String referenceValue = texture.getValue().getAsString();
            if (referenceValue.startsWith("#")) {
                continue;
            }
            String[] textureId = split(referenceValue, "minecraft");
            if (AbyssalCraft.MODID.equals(textureId[0])) {
                requireResource("assets/" + textureId[0] + "/textures/" + textureId[1] + ".png");
            }
        }
    }

    private static void validateMachineStates() {
        validateLitMachine("crystallizer");
        validateLitMachine("transmutator");

        JsonObject variants = requireJson(ASSET_ROOT + "blockstates/materializer.json").getAsJsonObject("variants");
        require(variants.size() == 4, "materializer must expose exactly four facing states");
        for (Map.Entry<String, Integer> facing : FACING_ROTATIONS.entrySet()) {
            String key = "facing=" + facing.getKey();
            require(variants.has(key), "materializer missing " + key);
            validateRotation("materializer", variants.getAsJsonObject(key), facing.getValue());
        }
        require(variants.keySet().stream().noneMatch(key -> key.contains("lit=")),
            "materializer must not invent a lit property");
    }

    private static void validateLitMachine(String machine) {
        JsonObject variants = requireJson(ASSET_ROOT + "blockstates/" + machine + ".json")
            .getAsJsonObject("variants");
        require(variants.size() == 8, machine + " must expose four facing x two lit states");
        for (Map.Entry<String, Integer> facing : FACING_ROTATIONS.entrySet()) {
            String idleKey = "facing=" + facing.getKey() + ",lit=false";
            String activeKey = "facing=" + facing.getKey() + ",lit=true";
            require(variants.has(idleKey) && variants.has(activeKey),
                machine + " missing " + facing.getKey() + " states");
            String idleModel = variants.getAsJsonObject(idleKey).get("model").getAsString();
            String activeModel = variants.getAsJsonObject(activeKey).get("model").getAsString();
            require(!idleModel.equals(activeModel), machine + " idle and active states share a model");
            validateRotation(machine, variants.getAsJsonObject(idleKey), facing.getValue());
            validateRotation(machine, variants.getAsJsonObject(activeKey), facing.getValue());
        }
        require(!digest(ASSET_ROOT + "textures/block/" + machine + "_front.png").equals(
            digest(ASSET_ROOT + "textures/block/" + machine + "_front_active.png")),
            machine + " idle and active front textures are identical");
    }

    private static void validateRotation(String machine, JsonObject variant, int expected) {
        int actual = variant.has("y") ? variant.get("y").getAsInt() : 0;
        require(actual == expected, machine + " facing rotation mismatch: expected " + expected + ", found " + actual);
        require(expected == 0 || variant.has("uvlock") && variant.get("uvlock").getAsBoolean(),
            machine + " rotated facing must use uvlock");
    }

    private static JsonObject requireJson(String path) {
        try (InputStream stream = resource(path)) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (!json.isEmpty() && json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
            }
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("RR-ASSET-BLOCK invalid JSON " + path, exception);
        }
    }

    private static String digest(String path) {
        try (InputStream stream = resource(path)) {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RR-ASSET-BLOCK unable to hash " + path, exception);
        }
    }

    private static void requireResource(String path) {
        try (InputStream stream = resource(path)) {
            require(stream.read() >= 0, "empty resource " + path);
        } catch (IOException exception) {
            throw new IllegalStateException("RR-ASSET-BLOCK unable to close " + path, exception);
        }
    }

    private static InputStream resource(String path) {
        InputStream stream = AssetBlockSelfTest.class.getClassLoader().getResourceAsStream(path);
        require(stream != null, "missing resource " + path);
        return stream;
    }

    private static String[] split(String reference, String defaultNamespace) {
        int separator = reference.indexOf(':');
        return separator < 0
            ? new String[] { defaultNamespace, reference }
            : new String[] { reference.substring(0, separator), reference.substring(separator + 1) };
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("RR-ASSET-BLOCK audit failed: " + message);
        }
    }
}
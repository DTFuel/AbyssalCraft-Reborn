package com.shinoow.abyssalcraft.data;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.AbyssalCraft;

/**
 * Example datagen provider (shell, owned by PA-4).
 *
 * <p>Proves the datagen pipeline runs by writing one sample file into the mod's data pack. Uses only
 * vanilla {@link DataProvider} API (no loader-forked types), so it is shared across both nodes. Real
 * providers join / replace it in later stages.
 */
public final class ACExampleProvider implements DataProvider {

    private final PackOutput output;

    public ACExampleProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject json = new JsonObject();
        json.addProperty("_comment", "AbyssalCraft datagen smoke output; replaced by real providers in later stages.");
        json.addProperty("modid", AbyssalCraft.MODID);
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
            .resolve(AbyssalCraft.MODID)
            .resolve("datagen_smoke.json");
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Datagen Smoke";
    }
}

package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Read-only datagen entry point for the permanent RR-ASSET-BLOCK audit. */
public final class AssetBlockValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        AssetBlockSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ASSET-BLOCK Validation";
    }
}
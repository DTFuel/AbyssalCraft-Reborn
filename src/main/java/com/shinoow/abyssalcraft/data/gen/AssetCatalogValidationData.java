package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Read-only datagen entry point for the complete R6 RR-ASSET catalog. */
public final class AssetCatalogValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        AssetCatalogSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ASSET Catalog Validation";
    }
}

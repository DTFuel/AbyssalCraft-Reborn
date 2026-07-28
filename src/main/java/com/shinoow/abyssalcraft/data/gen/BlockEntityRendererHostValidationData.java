package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for the headless BlockEntityRenderer host closure. */
public final class BlockEntityRendererHostValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        BlockEntityRendererHostAudit.validate();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft BER Host Closure Validation";
    }
}
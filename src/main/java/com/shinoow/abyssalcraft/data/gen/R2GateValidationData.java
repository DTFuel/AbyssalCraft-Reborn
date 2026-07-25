package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for the implementation-side R2 Gate invariants. */
public final class R2GateValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        R2GateSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft R2 Gate Validation";
    }
}
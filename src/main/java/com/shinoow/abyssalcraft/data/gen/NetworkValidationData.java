package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.net.NetworkSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-NET invariants. */
public final class NetworkValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        NetworkSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-NET Validation";
    }
}
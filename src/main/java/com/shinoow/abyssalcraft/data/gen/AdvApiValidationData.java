package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.advancement.AdvApiSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-ADV-API invariants. */
public final class AdvApiValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        AdvApiSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ADV-API Validation";
    }
}
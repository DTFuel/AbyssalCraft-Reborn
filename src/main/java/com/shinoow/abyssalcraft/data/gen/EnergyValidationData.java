package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.energy.EnergySelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-ENERGY invariants. */
public final class EnergyValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        EnergySelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ENERGY Validation";
    }
}
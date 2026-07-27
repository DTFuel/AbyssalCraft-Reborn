package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.energy.EnergySelfTest;
import com.shinoow.abyssalcraft.content.block.energy.EnergyGuiSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import net.minecraft.core.HolderLookup;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-ENERGY invariants. */
public final class EnergyValidationData implements DataProvider {

    private final java.util.concurrent.CompletableFuture<HolderLookup.Provider> lookup;

    public EnergyValidationData(DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        EnergySelfTest.run();
        EnergyGuiSelfTest.run(lookup.join());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ENERGY Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestalSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

public final class RendingPedestalValidationData implements DataProvider {

    private final CompletableFuture<HolderLookup.Provider> lookup;

    public RendingPedestalValidationData(DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        RendingPedestalSelfTest.run(lookup.join());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Rending Pedestal Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import com.shinoow.abyssalcraft.content.machine.MenuHostSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

public final class MenuHostValidationData implements DataProvider {

    private final CompletableFuture<HolderLookup.Provider> lookup;

    public MenuHostValidationData(DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        MenuHostSelfTest.run(lookup.join());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-MENU-HOST Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-CLIENT-FX invariants (sky / particle / sound resources). */
public final class ClientFxValidationData implements DataProvider {

    private final java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookup;

    public ClientFxValidationData(com.shinoow.abyssalcraft.platform.DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookup.thenAccept(ClientFxSelfTest::run);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-CLIENT-FX Validation";
    }
}

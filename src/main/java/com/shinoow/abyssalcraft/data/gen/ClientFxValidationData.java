package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-CLIENT-FX invariants (sky / particle / sound resources). */
public final class ClientFxValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        ClientFxSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-CLIENT-FX Validation";
    }
}

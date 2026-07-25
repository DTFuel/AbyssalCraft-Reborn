package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import com.shinoow.abyssalcraft.system.portal.PortalSelfTest;

/** Datagen entry point for permanent RR-PORTAL invariants. */
public final class PortalValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        PortalSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-PORTAL Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSystemSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-KNOWLEDGE invariants. */
public final class KnowledgeValidationData implements DataProvider {

    private final java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookup;

    public KnowledgeValidationData(com.shinoow.abyssalcraft.platform.DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookup.thenAccept(KnowledgeSystemSelfTest::run);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-KNOWLEDGE Validation";
    }
}
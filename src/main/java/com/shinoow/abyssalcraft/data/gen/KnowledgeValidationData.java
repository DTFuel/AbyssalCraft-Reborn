package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSystemSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-KNOWLEDGE invariants. */
public final class KnowledgeValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        KnowledgeSystemSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-KNOWLEDGE Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import com.shinoow.abyssalcraft.content.entity.behavior.EntityBehaviorSelfTest;

/** Datagen entry point for permanent RR-ENTITY-BEHAVIOR invariants. */
public final class EntityBehaviorValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        EntityBehaviorSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-ENTITY-BEHAVIOR Validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import com.shinoow.abyssalcraft.validation.world.WorldgenFinalMatrix;
import com.shinoow.abyssalcraft.validation.world.StructureContentSelfTest;
import com.shinoow.abyssalcraft.world.WorldgenInvariant;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen entry point for worldgen validation (T5.9b).
 * <p>
 * Executes the complete RR-WORLD-FIDELITY-AUTO matrix during data generation phase.
 * Server-context validations (performance, spawn) will log BLOCKED status.
 * </p>
 */
public final class WorldgenValidationData implements DataProvider {

    public WorldgenValidationData(PackOutput output) {
        // No file output - validation results go to logs
    }

    @Override
    public CompletableFuture<?> run(net.minecraft.data.CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            // Existing invariant from RR-WORLD
            WorldgenInvariant.validate();
            StructureContentSelfTest.run();

            // New T5.9b final matrix
            String result = WorldgenFinalMatrix.executeMatrix();
            if (!result.startsWith("RR_WORLD_FINAL_MATRIX_PASS ")) {
                throw new IllegalStateException(result);
            }
        });
    }

    @Override
    public String getName() {
        return "Worldgen Validation (RR-WORLD-FIDELITY-AUTO)";
    }
}
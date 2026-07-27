package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.config.ConfigContractSelfTest;
import com.shinoow.abyssalcraft.config.ContentConfigMatrixSelfTest;
import com.shinoow.abyssalcraft.config.WorldConfigConsumerAudit;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent T8.2c config contracts. */
public final class ConfigValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        ConfigContractSelfTest.run();
        ContentConfigMatrixSelfTest.run();
        WorldConfigConsumerAudit.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Config Contract Validation";
    }
}
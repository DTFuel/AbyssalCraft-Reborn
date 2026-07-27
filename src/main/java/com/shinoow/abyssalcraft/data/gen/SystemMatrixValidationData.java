package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.enchant.EnchantmentMatrixSelfTest;
import com.shinoow.abyssalcraft.system.ritual.ResurrectionMatrixSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent T7.9b/c and T7.11c system matrices. */
public final class SystemMatrixValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        EnchantmentMatrixSelfTest.run();
        ResurrectionMatrixSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-SYSTEM-AUTO Matrix Validation";
    }
}
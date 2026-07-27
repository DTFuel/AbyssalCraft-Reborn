package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformerSelfTest;
import com.shinoow.abyssalcraft.content.menu.facebook.BookOfManyFacesSelfTest;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

public final class StateTransformerValidationData implements DataProvider {

    private final CompletableFuture<HolderLookup.Provider> lookup;

    public StateTransformerValidationData(DataGenCompat.Gen gen) {
        lookup = gen.lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        StateTransformerSelfTest.run(lookup.join());
        BookOfManyFacesSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft State Transformer Validation";
    }
}
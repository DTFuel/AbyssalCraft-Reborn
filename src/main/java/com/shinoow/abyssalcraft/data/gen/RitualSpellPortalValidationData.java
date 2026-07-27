package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.system.ritual.RitualManifestSelfTest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestSelfTest;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Datagen entry point for permanent RR-RITUAL-SPELL-PORTAL manifest invariants. */
public final class RitualSpellPortalValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        RitualManifestSelfTest.run();
        SpellManifestSelfTest.run();
        RitualSpellResourceSelfTest.run();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft RR-RITUAL-SPELL-PORTAL Validation";
    }
}
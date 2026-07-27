package com.shinoow.abyssalcraft.data.gen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/** Headless JEI catalog gate; level-backed recipe registries are checked when JEI receives a level. */
public final class JEIAutomationValidationData implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JEIHeadlessAudit.Result result = JEIHeadlessAudit.run();
        if (!result.passed()) {
            throw new IllegalStateException("JEI auto gate failed: " + String.join("; ", result.errors()));
        }
        System.out.printf("RR_JEI_TRANSFER_GATE_OK decisions=%d supported=%d%n",
            result.counts().get("transfer_decisions"), result.counts().get("supported_transfers"));
        System.out.printf("RR_JEI_AUTO_GATE_OK categories=%d rituals=%d spells=%d crystallizerFuels=%d transmutatorFuels=%d rending=%d dataManifest=%d levelCheck=RecipeManager-hook%n",
            result.counts().get("categories"), result.counts().get("rituals"), result.counts().get("spells"),
            result.counts().get("crystallizer_fuels"), result.counts().get("transmutator_fuels"),
            result.counts().get("rending_recipes"), result.counts().get("data_recipe_types"));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "AbyssalCraft JEI automation validation";
    }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shinoow.abyssalcraft.integration.jei.JEIAutomationCatalog;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

/** Catalog-only JEI audit with no dependency on JEI or Minecraft client classes. */
public final class JEIHeadlessAudit {

    private JEIHeadlessAudit() {}

    public static Result run() {
        List<String> errors = new ArrayList<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("categories", JEIAutomationCatalog.CATEGORIES.size());
        counts.put("rituals", RitualManifestCatalog.entries().size());
        counts.put("spells", SpellManifestCatalog.entries().size());
        counts.put("crystallizer_fuels", JEIAutomationCatalog.CRYSTALLIZER_FUELS.size());
        counts.put("transmutator_fuels", JEIAutomationCatalog.TRANSMUTATOR_FUELS.size());
        counts.put("rending_recipes", JEIAutomationCatalog.RENDING_RECIPES.size());
        counts.put("data_recipe_types", JEIAutomationCatalog.DATA_RECIPE_TYPES.size());
        counts.put("transfer_decisions", JEIAutomationCatalog.transfers().size());
        counts.put("supported_transfers", (int) JEIAutomationCatalog.transfers().values().stream()
            .filter(decision -> decision.status() == JEIAutomationCatalog.Status.SUPPORTED).count());

        require(counts.get("categories") == 12, "expected 12 categories", errors);
        require(counts.get("rituals") == 62, "expected 62 rituals", errors);
        require(counts.get("spells") == 14, "expected 14 spells", errors);
        require(counts.get("rending_recipes") == 4, "expected 4 rending manifests", errors);
        require(counts.get("data_recipe_types") == 5, "expected 5 level-aware data recipe types", errors);
        require(JEIAutomationCatalog.transfers().keySet().equals(JEIAutomationCatalog.CATEGORIES),
            "transfer decisions do not cover the category manifest", errors);
        JEIAutomationCatalog.transfers().forEach((category, decision) -> {
            if (decision.status() == JEIAutomationCatalog.Status.SUPPORTED) {
                require(decision.recipeSlotStart() >= 0 && decision.recipeSlotCount() > 0,
                    "invalid input slots for " + category, errors);
                require(decision.playerSlotStart() >= 0 && decision.playerSlotCount() == 36,
                    "invalid player slots for " + category, errors);
            } else {
                require(decision.reason() != null && !decision.reason().isBlank(),
                    "UNSUPPORTED category lacks a reason: " + category, errors);
            }
        });
        return new Result(errors.isEmpty(), Map.copyOf(counts), List.copyOf(errors));
    }

    private static void require(boolean condition, String error, List<String> errors) {
        if (!condition) errors.add(error);
    }

    public record Result(boolean passed, Map<String, Integer> counts, List<String> errors) {}
}
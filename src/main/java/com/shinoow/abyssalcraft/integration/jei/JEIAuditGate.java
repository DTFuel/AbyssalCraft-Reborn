package com.shinoow.abyssalcraft.integration.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

/** Headless-safe catalog and registry audit for the complete JEI integration. */
public final class JEIAuditGate {

    private static final Set<String> CATEGORY_UIDS = Set.of(
        "abyssalcraft:crystallization", "abyssalcraft:materialization", "abyssalcraft:transmutation",
        "abyssalcraft:anvil_forging", "abyssalcraft:crystallizer_fuel", "abyssalcraft:transmutator_fuel",
        "abyssalcraft:rending", "abyssalcraft:infusion_ritual", "abyssalcraft:ritual", "abyssalcraft:creation_ritual",
        "abyssalcraft:transformation_ritual", "abyssalcraft:spell");

    private JEIAuditGate() {}

    public static AuditResult runFullAudit() {
        return runFullAudit(null);
    }

    /** The optional level enables checks for the five data-driven recipe registries. */
    public static AuditResult runFullAudit(Level level) {
        List<String> errors = new ArrayList<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("categories", CATEGORY_UIDS.size());
        require(CATEGORY_UIDS.size() == 12, "expected 12 unique category UIDs", errors);
        auditTransfers(counts, errors);
        errors.addAll(RitualJeiLayout.audit());
        counts.put("ritual_layout_slots", 10);

        List<RitualManifest> rituals = RitualManifestCatalog.entries();
        List<RitualManifest> infusions = InfusionRitualCategory.getInfusionRituals();
        List<RitualManifest> specialRituals = RitualCategory.getRituals();
        List<RitualManifest> creations = CreationRitualCategory.getCreationRituals();
        List<RitualManifest> transformations = TransformationRitualCategory.getTransformationRituals();
        List<SpellManifest> spells = SpellManifestCatalog.entries();

        counts.put("rituals", rituals.size());
        counts.put("infusion_rituals", infusions.size());
        counts.put("special_rituals", specialRituals.size());
        counts.put("creation_rituals", creations.size());
        counts.put("transformation_rituals", transformations.size());
        counts.put("spells", spells.size());
        require(rituals.size() == 62, "expected 62 ritual manifests", errors);
        require(infusions.size() == 40, "expected 40 infusion rituals", errors);
        require(specialRituals.size() == 18, "expected 18 non-item-output rituals", errors);
        require(creations.size() == 3, "expected 3 creation rituals", errors);
        require(transformations.size() == 1, "expected 1 transformation ritual", errors);
        require(spells.size() == 14, "expected 14 spell manifests", errors);
        require(SpellCategory.getValuableSpells().equals(spells), "JEI omits spell manifests", errors);

        auditUniqueIds(rituals, RitualManifest::id, "ritual", errors);
        auditUniqueIds(spells, SpellManifest::id, "spell", errors);
        auditOutputs(infusions, "infusion ritual", errors);
        auditOutputs(creations, "creation ritual", errors);
        auditOutputs(transformations, "transformation ritual", errors);
        Set<RitualManifest> exposedRituals = new LinkedHashSet<>();
        exposedRituals.addAll(infusions);
        exposedRituals.addAll(specialRituals);
        exposedRituals.addAll(creations);
        exposedRituals.addAll(transformations);
        require(exposedRituals.size() == rituals.size(), "JEI ritual categories overlap or omit manifests", errors);
        require(exposedRituals.containsAll(rituals), "JEI omits ritual manifests", errors);
        require(infusions.stream().allMatch(rituals::contains), "JEI contains unknown infusion ritual", errors);
        require(creations.stream().allMatch(rituals::contains), "JEI contains unknown creation ritual", errors);
        require(transformations.stream().allMatch(rituals::contains), "JEI contains unknown transformation ritual", errors);

        List<FuelRecipe> crystallizerFuels = CrystallizerFuelCategory.getAllFuels();
        List<FuelRecipe> transmutatorFuels = TransmutatorFuelCategory.getAllFuels();
        counts.put("crystallizer_fuels", crystallizerFuels.size());
        counts.put("transmutator_fuels", transmutatorFuels.size());
        auditFuels(crystallizerFuels, "crystallizer", errors);
        auditFuels(transmutatorFuels, "transmutator", errors);

        if (level != null) auditDataRecipes(level, counts, errors);
        return new AuditResult(errors.isEmpty(), Map.copyOf(counts), List.copyOf(errors));
    }

    private static void auditTransfers(Map<String, Integer> counts, List<String> errors) {
        Map<String, JEIAutomationCatalog.TransferDecision> transfers = JEIAutomationCatalog.transfers();
        counts.put("transfer_decisions", transfers.size());
        counts.put("supported_transfers", (int) transfers.values().stream()
            .filter(decision -> decision.status() == JEIAutomationCatalog.Status.SUPPORTED).count());
        require(transfers.size() == CATEGORY_UIDS.size(), "transfer audit does not cover all JEI categories", errors);
        for (String uid : CATEGORY_UIDS) {
            String category = uid.substring(uid.indexOf(':') + 1);
            JEIAutomationCatalog.TransferDecision decision = transfers.get(category);
            require(decision != null, "missing transfer decision for " + category, errors);
            if (decision == null) continue;
            if (decision.status() == JEIAutomationCatalog.Status.SUPPORTED) {
                require(decision.recipeSlotStart() >= 0 && decision.recipeSlotCount() > 0,
                    "invalid recipe slot range for " + category, errors);
                require(decision.playerSlotStart() >= 0 && decision.playerSlotCount() == 36,
                    "invalid player inventory range for " + category, errors);
            } else {
                require(decision.reason() != null && !decision.reason().isBlank(),
                    "UNSUPPORTED transfer has no reason for " + category, errors);
            }
        }
    }

    private static void auditDataRecipes(Level level, Map<String, Integer> counts, List<String> errors) {
        auditEntries("crystallization", DataRecipeCompat.entriesOfType(level, ModRecipes.CRYSTALLIZATION.get()), counts, errors);
        auditEntries("materialization", DataRecipeCompat.entriesOfType(level, ModRecipes.MATERIALIZATION.get()), counts, errors);
        auditEntries("transmutation", DataRecipeCompat.entriesOfType(level, ModRecipes.TRANSMUTATION.get()), counts, errors);
        auditEntries("anvil_forging", DataRecipeCompat.entriesOfType(level, ModRecipes.ANVIL_FORGING.get()), counts, errors);
        List<DataRecipeCompat.Entry<com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe>> rending =
            DataRecipeCompat.entriesOfType(level, ModRecipes.RENDING.get());
        auditEntries("rending", rending, counts, errors);
        require(rending.size() == 4, "expected 4 rending recipes", errors);
    }

    private static void auditEntries(String name, List<? extends DataRecipeCompat.Entry<?>> entries,
                                     Map<String, Integer> counts, List<String> errors) {
        counts.put(name + "_recipes", entries.size());
        require(!entries.isEmpty(), name + " recipes are empty", errors);
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (DataRecipeCompat.Entry<?> entry : entries) {
            require(ids.add(entry.id()), "duplicate " + name + " recipe id " + entry.id(), errors);
            require(!entry.value().result().isEmpty(), "empty output for " + name + " recipe " + entry.id(), errors);
        }
    }

    private static <T> void auditUniqueIds(List<T> entries, Function<T, String> id,
                                           String label, List<String> errors) {
        Set<String> ids = new LinkedHashSet<>();
        for (T entry : entries) {
            String value = id.apply(entry);
            require(ids.add(value), "duplicate " + label + " id " + value, errors);
        }
    }

    private static void auditOutputs(List<RitualManifest> rituals, String label, List<String> errors) {
        for (RitualManifest ritual : rituals) {
            require(ritual.result() != null, "empty output for " + label + " " + ritual.id(), errors);
        }
    }

    private static void auditFuels(List<FuelRecipe> fuels, String label, List<String> errors) {
        require(!fuels.isEmpty(), label + " fuel catalog is empty", errors);
        for (FuelRecipe fuel : fuels) {
            require(!fuel.fuel().isEmpty(), "empty " + label + " fuel", errors);
            require(fuel.burnTime() > 0, "non-positive " + label + " fuel time", errors);
        }
    }

    private static void require(boolean condition, String message, List<String> errors) {
        if (!condition) errors.add(message);
    }

    public static CategoryStatusClosure checkLegacyCategories() {
        Map<String, CategoryStatus> statuses = new LinkedHashMap<>();
        for (String uid : CATEGORY_UIDS) statuses.put(uid.substring(uid.indexOf(':') + 1), CategoryStatus.ACTIVE);
        return new CategoryStatusClosure(Map.copyOf(statuses));
    }

    public record AuditResult(boolean passed, Map<String, Integer> counts, List<String> errors) {}
    public record CategoryStatusClosure(Map<String, CategoryStatus> statuses) {}
    public enum CategoryStatus { ACTIVE }
}

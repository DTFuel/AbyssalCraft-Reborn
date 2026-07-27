package com.shinoow.abyssalcraft.integration.jei;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Headless-safe closure for every JEI category's recipe-transfer capability. */
public final class JEIAutomationCatalog {

    public static final Set<String> CATEGORIES = Set.of(
        "crystallization", "materialization", "transmutation", "anvil_forging",
        "crystallizer_fuel", "transmutator_fuel", "rending", "infusion_ritual",
        "ritual", "creation_ritual", "transformation_ritual", "spell");
    public static final List<String> CRYSTALLIZER_FUELS = List.of(
        "abyssalcraft:dread_fragment", "abyssalcraft:dreaded_shard_of_abyssalnite",
        "minecraft:blaze_powder", "minecraft:blaze_rod", "abyssalcraft:methane");
    public static final List<String> TRANSMUTATOR_FUELS = List.of(
        "abyssalcraft:coralium_plagued_flesh", "abyssalcraft:abyssal_ghoul_flesh",
        "abyssalcraft:coralium_brick", "abyssalcraft:coralium_gem",
        "abyssalcraft:coralium_gem_cluster_2", "abyssalcraft:coralium_gem_cluster_3",
        "abyssalcraft:coralium_gem_cluster_4", "abyssalcraft:coralium_gem_cluster_5",
        "abyssalcraft:coralium_gem_cluster_6", "abyssalcraft:coralium_gem_cluster_7",
        "abyssalcraft:coralium_gem_cluster_8", "abyssalcraft:coralium_gem_cluster_9",
        "abyssalcraft:coralium_pearl", "abyssalcraft:transmutation_gem", "abyssalcraft:chunk_of_coralium");
    public static final Set<String> RENDING_RECIPES = Set.of(
        "rending_abyssal", "rending_dread", "rending_omothol", "rending_shadow");
    public static final Set<String> DATA_RECIPE_TYPES = Set.of(
        "crystallization", "materialization", "transmutation", "anvil_forging", "rending");

    private static final Map<String, TransferDecision> TRANSFERS = createTransfers();

    private JEIAutomationCatalog() {}

    public static Map<String, TransferDecision> transfers() {
        return TRANSFERS;
    }

    private static Map<String, TransferDecision> createTransfers() {
        Map<String, TransferDecision> transfers = new LinkedHashMap<>();
        supported(transfers, "crystallization", 0, 1, 4, 36);
        unsupported(transfers, "materialization", "recipe inputs are stored in the crystal bag, not menu slots");
        supported(transfers, "transmutation", 0, 1, 3, 36);
        unsupported(transfers, "anvil_forging", "no AbyssalCraft menu executes anvil forging recipes");
        unsupported(transfers, "crystallizer_fuel", "fuel display is not a processing recipe");
        unsupported(transfers, "transmutator_fuel", "fuel display is not a processing recipe");
        unsupported(transfers, "rending", "recipe input is an entity; pedestal slots only accept energy and a staff");
        unsupported(transfers, "infusion_ritual", "ritual inputs are placed in the world around an altar");
        unsupported(transfers, "ritual", "ritual inputs are placed in the world around an altar");
        unsupported(transfers, "creation_ritual", "ritual inputs are placed in the world around an altar");
        unsupported(transfers, "transformation_ritual", "ritual inputs are placed in the world around an altar");
        unsupported(transfers, "spell", "spell components are consumed from player inventory, not a machine menu");
        return Map.copyOf(transfers);
    }

    private static void supported(Map<String, TransferDecision> transfers, String category,
                                  int recipeStart, int recipeCount, int playerStart, int playerCount) {
        transfers.put(category, new TransferDecision(Status.SUPPORTED, null,
            recipeStart, recipeCount, playerStart, playerCount));
    }

    private static void unsupported(Map<String, TransferDecision> transfers, String category, String reason) {
        transfers.put(category, new TransferDecision(Status.UNSUPPORTED, reason, -1, 0, -1, 0));
    }

    public record TransferDecision(Status status, String reason, int recipeSlotStart, int recipeSlotCount,
                                   int playerSlotStart, int playerSlotCount) {}

    public enum Status { SUPPORTED, UNSUPPORTED }
}
package com.shinoow.abyssalcraft.data.gen;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Permanent one-for-one audit catalog for the 223 machine calls in legacy AbyssalCrafting. */
public final class LegacyMachineRecipeCatalog {

    public static final int SOURCE_COUNT = 223;
    private static final Map<Status, Integer> EXPECTED_COUNTS = Map.of(
        Status.MIGRATED, 142,
        Status.REPLACED, 77,
        Status.RETIRED, 4,
        Status.BLOCKED, 0);

    public enum Status { MIGRATED, REPLACED, BLOCKED, RETIRED }

    public enum Kind { CRYSTALLIZATION, TRANSMUTATION, MATERIALIZATION }

    public record Entry(int sourceOrdinal, int sourceLine, Kind kind, String legacyCall, Status status,
                        String recipeId, String owner, String reason,
                        List<LegacyMachineRecipeSource.StackRef> inputs,
                        List<LegacyMachineRecipeSource.StackRef> outputs,
                        List<MachineOutputResolutionCatalog.Resolution> outputResolutions, float experience) {}

    private static final List<Entry> ENTRIES = build();

    private LegacyMachineRecipeCatalog() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Map<Status, Integer> counts() {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status status : Status.values()) counts.put(status, 0);
        for (Entry entry : ENTRIES) counts.compute(entry.status(), (status, count) -> count + 1);
        return Map.copyOf(counts);
    }

    private static List<Entry> build() {
        List<Entry> entries = LegacyMachineRecipeSource.read().stream().map(LegacyMachineRecipeCatalog::classify).toList();
        validate(entries);
        return List.copyOf(entries);
    }

    private static Entry classify(LegacyMachineRecipeSource.Definition definition) {
        String dependency = LegacyMachineRecipeSource.missing(definition);
        Status status;
        String owner = "";
        String reason;
        String recipeId = "";
        if (dependency != null) {
            status = Status.BLOCKED;
            owner = dependency;
            reason = "missing current registry or item-tag dependency: " + dependency;
        } else if (retiredAlias(definition)) {
            status = Status.RETIRED;
            owner = "legacy-oredictionary-alias";
            reason = "duplicate spelling alias resolves to the preceding modern recipe";
        } else {
            boolean replaced = definition.call().contains("OreDictionary") || definition.call().contains("PotionUtils")
                || definition.inputs().stream().anyMatch(LegacyMachineRecipeSource.StackRef::tag)
                || !definition.outputResolutions().isEmpty()
                || definition.call().matches(".*new ItemStack\\([^,]+,\\s*1,\\s*[1-9].*");
            status = replaced ? Status.REPLACED : Status.MIGRATED;
            reason = replaced ? "legacy metadata or OreDictionary value replaced by its explicit modern item/tag"
                : "all referenced input and output registry content is present";
            String output = definition.outputs().get(0).id();
            recipeId = "abyssalcraft:" + definition.kind().name().toLowerCase(Locale.ROOT) + "_"
                + String.format(Locale.ROOT, "%03d", definition.ordinal()) + "_"
                + output.substring(output.indexOf(':') + 1).replace('/', '_');
        }
        return new Entry(definition.ordinal(), definition.line(), definition.kind(), definition.call(), status,
            recipeId, owner, reason, definition.inputs(), definition.outputs(), definition.outputResolutions(),
            definition.experience());
    }

    private static boolean retiredAlias(LegacyMachineRecipeSource.Definition definition) {
        return definition.call().contains("\"ingotAluminium\"")
            || definition.call().contains("\"nuggetAluminium\"");
    }

    private static void validate(List<Entry> entries) {
        if (entries.size() != SOURCE_COUNT) {
            throw new IllegalStateException("Legacy machine catalog source count changed: " + entries.size());
        }
        int statusTotal = counts(entries).values().stream().mapToInt(Integer::intValue).sum();
        if (statusTotal != SOURCE_COUNT) {
            throw new IllegalStateException("Legacy machine catalog status total changed: " + statusTotal);
        }
        if (!counts(entries).equals(EXPECTED_COUNTS)) {
            throw new IllegalStateException("Legacy machine catalog classification changed: " + counts(entries));
        }
        long distinctOrdinals = entries.stream().map(Entry::sourceOrdinal).distinct().count();
        if (distinctOrdinals != SOURCE_COUNT) {
            throw new IllegalStateException("Legacy machine catalog has duplicate source ordinals");
        }
        Set<String> recipeIds = new HashSet<>();
        for (Entry entry : entries) {
            if (entry.legacyCall().isBlank() || entry.reason().isBlank()
                || entry.inputs().isEmpty() || entry.outputs().isEmpty()) {
                throw new IllegalStateException("Incomplete legacy machine catalog entry " + entry.sourceOrdinal());
            }
            if (entry.status() == Status.MIGRATED || entry.status() == Status.REPLACED) {
                if (entry.recipeId().isBlank()) {
                    throw new IllegalStateException("Executable entry lacks recipe id " + entry.sourceOrdinal());
                }
                if (!recipeIds.add(entry.recipeId())) {
                    throw new IllegalStateException("Duplicate executable recipe id " + entry.recipeId());
                }
            } else if (!entry.recipeId().isBlank() || entry.owner().isBlank()) {
                throw new IllegalStateException("Non-executable entry contract failed " + entry.sourceOrdinal());
            }
        }
        validateRegistryAliasClosure(entries);
    }

    private static void validateRegistryAliasClosure(List<Entry> entries) {
        Set<Integer> closedOrdinals = Set.of(56, 57, 58, 59, 63, 64, 79, 83, 84, 90, 91, 93, 94,
            103, 104, 108, 109, 120);
        List<Entry> closed = entries.stream().filter(entry -> closedOrdinals.contains(entry.sourceOrdinal())).toList();
        if (closed.size() != closedOrdinals.size()
            || closed.stream().anyMatch(entry -> entry.status() == Status.BLOCKED)) {
            throw new IllegalStateException("Legacy machine registry/alias closure regressed: " + closed);
        }
        Entry gateway = entries.get(119);
        if (!gateway.inputs().equals(List.of(new LegacyMachineRecipeSource.StackRef(
                "abyssalcraft:gatewaykeydl", 1, false)))
            || !gateway.outputs().equals(List.of(new LegacyMachineRecipeSource.StackRef(
                "abyssalcraft:gatewaykeyjzh", 1, false)))) {
            throw new IllegalStateException("Legacy gateway aliases no longer resolve to modern gateway keys");
        }
        LegacyMachineRecipeSource.Definition multipleMissing = new LegacyMachineRecipeSource.Definition(
            0, 0, Kind.MATERIALIZATION, "self-test",
            List.of(new LegacyMachineRecipeSource.StackRef("abyssalcraft:self_test_missing_input", 1, false)),
            List.of(new LegacyMachineRecipeSource.StackRef("abyssalcraft:self_test_missing_output", 1, false)),
            List.of(), 0F);
        String dependencies = LegacyMachineRecipeSource.missing(multipleMissing);
        if (!"item-registry/abyssalcraft:self_test_missing_output,item-registry/abyssalcraft:self_test_missing_input"
            .equals(dependencies)) {
            throw new IllegalStateException("Legacy machine multi-dependency audit regressed: " + dependencies);
        }
    }

    private static Map<Status, Integer> counts(List<Entry> entries) {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status status : Status.values()) counts.put(status, 0);
        for (Entry entry : entries) counts.compute(entry.status(), (status, count) -> count + 1);
        return counts;
    }
}
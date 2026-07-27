package com.shinoow.abyssalcraft.validation.world;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.shinoow.abyssalcraft.world.density.DarkRealmCavityMask;
import com.shinoow.abyssalcraft.world.density.LegacyDarkRealmNoise;

/** Compares the modern cavity mask with an offline extraction of the 1.12.2 generator. */
public final class DarkRealmNoiseOracle {

    private static final String BASELINE_RESOURCE =
        "data/abyssalcraft/validation/dark_realm_noise_1_12_2.json";
    private static final String LEGACY_SOURCE =
        "docs/AbyssalCraft-1.12.2/src/main/java/com/shinoow/abyssalcraft/common/world/ChunkGeneratorDarkRealm.java";
    private static final long FIXED_SEED = 1_251_393_890L;
    private static final String LEGACY_SOURCE_SHA256 =
        "a72af19687cf4ef7554cc0b051718237d97830eb84dd6646072ab6d48ecd391d";
    private static final double DENSITY_EPSILON = 1.0E-12D;

    private DarkRealmNoiseOracle() {}

    public static String validateOracle() {
        try (InputStream stream = DarkRealmNoiseOracle.class.getClassLoader()
                .getResourceAsStream(BASELINE_RESOURCE)) {
            if (stream == null) return "RR_WORLD_ORACLE_DARK_REALM_FAIL baselineMissing=true";
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            JsonObject provenance = root.getAsJsonObject("provenance");
            if (root.get("schema").getAsInt() != 1
                    || !LEGACY_SOURCE.equals(provenance.get("source").getAsString())
                    || provenance.get("fixedSeed").getAsLong() != FIXED_SEED
                    || !LEGACY_SOURCE_SHA256.equals(provenance.get("sourceSha256").getAsString())
                    || !provenance.get("algorithm").getAsString().contains("minecraft-1.12.2")
                    || !provenance.get("output").getAsString().contains("density>0")) {
                return "RR_WORLD_ORACLE_DARK_REALM_FAIL provenanceInvalid=true";
            }

            JsonArray samples = root.getAsJsonArray("samples");
            int matches = 0;
            int mismatches = 0;
            int positiveSamples = 0;
            StringBuilder diffs = new StringBuilder();
            for (int index = 0; index < samples.size(); index++) {
                JsonObject sample = samples.get(index).getAsJsonObject();
                JsonArray pos = sample.getAsJsonArray("pos");
                int x = pos.get(0).getAsInt();
                int y = pos.get(1).getAsInt();
                int z = pos.get(2).getAsInt();
                boolean expected = sample.get("air").getAsBoolean();
                double expectedDensity = sample.get("density").getAsDouble();
                if (expected != (expectedDensity > 0.0D) || !coordinatesMatch(sample, x, y, z)) {
                    return "RR_WORLD_ORACLE_DARK_REALM_FAIL sampleProvenance=" + index;
                }
                double actualDensity = LegacyDarkRealmNoise.INSTANCE.density(x, y, z);
                boolean actual = DarkRealmCavityMask.carves(x, y, z);
                if (expected) positiveSamples++;
                if (actual == expected && Math.abs(actualDensity - expectedDensity) <= DENSITY_EPSILON) {
                    matches++;
                } else {
                    mismatches++;
                    if (diffs.length() < 512) {
                        diffs.append(String.format(" [%d,%d,%d]:expected=%b/%.17g,actual=%b/%.17g",
                            x, y, z, expected, expectedDensity, actual, actualDensity));
                    }
                }
            }
            if (samples.size() < 20 || positiveSamples == 0) {
                return "RR_WORLD_ORACLE_DARK_REALM_FAIL baselineCoverage=true samples=" + samples.size()
                    + " positive=" + positiveSamples;
            }
            return String.format(
                "RR_WORLD_ORACLE_DARK_REALM_%s matches=%d mismatches=%d samples=%d positive=%d sourceSha256=%s%s",
                mismatches == 0 ? "OK" : "FAIL", matches, mismatches, samples.size(), positiveSamples,
                provenance.get("sourceSha256").getAsString(), mismatches == 0 ? "" : " diffs=" + diffs);
        } catch (Exception exception) {
            return "RR_WORLD_ORACLE_DARK_REALM_FAIL baselineRead="
                + exception.getClass().getSimpleName() + ":" + String.valueOf(exception.getMessage()).replace(' ', '_');
        }
    }

    private static boolean coordinatesMatch(JsonObject sample, int x, int y, int z) {
        JsonArray chunk = sample.getAsJsonArray("chunk");
        JsonArray local = sample.getAsJsonArray("local");
        JsonArray cell = sample.getAsJsonArray("cell");
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);
        return chunk.size() == 2 && chunk.get(0).getAsInt() == Math.floorDiv(x, 16)
            && chunk.get(1).getAsInt() == Math.floorDiv(z, 16)
            && local.size() == 2 && local.get(0).getAsInt() == localX && local.get(1).getAsInt() == localZ
            && cell.size() == 3 && cell.get(0).getAsInt() == localX / 8
            && cell.get(1).getAsInt() == Math.min(31, (y - 30) / 4)
            && cell.get(2).getAsInt() == localZ / 8;
    }
}

package com.shinoow.abyssalcraft.world.density;

import java.util.Arrays;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * World-seed-independent Dark Realm cavity mask.
 *
 * <p>The legacy generator constructed a second noise stack from the fixed seed {@code 1251393890}
 * and carved its positive region between Y 30 and 157. This modern density function preserves that
 * contract without retaining server/world references. Four per-thread chunk masks bound memory and
 * make repeated interpolation queries deterministic and inexpensive.
 */
public final class DarkRealmCavityMask implements DensityFunction.SimpleFunction {

    public static final long FIXED_SEED = 1_251_393_890L;
    public static final DarkRealmCavityMask INSTANCE = new DarkRealmCavityMask();
    public static final KeyDispatchDataCodec<DarkRealmCavityMask> CODEC =
        KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

    private static final int MIN_Y = 30;
    private static final int MAX_Y = 157;
    private static final int CACHE_SLOTS = 4;
    private static final ImprovedNoise[] OCTAVES = createOctaves();
    private static final ThreadLocal<ChunkCache> CACHE = ThreadLocal.withInitial(ChunkCache::new);

    private DarkRealmCavityMask() {}

    @Override
    public double compute(FunctionContext context) {
        int y = context.blockY();
        if (y < MIN_Y || y > MAX_Y) return 0.0D;
        int chunkX = Math.floorDiv(context.blockX(), 16);
        int chunkZ = Math.floorDiv(context.blockZ(), 16);
        int localX = Math.floorMod(context.blockX(), 16);
        int localZ = Math.floorMod(context.blockZ(), 16);
        return CACHE.get().mask(chunkX, chunkZ).get(localX, y - MIN_Y, localZ) ? 1.0D : 0.0D;
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    /** Exposed for deterministic validation without constructing a world. */
    public static boolean carves(int blockX, int blockY, int blockZ) {
        if (blockY < MIN_Y || blockY > MAX_Y) return false;
        return sample(blockX, blockY, blockZ) > 0.12D;
    }

    private static ImprovedNoise[] createOctaves() {
        LegacyRandomSource random = new LegacyRandomSource(FIXED_SEED);
        ImprovedNoise[] octaves = new ImprovedNoise[16];
        for (int i = 0; i < octaves.length; i++) octaves[i] = new ImprovedNoise(random);
        return octaves;
    }

    private static double sample(int x, int y, int z) {
        double frequency = 1.0D / 96.0D;
        double amplitude = 1.0D;
        double total = 0.0D;
        double normalization = 0.0D;
        for (ImprovedNoise octave : OCTAVES) {
            total += octave.noise(x * frequency, y * frequency * 1.75D, z * frequency) * amplitude;
            normalization += amplitude;
            frequency *= 2.0D;
            amplitude *= 0.5D;
        }
        double verticalEnvelope = 1.0D - Math.abs(y - 94.0D) / 76.0D;
        return total / normalization + verticalEnvelope * 0.18D;
    }

    private static final class ChunkCache {
        private final ChunkMask[] slots = new ChunkMask[CACHE_SLOTS];
        private int nextSlot;

        private ChunkCache() {
            Arrays.setAll(slots, ignored -> new ChunkMask());
        }

        private ChunkMask mask(int chunkX, int chunkZ) {
            for (ChunkMask slot : slots) {
                if (slot.matches(chunkX, chunkZ)) return slot;
            }
            ChunkMask slot = slots[nextSlot++ & (CACHE_SLOTS - 1)];
            slot.fill(chunkX, chunkZ);
            return slot;
        }
    }

    private static final class ChunkMask {
        private static final int HEIGHT = MAX_Y - MIN_Y + 1;
        private static final int GRID_XZ = 3;
        private static final int GRID_Y = 33;
        private final long[] bits = new long[(16 * HEIGHT * 16 + 63) / 64];
        private final double[] coarse = new double[GRID_XZ * GRID_XZ * GRID_Y];
        private int chunkX = Integer.MIN_VALUE;
        private int chunkZ = Integer.MIN_VALUE;

        private boolean matches(int requestedX, int requestedZ) {
            return chunkX == requestedX && chunkZ == requestedZ;
        }

        private void fill(int requestedX, int requestedZ) {
            Arrays.fill(bits, 0L);
            int baseX = requestedX << 4;
            int baseZ = requestedZ << 4;
            for (int gridX = 0; gridX < GRID_XZ; gridX++) {
                for (int gridZ = 0; gridZ < GRID_XZ; gridZ++) {
                    for (int gridY = 0; gridY < GRID_Y; gridY++) {
                        coarse[coarseIndex(gridX, gridY, gridZ)] =
                            sample(baseX + gridX * 8, MIN_Y + gridY * 4, baseZ + gridZ * 8);
                    }
                }
            }
            interpolate();
            chunkX = requestedX;
            chunkZ = requestedZ;
        }

        private void interpolate() {
            for (int cellX = 0; cellX < 2; cellX++) {
                for (int cellZ = 0; cellZ < 2; cellZ++) {
                    for (int cellY = 0; cellY < 32; cellY++) {
                        double n000 = coarse[coarseIndex(cellX, cellY, cellZ)];
                        double n001 = coarse[coarseIndex(cellX, cellY, cellZ + 1)];
                        double n100 = coarse[coarseIndex(cellX + 1, cellY, cellZ)];
                        double n101 = coarse[coarseIndex(cellX + 1, cellY, cellZ + 1)];
                        double n010 = coarse[coarseIndex(cellX, cellY + 1, cellZ)];
                        double n011 = coarse[coarseIndex(cellX, cellY + 1, cellZ + 1)];
                        double n110 = coarse[coarseIndex(cellX + 1, cellY + 1, cellZ)];
                        double n111 = coarse[coarseIndex(cellX + 1, cellY + 1, cellZ + 1)];
                        for (int subY = 0; subY < 4; subY++) {
                            double fy = subY / 4.0D;
                            double a00 = lerp(fy, n000, n010);
                            double a01 = lerp(fy, n001, n011);
                            double a10 = lerp(fy, n100, n110);
                            double a11 = lerp(fy, n101, n111);
                            for (int subX = 0; subX < 8; subX++) {
                                double fx = subX / 8.0D;
                                double b0 = lerp(fx, a00, a10);
                                double b1 = lerp(fx, a01, a11);
                                for (int subZ = 0; subZ < 8; subZ++) {
                                    int y = cellY * 4 + subY;
                                    if (y >= HEIGHT) continue;
                                    double value = lerp(subZ / 8.0D, b0, b1);
                                    if (value > 0.12D) {
                                        set(cellX * 8 + subX, y, cellZ * 8 + subZ);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private static int coarseIndex(int x, int y, int z) {
            return (x * GRID_XZ + z) * GRID_Y + y;
        }

        private static double lerp(double delta, double start, double end) {
            return start + delta * (end - start);
        }

        private boolean get(int x, int y, int z) {
            int index = (x * HEIGHT + y) * 16 + z;
            return (bits[index >>> 6] & (1L << (index & 63))) != 0L;
        }

        private void set(int x, int y, int z) {
            int index = (x * HEIGHT + y) * 16 + z;
            bits[index >>> 6] |= 1L << (index & 63);
        }
    }
}
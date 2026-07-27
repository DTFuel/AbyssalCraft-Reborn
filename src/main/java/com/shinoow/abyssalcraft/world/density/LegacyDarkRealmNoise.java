package com.shinoow.abyssalcraft.world.density;

import java.util.Arrays;
import java.util.Random;

/** Deterministic offline reconstruction of the 1.12.2 Dark Realm fixed-noise carve pass. */
public final class LegacyDarkRealmNoise {

    public static final long FIXED_SEED = 1_251_393_890L;
    public static final LegacyDarkRealmNoise INSTANCE = new LegacyDarkRealmNoise();

    private static final int CACHE_SLOTS = 4;

    private final Octaves noise1;
    private final Octaves noise2;
    private final Octaves noise3;
    private final ThreadLocal<Cache> cache = ThreadLocal.withInitial(Cache::new);

    private LegacyDarkRealmNoise() {
        Random random = new Random(FIXED_SEED);
        noise1 = new Octaves(random, 16);
        noise2 = new Octaves(random, 16);
        noise3 = new Octaves(random, 8);
    }

    public boolean carves(int worldX, int worldY, int worldZ) {
        return density(worldX, worldY, worldZ) > 0.0D;
    }

    public double density(int worldX, int worldY, int worldZ) {
        if (worldY < 30 || worldY > 157) return Double.NEGATIVE_INFINITY;
        int chunkX = Math.floorDiv(worldX, 16);
        int chunkZ = Math.floorDiv(worldZ, 16);
        int localX = Math.floorMod(worldX, 16);
        int localZ = Math.floorMod(worldZ, 16);
        int cellX = localX / 8;
        int cellZ = localZ / 8;
        int cellY = Math.min(31, (worldY - 30) / 4);
        double fractionX = Math.floorMod(localX, 8) / 8.0D;
        double fractionZ = Math.floorMod(localZ, 8) / 8.0D;
        double fractionY = Math.floorMod(worldY - 30, 4) / 4.0D;
        double[] values = cache.get().densities(chunkX, chunkZ);
        double lowerA = lerp(fractionZ, at(values, cellX, cellZ, cellY), at(values, cellX, cellZ + 1, cellY));
        double lowerB = lerp(fractionZ, at(values, cellX + 1, cellZ, cellY), at(values, cellX + 1, cellZ + 1, cellY));
        double upperA = lerp(fractionZ, at(values, cellX, cellZ, cellY + 1), at(values, cellX, cellZ + 1, cellY + 1));
        double upperB = lerp(fractionZ, at(values, cellX + 1, cellZ, cellY + 1), at(values, cellX + 1, cellZ + 1, cellY + 1));
        return lerp(fractionY, lerp(fractionX, lowerA, lowerB), lerp(fractionX, upperA, upperB));
    }

    private double[] generateDensities(int chunkX, int chunkZ) {
        int x = chunkX * 2;
        int z = chunkZ * 2;
        double[] noiseData1 = noise3.generate3d(x, 0, z, 3, 33, 3,
            1368.824D / 80.0D, 684.412D / 160.0D, 1368.824D / 80.0D);
        double[] noiseData2 = noise1.generate3d(x, 0, z, 3, 33, 3, 1368.824D, 684.412D, 1368.824D);
        double[] noiseData3 = noise2.generate3d(x, 0, z, 3, 33, 3, 1368.824D, 684.412D, 1368.824D);
        double[] result = new double[297];
        int index = 0;
        for (int xIndex = 0; xIndex < 3; xIndex++) {
            for (int zIndex = 0; zIndex < 3; zIndex++) {
                for (int yIndex = 0; yIndex < 33; yIndex++) {
                    double blend = (noiseData1[index] / 10.0D + 1.0D) / 2.0D;
                    double low = noiseData2[index] / 512.0D;
                    double high = noiseData3[index] / 512.0D;
                    double density = blend < 0.0D ? low : blend > 1.0D ? high : lerp(blend, low, high);
                    density -= 8.0D;
                    if (yIndex > 1) {
                        double amount = (yIndex - 1) / 31.0D;
                        density = density * (1.0D - amount) - 30.0D * amount;
                    }
                    if (yIndex < 8) {
                        double amount = (8 - yIndex) / 7.0D;
                        density = density * (1.0D - amount) - 30.0D * amount;
                    }
                    result[index++] = density;
                }
            }
        }
        return result;
    }

    private static double at(double[] values, int x, int z, int y) {
        return values[(x * 3 + z) * 33 + y];
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    private final class Cache {
        private final Entry[] entries = new Entry[CACHE_SLOTS];
        private int next;

        private Cache() {
            Arrays.setAll(entries, ignored -> new Entry());
        }

        private double[] densities(int chunkX, int chunkZ) {
            for (Entry entry : entries) if (entry.chunkX == chunkX && entry.chunkZ == chunkZ) return entry.values;
            Entry entry = entries[next++ & (CACHE_SLOTS - 1)];
            entry.chunkX = chunkX;
            entry.chunkZ = chunkZ;
            entry.values = generateDensities(chunkX, chunkZ);
            return entry.values;
        }
    }

    private static final class Entry {
        int chunkX = Integer.MIN_VALUE;
        int chunkZ = Integer.MIN_VALUE;
        double[] values;
    }

    private static final class Octaves {
        private final Improved[] generators;

        private Octaves(Random random, int count) {
            generators = new Improved[count];
            Arrays.setAll(generators, ignored -> new Improved(random));
        }

        private double[] generate3d(double x, double y, double z, int xSize, int ySize, int zSize,
                                    double xScale, double yScale, double zScale) {
            double[] output = new double[xSize * ySize * zSize];
            double frequency = 1.0D;
            for (Improved generator : generators) {
                generator.populate(output, x * frequency, y * frequency, z * frequency, xSize, ySize, zSize,
                    xScale * frequency, yScale * frequency, zScale * frequency, frequency);
                frequency /= 2.0D;
            }
            return output;
        }
    }

    private static final class Improved {
        private final int[] permutations = new int[512];
        private final double xCoord;
        private final double yCoord;
        private final double zCoord;

        private Improved(Random random) {
            xCoord = random.nextDouble() * 256.0D;
            yCoord = random.nextDouble() * 256.0D;
            zCoord = random.nextDouble() * 256.0D;
            for (int index = 0; index < 256; index++) permutations[index] = index;
            for (int index = 0; index < 256; index++) {
                int swap = random.nextInt(256 - index) + index;
                int value = permutations[index];
                permutations[index] = permutations[swap];
                permutations[swap] = value;
                permutations[index + 256] = permutations[index];
            }
        }

        private void populate(double[] output, double x, double y, double z, int xSize, int ySize, int zSize,
                              double xScale, double yScale, double zScale, double amplitude) {
            int index = 0;
            for (int xIndex = 0; xIndex < xSize; xIndex++) {
                double sampleX = x + xIndex * xScale + xCoord;
                for (int zIndex = 0; zIndex < zSize; zIndex++) {
                    double sampleZ = z + zIndex * zScale + zCoord;
                    for (int yIndex = 0; yIndex < ySize; yIndex++) {
                        double sampleY = y + yIndex * yScale + yCoord;
                        output[index++] += sample(sampleX, sampleY, sampleZ) / amplitude;
                    }
                }
            }
        }

        private double sample(double x, double y, double z) {
            int floorX = floor(x), floorY = floor(y), floorZ = floor(z);
            double localX = x - floorX, localY = y - floorY, localZ = z - floorZ;
            int px = floorX & 255, py = floorY & 255, pz = floorZ & 255;
            int a = permutations[px] + py, aa = permutations[a] + pz, ab = permutations[a + 1] + pz;
            int b = permutations[px + 1] + py, ba = permutations[b] + pz, bb = permutations[b + 1] + pz;
            return lerp(fade(localZ),
                lerp(fade(localY), lerp(fade(localX), grad(permutations[aa], localX, localY, localZ),
                    grad(permutations[ba], localX - 1, localY, localZ)),
                    lerp(fade(localX), grad(permutations[ab], localX, localY - 1, localZ),
                        grad(permutations[bb], localX - 1, localY - 1, localZ))),
                lerp(fade(localY), lerp(fade(localX), grad(permutations[aa + 1], localX, localY, localZ - 1),
                    grad(permutations[ba + 1], localX - 1, localY, localZ - 1)),
                    lerp(fade(localX), grad(permutations[ab + 1], localX, localY - 1, localZ - 1),
                        grad(permutations[bb + 1], localX - 1, localY - 1, localZ - 1))));
        }

        private static int floor(double value) {
            int integer = (int) value;
            return value < integer ? integer - 1 : integer;
        }

        private static double fade(double value) {
            return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
        }

        private static double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double first = h < 8 ? x : y;
            double second = h < 4 ? y : h == 12 || h == 14 ? x : z;
            return ((h & 1) == 0 ? first : -first) + ((h & 2) == 0 ? second : -second);
        }
    }
}
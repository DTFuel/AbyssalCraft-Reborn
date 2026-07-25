package com.shinoow.abyssalcraft.world;

import java.util.concurrent.CompletableFuture;

import com.shinoow.abyssalcraft.world.density.DarkRealmCavityMask;

/** Deterministic, world-independent RR-WORLD checks executed by datagen. */
public final class WorldgenInvariant {

    private static final int[][] SAMPLE_POINTS = {
        {0, 30, 0}, {15, 63, 15}, {16, 94, 16}, {-1, 94, -1}, {-16, 120, 31}, {31, 157, -32}
    };

    private WorldgenInvariant() {}

    public static void validate() {
        long sequential = sampleHash();
        CompletableFuture<Long> first = CompletableFuture.supplyAsync(WorldgenInvariant::sampleHash);
        CompletableFuture<Long> second = CompletableFuture.supplyAsync(WorldgenInvariant::sampleHash);
        require(sequential == first.join() && sequential == second.join(),
            "Dark Realm cavity mask changed across threads");
        require(!DarkRealmCavityMask.carves(0, 29, 0), "Dark Realm mask carved below Y30");
        require(!DarkRealmCavityMask.carves(0, 158, 0), "Dark Realm mask carved above Y157");
        System.out.printf("RR_WORLD_INVARIANT_OK cavityHash=%016x samples=%d%n", sequential, SAMPLE_POINTS.length);
    }

    private static long sampleHash() {
        long hash = 0xcbf29ce484222325L;
        for (int chunkX = -2; chunkX <= 1; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 1; chunkZ++) {
                for (int x = 0; x < 16; x += 3) {
                    for (int y = 30; y <= 157; y += 7) {
                        for (int z = 0; z < 16; z += 3) {
                            hash ^= DarkRealmCavityMask.carves((chunkX << 4) + x, y, (chunkZ << 4) + z) ? 1L : 0L;
                            hash *= 0x100000001b3L;
                        }
                    }
                }
            }
        }
        for (int[] point : SAMPLE_POINTS) {
            hash ^= DarkRealmCavityMask.carves(point[0], point[1], point[2]) ? 1L : 0L;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
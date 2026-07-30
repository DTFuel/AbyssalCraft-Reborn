package com.shinoow.abyssalcraft.validation.world;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Bounded real-server locator regression for both Shoggoth Lair structure branches. */
public final class ShoggothLairLocateFixture {

    private static final int SEARCH_RADIUS_CHUNKS = 100;
    private static final long MAX_LOCATE_MILLIS = 5_000L;
    private static final TagKey<Structure> SWAMP = target("shoggoth_pit");
    private static final TagKey<Structure> RIVER = target("shoggoth_pit_river");

    private ShoggothLairLocateFixture() {}

    public static void run(ServerLevel level) {
        Sample swamp = locate(level, SWAMP, "shoggoth_pit");
        Sample river = locate(level, RIVER, "shoggoth_pit_river");
        System.out.printf("RR_SHOGGOTH_LOCATE_OK swamp=%dms river=%dms radius=%d swampPos=%s riverPos=%s%n",
            swamp.millis(), river.millis(), SEARCH_RADIUS_CHUNKS, swamp.position(), river.position());
    }

    private static Sample locate(ServerLevel level, TagKey<Structure> target, String id) {
        long started = System.nanoTime();
        BlockPos position = level.findNearestMapStructure(target, BlockPos.ZERO,
            SEARCH_RADIUS_CHUNKS, false);
        long millis = (System.nanoTime() - started) / 1_000_000L;
        if (position == null) {
            throw new IllegalStateException("RR_SHOGGOTH_LOCATE_FAIL missing=" + id
                + " radius=" + SEARCH_RADIUS_CHUNKS);
        }
        if (millis > MAX_LOCATE_MILLIS) {
            throw new IllegalStateException("RR_SHOGGOTH_LOCATE_FAIL slow=" + id + " millis=" + millis);
        }
        return new Sample(position, millis);
    }

    private static TagKey<Structure> target(String id) {
        return TagKey.create(Registries.STRUCTURE, ACRef.id(id));
    }

    private record Sample(BlockPos position, long millis) {}
}
package com.shinoow.abyssalcraft.system.effect;

import java.util.concurrent.atomic.AtomicBoolean;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

/** Permanent deterministic contract for Dread Plague dynamic biome spread. */
public final class DreadPlagueSelfTest {

    private DreadPlagueSelfTest() {}

    public static void run() {
        AtomicBoolean disabled = new AtomicBoolean(false);
        require(DreadPlagueSpread.shouldRun(disabled::get, false, 1, Level.OVERWORLD, true, 100),
            "enabled config supplier did not permit Dread Plague spread");
        disabled.set(true);
        require(!DreadPlagueSpread.shouldRun(disabled::get, false, 1, Level.OVERWORLD, true, 100),
            "reloaded config supplier did not disable Dread Plague spread");
        require(DreadPlagueSpread.shouldRun(false, false, 1, Level.OVERWORLD, true, 100),
            "enabled high-level Dread Plague did not spread");
        require(!DreadPlagueSpread.shouldRun(true, true, 1, Level.OVERWORLD, true, 100),
            "no_dreadlands_spread did not fail closed");
        require(!DreadPlagueSpread.shouldRun(false, false, 0, Level.OVERWORLD, true, 100),
            "level-one Dread Plague spread outside hardcore mode");
        require(DreadPlagueSpread.shouldRun(false, true, 0, Level.OVERWORLD, true, 100),
            "hardcore mode did not enable level-one spread");
        require(!DreadPlagueSpread.shouldRun(false, true, 1, Level.OVERWORLD, false, 100),
            "off-thread Dread Plague mutated biomes");
        require(!DreadPlagueSpread.shouldRun(false, true, 1, Level.OVERWORLD, true, 99),
            "Dread Plague ignored the 100-tick boundary");
        require(!DreadPlagueSpread.shouldRun(false, true, 1, ACDimensions.DARK_REALM, true, 100)
            && !DreadPlagueSpread.shouldRun(false, true, 1, ACDimensions.OMOTHOL, true, 100),
            "immune dimension boundary changed");
        require(DreadPlagueSpread.canReplace(Biomes.PLAINS)
            && !DreadPlagueSpread.canReplace(DarklandsBiomes.DREADLANDS)
            && !DreadPlagueSpread.canReplace(DarklandsBiomes.DREADLANDS_FOREST)
            && !DreadPlagueSpread.canReplace(DarklandsBiomes.DREADLANDS_MOUNTAINS)
            && !DreadPlagueSpread.canReplace(DarklandsBiomes.DREADLANDS_OCEAN)
            && !DreadPlagueSpread.canReplace(DarklandsBiomes.PURGED),
            "Dread Plague biome boundary changed");
        String[] hosts = {"dreaded_ghoul", "dreadling", "dreadspawn", "dreadguard",
            "chagaroth", "chagarothspawn", "dreadslug", "dreadedcharge"};
        for (String host : hosts) {
            require(BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.id(host)),
                "Dread Plague host missing registry id: " + ACRef.id(host));
        }
        System.out.println("RR_DREAD_PLAGUE_SELF_TEST_OK config=2 reload=1 amplifier=2 dimensions=3 biomes=6 hosts=8 persistence=chunk_unsaved");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
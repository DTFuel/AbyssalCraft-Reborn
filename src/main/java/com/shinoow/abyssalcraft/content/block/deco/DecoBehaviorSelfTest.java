package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class DecoBehaviorSelfTest {

    private DecoBehaviorSelfTest() {}

    public static void run() {
        require(DreadlandsGroundBlock.shouldDecay(3, 3),
            "dreadlands grass did not decay below a dark opaque block");
        require(!DreadlandsGroundBlock.shouldDecay(4, 3),
            "dreadlands grass decayed despite sufficient neighboring light");
        require(!DreadlandsGroundBlock.shouldDecay(3, 2),
            "dreadlands grass decayed below a low-opacity block");
        require(DreadlandsGroundBlock.canSpreadTo(DecoBlocks.DREADLANDS_DIRT.get().defaultBlockState(),
            0), "dreadlands grass did not spread to exposed dreadlands dirt");
        require(!DreadlandsGroundBlock.canSpreadTo(Blocks.DIRT.defaultBlockState(), 0),
            "dreadlands grass spread to vanilla dirt");
        require(!DreadlandsGroundBlock.canSpreadTo(DecoBlocks.DREADLANDS_DIRT.get().defaultBlockState(),
            3), "dreadlands grass spread below an opaque block");

        Vec3 slowed = DreadlandsMuckBlock.slowMovement(new Vec3(2.0D, 1.0D, -4.0D));
        require(close(slowed.x, 1.6D) && close(slowed.y, 1.0D) && close(slowed.z, -3.2D),
            "dreadlands muck movement multiplier changed");

        require(DecoPlantBlock.shouldDamage(true, true, true), "wastelands thorn did not damage an unarmored player");
        require(!DecoPlantBlock.shouldDamage(false, true, true), "luminous thistle used thorn damage");
        require(!DecoPlantBlock.shouldDamage(true, false, true), "wastelands thorn ignored boots");
        require(!DecoPlantBlock.shouldDamage(true, true, false), "wastelands thorn ignored leggings");
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) < 0.0001D;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
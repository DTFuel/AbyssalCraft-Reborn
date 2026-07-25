package com.shinoow.abyssalcraft.world.darklands;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

import com.shinoow.abyssalcraft.registry.BaseBlocks;

public final class DarklandsSurfaceRules {

    private DarklandsSurfaceRules() {}

    public static SurfaceRules.RuleSource overworld() {
        SurfaceRules.RuleSource grass = state(Blocks.GRASS_BLOCK);
        SurfaceRules.RuleSource dirt = state(Blocks.DIRT);
        SurfaceRules.RuleSource darkstone = state(BaseBlocks.DARKSTONE.get());
        SurfaceRules.ConditionSource water = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.RuleSource grassSurface = SurfaceRules.sequence(SurfaceRules.ifTrue(water, grass), dirt);

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.isBiome(DarklandsBiomes.MOUNTAINS), darkstone),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(DarklandsBiomes.HILLS),
                SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grassSurface), darkstone)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(DarklandsBiomes.DARKLANDS, DarklandsBiomes.FOREST,
                DarklandsBiomes.PLAINS, DarklandsBiomes.CORALIUM_INFESTED_SWAMP),
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grassSurface))
        );
    }

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}

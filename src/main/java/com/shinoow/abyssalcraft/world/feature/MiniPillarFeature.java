package com.shinoow.abyssalcraft.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Stage G0 vertical-slice example {@link Feature} (owned by PG-0; seeds {@code world/feature/**} for
 * PG-4).
 *
 * <p>A deliberately trivial feature -- a four-block glowstone pillar at the placement origin -- whose
 * only purpose is to prove the <em>code</em> half of the worldgen pipeline end-to-end for Stage G1:
 * a custom {@code Feature} subclass registered through {@link com.shinoow.abyssalcraft.registry.ModWorldgen}
 * on the MOD bus, referenced from a {@code configured_feature}/{@code placed_feature} JSON, and
 * attached to a biome's decoration step. PG-4 (feature migration) owns the real AbyssalCraft features
 * (trees / lakes / stalagmites / monolith); this class is the template they follow.
 *
 * <p>The {@code place}/{@code setBlock}/{@code FeaturePlaceContext} surface and the
 * {@code Registries.FEATURE} value type are identical on both loaders and both MC versions, so no
 * {@code //?} fork is needed.
 */
public class MiniPillarFeature extends Feature<NoneFeatureConfiguration> {

    public MiniPillarFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockState state = Blocks.GLOWSTONE.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            level.setBlock(origin.above(i), state, 2);
        }
        return true;
    }
}

package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.util.valueproviders.IntProvider;

/**
 * Compat: constructors for the redstone/wood block variants whose ctors fork across versions.
 *
 * <p>1.21 reordered/reshaped these constructors: {@code ButtonBlock} dropped its trailing
 * {@code boolean} and moved {@code Properties} last; {@code PressurePlateBlock} dropped
 * {@code Sensitivity}; {@code DoorBlock}/{@code FenceGateBlock} swapped {@code Properties} with their
 * {@code BlockSetType}/{@code WoodType}; and {@code SaplingBlock}'s grower type was renamed
 * {@code AbstractTreeGrower} -&gt; {@code TreeGrower}. PB-3 sidestepped these by only using vanilla
 * classes with identical ctors; this factory hosts the forks so {@code BaseBlocks} can register the
 * remaining variants fork-free. Saplings bind the version-specific grower API to AbyssalCraft
 * configured tree features.
 */
public final class BlockFactory {

    private BlockFactory() {}

    /** Button. {@code arrowsCanPress} applies on 1.20.1 only (1.21 derives it from the set type). */
    public static ButtonBlock button(BlockBehaviour.Properties props, BlockSetType set, int ticksToStayPressed, boolean arrowsCanPress) {
        //? if >=1.21 {
        /*return new ButtonBlock(set, ticksToStayPressed, props);
        *///?} else {
        return new ButtonBlock(props, set, ticksToStayPressed, arrowsCanPress);
        //?}
    }

    /** Pressure plate (EVERYTHING sensitivity on 1.20.1; 1.21 has no sensitivity arg). */
    public static PressurePlateBlock pressurePlate(BlockBehaviour.Properties props, BlockSetType set) {
        //? if >=1.21 {
        /*return new PressurePlateBlock(set, props);
        *///?} else {
        return new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, props, set);
        //?}
    }

    /** Door. */
    public static DoorBlock door(BlockBehaviour.Properties props, BlockSetType set) {
        //? if >=1.21 {
        /*return new DoorBlock(set, props);
        *///?} else {
        return new DoorBlock(props, set);
        //?}
    }

    /** Fence gate. */
    public static FenceGateBlock fenceGate(BlockBehaviour.Properties props, WoodType wood) {
        //? if >=1.21 {
        /*return new FenceGateBlock(wood, props);
        *///?} else {
        return new FenceGateBlock(props, wood);
        //?}
    }

    /** Experience-dropping block; constructor arguments swapped in 1.21. */
    public static DropExperienceBlock experienceBlock(BlockBehaviour.Properties props, IntProvider xpRange) {
        //? if >=1.21 {
        /*return new DropExperienceBlock(xpRange, props);
        *///?} else {
        return new DropExperienceBlock(props, xpRange);
        //?}
    }

    /** Sapling bound to an AbyssalCraft configured tree feature. */
    public static SaplingBlock sapling(BlockBehaviour.Properties props, String featureName) {
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> feature =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
                ACRef.id(featureName));
        //? if >=1.21 {
        /*net.minecraft.world.level.block.grower.TreeGrower grower =
            new net.minecraft.world.level.block.grower.TreeGrower("abyssalcraft:" + featureName,
                java.util.Optional.empty(), java.util.Optional.of(feature), java.util.Optional.empty());
        return new SaplingBlock(grower, props);
        *///?} else {
        return new SaplingBlock(new net.minecraft.world.level.block.grower.AbstractTreeGrower() {
            @Override
            protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>>
                    getConfiguredFeature(net.minecraft.util.RandomSource random, boolean hasFlowers) {
                return feature;
            }
        }, props);
        //?}
    }
}

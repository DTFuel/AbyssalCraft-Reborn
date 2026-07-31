package com.shinoow.abyssalcraft.world.darklands;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import terrablender.api.ParameterUtils.Continentalness;
import terrablender.api.ParameterUtils.Depth;
import terrablender.api.ParameterUtils.Erosion;
import terrablender.api.ParameterUtils.Humidity;
import terrablender.api.ParameterUtils.ParameterPointListBuilder;
import terrablender.api.ParameterUtils.Temperature;
import terrablender.api.ParameterUtils.Weirdness;
import terrablender.api.Region;
import terrablender.api.RegionType;

public final class DarklandsRegion extends Region {

    private static final float FREQUENCY_PENALTY = 0.05F;

    public DarklandsRegion(ResourceLocation id, int weight) {
        super(id, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        add(mapper, DarklandsBiomes.DARKLANDS, Temperature.NEUTRAL, Humidity.NEUTRAL,
            Continentalness.NEAR_INLAND, Erosion.EROSION_3, Weirdness.MID_SLICE_NORMAL_ASCENDING);
        add(mapper, DarklandsBiomes.FOREST, Temperature.COOL, Humidity.HUMID,
            Continentalness.MID_INLAND, Erosion.EROSION_4, Weirdness.MID_SLICE_NORMAL_DESCENDING);
        add(mapper, DarklandsBiomes.PLAINS, Temperature.NEUTRAL, Humidity.NEUTRAL,
            Continentalness.FAR_INLAND, Erosion.EROSION_5, Weirdness.MID_SLICE_NORMAL_ASCENDING);
        add(mapper, DarklandsBiomes.HILLS, Temperature.COOL, Humidity.NEUTRAL,
            Continentalness.MID_INLAND, Erosion.EROSION_1, Weirdness.HIGH_SLICE_NORMAL_ASCENDING);
        add(mapper, DarklandsBiomes.MOUNTAINS, Temperature.COOL, Humidity.NEUTRAL,
            Continentalness.FAR_INLAND, Erosion.EROSION_0, Weirdness.HIGH_SLICE_NORMAL_DESCENDING);
        add(mapper, DarklandsBiomes.CORALIUM_INFESTED_SWAMP, Temperature.NEUTRAL, Humidity.HUMID,
            Continentalness.COAST, Erosion.EROSION_6, Weirdness.VALLEY);
    }

    private static void add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                            ResourceKey<Biome> biome, Temperature temperature, Humidity humidity,
                            Continentalness continentalness, Erosion erosion, Weirdness weirdness) {
        new ParameterPointListBuilder()
            .temperature(temperature)
            .humidity(humidity)
            .continentalness(continentalness)
            .erosion(erosion)
            .depth(Depth.SURFACE)
            .weirdness(halfWidth(weirdness.parameter()))
            .offset(FREQUENCY_PENALTY)
            .build()
            .forEach(point -> mapper.accept(Pair.of(point, biome)));
    }

    private static Climate.Parameter halfWidth(Climate.Parameter parameter) {
        long midpoint = (parameter.min() + parameter.max()) / 2L;
        long halfWidth = (parameter.max() - parameter.min()) / 4L;
        return new Climate.Parameter(midpoint - halfWidth, midpoint + halfWidth);
    }
}

package com.shinoow.abyssalcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.shinoow.abyssalcraft.world.WorldgenConfigGate;
import com.shinoow.abyssalcraft.config.ComplexConfig;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

    @Inject(method = "placeWithBiomeCheck", at = @At("HEAD"), cancellable = true)
    private void abyssalcraft$applyConfigGate(WorldGenLevel level, ChunkGenerator chunkGenerator,
                                               RandomSource random, BlockPos origin,
                                               CallbackInfoReturnable<Boolean> callback) {
        ResourceLocation id = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
            .getKey((PlacedFeature) (Object) this);
        boolean configuredOre = id != null && "abyssalcraft".equals(id.getNamespace())
            && (id.getPath().startsWith("ore_") || id.getPath().equals("coralium_swamp_ores"));
        if (configuredOre
                && ComplexConfig.oreGenerationDimensionBlacklist().contains(level.getLevel().dimension().location())
                || !WorldgenConfigGate.allowsPlacedFeature(id)) {
            callback.setReturnValue(false);
        }
    }
}
package com.shinoow.abyssalcraft.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import com.shinoow.abyssalcraft.world.structure.LegacyStructurePlacementContext;
import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ComplexConfig;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow @Final private Structure structure;

    @Inject(method = "placeInChunk", at = @At("HEAD"), cancellable = true)
    private void abyssalcraft$beginPalette(WorldGenLevel level, StructureManager structureManager,
                                            ChunkGenerator chunkGenerator, RandomSource random,
                                            BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo callback) {
        ResourceLocation structureId = level.registryAccess()
            .registryOrThrow(Registries.STRUCTURE)
            .getKey(structure);
        if (structureId != null && AbyssalCraft.MODID.equals(structureId.getNamespace())
                && ComplexConfig.structureGenerationDimensionBlacklist().contains(
                    level.getLevel().dimension().location())) {
            callback.cancel();
            return;
        }
        LegacyStructurePlacementContext.enter(structureId);
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void abyssalcraft$endPalette(WorldGenLevel level, StructureManager structureManager,
                                          ChunkGenerator chunkGenerator, RandomSource random,
                                          BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo callback) {
        LegacyStructurePlacementContext.exit();
    }
}
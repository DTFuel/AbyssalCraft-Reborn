package com.shinoow.abyssalcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;

import com.shinoow.abyssalcraft.platform.StructureLootCompat;

//? if >=1.21 {
/*import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}

@Mixin(MineshaftPieces.MineShaftCorridor.class)
public abstract class MineshaftCorridorMixin {

    //? if >=1.21 {
    /*@ModifyVariable(
        method = "createChest(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/util/RandomSource;IIILnet/minecraft/resources/ResourceKey;)Z",
        at = @At("HEAD"), argsOnly = true
    )
    private ResourceKey<LootTable> abyssalcraft$applyLegacyLoot(ResourceKey<LootTable> lootTable) {
        return StructureLootCompat.remap(lootTable);
    }
    *///?} else {
    @ModifyVariable(
        method = "createChest(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/util/RandomSource;IIILnet/minecraft/resources/ResourceLocation;)Z",
        at = @At("HEAD"), argsOnly = true
    )
    private ResourceLocation abyssalcraft$applyLegacyLoot(ResourceLocation lootTable) {
        return StructureLootCompat.remap(lootTable);
    }
    //?}
}
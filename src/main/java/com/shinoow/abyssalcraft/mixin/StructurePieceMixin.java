package com.shinoow.abyssalcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;
import com.shinoow.abyssalcraft.platform.StructureLootCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.world.structure.LegacyStructurePlacementContext;

//? if >=1.21 {
/*import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}

@Mixin(StructurePiece.class)
public abstract class StructurePieceMixin {

    @ModifyVariable(method = "placeBlock", at = @At("HEAD"), argsOnly = true)
    private BlockState abyssalcraft$applyLegacyPalette(BlockState state) {
        return switch (LegacyStructurePlacementContext.activeId()) {
            case 1 -> dreadlandsMineshaftState(state);
            case 2 -> abyssalStrongholdState(state);
            default -> state;
        };
    }

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

    private static BlockState dreadlandsMineshaftState(BlockState state) {
        if (state.is(Blocks.OAK_PLANKS) || state.is(Blocks.DARK_OAK_PLANKS)) {
            return copyProperties(state, BaseBlocks.DREADWOOD_PLANKS.get());
        }
        if (state.is(Blocks.OAK_FENCE) || state.is(Blocks.DARK_OAK_FENCE)) {
            return copyProperties(state, BaseBlocks.DREADWOOD_FENCE.get());
        }
        return state;
    }

    private static BlockState abyssalStrongholdState(BlockState state) {
        if (state.is(Blocks.CRACKED_STONE_BRICKS) || state.is(Blocks.INFESTED_CRACKED_STONE_BRICKS)) {
            return copyProperties(state, BaseBlocks.CRACKED_ABYSSAL_STONE_BRICK.get());
        }
        if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.INFESTED_STONE_BRICKS) || state.is(Blocks.INFESTED_MOSSY_STONE_BRICKS)
                || state.is(Blocks.INFESTED_CHISELED_STONE_BRICKS)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_STONE_BRICK.get());
        }
        if (state.is(Blocks.STONE_BRICK_SLAB)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_STONE_BRICK_SLAB.get());
        }
        if (state.is(Blocks.STONE_BRICK_STAIRS)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_STONE_BRICK_STAIRS.get());
        }
        if (state.is(Blocks.COBBLESTONE_STAIRS)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_COBBLESTONE_STAIRS.get());
        }
        if (state.is(Blocks.IRON_BARS)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_STONE_BRICK_FENCE.get());
        }
        if (state.is(Blocks.STONE_BUTTON)) {
            return copyProperties(state, BaseBlocks.ABYSSAL_STONE_BUTTON.get());
        }
        if (state.is(Blocks.OAK_DOOR)) {
            return copyProperties(state, BaseBlocks.DARKLANDS_OAK_DOOR.get());
        }
        if (state.is(Blocks.OAK_PLANKS)) {
            return copyProperties(state, BaseBlocks.DARKLANDS_OAK_PLANKS.get());
        }
        if (state.is(Blocks.LAVA)) {
            return copyProperties(state, LiquidCoraliumCompat.BLOCK.get());
        }
        return state;
    }

    private static BlockState copyProperties(BlockState source, Block target) {
        BlockState replacement = target.defaultBlockState();
        for (Property<?> property : source.getProperties()) {
            if (replacement.hasProperty(property)) {
                replacement = copyProperty(source, replacement, property);
            }
        }
        return replacement;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState source,
                                                                       BlockState replacement,
                                                                       Property<T> property) {
        return replacement.setValue(property, source.getValue(property));
    }
}
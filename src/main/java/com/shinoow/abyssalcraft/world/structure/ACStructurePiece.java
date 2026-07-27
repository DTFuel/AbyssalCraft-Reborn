package com.shinoow.abyssalcraft.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import com.shinoow.abyssalcraft.platform.StructureCompat;
import com.shinoow.abyssalcraft.registry.ModWorldgen;

/**
 * The block-placing piece for {@link ACStructure} (owned by PG-5 / Stage G1). Builds a small code
 * structure keyed on {@link StructureKind} (block palette + a loot chest) inside its bounding box.
 * Simplified-faithful (a floor + corner pillars + one chest) -- exact 1.12.2 shapes ride on the
 * deferred {@code .nbt} jigsaw pass. Every placement is gated by {@code boundingBox.isInside} so the
 * piece stitches cleanly across chunk borders (per worldgen reference 07).
 */
public class ACStructurePiece extends StructurePiece {

    private final StructureKind kind;

    public ACStructurePiece(StructureKind kind, BlockPos origin) {
        super(ModWorldgen.AC_PIECE.get(), 0,
            BoundingBox.fromCorners(origin.offset(-3, -1, -3), origin.offset(3, 4, 3)));
        this.kind = kind;
    }

    public ACStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModWorldgen.AC_PIECE.get(), tag);
        this.kind = StructureKind.valueOf(tag.getString("Kind"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Kind", kind.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        int ox = boundingBox.minX() + 3;
        int oy = boundingBox.minY() + 1;
        int oz = boundingBox.minZ() + 3;
        BlockState block = kind.block();

        int radius = kind == StructureKind.DARK_RITUAL_GROUNDS ? 3 : 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                set(level, box, ox + dx, oy, oz + dz, block);
            }
        }
        // Four corner pillars (graveyard tombstones / ruin columns).
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        int pillarHeight = kind == StructureKind.GRAVEYARD ? 2 : 3;
        for (int[] c : corners) {
            for (int h = 1; h <= pillarHeight; h++) {
                set(level, box, ox + c[0], oy + h, oz + c[1], block);
            }
        }
        if (kind == StructureKind.DARK_RITUAL_GROUNDS) {
            set(level, box, ox, oy + 1, oz, BaseBlocks.MONOLITH_STONE.get().defaultBlockState());
            return;
        }
        BlockPos chest = new BlockPos(ox, oy + 1, oz);
        if (box.isInside(chest)) {
            level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
            StructureCompat.setChestLoot(level, chest, kind.lootTable(), random.nextLong());
        }
    }

    private void set(WorldGenLevel level, BoundingBox box, int x, int y, int z, BlockState state) {
        BlockPos p = new BlockPos(x, y, z);
        if (box.isInside(p)) {
            level.setBlock(p, state, 2);
        }
    }
}

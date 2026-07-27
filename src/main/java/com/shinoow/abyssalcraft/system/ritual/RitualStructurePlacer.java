package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.world.structure.LegacyStructureLayout;
import com.shinoow.abyssalcraft.world.structure.StructureKind;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

/** Places an existing converted legacy structure layout immediately for a ritual. */
public final class RitualStructurePlacer {

    private RitualStructurePlacer() {}

    public static int place(ServerLevel level, StructureKind kind, BlockPos origin) {
        List<StructurePiece> pieces = new ArrayList<>();
        LegacyStructureLayout.addPieces(kind, level.getStructureManager(), level.random, origin, pieces::add);
        for (StructurePiece piece : pieces) {
            piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                level.random, piece.getBoundingBox(), new ChunkPos(origin), origin);
        }
        return pieces.size();
    }
}
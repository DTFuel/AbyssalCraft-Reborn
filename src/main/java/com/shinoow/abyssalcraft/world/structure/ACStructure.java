package com.shinoow.abyssalcraft.world.structure;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import com.shinoow.abyssalcraft.registry.ModWorldgen;
import com.shinoow.abyssalcraft.world.WorldgenConfigGate;

/**
 * The single programmatic AbyssalCraft structure (owned by PG-5 / Stage G1). Its {@link StructureKind}
 * field selects the code-built variant; {@link ACStructurePiece} does the block placement. The codec is
 * a version-neutral {@code MapCodec} (the loader {@code StructureType} split is adapted in
 * {@link com.shinoow.abyssalcraft.platform.StructureCompat}).
 */
public class ACStructure extends Structure {

    public static final MapCodec<ACStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            settingsCodec(instance),
            StructureKind.CODEC.fieldOf("kind").forGetter(s -> s.kind)
        ).apply(instance, ACStructure::new));

    private final StructureKind kind;

    public ACStructure(StructureSettings settings, StructureKind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!WorldgenConfigGate.allowsStructure(kind, context.chunkPos().x, context.chunkPos().z,
            context.random()::nextInt)) {
            return Optional.empty();
        }
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z,
            Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos origin = new BlockPos(x, y, z);
        if (kind.usesLegacyTemplate()) {
            return Optional.of(new GenerationStub(origin, builder -> LegacyStructureLayout.addPieces(
                kind, context.structureTemplateManager(), context.random(), origin, builder::addPiece)));
        }
        return Optional.of(new GenerationStub(origin,
            builder -> builder.addPiece(new ACStructurePiece(kind, origin))));
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.AC_STRUCTURE.get();
    }
}

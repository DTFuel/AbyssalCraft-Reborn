package com.shinoow.abyssalcraft.world.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.registry.ModWorldgen;
import com.shinoow.abyssalcraft.world.WorldgenConfigGate;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

/**
 * The single programmatic AbyssalCraft structure (owned by PG-5 / Stage G1). Its {@link StructureKind}
 * field selects the code-built variant; {@link ACStructurePiece} does the block placement. The codec is
 * a version-neutral {@code MapCodec} (the loader {@code StructureType} split is adapted in
 * {@link com.shinoow.abyssalcraft.platform.StructureCompat}).
 */
public class ACStructure extends Structure {

    private static final Set<ResourceKey<Biome>> LEGACY_DARKLANDS_BRANCH = Set.of(
        DarklandsBiomes.DARKLANDS, DarklandsBiomes.FOREST, DarklandsBiomes.PLAINS,
        DarklandsBiomes.HILLS, DarklandsBiomes.MOUNTAINS, DarklandsBiomes.DARK_REALM);
    private static final int LEGACY_SURFACE_RADIUS = 3;
    private record DarklandsPlacement(LegacyStructureLayout.DarklandsVariant variant,
                                      BlockPos origin) {}

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
        if (kind == StructureKind.ABYRUIN) {
            return findAbyruinGenerationPoint(context);
        }
        if (kind == StructureKind.DARK_SHRINE) {
            return findDarkShrineGenerationPoint(context);
        }
        if (kind == StructureKind.DARK_RITUAL_GROUNDS) {
            return findDarklandsCompanionGenerationPoint(context);
        }
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

    private Optional<GenerationStub> findAbyruinGenerationPoint(GenerationContext context) {
        RandomSource random = context.random();
        int x = context.chunkPos().getMinBlockX() + 8 + random.nextInt(16);
        int z = context.chunkPos().getMinBlockZ() + 8 + random.nextInt(16);
        int y = surfaceHeight(context, x, z);
        if (!WorldgenConfigGate.allowsStructure(kind, context.chunkPos().x, context.chunkPos().z,
            random::nextInt)) {
            return Optional.empty();
        }
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(
            QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z),
            context.randomState().sampler());
        if (!context.validBiome().test(biome)) {
            return Optional.empty();
        }
        BlockState surface = surfaceState(context, x, y, z);
        if (!surface.is(BaseBlocks.ABYSSAL_STONE.get())
            && !surface.is(DecoBlocks.FUSED_ABYSSAL_SAND.get())
            && !surface.is(DecoBlocks.ABYSSAL_SAND.get())) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin, builder -> LegacyStructureLayout.addPieces(
            kind, context.structureTemplateManager(), random, origin, builder::addPiece)));
    }

    private Optional<GenerationStub> findDarkShrineGenerationPoint(GenerationContext context) {
        int biomeX = context.chunkPos().getMinBlockX();
        int biomeZ = context.chunkPos().getMinBlockZ();
        int biomeY = surfaceHeight(context, biomeX, biomeZ);
        Holder<Biome> biome = biomeAt(context, biomeX, biomeY, biomeZ);
        if (!context.validBiome().test(biome) || !WorldgenConfigGate.allowsDarklandsStructures()) {
            return Optional.empty();
        }

        RandomSource random = context.random();
        boolean legacyDarklandsBranch = biome.unwrapKey()
            .map(LEGACY_DARKLANDS_BRANCH::contains).orElse(false);
        List<DarklandsPlacement> placements = new ArrayList<>();
        if (legacyDarklandsBranch) {
            BlockState base = legacyDarklandsBase(biome.unwrapKey().orElse(null),
                surfaceState(context, biomeX, biomeY, biomeZ));
            tryLegacyPool(context, random, LegacyStructureLayout.DARKLANDS_SHRINES,
                LEGACY_SURFACE_RADIUS, state -> state.equals(base), placements);
        } else {
            addVanillaDarklandsStructures(context, random, placements);
        }
        return darklandsStub(context, placements);
    }

    private Optional<GenerationStub> findDarklandsCompanionGenerationPoint(GenerationContext context) {
        int biomeX = context.chunkPos().getMinBlockX();
        int biomeZ = context.chunkPos().getMinBlockZ();
        int biomeY = surfaceHeight(context, biomeX, biomeZ);
        Holder<Biome> biome = biomeAt(context, biomeX, biomeY, biomeZ);
        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);
        if (!context.validBiome().test(biome) || !LEGACY_DARKLANDS_BRANCH.contains(biomeKey)
            || !WorldgenConfigGate.allowsDarklandsStructures()) {
            return Optional.empty();
        }

        RandomSource random = context.random();
        BlockState base = legacyDarklandsBase(biomeKey, surfaceState(context, biomeX, biomeY, biomeZ));
        Predicate<BlockState> baseSurface = state -> state.equals(base);
        List<DarklandsPlacement> placements = new ArrayList<>();
        tryLegacyPool(context, random, LegacyStructureLayout.DARKLANDS_RITUAL_GROUNDS,
            3, baseSurface, placements);
        tryLegacyPool(context, random, LegacyStructureLayout.DARKLANDS_HOUSES,
            5, baseSurface, placements);
        tryLegacyPool(context, random, LegacyStructureLayout.DARKLANDS_MISC,
            3, state -> state.is(Blocks.GRASS_BLOCK) || state.is(BaseBlocks.DARKSTONE.get()), placements);
        tryLegacyPool(context, random, LegacyStructureLayout.ALL_DARKLANDS_STRUCTURES,
            3, baseSurface, placements);
        return darklandsStub(context, placements);
    }

    private void addVanillaDarklandsStructures(GenerationContext context, RandomSource random,
                                               List<DarklandsPlacement> placements) {
        Predicate<BlockState> vanillaSurface = state -> state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.SAND) || state.is(Blocks.STONE);
        boolean generated = false;

        BlockPos shrineOrigin = randomSurface(context, random);
        if (WorldgenConfigGate.allowsDarkShrineAttempt(random::nextInt)) {
            generated = tryLegacyPoolAt(context, random, shrineOrigin,
                LegacyStructureLayout.DARKLANDS_SHRINES, 3, vanillaSurface, placements);
        }

        BlockPos ritualOrigin = randomSurface(context, random);
        if (WorldgenConfigGate.allowsDarkRitualGroundsAttempt(random::nextInt)
            && tryLegacyPoolAt(context, random, ritualOrigin,
                LegacyStructureLayout.DARKLANDS_RITUAL_GROUNDS, 3, vanillaSurface, placements)) {
            generated = true;
        }

        if (!generated && WorldgenConfigGate.allowsDarkShrineAttempt(random::nextInt)) {
            tryLegacyVariantAt(context, random, randomSurface(context, random),
                LegacyStructureLayout.DarklandsVariant.DARK_SHRINE, 3, vanillaSurface, placements);
        }
    }

    private static void tryLegacyPool(GenerationContext context, RandomSource random,
                                      List<LegacyStructureLayout.DarklandsVariant> pool,
                                      int radius, Predicate<BlockState> allowedSurface,
                                      List<DarklandsPlacement> placements) {
        tryLegacyPoolAt(context, random, randomSurface(context, random), pool, radius,
            allowedSurface, placements);
    }

    private static boolean tryLegacyPoolAt(GenerationContext context, RandomSource random,
                                           BlockPos origin,
                                           List<LegacyStructureLayout.DarklandsVariant> pool,
                                           int radius, Predicate<BlockState> allowedSurface,
                                           List<DarklandsPlacement> placements) {
        LegacyStructureLayout.DarklandsVariant variant = pool.get(random.nextInt(pool.size()));
        return tryLegacyVariantAt(context, random, origin, variant, radius, allowedSurface, placements);
    }

    private static boolean tryLegacyVariantAt(GenerationContext context, RandomSource random,
                                              BlockPos origin,
                                              LegacyStructureLayout.DarklandsVariant variant,
                                              int radius, Predicate<BlockState> allowedSurface,
                                              List<DarklandsPlacement> placements) {
        if (!hasLegacyFlatSurface(context, origin.getX(), origin.getY(), origin.getZ(),
            radius, allowedSurface) || !WorldgenConfigGate.passesLegacyStructureChance(random::nextFloat)) {
            return false;
        }
        placements.add(new DarklandsPlacement(variant, origin));
        return true;
    }

    private Optional<GenerationStub> darklandsStub(GenerationContext context,
                                                   List<DarklandsPlacement> placements) {
        if (placements.isEmpty()) return Optional.empty();
        List<DarklandsPlacement> snapshot = List.copyOf(placements);
        return Optional.of(new GenerationStub(snapshot.get(0).origin(), builder -> {
            for (DarklandsPlacement placement : snapshot) {
                LegacyStructureLayout.addDarklandsPiece(kind, placement.variant(),
                    context.structureTemplateManager(), placement.origin(), builder::addPiece);
            }
        }));
    }

    private static BlockPos randomSurface(GenerationContext context, RandomSource random) {
        int x = context.chunkPos().getMinBlockX() + 8 + random.nextInt(16);
        int z = context.chunkPos().getMinBlockZ() + 8 + random.nextInt(16);
        return new BlockPos(x, surfaceHeight(context, x, z), z);
    }

    private static BlockState legacyDarklandsBase(ResourceKey<Biome> biome, BlockState sampledSurface) {
        if (DarklandsBiomes.DARK_REALM.equals(biome)) {
            return BaseBlocks.DARKSTONE.get().defaultBlockState();
        }
        if (DarklandsBiomes.DARKLANDS.equals(biome)
            && sampledSurface.is(DecoBlocks.DREADLANDS_GRASS.get())) {
            return DecoBlocks.DREADLANDS_GRASS.get().defaultBlockState();
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static boolean hasLegacyFlatSurface(GenerationContext context, int x, int y, int z,
                                                int radius, Predicate<BlockState> allowedSurface) {
        BlockState center = surfaceState(context, x, y, z);
        if (center.isAir() || !center.getFluidState().isEmpty() || !allowedSurface.test(center)) {
            return false;
        }
        return matchesSurface(context, center, x + radius, y, z)
            && matchesSurface(context, center, x - radius, y, z)
            && matchesSurface(context, center, x, y, z + radius)
            && matchesSurface(context, center, x, y, z - radius);
    }

    private static boolean matchesSurface(GenerationContext context, BlockState center,
                                          int x, int y, int z) {
        return surfaceHeight(context, x, z) == y && surfaceState(context, x, y, z).equals(center);
    }

    private static Holder<Biome> biomeAt(GenerationContext context, int x, int y, int z) {
        return context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
            QuartPos.fromBlock(z), context.randomState().sampler());
    }

    private static int surfaceHeight(GenerationContext context, int x, int z) {
        return context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
            context.heightAccessor(), context.randomState());
    }

    private static BlockState surfaceState(GenerationContext context, int x, int y, int z) {
        return context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState())
            .getBlock(y);
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.AC_STRUCTURE.get();
    }
}

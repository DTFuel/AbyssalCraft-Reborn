package com.shinoow.abyssalcraft.registry;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.DensityFunctionCompat;
import com.shinoow.abyssalcraft.platform.StructureCompat;
import com.shinoow.abyssalcraft.world.density.DarkRealmCavityMask;
import com.shinoow.abyssalcraft.world.density.ConfigurableAmplifiedOffset;
import com.shinoow.abyssalcraft.world.feature.AbyssalWastelandPlantsFeature;
import com.shinoow.abyssalcraft.world.feature.DeadTreeFeature;
import com.shinoow.abyssalcraft.world.feature.CoraliumSwampOreFeature;
import com.shinoow.abyssalcraft.world.feature.ChainsFeature;
import com.shinoow.abyssalcraft.world.feature.MiniPillarFeature;
import com.shinoow.abyssalcraft.world.feature.MonolithFeature;
import com.shinoow.abyssalcraft.world.feature.StalagmiteFeature;
import com.shinoow.abyssalcraft.world.structure.ACStructure;
import com.shinoow.abyssalcraft.world.structure.ACStructurePiece;
import com.shinoow.abyssalcraft.world.structure.LegacyTemplatePiece;

/**
 * Worldgen registration framework (owned by PG-0 / Stage G0).
 *
 * <p>The home for every <em>code-defined</em> worldgen object AbyssalCraft registers: {@code Feature}s
 * (here), and -- when Stage G1 needs them -- programmatic {@code StructureType}/{@code StructurePiece}
 * and custom {@code ChunkGenerator}/{@code BiomeSource}/{@code DensityFunction} codecs. Attached to the
 * MOD bus via {@code ModRegistries.ALL} like every other {@link ModRegistrar}.
 *
 * <p>Stage G0 establishes only what the vertical-slice needs: one example {@link MiniPillarFeature}
 * registered against {@code minecraft:feature} (its value type {@code Feature<?>} is loader/version
 * stable, so no fork). The bulk of the dimensions is data-driven JSON (see
 * {@link com.shinoow.abyssalcraft.world.ACDimensions} and {@code data/abyssalcraft/worldgen/**}); per
 * Research R3 a custom-{@code ChunkGenerator}/{@code BiomeSource} codec (whose registry value type is
 * {@code Codec<..>} on 1.20.1 but {@code MapCodec<..>} on 1.21 -- a fork that would live in
 * {@code platform/}) is only introduced by a G1 dimension task that a pure datapack cannot express.
 */
public final class ModWorldgen {

    private ModWorldgen() {}

    /** {@code minecraft:feature} registrar in the AbyssalCraft namespace (PG-4 extends this). */
    public static final ModRegistrar<Feature<?>> FEATURES =
        ModRegistrar.of(Registries.FEATURE, AbyssalCraft.MODID);

    /** Fixed-seed Dark Realm cavity density function (Codec on 1.20, MapCodec on 1.21). */
    public static final ModRegistrar<?> DENSITY_FUNCTION_TYPES =
        DensityFunctionCompat.register(Map.of(
            "dark_realm_cavity_mask", DarkRealmCavityMask.CODEC,
            "configurable_amplified_offset", ConfigurableAmplifiedOffset.CODEC));

    /** Stage G0 example: a trivial pillar feature proving the code-feature -> JSON -> biome path. */
    public static final Supplier<MiniPillarFeature> MINI_PILLAR =
        FEATURES.register("mini_pillar", () -> new MiniPillarFeature(NoneFeatureConfiguration.CODEC));

    // Stage G1 (PG-4): AbyssalCraft's code-defined features. Each is block-state-configured so a single
    // Feature class serves every variant via its configured_feature JSON (e.g. STALAGMITE -> abyssal_stone
    // / dreadstone / coralium_stone). Trees, lakes and carvers are data-driven (vanilla Feature/Carver
    // types in worldgen/{configured_feature,configured_carver}/**), so they need no code here.

    /** Tapering stone spire ({@code WorldGenAbyssalStalagmite}/{@code WorldGenDreadlandsStalagmite}). */
    public static final Supplier<StalagmiteFeature> STALAGMITE =
        FEATURES.register("stalagmite", () -> new StalagmiteFeature(BlockStateConfiguration.CODEC));
    /** Shoggoth monolith slab ({@code WorldGenShoggothMonolith}). */
    public static final Supplier<MonolithFeature> MONOLITH =
        FEATURES.register("monolith", () -> new MonolithFeature(BlockStateConfiguration.CODEC));
    /** Bare dead tree / snag ({@code WorldGenDeadTree}, Abyssal Swamp). */
    public static final Supplier<DeadTreeFeature> DEAD_TREE =
        FEATURES.register("dead_tree", () -> new DeadTreeFeature(BlockStateConfiguration.CODEC));
    /** Coralium Infested Swamp's two legacy ore passes, executed once per generated chunk. */
    public static final Supplier<CoraliumSwampOreFeature> CORALIUM_SWAMP_ORES =
        FEATURES.register("coralium_swamp_ores", () -> new CoraliumSwampOreFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<AbyssalWastelandPlantsFeature> ABYSSAL_WASTELAND_PLANTS =
        FEATURES.register("abyssal_wasteland_plants", () -> new AbyssalWastelandPlantsFeature(NoneFeatureConfiguration.CODEC));
    public static final Supplier<ChainsFeature> CHAINS =
        FEATURES.register("chains", () -> new ChainsFeature(NoneFeatureConfiguration.CODEC));

    // Stage G1 (PG-5): programmatic StructureType + StructurePiece. A single enum-collapsed ACStructure
    // serves every code-built structure (graveyard / abyruin / dark_shrine) via its `kind`; the sole
    // Codec<->MapCodec fork is isolated in platform/StructureCompat. The 36 1.12.2 .nbt template
    // structures (jigsaw) need binary format conversion + palette remap and are deferred.

    /** {@code minecraft:structure_type} registrar. */
    public static final ModRegistrar<StructureType<?>> STRUCTURE_TYPES =
        ModRegistrar.of(Registries.STRUCTURE_TYPE, AbyssalCraft.MODID);
    /** {@code minecraft:structure_piece} registrar. */
    public static final ModRegistrar<StructurePieceType> STRUCTURE_PIECES =
        ModRegistrar.of(Registries.STRUCTURE_PIECE, AbyssalCraft.MODID);

    /** The single AbyssalCraft programmatic structure type (variant carried by {@code kind}). */
    public static final Supplier<StructureType<ACStructure>> AC_STRUCTURE =
        STRUCTURE_TYPES.register("structure", () -> StructureCompat.structureType(ACStructure.CODEC));
    /** The block-placing piece type for {@link ACStructure}. */
    public static final Supplier<StructurePieceType> AC_PIECE =
        STRUCTURE_PIECES.register("piece", () -> (StructurePieceType) (context, tag) ->
            tag.contains("Kind") && StructureKindUsesTemplate.uses(tag.getString("Kind"))
                ? new LegacyTemplatePiece(context, tag) : new ACStructurePiece(context, tag));

    private static final class StructureKindUsesTemplate {
        private static boolean uses(String name) {
            try {
                return com.shinoow.abyssalcraft.world.structure.StructureKind.valueOf(name).usesLegacyTemplate();
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
    }
}

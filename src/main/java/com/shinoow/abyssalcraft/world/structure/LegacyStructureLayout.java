package com.shinoow.abyssalcraft.world.structure;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import com.shinoow.abyssalcraft.platform.ACRef;

/** Deterministic template layouts reconstructed from the 1.12.2 generators. */
public final class LegacyStructureLayout {

    private static final List<String> CITY = List.of("bar", "blacksmith", "church", "farm", "farmhouse", "house", "library");

    public enum DarklandsVariant {
        HOUSE_1("darklands/house_1", -3, 1, -4),
        HOUSE_2("darklands/house_2", -6, 1, -6),
        SCION_1("darklands/scion_1", -4, 1, -4),
        SCION_2("darklands/scion_2", -3, 1, -3),
        RITUAL_GROUNDS("darklands/ritual_grounds", -6, 1, -6),
        RITUAL_GROUNDS_COLUMNS("darklands/ritual_grounds_columns", -5, 0, -5),
        CIRCULAR_SHRINE("darklands/circular_shrine", -6, -1, -6),
        CIRCULAR_SHRINE_COLUMNS("darklands/circular_shrine_columns", -5, 1, -5),
        ELEVATED_SHRINE("darklands/elevated_shrine", -4, 1, -4),
        ELEVATED_SHRINE_LARGE("darklands/elevated_shrine_large", -5, 1, -5),
        DARK_SHRINE("shrine/dark_shrine", 0, -3, 0);

        private final String template;
        private final int x;
        private final int y;
        private final int z;

        DarklandsVariant(String template, int x, int y, int z) {
            this.template = template;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final List<DarklandsVariant> DARKLANDS_SHRINES = List.of(
        DarklandsVariant.CIRCULAR_SHRINE, DarklandsVariant.CIRCULAR_SHRINE_COLUMNS,
        DarklandsVariant.ELEVATED_SHRINE, DarklandsVariant.ELEVATED_SHRINE_LARGE,
        DarklandsVariant.DARK_SHRINE);
    public static final List<DarklandsVariant> DARKLANDS_RITUAL_GROUNDS = List.of(
        DarklandsVariant.RITUAL_GROUNDS, DarklandsVariant.RITUAL_GROUNDS_COLUMNS);
    public static final List<DarklandsVariant> DARKLANDS_HOUSES = List.of(
        DarklandsVariant.HOUSE_1, DarklandsVariant.HOUSE_2);
    public static final List<DarklandsVariant> DARKLANDS_MISC = List.of(
        DarklandsVariant.SCION_1, DarklandsVariant.SCION_2);
    public static final List<DarklandsVariant> ALL_DARKLANDS_STRUCTURES = List.of(
        DarklandsVariant.HOUSE_1, DarklandsVariant.HOUSE_2,
        DarklandsVariant.SCION_1, DarklandsVariant.SCION_2,
        DarklandsVariant.RITUAL_GROUNDS, DarklandsVariant.RITUAL_GROUNDS_COLUMNS,
        DarklandsVariant.CIRCULAR_SHRINE, DarklandsVariant.CIRCULAR_SHRINE_COLUMNS,
        DarklandsVariant.ELEVATED_SHRINE, DarklandsVariant.ELEVATED_SHRINE_LARGE,
        DarklandsVariant.DARK_SHRINE);

    private LegacyStructureLayout() {}

    public static void addPieces(StructureKind kind, StructureTemplateManager manager, RandomSource random,
                                 BlockPos origin, Consumer<StructurePiece> output) {
        switch (kind) {
            case GRAVEYARD, ABYRUIN, DARK_SHRINE, SHOGGOTH_PIT, SHOGGOTH_PIT_RIVER ->
                output.accept(LegacyTemplatePiece.create(kind, manager, random, origin));
            case OMOTHOL_CITY -> add(output, kind, manager, template("omothol/" + CITY.get(random.nextInt(CITY.size()))),
                Rotation.getRandom(random), origin);
            case OMOTHOL_TEMPLE -> add(output, kind, manager, template("omothol/temple"), Rotation.getRandom(random), origin);
            case OMOTHOL_TOWER -> {
                BlockPos base = origin.offset(-6, 1, -6);
                add(output, kind, manager, template("omothol/tower_1"), Rotation.NONE, base);
                add(output, kind, manager, template("omothol/tower_2"), Rotation.NONE, base.above(32));
            }
            case OMOTHOL_STORAGE -> {
                Rotation rotation = Rotation.getRandom(random);
                add(output, kind, manager, template("omothol/storage"), rotation, origin);
                add(output, kind, manager, template("omothol/crates_" + (random.nextInt(4) + 1)), rotation,
                    rotatedOffset(origin, rotation, 4, 0, 4));
            }
            case ETHAXIUM_HOUSE -> add(output, kind, manager, template("omothol/ethaxium_house"),
                Rotation.NONE, origin.offset(-9, 0, -23));
            case CHAGAROTH_LAIR -> {
                add(output, kind, manager, template("chagarothlair/chagarothlair_top"), Rotation.NONE, origin.offset(-6, -5, -23));
                add(output, kind, manager, template("chagarothlair/chagarothlair_front"), Rotation.NONE, origin.offset(-8, -11, -39));
                add(output, kind, manager, template("chagarothlair/chagarothlair_middle"), Rotation.NONE, origin.offset(-8, -26, -71));
                add(output, kind, manager, template("chagarothlair/chagarothlair_middle_left"), Rotation.NONE, origin.offset(-24, -8, -58));
                add(output, kind, manager, template("chagarothlair/chagarothlair_middle_right"), Rotation.NONE, origin.offset(9, -8, -58));
                add(output, kind, manager, template("chagarothlair/chagarothlair_back"), Rotation.NONE, origin.offset(-9, -27, -101));
                add(output, kind, manager, template("chagarothlair/chagarothlair_entrance"), Rotation.NONE, origin.offset(-6, -11, -13));
            }
            case JZAHAR_TEMPLE -> {
                BlockPos base = origin.offset(-4, -2, -1);
                add(output, kind, manager, template("temple/jzahartemple_front_right"), Rotation.NONE, base.west(32));
                add(output, kind, manager, template("temple/jzahartemple_front_middle"), Rotation.NONE, base);
                add(output, kind, manager, template("temple/jzahartemple_front_left"), Rotation.NONE, base.east(32));
                BlockPos middle = base.south(32);
                add(output, kind, manager, template("temple/jzahartemple_middle_right"), Rotation.NONE, middle.west(32));
                add(output, kind, manager, template("temple/jzahartemple_middle_middle"), Rotation.NONE, middle);
                add(output, kind, manager, template("temple/jzahartemple_middle_left"), Rotation.NONE, middle.east(32));
                add(output, kind, manager, template("temple/jzahartemple_back"), Rotation.NONE, middle.south(32).west(9));
            }
            case DARK_RITUAL_GROUNDS ->
                throw new IllegalArgumentException(kind.getSerializedName() + " is not template-backed");
        }
    }

    public static void addDarklandsPiece(StructureKind owner, DarklandsVariant variant,
                                         StructureTemplateManager manager, BlockPos origin,
                                         Consumer<StructurePiece> output) {
        add(output, owner, manager, template(variant.template), Rotation.NONE,
            origin.offset(variant.x, variant.y, variant.z));
    }

    private static void add(Consumer<StructurePiece> output, StructureKind kind, StructureTemplateManager manager,
                            ResourceLocation template, Rotation rotation, BlockPos position) {
        output.accept(LegacyTemplatePiece.of(kind, manager, template, rotation, position));
    }

    private static ResourceLocation template(String path) {
        return ACRef.id("legacy/" + path);
    }

    private static BlockPos rotatedOffset(BlockPos origin, Rotation rotation, int x, int y, int z) {
        return switch (rotation) {
            case NONE -> origin.offset(x, y, z);
            case CLOCKWISE_90 -> origin.offset(-z, y, x);
            case CLOCKWISE_180 -> origin.offset(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> origin.offset(z, y, -x);
        };
    }
}
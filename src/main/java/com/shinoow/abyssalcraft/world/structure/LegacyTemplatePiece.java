package com.shinoow.abyssalcraft.world.structure;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.WorldGenLevel;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.IdolOfFadingBlockEntity;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.boss.RemnantMob;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.platform.StructureCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.registry.ModWorldgen;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

/** Template-backed piece for converted 1.12.2 structures. */
public final class LegacyTemplatePiece extends TemplateStructurePiece {

    private static final List<java.util.function.Supplier<Block>> STATUES = List.of(
        DecoBlocks.DECORATIVE_CTHULHU_STATUE, DecoBlocks.DECORATIVE_HASTUR_STATUE,
        DecoBlocks.DECORATIVE_JZAHAR_STATUE, DecoBlocks.DECORATIVE_AZATHOTH_STATUE,
        DecoBlocks.DECORATIVE_NYARLATHOTEP_STATUE, DecoBlocks.DECORATIVE_YOG_SOTHOTH_STATUE,
        DecoBlocks.DECORATIVE_SHUB_NIGGURATH_STATUE);
    private static final List<java.util.function.Supplier<Block>> FUNCTIONAL_STATUES = EnergyBlocks.DEITY_STATUES;
    private static final Set<net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>
        DREADLANDS_BIOMES = Set.of(DarklandsBiomes.DREADLANDS, DarklandsBiomes.DREADLANDS_FOREST,
            DarklandsBiomes.DREADLANDS_MOUNTAINS, DarklandsBiomes.DREADLANDS_OCEAN);

    private final StructureKind kind;
    private final ResourceLocation template;
    private final Rotation rotation;
    private boolean storageTreasurePlaced;

    private LegacyTemplatePiece(StructureKind kind, StructureTemplateManager manager,
                                ResourceLocation template, Rotation rotation, BlockPos position) {
        super(ModWorldgen.AC_PIECE.get(), 0, manager, template, template.toString(),
            settings(rotation), position);
        this.kind = kind;
        this.template = template;
        this.rotation = rotation;
    }

    public LegacyTemplatePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModWorldgen.AC_PIECE.get(), tag, context.structureTemplateManager(),
            ignored -> settings(Rotation.valueOf(tag.getString("Rotation"))));
        this.kind = StructureKind.valueOf(tag.getString("Kind"));
        this.template = tag.contains("LegacyTemplate")
            ? ACRef.parse(tag.getString("LegacyTemplate")) : fallbackTemplate(kind);
        this.rotation = Rotation.valueOf(tag.getString("Rotation"));
        this.storageTreasurePlaced = tag.getBoolean("StorageTreasurePlaced");
    }

    public static LegacyTemplatePiece create(StructureKind kind, StructureTemplateManager manager,
                                             RandomSource random, BlockPos origin) {
        Rotation rotation = kind == StructureKind.SHOGGOTH_PIT || kind == StructureKind.SHOGGOTH_PIT_RIVER
            ? Rotation.getRandom(random) : Rotation.NONE;
        ResourceLocation template;
        BlockPos position = origin;
        if (kind == StructureKind.GRAVEYARD) {
            String size = switch (random.nextInt(3)) { case 0 -> "small"; case 1 -> "medium"; default -> "large"; };
            template = ACRef.id("legacy/graveyard/graveyard_" + size);
            position = position.below();
        } else if (kind == StructureKind.SHOGGOTH_PIT || kind == StructureKind.SHOGGOTH_PIT_RIVER) {
            int variant = random.nextInt(3) + 1;
            template = ACRef.id("legacy/shoggothlair/shoggothlair_" + variant);
            position = position.offset(-6, -9, variant == 1 ? -27 : variant == 2 ? -21 : -19);
        } else {
            template = ACRef.id("legacy/shrine/dark_shrine");
            position = position.below(3);
        }
        return new LegacyTemplatePiece(kind, manager, template, rotation, position);
    }

    public static LegacyTemplatePiece of(StructureKind kind, StructureTemplateManager manager,
                                         ResourceLocation template, Rotation rotation, BlockPos position) {
        return new LegacyTemplatePiece(kind, manager, template, rotation, position);
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Kind", kind.name());
        tag.putString("LegacyTemplate", template.toString());
        tag.putString("Rotation", rotation.name());
        tag.putBoolean("StorageTreasurePlaced", storageTreasurePlaced);
    }

    @Override
    protected void handleDataMarker(String metadata, BlockPos pos, ServerLevelAccessor level,
                                    RandomSource random, BoundingBox box) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        if (metadata.equals("tombstone")) {
            level.setBlock(pos.below(), randomTombstone(level.getLevel(), random), 2);
        } else if (metadata.equals("tree")) {
            if (random.nextInt(3) == 0) {
                placeLegacyTree(level, pos, random);
            } else {
                level.setBlock(pos.below(), graveyardTopBlock(level.getLevel()), 2);
            }
        } else if (metadata.startsWith("spawn:")) {
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        } else if (metadata.equals("remnant")) {
            var created = BossEntities.REMNANT.get().create(level.getLevel());
            if (!(created instanceof RemnantMob remnant)) {
                throw new IllegalStateException("Remnant marker created an invalid entity at " + pos);
            }
            remnant.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                random.nextFloat() * 360.0F, 0.0F);
            MobSpawnCompat.finalizeTriggeredSpawn(level.getLevel(), remnant);
            remnant.setProfession(remnantProfession(random));
            remnant.setPersistenceRequired();
            if (!level.addFreshEntity(remnant)) {
                throw new IllegalStateException("Remnant marker could not add its entity at " + pos);
            }
        } else if (metadata.equals("treasure")) {
            if (random.nextBoolean()) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
                StructureCompat.setChestLoot(level.getLevel(), pos, kind.lootTable(), random.nextLong());
            }
        } else if (metadata.equals("chest")) {
            if (kind == StructureKind.OMOTHOL_CITY) {
                placeCrate(level, pos, omotholCityLoot(), random);
            } else if (random.nextBoolean()) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
                StructureCompat.setChestLoot(level.getLevel(), pos, kind.lootTable(), random.nextLong());
            }
        } else if (metadata.startsWith("crate")) {
            if (kind == StructureKind.OMOTHOL_STORAGE) {
                boolean treasure = !storageTreasurePlaced && random.nextInt(5) == 0;
                placeCrate(level, pos, ACRef.id("chests/omothol/storage_" + (treasure ? "treasure" : "junk")), random);
                storageTreasurePlaced |= treasure;
            } else {
                placeCrate(level, pos, kind.lootTable(), random);
            }
        } else if (metadata.equals("pedestal")) {
            if (kind == StructureKind.OMOTHOL_CITY) {
                level.setBlock(pos, EnergyBlocks.ENERGY_PEDESTALS.get(random.nextInt(4)).get().defaultBlockState(), 2);
            } else {
                level.setBlock(pos, RendingPedestals.RENDING_PEDESTAL.get().defaultBlockState(), 2);
            }
        } else if (metadata.startsWith("statue")) {
            if (random.nextInt(10) < 6) {
                level.setBlock(pos, BaseBlocks.MONOLITH_STONE.get().defaultBlockState(), 2);
            } else {
                List<java.util.function.Supplier<Block>> statues = ACConfig.generateStatuesInLairs.get()
                    && random.nextInt(5) == 0 ? FUNCTIONAL_STATUES : STATUES;
                level.setBlock(pos, facing(statues.get(random.nextInt(statues.size())).get().defaultBlockState(),
                    statueFacing(metadata)), 2);
            }
        } else if (metadata.startsWith("bm") || metadata.equals("shoggoth_biomass")
            || metadata.startsWith("replacement:shoggoth_biomass")) {
            level.setBlock(pos, ShoggothBlocks.SHOGGOTH_BIOMASS.get().defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBiomassBlockEntity biomass) {
                biomass.setInitialCooldown(random.nextInt(100));
            }
        } else if (metadata.equals("shoggoth_ooze") || metadata.startsWith("replacement:shoggoth_ooze")) {
            level.setBlock(pos, ShoggothBlocks.SHOGGOTH_OOZE.get().defaultBlockState(), 2);
        } else if (metadata.equals("idol")) {
            level.setBlock(pos, EnergyBlocks.IDOL_OF_FADING.get().defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof IdolOfFadingBlockEntity idol) {
                idol.setEnergy(idol.getMaxEnergy());
            } else {
                throw new IllegalStateException("Idol marker did not create its energy host at " + pos);
            }
        } else if (metadata.startsWith("sealing_lock:") || metadata.startsWith("replacement:sealing_lock")) {
            level.setBlock(pos, StructureContent.SEALING_LOCK.get().defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.structure.SealingLockBlockEntity lock) {
                lock.configureMarker(metadata);
            } else {
                throw new IllegalStateException("Sealing lock marker did not create its host at " + pos);
            }
        } else if (metadata.startsWith("crystal")) {
            if (random.nextBoolean()) {
                BlockState crystal = random.nextInt(100) == 0
                    ? RitualBlocks.DREADLANDS_INFUSED_POWERSTONE.get().defaultBlockState()
                    : CrystalClusterBlocks.CLUSTERS.get(random.nextInt(CrystalClusterBlocks.CLUSTERS.size()))
                        .get().defaultBlockState();
                level.setBlock(pos, crystal, 2);
            }
        } else if (metadata.startsWith("replacement:")) {
            AbyssalCraft.LOGGER.debug("Using stable structure replacement for {} at {}", metadata, pos);
            level.setBlock(pos, kind.block(), 2);
        }
    }

    private void placeCrate(ServerLevelAccessor level, BlockPos pos, ResourceLocation loot, RandomSource random) {
        level.setBlock(pos, StructureContent.CRATE.get().defaultBlockState(), 2);
        if (!(level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.structure.CrateBlockEntity)) {
            throw new IllegalStateException("Crate marker did not create its loot host at " + pos);
        }
        StructureCompat.setChestLoot(level.getLevel(), pos, loot, random.nextLong());
    }

    private int remnantProfession(RandomSource random) {
        return switch (template.getPath()) {
            case "legacy/omothol/bar" -> random.nextBoolean() ? 4 : 0;
            case "legacy/omothol/blacksmith" -> random.nextInt(3) == 0 ? 6 : 3;
            case "legacy/omothol/church" -> 2;
            case "legacy/omothol/farmhouse" -> 4;
            case "legacy/omothol/house" -> random.nextBoolean() ? 5 : 0;
            case "legacy/omothol/library" -> 1;
            default -> 0;
        };
    }

    private ResourceLocation omotholCityLoot() {
        return switch (template.getPath()) {
            case "legacy/omothol/blacksmith" -> ACRef.id("chests/omothol/blacksmith");
            case "legacy/omothol/farmhouse" -> ACRef.id("chests/omothol/farmhouse");
            case "legacy/omothol/library" -> ACRef.id("chests/omothol/library");
            default -> ACRef.id("chests/omothol/house");
        };
    }

    private Direction statueFacing(String metadata) {
        Direction facing = switch (template.getPath()) {
            case "legacy/shoggothlair/shoggothlair_1" -> metadata.matches("statue[1-4]")
                ? Direction.EAST : Direction.WEST;
            case "legacy/shoggothlair/shoggothlair_2" -> switch (metadata) {
                case "statue1" -> Direction.EAST;
                case "statue2" -> Direction.SOUTH;
                default -> Direction.WEST;
            };
            default -> Direction.SOUTH;
        };
        return rotation.rotate(facing);
    }

    private static BlockState randomTombstone(ServerLevel level, RandomSource random) {
        if (level.dimension() == ACDimensions.ABYSSAL_WASTELAND) {
            return facing((random.nextBoolean() ? DecoBlocks.TOMBSTONE_CORALIUM_STONE
                : DecoBlocks.TOMBSTONE_ABYSSAL_STONE).get().defaultBlockState(), Direction.NORTH);
        }
        if (level.dimension() == ACDimensions.DREADLANDS) {
            return facing((random.nextBoolean() ? DecoBlocks.TOMBSTONE_ELYSIAN_STONE
                : DecoBlocks.TOMBSTONE_DREADSTONE).get().defaultBlockState(), Direction.NORTH);
        }
        if (level.dimension() == ACDimensions.OMOTHOL) {
            return facing((random.nextBoolean() ? DecoBlocks.TOMBSTONE_ETHAXIUM
                : DecoBlocks.TOMBSTONE_OMOTHOL_STONE).get().defaultBlockState(), Direction.NORTH);
        }
        return facing((random.nextInt(10) == 0 ? DecoBlocks.TOMBSTONE_DARKSTONE
            : DecoBlocks.TOMBSTONE_STONE).get().defaultBlockState(), Direction.NORTH);
    }

    private static BlockState graveyardTopBlock(ServerLevel level) {
        if (level.dimension() == ACDimensions.ABYSSAL_WASTELAND) return DecoBlocks.FUSED_ABYSSAL_SAND.get().defaultBlockState();
        if (level.dimension() == ACDimensions.DREADLANDS) return DecoBlocks.DREADLANDS_GRASS.get().defaultBlockState();
        if (level.dimension() == ACDimensions.OMOTHOL) return BaseBlocks.OMOTHOL_STONE.get().defaultBlockState();
        if (level.dimension() == ACDimensions.DARK_REALM) return BaseBlocks.DARKSTONE.get().defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static void placeLegacyTree(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (!(level instanceof WorldGenLevel worldGenLevel)) return;
        if (level.getLevel().dimension() == ACDimensions.ABYSSAL_WASTELAND) {
            ModWorldgen.DEAD_TREE.get().placeFixed(worldGenLevel, pos, random,
                BaseBlocks.DEAD_TREE_LOG.get().defaultBlockState());
            return;
        }
        boolean dreadwood = level.getLevel().dimension() == ACDimensions.DREADLANDS
            && level.getBiome(pos).unwrapKey().map(DREADLANDS_BIOMES::contains).orElse(false);
        placeFixedBranchedTree(level, pos, random, dreadwood);
    }

    private static boolean placeFixedBranchedTree(ServerLevelAccessor level, BlockPos origin,
                                                  RandomSource random, boolean dreadwood) {
        BlockState soil = level.getBlockState(origin.below());
        boolean validSoil = soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK)
            || soil.is(DecoBlocks.DREADLANDS_DIRT.get()) || soil.is(DecoBlocks.DREADLANDS_GRASS.get())
            || dreadwood && soil.is(BaseBlocks.DREADSTONE.get());
        if (!validSoil || origin.getY() >= level.getMaxBuildHeight() - 7) return false;

        BlockState log = (dreadwood ? BaseBlocks.DREADWOOD_LOG : BaseBlocks.DARKLANDS_OAK_LOG)
            .get().defaultBlockState();
        BlockState leaves = (dreadwood ? BaseBlocks.DREADWOOD_LEAVES : BaseBlocks.DARKLANDS_OAK_LEAVES)
            .get().defaultBlockState();
        BlockState dirt = level.getLevel().dimension() == ACDimensions.DREADLANDS
            ? DecoBlocks.DREADLANDS_DIRT.get().defaultBlockState() : Blocks.DIRT.defaultBlockState();
        level.setBlock(origin.below(), dirt, 2);

        for (int y = 0; y < 6; y++) level.setBlock(origin.above(y), withAxis(log, Direction.Axis.Y), 2);
        level.setBlock(origin.above(6), leaves, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.setBlock(origin.relative(direction), withAxis(log, direction.getAxis()), 2);
        }

        int crownVariance = random.nextInt(3);
        int angle = random.nextInt(60);
        for (int branch = 0; branch < 6; branch++) {
            double distance = 0.0D;
            double branchHeight = 4.0D - random.nextFloat() * crownVariance;
            angle += 60;
            double xDirection = Math.cos(Math.toRadians(angle));
            double zDirection = Math.sin(Math.toRadians(angle));
            Direction.Axis axis = Math.abs(xDirection) >= Math.abs(zDirection)
                ? Direction.Axis.X : Direction.Axis.Z;
            while (distance < 6.0D) {
                distance += 1.0D;
                branchHeight += 0.5D;
                BlockPos branchPos = origin.offset((int) (distance * xDirection), (int) branchHeight,
                    (int) (distance * zDirection));
                level.setBlock(branchPos, withAxis(log, axis), 2);
                if (level.isEmptyBlock(branchPos.above())) level.setBlock(branchPos.above(), leaves, 2);
            }
        }
        return true;
    }

    private static ResourceLocation fallbackTemplate(StructureKind kind) {
        return switch (kind) {
            case GRAVEYARD -> ACRef.id("legacy/graveyard/graveyard_small");
            case SHOGGOTH_PIT, SHOGGOTH_PIT_RIVER -> ACRef.id("legacy/shoggothlair/shoggothlair_1");
            default -> ACRef.id("legacy/shrine/dark_shrine");
        };
    }

    private static BlockState facing(BlockState state, Direction direction) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING)
            ? state.setValue(HorizontalDirectionalBlock.FACING, direction) : state;
    }

    private static BlockState withAxis(BlockState state, Direction.Axis axis) {
        return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)
            ? state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, axis)
            : state;
    }
}
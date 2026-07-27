package com.shinoow.abyssalcraft.world.structure;

import java.util.List;

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

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.StructureCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.registry.ModWorldgen;

/** Template-backed piece for converted 1.12.2 structures. */
public final class LegacyTemplatePiece extends TemplateStructurePiece {

    private static final List<java.util.function.Supplier<Block>> STATUES = List.of(
        DecoBlocks.DECORATIVE_CTHULHU_STATUE, DecoBlocks.DECORATIVE_HASTUR_STATUE,
        DecoBlocks.DECORATIVE_JZAHAR_STATUE, DecoBlocks.DECORATIVE_AZATHOTH_STATUE,
        DecoBlocks.DECORATIVE_NYARLATHOTEP_STATUE, DecoBlocks.DECORATIVE_YOG_SOTHOTH_STATUE,
        DecoBlocks.DECORATIVE_SHUB_NIGGURATH_STATUE);
    private static final List<java.util.function.Supplier<Block>> FUNCTIONAL_STATUES = EnergyBlocks.DEITY_STATUES;

    private final StructureKind kind;
    private final Rotation rotation;

    private LegacyTemplatePiece(StructureKind kind, StructureTemplateManager manager,
                                ResourceLocation template, Rotation rotation, BlockPos position) {
        super(ModWorldgen.AC_PIECE.get(), 0, manager, template, template.toString(),
            settings(rotation), position);
        this.kind = kind;
        this.rotation = rotation;
    }

    public LegacyTemplatePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModWorldgen.AC_PIECE.get(), tag, context.structureTemplateManager(),
            ignored -> settings(Rotation.valueOf(tag.getString("Rotation"))));
        this.kind = StructureKind.valueOf(tag.getString("Kind"));
        this.rotation = Rotation.valueOf(tag.getString("Rotation"));
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
        tag.putString("Rotation", rotation.name());
    }

    @Override
    protected void handleDataMarker(String metadata, BlockPos pos, ServerLevelAccessor level,
                                    RandomSource random, BoundingBox box) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        if (metadata.equals("tombstone")) {
            level.setBlock(pos.below(), randomTombstone(random, Direction.NORTH), 2);
        } else if (metadata.equals("tree")) {
            if (random.nextInt(3) == 0) {
                level.getLevel().registryAccess();
                level.setBlock(pos.below(), BaseBlocks.DEAD_TREE_LOG.get().defaultBlockState(), 2);
            }
        } else if (metadata.startsWith("spawn:")) {
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        } else if (metadata.equals("treasure") || metadata.equals("chest")) {
            if (random.nextBoolean()) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
                StructureCompat.setChestLoot(level.getLevel(), pos, kind.lootTable(), random.nextLong());
            }
        } else if (metadata.startsWith("crate")) {
            level.setBlock(pos, StructureContent.CRATE.get().defaultBlockState(), 2);
            if (!(level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.structure.CrateBlockEntity)) {
                throw new IllegalStateException("Crate marker did not create its loot host at " + pos);
            }
            StructureCompat.setChestLoot(level.getLevel(), pos, kind.lootTable(), random.nextLong());
        } else if (metadata.equals("pedestal")) {
            level.setBlock(pos, RendingPedestals.RENDING_PEDESTAL.get().defaultBlockState(), 2);
        } else if (metadata.startsWith("statue")) {
            if (random.nextInt(10) < 6) {
                level.setBlock(pos, BaseBlocks.MONOLITH_STONE.get().defaultBlockState(), 2);
            } else {
                List<java.util.function.Supplier<Block>> statues = ACConfig.generateStatuesInLairs.get()
                    && random.nextInt(5) == 0 ? FUNCTIONAL_STATUES : STATUES;
                level.setBlock(pos, facing(statues.get(random.nextInt(statues.size())).get().defaultBlockState(),
                    Direction.SOUTH), 2);
            }
        } else if (metadata.startsWith("bm") || metadata.equals("shoggoth_biomass")
            || metadata.startsWith("replacement:shoggoth_biomass")) {
            level.setBlock(pos, ShoggothBlocks.SHOGGOTH_BIOMASS.get().defaultBlockState(), 2);
        } else if (metadata.equals("shoggoth_ooze") || metadata.startsWith("replacement:shoggoth_ooze")) {
            level.setBlock(pos, ShoggothBlocks.SHOGGOTH_OOZE.get().defaultBlockState(), 2);
        } else if (metadata.equals("idol") || metadata.startsWith("sealing_lock:")
            || metadata.startsWith("replacement:sealing_lock")) {
            level.setBlock(pos, StructureContent.SEALING_LOCK.get().defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.structure.SealingLockBlockEntity lock) {
                lock.configureMarker(metadata);
            } else {
                throw new IllegalStateException("Sealing lock marker did not create its host at " + pos);
            }
        } else if (metadata.startsWith("crystal")) {
            level.setBlock(pos, CrystalClusterBlocks.CLUSTERS.get(random.nextInt(CrystalClusterBlocks.CLUSTERS.size()))
                .get().defaultBlockState(), 2);
        } else if (metadata.startsWith("replacement:")) {
            AbyssalCraft.LOGGER.debug("Using stable structure replacement for {} at {}", metadata, pos);
            level.setBlock(pos, kind.block(), 2);
        }
    }

    private static BlockState randomTombstone(RandomSource random, Direction facing) {
        List<java.util.function.Supplier<Block>> tombstones = List.of(
            DecoBlocks.TOMBSTONE_STONE, DecoBlocks.TOMBSTONE_ABYSSAL_STONE,
            DecoBlocks.TOMBSTONE_CORALIUM_STONE, DecoBlocks.TOMBSTONE_DARKSTONE,
            DecoBlocks.TOMBSTONE_DREADSTONE, DecoBlocks.TOMBSTONE_ELYSIAN_STONE,
            DecoBlocks.TOMBSTONE_ETHAXIUM, DecoBlocks.TOMBSTONE_MONOLITH_STONE,
            DecoBlocks.TOMBSTONE_OMOTHOL_STONE);
        return facing(tombstones.get(random.nextInt(tombstones.size())).get().defaultBlockState(), facing);
    }

    private static BlockState facing(BlockState state, Direction direction) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING)
            ? state.setValue(HorizontalDirectionalBlock.FACING, direction) : state;
    }
}
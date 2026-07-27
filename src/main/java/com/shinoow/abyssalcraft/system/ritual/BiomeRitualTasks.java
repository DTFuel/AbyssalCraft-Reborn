package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.BiomeMutationCompat;
import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;
import com.shinoow.abyssalcraft.platform.SavedDataCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.content.block.ore.OreBlocks;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/** Persistent, bounded-work queue for the five large biome rituals. */
public final class BiomeRitualTasks extends SavedDataCompat {

    private static final String DATA_NAME = "abyssalcraft_biome_rituals";
    private static final int CHUNKS_PER_LEVEL_TICK = 1;
    private static final Set<ResourceKey<Biome>> DARKLANDS = Set.of(
        DarklandsBiomes.DARKLANDS, DarklandsBiomes.PLAINS, DarklandsBiomes.FOREST,
        DarklandsBiomes.HILLS, DarklandsBiomes.MOUNTAINS);
    private static final Set<ResourceKey<Biome>> DREADLANDS = Set.of(
        DarklandsBiomes.DREADLANDS, DarklandsBiomes.DREADLANDS_FOREST,
        DarklandsBiomes.DREADLANDS_MOUNTAINS, DarklandsBiomes.DREADLANDS_OCEAN);
    private static final Set<ResourceKey<Biome>> PLAINS = Set.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
    private static final Set<ResourceKey<Biome>> FORESTS = Set.of(
        Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST,
        Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);
    private static final Set<ResourceKey<Biome>> HILLS = Set.of(
        Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_GRAVELLY_HILLS,
        Biomes.MEADOW, Biomes.STONY_PEAKS);
    private static final Set<ResourceKey<Biome>> SNOWY_MOUNTAINS = Set.of(
        Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.GROVE);

    private final List<Task> tasks = new ArrayList<>();

    public static BiomeRitualTasks get(ServerLevel level) {
        return SavedDataCompat.getOrCreate(level, DATA_NAME, BiomeRitualTasks::new, BiomeRitualTasks::load);
    }

    public static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) get(level).tick(level);
    }

    public void enqueue(Kind kind, BlockPos center, int configuredRange, ResourceKey<Biome> replacement) {
        int blockRadius = configuredRange * 8;
        int minChunkX = Math.floorDiv(center.getX() - blockRadius, 16);
        int maxChunkX = Math.floorDiv(center.getX() + blockRadius, 16);
        int minChunkZ = Math.floorDiv(center.getZ() - blockRadius, 16);
        int maxChunkZ = Math.floorDiv(center.getZ() + blockRadius, 16);
        tasks.add(new Task(kind, minChunkX, maxChunkX, minChunkZ, maxChunkZ,
            minChunkX, minChunkZ, replacement, center.getY(), levelVariant(center)));
        setDirty();
    }

    public int size() {
        return tasks.size();
    }

    public static int chunkRadius(int configuredRange) {
        return (configuredRange * 8 + 15) / 16;
    }

    public static boolean canStart(Kind kind, ServerLevel level, BlockPos center) {
        if ((kind == Kind.CURING || kind == Kind.PURGING) && level.dimension() == ACDimensions.DREADLANDS) {
            return false;
        }
        return scanNearby(level, center, key -> applies(kind, key));
    }

    public static ResourceKey<Biome> curingReplacement(ServerLevel level, BlockPos center) {
        for (int x = center.getX() - 24; x <= center.getX() + 24; x += 4) {
            for (int z = center.getZ() - 24; z <= center.getZ() + 24; z += 4) {
                ResourceKey<Biome> key = level.getBiome(new BlockPos(x, center.getY(), z)).unwrapKey().orElse(null);
                if (key != null && !DREADLANDS.contains(key) && key != DarklandsBiomes.PURGED) return key;
            }
        }
        return null;
    }

    private void tick(ServerLevel level) {
        int processed = 0;
        for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext() && processed < CHUNKS_PER_LEVEL_TICK;) {
            Task task = iterator.next();
            task.process(level);
            processed++;
            if (task.done()) iterator.remove();
            setDirty();
        }
    }

    private static boolean scanNearby(ServerLevel level, BlockPos center,
                                      java.util.function.Predicate<ResourceKey<Biome>> predicate) {
        for (int x = center.getX() - 24; x <= center.getX() + 24; x += 4) {
            for (int z = center.getZ() - 24; z <= center.getZ() + 24; z += 4) {
                ResourceKey<Biome> key = level.getBiome(new BlockPos(x, center.getY(), z)).unwrapKey().orElse(null);
                if (key != null && predicate.test(key)) return true;
            }
        }
        return false;
    }

    private static boolean applies(Kind kind, ResourceKey<Biome> key) {
        return switch (kind) {
            case CLEANSING -> DARKLANDS.contains(key);
            case CORRUPTION -> PLAINS.contains(key) || FORESTS.contains(key)
                || HILLS.contains(key) || SNOWY_MOUNTAINS.contains(key);
            case INFESTING -> key == Biomes.SWAMP || key == Biomes.MANGROVE_SWAMP;
            case CURING, PURGING -> DREADLANDS.contains(key);
        };
    }

    private static ResourceKey<Biome> target(Task task, ResourceKey<Biome> key) {
        return switch (task.kind) {
            case CLEANSING -> key == DarklandsBiomes.FOREST ? Biomes.FOREST
                : key == DarklandsBiomes.HILLS ? Biomes.WINDSWEPT_HILLS
                : key == DarklandsBiomes.MOUNTAINS ? Biomes.SNOWY_SLOPES : Biomes.PLAINS;
            case CORRUPTION -> FORESTS.contains(key) ? DarklandsBiomes.FOREST
                : SNOWY_MOUNTAINS.contains(key) ? DarklandsBiomes.MOUNTAINS
                : HILLS.contains(key) ? (task.variant ? DarklandsBiomes.MOUNTAINS : DarklandsBiomes.HILLS)
                : task.variant ? DarklandsBiomes.PLAINS : DarklandsBiomes.DARKLANDS;
            case INFESTING -> DarklandsBiomes.CORALIUM_INFESTED_SWAMP;
            case CURING -> task.replacement;
            case PURGING -> DarklandsBiomes.PURGED;
        };
    }

    private static boolean levelVariant(BlockPos center) {
        long hash = center.asLong() * 0x9E3779B97F4A7C15L;
        return (hash & 1L) == 0;
    }

    @Override
    protected CompoundTag saveData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Task task : tasks) list.add(task.save());
        tag.put("Tasks", list);
        return tag;
    }

    public static BiomeRitualTasks load(CompoundTag tag) {
        BiomeRitualTasks result = new BiomeRitualTasks();
        ListTag list = tag.getList("Tasks", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) result.tasks.add(Task.load(list.getCompound(index)));
        return result;
    }

    public enum Kind {
        CLEANSING,
        CORRUPTION,
        INFESTING,
        CURING,
        PURGING
    }

    private static final class Task {

        private final Kind kind;
        private final int minChunkX;
        private final int maxChunkX;
        private final int minChunkZ;
        private final int maxChunkZ;
        private final ResourceKey<Biome> replacement;
        private final int sampleY;
        private final boolean variant;
        private int chunkX;
        private int chunkZ;

        private Task(Kind kind, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ,
                     int chunkX, int chunkZ, ResourceKey<Biome> replacement, int sampleY, boolean variant) {
            this.kind = kind;
            this.minChunkX = minChunkX;
            this.maxChunkX = maxChunkX;
            this.minChunkZ = minChunkZ;
            this.maxChunkZ = maxChunkZ;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.replacement = replacement;
            this.sampleY = sampleY;
            this.variant = variant;
        }

        private void process(ServerLevel level) {
            LevelChunk chunk = level.getChunk(chunkX, chunkZ);
            boolean[][] columns = affectedColumns(level, chunk);
            boolean changed = BiomeMutationCompat.rewriteChunk(level, chunk,
                key -> applies(kind, key) ? target(this, key) : key);
            if (changed) {
                switch (kind) {
                    case CLEANSING -> transformColumns(level, chunk, columns, true);
                    case CORRUPTION -> transformColumns(level, chunk, columns, false);
                    case INFESTING -> maybeGenerateAntimatter(level, chunk);
                    case CURING -> clearDreadCarriers(level, chunk);
                    case PURGING -> purge(level, chunk, columns);
                }
            }
            advance();
        }

        private boolean[][] affectedColumns(ServerLevel level, LevelChunk chunk) {
            boolean[][] affected = new boolean[16][16];
            int baseX = chunk.getPos().getMinBlockX();
            int baseZ = chunk.getPos().getMinBlockZ();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    ResourceKey<Biome> key = level.getBiome(new BlockPos(baseX + x, sampleY, baseZ + z))
                        .unwrapKey().orElse(null);
                    affected[x][z] = key != null && applies(kind, key);
                }
            }
            return affected;
        }

        private void advance() {
            if (++chunkZ > maxChunkZ) {
                chunkZ = minChunkZ;
                chunkX++;
            }
        }

        private boolean done() {
            return chunkX > maxChunkX;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Kind", kind.name());
            tag.putInt("MinX", minChunkX);
            tag.putInt("MaxX", maxChunkX);
            tag.putInt("MinZ", minChunkZ);
            tag.putInt("MaxZ", maxChunkZ);
            tag.putInt("X", chunkX);
            tag.putInt("Z", chunkZ);
            tag.putInt("SampleY", sampleY);
            tag.putBoolean("Variant", variant);
            if (replacement != null) tag.putString("Replacement", replacement.location().toString());
            return tag;
        }

        private static Task load(CompoundTag tag) {
            ResourceKey<Biome> replacement = tag.contains("Replacement")
                ? ResourceKey.create(Registries.BIOME, ACRef.parse(tag.getString("Replacement"))) : null;
            return new Task(Kind.valueOf(tag.getString("Kind")),
                tag.getInt("MinX"), tag.getInt("MaxX"), tag.getInt("MinZ"), tag.getInt("MaxZ"),
                tag.getInt("X"), tag.getInt("Z"), replacement, tag.getInt("SampleY"), tag.getBoolean("Variant"));
        }
    }

    private static void transformColumns(ServerLevel level, LevelChunk chunk,
                                         boolean[][] columns, boolean cleanse) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            if (!columns[x][z]) continue;
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                pos.set(baseX + x, y, baseZ + z);
                BlockState state = level.getBlockState(pos);
                BlockState replacement = cleanse ? cleanse(state) : corrupt(state);
                if (replacement != null) level.setBlock(pos, replacement, 2);
            }
        }
    }

    private static BlockState cleanse(BlockState state) {
        if (state.is(BaseBlocks.DARKSTONE.get())) return Blocks.STONE.defaultBlockState();
        if (state.is(BaseBlocks.DARKSTONE_COBBLESTONE.get())) return Blocks.COBBLESTONE.defaultBlockState();
        if (state.is(BaseBlocks.DARKSTONE_BRICK.get())) return Blocks.STONE_BRICKS.defaultBlockState();
        if (state.is(BaseBlocks.CHISELED_DARKSTONE_BRICK.get())) return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        if (state.is(BaseBlocks.CRACKED_DARKSTONE_BRICK.get())) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (state.is(BaseBlocks.GLOWING_DARKSTONE_BRICKS.get())) return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        if (state.is(BaseBlocks.DARKSTONE_COBBLESTONE_WALL.get())) return copy(state, Blocks.COBBLESTONE_WALL);
        if (state.is(BaseBlocks.DARKSTONE_BRICK_STAIRS.get())) return copy(state, Blocks.STONE_BRICK_STAIRS);
        if (state.is(BaseBlocks.DARKSTONE_COBBLESTONE_STAIRS.get())) return copy(state, Blocks.COBBLESTONE_STAIRS);
        if (state.is(BaseBlocks.DARKSTONE_BRICK_SLAB.get())) return copy(state, Blocks.STONE_BRICK_SLAB);
        if (state.is(BaseBlocks.DARKSTONE_COBBLESTONE_SLAB.get())) return copy(state, Blocks.COBBLESTONE_SLAB);
        if (state.is(BaseBlocks.DARKLANDS_OAK_LEAVES.get())) return copy(state, Blocks.OAK_LEAVES);
        if (state.is(BaseBlocks.DARKLANDS_OAK_LOG.get())) return copy(state, Blocks.OAK_LOG);
        if (state.is(BaseBlocks.DARKLANDS_OAK_PLANKS.get())) return Blocks.OAK_PLANKS.defaultBlockState();
        if (state.is(BaseBlocks.DARKLANDS_OAK_STAIRS.get())) return copy(state, Blocks.OAK_STAIRS);
        if (state.is(BaseBlocks.DARKLANDS_OAK_SLAB.get())) return copy(state, Blocks.OAK_SLAB);
        if (state.is(BaseBlocks.DARKLANDS_OAK_FENCE.get())) return copy(state, Blocks.OAK_FENCE);
        if (state.is(OreBlocks.ABYSSALNITE_ORE.get())) return Blocks.IRON_ORE.defaultBlockState();
        return null;
    }

    private static BlockState corrupt(BlockState state) {
        if (state.is(Blocks.STONE)) return BaseBlocks.DARKSTONE.get().defaultBlockState();
        if (state.is(Blocks.COBBLESTONE)) return BaseBlocks.DARKSTONE_COBBLESTONE.get().defaultBlockState();
        if (state.is(Blocks.STONE_BRICKS)) return BaseBlocks.DARKSTONE_BRICK.get().defaultBlockState();
        if (state.is(Blocks.CHISELED_STONE_BRICKS)) return BaseBlocks.CHISELED_DARKSTONE_BRICK.get().defaultBlockState();
        if (state.is(Blocks.CRACKED_STONE_BRICKS)) return BaseBlocks.CRACKED_DARKSTONE_BRICK.get().defaultBlockState();
        if (state.is(Blocks.COBBLESTONE_WALL)) return copy(state, BaseBlocks.DARKSTONE_COBBLESTONE_WALL.get());
        if (state.is(Blocks.STONE_BRICK_STAIRS)) return copy(state, BaseBlocks.DARKSTONE_BRICK_STAIRS.get());
        if (state.is(Blocks.COBBLESTONE_STAIRS)) return copy(state, BaseBlocks.DARKSTONE_COBBLESTONE_STAIRS.get());
        if (state.is(Blocks.STONE_BRICK_SLAB)) return copy(state, BaseBlocks.DARKSTONE_BRICK_SLAB.get());
        if (state.is(Blocks.COBBLESTONE_SLAB)) return copy(state, BaseBlocks.DARKSTONE_COBBLESTONE_SLAB.get());
        if (state.is(Blocks.OAK_LEAVES)) return copy(state, BaseBlocks.DARKLANDS_OAK_LEAVES.get());
        if (state.is(Blocks.OAK_LOG)) return copy(state, BaseBlocks.DARKLANDS_OAK_LOG.get());
        if (state.is(Blocks.OAK_PLANKS)) return BaseBlocks.DARKLANDS_OAK_PLANKS.get().defaultBlockState();
        if (state.is(Blocks.OAK_STAIRS)) return copy(state, BaseBlocks.DARKLANDS_OAK_STAIRS.get());
        if (state.is(Blocks.OAK_SLAB)) return copy(state, BaseBlocks.DARKLANDS_OAK_SLAB.get());
        if (state.is(Blocks.OAK_FENCE)) return copy(state, BaseBlocks.DARKLANDS_OAK_FENCE.get());
        return null;
    }

    private static void clearDreadCarriers(ServerLevel level, LevelChunk chunk) {
        AABB area = chunkArea(level, chunk);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && EffectHooks.isDreadCarrier(entity))) entity.kill();
    }

    private static void purge(ServerLevel level, LevelChunk chunk, boolean[][] columns) {
        AABB area = chunkArea(level, chunk);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && !(entity instanceof Player))) entity.kill();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            if (!columns[x][z]) continue;
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                pos.set(baseX + x, y, baseZ + z);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0) continue;
                boolean solid = Block.isShapeFullBlock(state.getCollisionShape(level, pos))
                    && state.getFluidState().isEmpty();
                level.setBlock(pos, solid ? BaseBlocks.OMOTHOL_STONE.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static void maybeGenerateAntimatter(ServerLevel level, LevelChunk chunk) {
        if (level.random.nextInt(12) != 0) return;
        int x = chunk.getPos().getMinBlockX() + 4 + level.random.nextInt(8);
        int z = chunk.getPos().getMinBlockZ() + 4 + level.random.nextInt(8);
        int y = Math.max(level.getMinBuildHeight() + 4,
            Math.min(level.getMaxBuildHeight() - 5, 40 + level.random.nextInt(40)));
        BlockPos center = new BlockPos(x, y, z);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (dx * dx + dz * dz > 5) continue;
            BlockPos pos = center.offset(dx, 0, dz);
            if (level.getBlockState(pos).getDestroySpeed(level, pos) >= 0) {
                level.setBlock(pos, LiquidAntimatterCompat.BLOCK.get().defaultBlockState(), 2);
            }
        }
    }

    private static AABB chunkArea(ServerLevel level, LevelChunk chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        return new AABB(minX, level.getMinBuildHeight(), minZ,
            minX + 16, level.getMaxBuildHeight(), minZ + 16);
    }

    private static BlockState copy(BlockState source, Block target) {
        BlockState replacement = target.defaultBlockState();
        for (Property<?> property : source.getProperties()) {
            if (replacement.hasProperty(property)) replacement = copy(source, replacement, property);
        }
        return replacement;
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState source, BlockState replacement,
                                                               Property<T> property) {
        return replacement.setValue(property, source.getValue(property));
    }
}
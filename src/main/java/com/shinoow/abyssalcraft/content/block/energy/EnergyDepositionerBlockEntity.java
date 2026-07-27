package com.shinoow.abyssalcraft.content.block.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletItem;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.BiomeMutationCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.IEnergyManipulator;
import com.shinoow.abyssalcraft.system.energy.ManipulatorState;
import com.shinoow.abyssalcraft.system.energy.PEUtils;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

/** Server-side PE routing core for the Energy Depositioner. */
public class EnergyDepositionerBlockEntity extends InventoryEnergyBlockEntity
    implements IEnergyManipulator, TickingBlockEntity, MenuProvider, WorldlyContainer {

    private static final int MAX_ENERGY = 10000;
    private static final int SCAN_INTERVAL = 200;
    private static final int DISRUPTION_TOLERANCE = 200;
    public static final int PROCESS_DURATION = 200;
    public static final int DATA_PE = 0;
    public static final int DATA_MAX_PE = 1;
    public static final int DATA_PROCESSING = 2;
    public static final int DATA_TOLERANCE = 3;
    public static final int DATA_COLLECTORS = 4;
    public static final int DATA_DEITY = 5;
    public static final int DATA_AMPLIFIER = 6;
    public static final int DATA_COUNT = 7;
    private static final int[] INPUT_SLOT = {0};
    private static final int[] OUTPUT_SLOT = {1};
    private static final Set<ResourceKey<Biome>> DARKLANDS = Set.of(
        DarklandsBiomes.DARKLANDS, DarklandsBiomes.FOREST, DarklandsBiomes.PLAINS,
        DarklandsBiomes.HILLS, DarklandsBiomes.MOUNTAINS);

    private final ManipulatorState manipulatorState = new ManipulatorState();
    private int processingTime;
    private ItemStack processingStack = ItemStack.EMPTY;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PE -> (int) getContainedEnergy();
                case DATA_MAX_PE -> getMaxEnergy();
                case DATA_PROCESSING -> processingTime;
                case DATA_TOLERANCE -> getTolerance();
                case DATA_COLLECTORS -> getEnergyCollectors().size();
                case DATA_DEITY -> getActiveDeity() == null ? -1 : getActiveDeity().ordinal();
                case DATA_AMPLIFIER -> getActiveAmplifier() == null ? -1 : getActiveAmplifier().ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PE) setEnergy(value);
            else if (index == DATA_PROCESSING) processingTime = value;
            else if (index == DATA_TOLERANCE) setTolerance(value);
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public EnergyDepositionerBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_DEPOSITIONER_BE.get(), pos, state, 2, MAX_ENERGY);
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        processTabletTick();
        if (!PEUtils.hasNoAdjacentManipulators(level, worldPosition)) return;
        if (level.getGameTime() % SCAN_INTERVAL == 0) {
            PEUtils.locateCollectors(level, worldPosition, this);
        }
        PEUtils.transferToCollectors(level, this);
        if (getTolerance() >= DISRUPTION_TOLERANCE) {
            triggerDisruption();
        }
    }

    void processTabletTick() {
        if (processingStack.isEmpty()) {
            if (!getItem(1).isEmpty() || !isProcessableTablet(getItem(0))) return;
            processingStack = removeItemNoUpdate(0);
            processingTime = 0;
            setChanged();
        }
        if (!getItem(1).isEmpty()) return;

        float storedEnergy = StoneTabletStorage.potentialEnergy(processingStack);
        float previous = storedEnergy * processingTime / PROCESS_DURATION;
        processingTime++;
        float current = storedEnergy * processingTime / PROCESS_DURATION;
        addEnergy(current - previous);
        if (processingTime >= PROCESS_DURATION) {
            ItemStack result = processingStack.copy();
            int affected = level instanceof ServerLevel server ? corruptArea(server) : 0;
            StoneTabletStorage.clear(result);
            if (level != null && level.random.nextFloat() < (float) affected / 289.0F) {
                StoneTabletStorage.setCursed(result, true);
            }
            items.set(1, result);
            processingStack = ItemStack.EMPTY;
            processingTime = 0;
            markUpdated();
        } else {
            setChanged();
        }
    }

    private int corruptArea(ServerLevel server) {
        int affected = 0;
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = worldPosition.getX() - 8; x <= worldPosition.getX() + 8; x++) {
            for (int z = worldPosition.getZ() - 8; z <= worldPosition.getZ() + 8; z++) {
                BlockPos sample = new BlockPos(x, worldPosition.getY(), z);
                ResourceKey<Biome> biome = server.getBiome(sample).unwrapKey().orElse(null);
                if (DARKLANDS.contains(biome)) affected++;
                else candidates.add(sample);
            }
        }

        Map<ChunkPos, Set<Long>> changedQuartColumns = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos column : candidates) {
            if (server.random.nextInt(289) > affected) continue;
            affected++;
            for (int y = server.getMinBuildHeight(); y < server.getMaxBuildHeight(); y++) {
                cursor.set(column.getX(), y, column.getZ());
                BlockState replacement = corrupt(server.getBlockState(cursor));
                if (replacement != null) server.setBlock(cursor, replacement, 2);
            }
            ChunkPos chunkPos = new ChunkPos(column);
            changedQuartColumns.computeIfAbsent(chunkPos, ignored -> new HashSet<>())
                .add(BiomeMutationCompat.quartColumn(
                    QuartPos.fromBlock(column.getX()), QuartPos.fromBlock(column.getZ())));
        }
        for (Map.Entry<ChunkPos, Set<Long>> entry : changedQuartColumns.entrySet()) {
            LevelChunk chunk = server.getChunk(entry.getKey().x, entry.getKey().z);
            BiomeMutationCompat.rewriteQuartColumns(server, chunk, entry.getValue(), DarklandsBiomes.DARKLANDS);
        }
        return affected;
    }

    static BlockState corrupt(BlockState state) {
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

    private static boolean isProcessableTablet(ItemStack stack) {
        return stack.getItem() instanceof StoneTabletItem && StoneTabletStorage.hasInventory(stack)
            && !StoneTabletStorage.isCursed(stack);
    }

    @Override
    public float getEnergyQuanta() {
        float quanta = isActive()
            ? 20.0F * Math.max(getAmplifier(AmplifierType.POWER), 1.0F)
            : 15.0F;
        return consumeEnergy(quanta);
    }

    @Override
    public boolean canTransferPE() {
        return getContainedEnergy() > 0;
    }

    @Override
    public Set<BlockPos> getEnergyCollectors() {
        return manipulatorState.collectors();
    }

    @Override
    public boolean isActive() {
        return getActiveAmplifier() != null;
    }

    @Override
    public void addTolerance(int amount) {
        setTolerance(getTolerance() + amount);
    }

    @Override
    public int getTolerance() {
        return manipulatorState.tolerance();
    }

    @Override
    public void setTolerance(int tolerance) {
        manipulatorState.setTolerance(tolerance);
        setChanged();
    }

    @Override
    public DeityType getActiveDeity() {
        return manipulatorState.activeDeity();
    }

    @Override
    public AmplifierType getActiveAmplifier() {
        return manipulatorState.activeAmplifier();
    }

    @Override
    public void setActiveDeity(DeityType deity) {
        manipulatorState.setActiveDeity(deity);
        setChanged();
    }

    @Override
    public void setActiveAmplifier(AmplifierType amplifier) {
        manipulatorState.setActiveAmplifier(amplifier);
        setChanged();
    }

    @Override
    public float getAmplifier(AmplifierType type) {
        if (type != getActiveAmplifier()) {
            return 0.0F;
        }
        return switch (type) {
            case DURATION -> 2.0F;
            case POWER -> 1.5F;
            case RANGE -> 4.0F;
        };
    }

    @Override
    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        manipulatorState.save(tag);
        tag.putInt("ProcessingTime", processingTime);
        NonNullList<ItemStack> processing = NonNullList.withSize(1, ItemStack.EMPTY);
        processing.set(0, processingStack);
        CompoundTag processingTag = new CompoundTag();
        ContainerCompat.saveItems(processingTag, processing, registries);
        tag.put("ProcessingStack", processingTag);
    }

    @Override
    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        manipulatorState.load(tag);
        processingTime = Math.max(0, Math.min(PROCESS_DURATION - 1, tag.getInt("ProcessingTime")));
        NonNullList<ItemStack> processing = NonNullList.withSize(1, ItemStack.EMPTY);
        if (tag.contains("ProcessingStack", Tag.TAG_COMPOUND)) {
            CompoundTag processingTag = tag.getCompound("ProcessingStack");
            if (!processingTag.contains("Items", Tag.TAG_LIST) && processingTag.contains("id")) {
                CompoundTag legacyStack = processingTag.copy();
                legacyStack.putByte("Slot", (byte) 0);
                ListTag items = new ListTag();
                items.add(legacyStack);
                processingTag = new CompoundTag();
                processingTag.put("Items", items);
            }
            ContainerCompat.loadItems(processingTag, processing, registries);
        }
        processingStack = processing.get(0);
        if (!processingStack.isEmpty() && !isProcessableTablet(processingStack)) {
            processingStack = ItemStack.EMPTY;
            processingTime = 0;
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && processingStack.isEmpty() && isProcessableTablet(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? INPUT_SLOT : side == Direction.DOWN ? OUTPUT_SLOT : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.UP && slot == 0 && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == 1;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.energy_depositioner");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new EnergyDepositionerMenu(windowId, inventory, this, dataAccess);
    }

    int processingTime() {
        return processingTime;
    }

    ItemStack processingStack() {
        return processingStack;
    }

    ItemStack removeProcessingStack() {
        ItemStack stack = processingStack;
        processingStack = ItemStack.EMPTY;
        processingTime = 0;
        setChanged();
        return stack;
    }

    private void triggerDisruption() {
        resetTolerance();
        AABB bounds = new AABB(worldPosition).inflate(16.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, bounds);
        DeityType[] deities = DeityType.values();
        DeityType deity = deities[level.random.nextInt(deities.length)];
        DisruptionHandler.instance().generate(deity, level, worldPosition, players);
    }
}
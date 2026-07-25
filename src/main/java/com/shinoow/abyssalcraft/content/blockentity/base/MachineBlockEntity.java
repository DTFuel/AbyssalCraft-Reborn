package com.shinoow.abyssalcraft.content.blockentity.base;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.menu.base.MachineMenu;
import com.shinoow.abyssalcraft.content.menu.base.MachineMenus;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * Base furnace-like machine block entity (owned by PP-1; frozen for P2 reuse).
 *
 * <p>Three slots (input/fuel/output), a burn timer and a form-progress counter exposed through a
 * {@link ContainerData} for the menu -- the shared shape of the crystallizer/materializer/transmutator.
 * Concrete so an example {@link BlockEntityType} can be registered; P2 machines subclass and override
 * {@link #serverTick()} with their recipe processing. Only vanilla + the compat layer, so no loader
 * {@code //?} here (item NBT and save/load hooks are absorbed by {@link ContainerCompat} /
 * {@link BlockEntityCompat}).
 */
public class MachineBlockEntity extends BlockEntityCompat implements Container, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_BURN = 2;
    public static final int DATA_MAX_BURN = 3;
    public static final int DATA_COUNT = 4;

    private static final int DATA_VERSION = 2;
    private static final String EXPERIENCE_LEDGER_KEY = "OutputExperience";

    protected final NonNullList<ItemStack> items;
    private final int defaultMaxProgress;
    private final Map<Integer, ArrayDeque<ExperienceBatch>> experienceLedger = new HashMap<>();
    private int playerExtractionDepth;
    protected int progress;
    protected int maxProgress;
    protected int burnTime;
    protected int maxBurnTime;
    protected ResourceLocation activeRecipeId;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_BURN -> burnTime;
                case DATA_MAX_BURN -> maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                case DATA_BURN -> burnTime = value;
                case DATA_MAX_BURN -> maxBurnTime = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, SLOT_COUNT, 200);
    }

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                 int slotCount, int defaultMaxProgress) {
        super(type, pos, state);
        if (slotCount <= 0 || defaultMaxProgress <= 0) {
            throw new IllegalArgumentException("Machine inventory size and default progress must be positive");
        }
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.defaultMaxProgress = defaultMaxProgress;
        this.maxProgress = defaultMaxProgress;
    }

    /** Ticker entry wired by the machine block; default processing is a no-op until P2 overrides. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity be) {
        be.serverTick();
    }

    protected void serverTick() { }

    /** Select the recipe currently being processed, resetting progress when the recipe changes. */
    protected boolean selectRecipe(ResourceLocation recipeId) {
        if (Objects.equals(activeRecipeId, recipeId)) {
            return false;
        }
        activeRecipeId = recipeId;
        progress = 0;
        return true;
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            if (playerExtractionDepth == 0) {
                consumeExperience(slot, removed.getCount(), false, null);
            }
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            consumeExperience(slot, removed.getCount(), false, null);
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        experienceLedger.clear();
        setChanged();
    }

    public void beginPlayerExtraction() {
        playerExtractionDepth++;
    }

    public void endPlayerExtraction() {
        playerExtractionDepth = Math.max(0, playerExtractionDepth - 1);
    }

    protected void recordExperience(int slot, ResourceLocation recipeId, int outputCount, float experiencePerItem) {
        if (outputCount <= 0) {
            return;
        }
        ArrayDeque<ExperienceBatch> batches = experienceLedger.computeIfAbsent(slot, ignored -> new ArrayDeque<>());
        int tracked = batches.stream().mapToInt(batch -> batch.remaining).sum();
        int existingBeforeProduction = Math.max(0, items.get(slot).getCount() - outputCount);
        if (tracked < existingBeforeProduction) {
            batches.addLast(new ExperienceBatch(ACRef.id("untracked"), existingBeforeProduction - tracked, 0F));
        }
        batches
            .addLast(new ExperienceBatch(recipeId, outputCount, experiencePerItem));
    }

    public void awardPlayerExperience(Player player, int slot, int amount) {
        consumeExperience(slot, amount, true, player);
    }

    private void consumeExperience(int slot, int amount, boolean award, Player player) {
        if (amount <= 0) {
            return;
        }
        ArrayDeque<ExperienceBatch> batches = experienceLedger.get(slot);
        if (batches == null) {
            return;
        }
        int remaining = amount;
        float totalExperience = 0F;
        while (remaining > 0 && !batches.isEmpty()) {
            ExperienceBatch batch = batches.peekFirst();
            int consumed = Math.min(remaining, batch.remaining);
            if (award) {
                totalExperience += consumed * batch.experiencePerItem;
            }
            batch.remaining -= consumed;
            remaining -= consumed;
            if (batch.remaining == 0) {
                batches.removeFirst();
            }
        }
        if (batches.isEmpty()) {
            experienceLedger.remove(slot);
        }
        if (award && totalExperience > 0F && player != null && player.level() instanceof ServerLevel serverLevel) {
            int whole = (int) totalExperience;
            if (serverLevel.random.nextFloat() < totalExperience - whole) {
                whole++;
            }
            if (whole > 0) {
                ExperienceOrb.award(serverLevel, player.position(), whole);
            }
        }
        setChanged();
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new MachineMenu(MachineMenus.MACHINE.get(), windowId, playerInv, this, dataAccess);
    }

    // --- persistence (registries is null on 1.20.1, the component lookup on 1.21) ---

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("MachineDataVersion", DATA_VERSION);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
        ContainerCompat.saveItems(tag, items, registries);
        ListTag ledger = new ListTag();
        for (Map.Entry<Integer, ArrayDeque<ExperienceBatch>> entry : experienceLedger.entrySet()) {
            for (ExperienceBatch batch : entry.getValue()) {
                CompoundTag batchTag = new CompoundTag();
                batchTag.putInt("Slot", entry.getKey());
                batchTag.putString("Recipe", batch.recipeId.toString());
                batchTag.putInt("Count", batch.remaining);
                batchTag.putFloat("Experience", batch.experiencePerItem);
                ledger.add(batchTag);
            }
        }
        tag.put(EXPERIENCE_LEDGER_KEY, ledger);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress", Tag.TAG_INT) && tag.getInt("MaxProgress") > 0
            ? tag.getInt("MaxProgress") : defaultMaxProgress;
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        activeRecipeId = tag.contains("ActiveRecipe", Tag.TAG_STRING)
            ? ACRef.parse(tag.getString("ActiveRecipe")) : null;
        ContainerCompat.loadItems(tag, items, registries);
        experienceLedger.clear();
        ListTag ledger = tag.getList(EXPERIENCE_LEDGER_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < ledger.size(); index++) {
            CompoundTag batchTag = ledger.getCompound(index);
            int slot = batchTag.getInt("Slot");
            int count = batchTag.getInt("Count");
            float experience = batchTag.getFloat("Experience");
            if (slot >= 0 && slot < items.size() && count > 0 && experience >= 0F) {
                ResourceLocation recipeId = ACRef.parse(batchTag.getString("Recipe"));
                experienceLedger.computeIfAbsent(slot, ignored -> new ArrayDeque<>())
                    .addLast(new ExperienceBatch(recipeId, count, experience));
            }
        }
    }

    private static final class ExperienceBatch {
        private final ResourceLocation recipeId;
        private int remaining;
        private final float experiencePerItem;

        private ExperienceBatch(ResourceLocation recipeId, int remaining, float experiencePerItem) {
            this.recipeId = recipeId;
            this.remaining = remaining;
            this.experiencePerItem = experiencePerItem;
        }
    }
}

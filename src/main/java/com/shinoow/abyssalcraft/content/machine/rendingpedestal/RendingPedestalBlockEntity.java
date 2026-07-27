package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.InventoryEnergyBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RendingPedestalBlockEntity extends InventoryEnergyBlockEntity
        implements MenuProvider, TickingBlockEntity, WorldlyContainer {

    public static final int SLOT_ENERGY = 0;
    public static final int SLOT_STAFF = 1;
    public static final int FIRST_OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 6;
    public static final int MAX_ENERGY = 5000;
    public static final int DATA_PE = 4;
    public static final int DATA_MAX_PE = 5;
    public static final int DATA_COUNT = 6;
    private static final int[] OUTPUT_SLOTS = {2, 3, 4, 5};

    private final int[] rendingEnergy = new int[RendingEnergyType.values().length];
    private int ticksExisted;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= 0 && index < rendingEnergy.length) return rendingEnergy[index];
            if (index == DATA_PE) return Mth.floor(getContainedEnergy());
            return index == DATA_MAX_PE ? getMaxEnergy() : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < rendingEnergy.length) setRendingEnergy(index, value);
            else if (index == DATA_PE) setEnergy(value);
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public RendingPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(RendingPedestals.RENDING_PEDESTAL_BE.get(), pos, state, SLOT_COUNT, MAX_ENERGY);
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        transferInputEnergy();
        ticksExisted++;
        if (ticksExisted % 40 == 0) {
            drainStaffLedgers();
            rendNearbyTargets();
        }
        produceOutputs(DataRecipeCompat.allOfType(level, ModRecipes.RENDING.get()));
    }

    void transferInputEnergy() {
        PEUtils.transferFromItem(getItem(SLOT_ENERGY), this, 20.0F);
    }

    void drainStaffLedgers() {
        ItemStack stack = getItem(SLOT_STAFF);
        if (!(stack.getItem() instanceof StaffOfRendingItem staff)) return;
        boolean changed = false;
        for (RendingEnergyType type : RendingEnergyType.values()) {
            int stored = staff.getEnergy(stack, type);
            if (stored > 0) {
                addRendingEnergy(type, stored);
                staff.setEnergy(stack, type, 0);
                changed = true;
            }
        }
        if (changed) setChanged();
    }

    private void rendNearbyTargets() {
        ItemStack stack = getItem(SLOT_STAFF);
        if (!(stack.getItem() instanceof StaffOfRendingItem staff) || level == null) return;
        int drainAmount = staff.drainAmount(stack);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(worldPosition).inflate(15.0D, 3.0D, 15.0D), LivingEntity::isAlive)) {
            for (RendingEnergyType type : RendingEnergyType.values()) {
                if (type.matches(target)) {
                    rendTarget(target, type, drainAmount);
                    break;
                }
            }
        }
    }

    private boolean rendTarget(LivingEntity target, RendingEnergyType type, int drainAmount) {
        float cost = target.getMaxHealth() / 2.0F;
        if (cost <= 0.0F || getContainedEnergy() < cost
            || !target.hurt(target.damageSources().magic(), drainAmount)) return false;
        consumeEnergy(cost);
        addRendingEnergy(type, drainAmount);
        return true;
    }

    void produceOutputs(List<RendingRecipe> recipes) {
        for (RendingEnergyType type : RendingEnergyType.values()) {
            RendingRecipe recipe = recipes.stream()
                .filter(candidate -> RendingEnergyType.fromRecipe(candidate).orElse(null) == type
                    && type.validates(candidate))
                .findFirst().orElse(null);
            if (recipe == null) continue;
            int outputSlot = FIRST_OUTPUT_SLOT + type.ordinal();
            while (getRendingEnergy(type) >= type.threshold() && canOutput(outputSlot, recipe.result())) {
                putOutput(outputSlot, recipe.result());
                setRendingEnergy(type, getRendingEnergy(type) - type.threshold());
            }
        }
    }

    private boolean canOutput(int slot, ItemStack result) {
        ItemStack output = getItem(slot);
        return output.isEmpty() || ContainerCompat.canStack(output, result)
            && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void putOutput(int slot, ItemStack result) {
        ItemStack output = getItem(slot);
        if (output.isEmpty()) items.set(slot, result.copy());
        else output.grow(result.getCount());
        setChanged();
    }

    public int getRendingEnergy(RendingEnergyType type) {
        return rendingEnergy[type.ordinal()];
    }

    public void setRendingEnergy(RendingEnergyType type, int amount) {
        setRendingEnergy(type.ordinal(), amount);
    }

    private void setRendingEnergy(int index, int amount) {
        int clamped = Math.max(0, amount);
        if (rendingEnergy[index] != clamped) {
            rendingEnergy[index] = clamped;
            setChanged();
        }
    }

    public void addRendingEnergy(RendingEnergyType type, int amount) {
        if (amount <= 0) return;
        long total = (long) getRendingEnergy(type) + amount;
        setRendingEnergy(type, (int) Math.min(Integer.MAX_VALUE, total));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_ENERGY && stack.getItem() instanceof IEnergyContainerItem
            || slot == SLOT_STAFF && stack.getItem() instanceof StaffOfRendingItem;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot >= FIRST_OUTPUT_SLOT && slot < SLOT_COUNT;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == SLOT_STAFF) markUpdated();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.rending_pedestal");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new RendingPedestalMenu(RendingPedestals.RENDING_PEDESTAL_MENU.get(), windowId,
            playerInventory, this, dataAccess);
    }

    @Override
    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("TicksExisted", ticksExisted);
        for (RendingEnergyType type : RendingEnergyType.values()) {
            tag.putInt(type.dataKey(), getRendingEnergy(type));
        }
    }

    @Override
    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        ticksExisted = Math.max(0, tag.getInt("TicksExisted"));
        for (RendingEnergyType type : RendingEnergyType.values()) {
            rendingEnergy[type.ordinal()] = Math.max(0, tag.getInt(type.dataKey()));
        }
    }
}
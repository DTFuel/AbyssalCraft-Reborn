package com.shinoow.abyssalcraft.content.machine.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.InventoryBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.BrewingHooksCompat;
import com.shinoow.abyssalcraft.platform.MachineItemCompat;
import com.shinoow.abyssalcraft.platform.PotionBrewingCompat;

/**
 * Sequential Brewing Stand block entity (owned by PC-8, Stage C2a).
 *
 * <p>Ports the 1.12.2 {@code TileEntitySequentialBrewingStand}: 8 slots -- three potion slots (0,1,2),
 * an ingredient (3) and fuel (4) driving vanilla brewing, plus three transfer-out slots (5,6,7). The
 * distinctive "sequential" behaviour: once per second the transfer-out slots are pushed into the input
 * slots of the neighbouring stand this one faces, so brewed potions flow down a chain of stands. Built
 * on the PC-1 {@link InventoryBlockEntity} (the 8-slot container) + {@link TickingBlockEntity}; the
 * vanilla-brewing version fork lives in {@link PotionBrewingCompat}, so this class is {@code //?}-free.
 */
public class BrewingStandBlockEntity extends InventoryBlockEntity
        implements MenuProvider, TickingBlockEntity, WorldlyContainer {

    public static final int SLOT_COUNT = 8;
    public static final int SLOT_INGREDIENT = 3;
    public static final int SLOT_FUEL = 4;
    private static final int[] SLOTS_UP = {SLOT_INGREDIENT};
    private static final int[] SLOTS_DOWN = {5, 6, 7};
    private static final int[] SLOTS_SIDE = {0, 1, 2, SLOT_FUEL};
    private static final int FUEL_PER_POWDER = 20;
    private static final int BREW_DURATION = 400;

    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL = 1;
    public static final int DATA_COUNT = 2;

    private int brewTime;
    private int fuel;
    private Item ingredient;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == DATA_BREW_TIME ? brewTime : fuel;
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_BREW_TIME) {
                brewTime = value;
            } else {
                fuel = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public BrewingStandBlockEntity(BlockPos pos, BlockState state) {
        super(BrewingStands.BREWING_STAND_BE.get(), pos, state, SLOT_COUNT);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(BrewingStandBlock.FACING) ? state.getValue(BrewingStandBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }

        ItemStack fuelStack = getItem(SLOT_FUEL);
        if (fuel <= 0 && fuelStack.is(Items.BLAZE_POWDER)) {
            fuel = FUEL_PER_POWDER;
            fuelStack.shrink(1);
            setChanged();
        }

        boolean canBrew = canBrew();
        boolean brewing = brewTime > 0;
        ItemStack ingredientStack = getItem(SLOT_INGREDIENT);

        if (brewing) {
            brewTime--;
            if (brewTime == 0 && canBrew) {
                brewPotions();
                setChanged();
            } else if (!canBrew || ingredient != ingredientStack.getItem()) {
                brewTime = 0;
                setChanged();
            }
        } else if (canBrew && fuel > 0) {
            fuel--;
            brewTime = BREW_DURATION;
            ingredient = ingredientStack.getItem();
            setChanged();
        }

        // Sequential chaining: once per second push the transfer-out slots (5,6,7) into the input slots
        // (0,1,2) of the neighbouring stand this one faces -- potions flow down the chain.
        if (level.getGameTime() % 20L == 0L) {
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(facing()));
            if (neighbour instanceof BrewingStandBlockEntity stand) {
                for (int slot = 5; slot <= 7; slot++) {
                    ItemStack output = getItem(slot);
                    int targetSlot = slot - 5;
                    if (!output.isEmpty() && stand.getItem(targetSlot).isEmpty()
                            && stand.canPlaceItem(targetSlot, output)) {
                        stand.setItem(targetSlot, removeItem(slot, output.getCount()));
                    }
                }
            }
        }
    }

    private boolean canBrew() {
        ItemStack ingredientStack = getItem(SLOT_INGREDIENT);
        if (ingredientStack.isEmpty() || !PotionBrewingCompat.isIngredient(level, ingredientStack)) {
            return false;
        }
        boolean hasMix = false;
        for (int i = 0; i < 3; i++) {
            ItemStack potion = getItem(i);
            if (!potion.isEmpty() && PotionBrewingCompat.hasMix(level, potion, ingredientStack)) {
                if (!getItem(i + 5).isEmpty()) {
                    return false;
                }
                hasMix = true;
            }
        }
        return hasMix;
    }

    private void brewPotions() {
        if (BrewingHooksCompat.onAttempt(items)) {
            return;
        }
        ItemStack ingredientStack = getItem(SLOT_INGREDIENT);
        for (int i = 0; i < 3; i++) {
            ItemStack potion = getItem(i);
            if (!potion.isEmpty() && PotionBrewingCompat.hasMix(level, potion, ingredientStack)) {
                items.set(i, PotionBrewingCompat.mix(level, ingredientStack, potion));
            }
        }
        ItemStack consumedIngredient = ingredientStack.copyWithCount(1);
        ingredientStack.shrink(1);
        ItemStack remainder = MachineItemCompat.craftingRemainder(consumedIngredient);
        if (!remainder.isEmpty()) {
            if (ingredientStack.isEmpty()) {
                items.set(SLOT_INGREDIENT, remainder);
            } else {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), remainder);
            }
        }
        BrewingHooksCompat.onBrewed(items);
        // Move finished potions (0,1,2) into the transfer-out slots (5,6,7) so the chain can carry them.
        for (int slot = 5; slot <= 7; slot++) {
            if (getItem(slot).isEmpty() && !getItem(slot - 5).isEmpty()) {
                setItem(slot, removeItemNoUpdate(slot - 5));
            }
        }
        level.levelEvent(1035, worldPosition, 0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot == SLOT_FUEL) {
            return stack.is(Items.BLAZE_POWDER);
        }
        if (level == null) {
            return false;
        }
        if (slot == SLOT_INGREDIENT) {
            return PotionBrewingCompat.isIngredient(level, stack);
        }
        return (slot >= 0 && slot <= 2 || slot >= 5 && slot <= 7)
            && getItem(slot).isEmpty() && PotionBrewingCompat.isInput(level, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? SLOTS_UP : side == Direction.DOWN ? SLOTS_DOWN : SLOTS_SIDE;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_INGREDIENT && stack.is(Items.GLASS_BOTTLE)
            || side == Direction.DOWN && slot >= 5 && slot <= 7;
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.sequential_brewing_stand");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new BrewingStandMenu(BrewingStands.BREWING_STAND_MENU.get(), windowId, playerInv, this, dataAccess);
    }

    // --- persistence (super handles the 8 item stacks) ---

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveData(tag, registries);
        tag.putShort("BrewTime", (short) brewTime);
        tag.putByte("Fuel", (byte) fuel);
        if (ingredient != null) {
            tag.putString("Ingredient", BuiltInRegistries.ITEM.getKey(ingredient).toString());
        }
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadData(tag, registries);
        brewTime = tag.getShort("BrewTime");
        fuel = tag.getByte("Fuel");
        ingredient = tag.contains("Ingredient", Tag.TAG_STRING)
            ? BuiltInRegistries.ITEM.get(ACRef.parse(tag.getString("Ingredient"))) : null;
    }
}

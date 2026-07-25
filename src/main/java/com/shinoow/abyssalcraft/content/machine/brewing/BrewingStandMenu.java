package com.shinoow.abyssalcraft.content.machine.brewing;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.menu.base.ContainerMenuBase;
import com.shinoow.abyssalcraft.platform.BrewingHooksCompat;
import com.shinoow.abyssalcraft.platform.PotionBrewingCompat;

/**
 * Sequential Brewing Stand menu (owned by PC-8, Stage C2a).
 *
 * <p>Ports the 1.12.2 {@code ContainerSequentialBrewingStand} slot layout (three potion slots, an
 * ingredient, fuel, and the three transfer-out slots on the right) on the PC-3 {@link ContainerMenuBase}
 * with the brew-time / fuel {@link ContainerData} for the screen.
 */
public class BrewingStandMenu extends ContainerMenuBase {

    private static final int BREW_DURATION = 400;

    private final Container brewingStand;
    private final ContainerData data;
    private final Level level;

    /** Server-side: bound to the real brewing-stand block entity + its data. */
    public BrewingStandMenu(MenuType<?> type, int windowId, Inventory playerInv, Container brewingStand, ContainerData data) {
        super(type, windowId, BrewingStandBlockEntity.SLOT_COUNT);
        checkContainerSize(brewingStand, BrewingStandBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, BrewingStandBlockEntity.DATA_COUNT);
        this.brewingStand = brewingStand;
        this.data = data;
        this.level = playerInv.player == null ? null : playerInv.player.level();

        addSlot(new PotionSlot(brewingStand, 0, 56, 51));
        addSlot(new PotionSlot(brewingStand, 1, 79, 58));
        addSlot(new PotionSlot(brewingStand, 2, 102, 51));
        addSlot(new IngredientSlot(brewingStand, 3, 79, 17));
        addSlot(new FuelSlot(brewingStand, 4, 17, 17));
        addSlot(new PotionSlot(brewingStand, 5, 152, 17));
        addSlot(new PotionSlot(brewingStand, 6, 152, 35));
        addSlot(new PotionSlot(brewingStand, 7, 152, 53));

        addPlayerInventory(playerInv, 84);
        addDataSlots(data);
    }

    /** Client-side factory -- dummy backing store until the client screen binds to the server menu state. */
    public BrewingStandMenu(MenuType<?> type, int windowId, Inventory playerInv) {
        this(type, windowId, playerInv,
            new SimpleContainer(BrewingStandBlockEntity.SLOT_COUNT),
            new SimpleContainerData(BrewingStandBlockEntity.DATA_COUNT));
    }

    /** Brew progress fraction in {@code [0,1]} (brew time counts down from {@value #BREW_DURATION}). */
    public float brewProgress() {
        int remaining = data.get(BrewingStandBlockEntity.DATA_BREW_TIME);
        return remaining <= 0 ? 0F : (float) (BREW_DURATION - remaining) / BREW_DURATION;
    }

    public int fuel() {
        return data.get(BrewingStandBlockEntity.DATA_FUEL);
    }

    public int brewTime() {
        return data.get(BrewingStandBlockEntity.DATA_BREW_TIME);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < BrewingStandBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, BrewingStandBlockEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (stack.is(Items.BLAZE_POWDER) && !slots.get(4).hasItem()) {
            if (!moveItemStackTo(stack, 4, 5, false)) {
                return ItemStack.EMPTY;
            }
        } else if (level != null && PotionBrewingCompat.isIngredient(level, stack)) {
            if (!moveItemStackTo(stack, 3, 4, false)) {
                return ItemStack.EMPTY;
            }
        } else if (level != null && PotionBrewingCompat.isInput(level, stack) && stack.getCount() == 1) {
            if (!moveItemStackTo(stack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int mainStart = BrewingStandBlockEntity.SLOT_COUNT;
            int hotbarStart = mainStart + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, mainStart, hotbarStart, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, original);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return brewingStand.stillValid(player);
    }

    private final class PotionSlot extends Slot {
        private PotionSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return level != null && PotionBrewingCompat.isInput(level, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            BrewingHooksCompat.onPlayerBrewed(player, stack);
            super.onTake(player, stack);
        }
    }

    private final class IngredientSlot extends Slot {
        private IngredientSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return level != null && PotionBrewingCompat.isIngredient(level, stack);
        }
    }

    private static final class FuelSlot extends Slot {
        private FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.BLAZE_POWDER);
        }
    }
}

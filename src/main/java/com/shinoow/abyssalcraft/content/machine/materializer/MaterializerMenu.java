package com.shinoow.abyssalcraft.content.machine.materializer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagStorage;
import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;

public final class MaterializerMenu extends AbstractContainerMenu {

    public static final int RESULT_COUNT = 18;
    private static final int PLAYER_START = 2 + RESULT_COUNT;
    private final Container machine;
    private final SimpleContainer results = new SimpleContainer(RESULT_COUNT);
    private final ContainerData pages;
    private List<DataRecipeCompat.Entry<MaterializationRecipe>> visible = List.of();
    private int lastMachineFingerprint = Integer.MIN_VALUE;
    private int lastPage = -1;
    private int refreshTicks;

    public MaterializerMenu(int windowId, Inventory inventory, MaterializerBlockEntity machine) {
        this(windowId, inventory, machine, new ContainerData() {
            private int page;
            private int maxPage;
            @Override public int get(int index) { return index == 0 ? page : maxPage; }
            @Override public void set(int index, int value) { if (index == 0) page = value; else maxPage = value; }
            @Override public int getCount() { return 2; }
        });
    }

    private MaterializerMenu(int windowId, Inventory inventory, Container machine, ContainerData pages) {
        super(Materializers.MATERIALIZER_MENU.get(), windowId);
        checkContainerSize(machine, MaterializerBlockEntity.SLOT_COUNT);
        checkContainerDataCount(pages, 2);
        this.machine = machine;
        this.pages = pages;
        addSlot(new Slot(machine, MaterializerBlockEntity.SLOT_BAG, 14, 17) {
            @Override public boolean mayPlace(ItemStack stack) { return CrystalBagStorage.isBag(stack); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(machine, MaterializerBlockEntity.SLOT_BOOK, 14, 53) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof NecronomiconItem; }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 6; col++) {
            int index = col + row * 6;
            addSlot(new MaterializerResultSlot(this, results, index, 44 + col * 18, 17 + row * 18));
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(pages);
        refreshResults();
    }

    public MaterializerMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(windowId, inventory, new SimpleContainer(MaterializerBlockEntity.SLOT_COUNT), new SimpleContainerData(2));
    }

    @Override public boolean stillValid(Player player) { return machine.stillValid(player); }
    public int page() { return pages.get(0); }
    public int maxPage() { return pages.get(1); }

    ItemStack displayedResult(int index) {
        return index >= 0 && index < RESULT_COUNT ? results.getItem(index) : ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && page() > 0) pages.set(0, page() - 1);
        else if (id == 1 && page() < maxPage()) pages.set(0, page() + 1);
        else return false;
        refreshResults();
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (machine instanceof MaterializerBlockEntity blockEntity) {
            int fingerprint = machineFingerprint(blockEntity);
            if (fingerprint != lastMachineFingerprint || page() != lastPage || ++refreshTicks >= 20) {
                refreshResults();
            }
        }
        super.broadcastChanges();
    }

    private void refreshResults() {
        if (!(machine instanceof MaterializerBlockEntity blockEntity) || blockEntity.getLevel() == null) return;
        if (blockEntity.getLevel().isClientSide) return;
        List<DataRecipeCompat.Entry<MaterializationRecipe>> available = blockEntity.availableRecipes();
        int maxPage = Math.max(0, (available.size() - 1) / RESULT_COUNT);
        pages.set(1, maxPage);
        if (page() > maxPage) pages.set(0, maxPage);
        int start = page() * RESULT_COUNT;
        int end = Math.min(start + RESULT_COUNT, available.size());
        visible = new ArrayList<>(available.subList(start, end));
        for (int index = 0; index < RESULT_COUNT; index++) {
            ItemStack display = index < visible.size() ? visible.get(index).value().result().copy() : ItemStack.EMPTY;
            if (!ItemStack.matches(results.getItem(index), display)) results.setItem(index, display);
        }
        lastMachineFingerprint = machineFingerprint(blockEntity);
        lastPage = page();
        refreshTicks = 0;
    }

    private int machineFingerprint(MaterializerBlockEntity blockEntity) {
        ItemStack bag = blockEntity.getItem(MaterializerBlockEntity.SLOT_BAG);
        ItemStack book = blockEntity.getItem(MaterializerBlockEntity.SLOT_BOOK);
        int hash = 31 * BuiltInRegistries.ITEM.getId(bag.getItem()) + ItemDataCompat.copyData(bag).hashCode();
        return 31 * hash + BuiltInRegistries.ITEM.getId(book.getItem());
    }

    boolean canCraftVisible(int index, Player player) {
        ItemStack carried = getCarried();
        ItemStack result = displayedResult(index);
        boolean cursorRoom = carried.isEmpty() || ContainerCompat.canStack(carried, result)
            && carried.getCount() + result.getCount() <= carried.getMaxStackSize();
        return index >= 0 && index < RESULT_COUNT && !results.getItem(index).isEmpty()
            && cursorRoom && (!(machine instanceof MaterializerBlockEntity) || index < visible.size());
    }

    boolean commitVisible(int index) {
        if (!(machine instanceof MaterializerBlockEntity blockEntity) || index < 0 || index >= visible.size()) {
            return false;
        }
        boolean crafted = !blockEntity.craft(visible.get(index).id()).isEmpty();
        refreshResults();
        return crafted;
    }

    ItemStack takeVisible(int index) {
        ItemStack display = displayedResult(index).copy();
        if (display.isEmpty()) return ItemStack.EMPTY;
        if (machine instanceof MaterializerBlockEntity) {
            return commitVisible(index) ? display : ItemStack.EMPTY;
        }
        return display;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        if (index >= 2 && index < PLAYER_START) return quickCraftResult(player, index - 2);
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_START + 36, true)) return ItemStack.EMPTY;
        } else if (CrystalBagStorage.isBag(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof NecronomiconItem) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = PLAYER_START + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, PLAYER_START + 36, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, PLAYER_START, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        refreshResults();
        return original;
    }

    private ItemStack quickCraftResult(Player player, int visibleIndex) {
        if (!(machine instanceof MaterializerBlockEntity blockEntity) || visibleIndex >= visible.size()) return ItemStack.EMPTY;
        ResourceLocation recipeId = visible.get(visibleIndex).id();
        ItemStack result = visible.get(visibleIndex).value().result();
        ItemStack moved = ItemStack.EMPTY;
        int maxExecutions = Math.max(1, result.getMaxStackSize() / Math.max(1, result.getCount()));
        for (int execution = 0; execution < maxExecutions && canFullyMove(result); execution++) {
            ItemStack crafted = blockEntity.craft(recipeId);
            if (crafted.isEmpty()) break;
            ItemStack transfer = crafted.copy();
            if (!moveItemStackTo(transfer, PLAYER_START, PLAYER_START + 36, false) || !transfer.isEmpty()) {
                player.drop(transfer, false);
                break;
            }
            if (moved.isEmpty()) moved = crafted.copy(); else moved.grow(crafted.getCount());
        }
        refreshResults();
        return moved;
    }

    private boolean canFullyMove(ItemStack stack) {
        int remaining = stack.getCount();
        for (int index = PLAYER_START; index < PLAYER_START + 36; index++) {
            ItemStack target = slots.get(index).getItem();
            if (target.isEmpty()) remaining -= stack.getMaxStackSize();
            else if (ContainerCompat.canStack(target, stack)) remaining -= target.getMaxStackSize() - target.getCount();
            if (remaining <= 0) return true;
        }
        return false;
    }
}
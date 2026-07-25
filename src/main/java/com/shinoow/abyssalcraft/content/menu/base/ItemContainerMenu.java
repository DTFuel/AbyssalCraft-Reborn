package com.shinoow.abyssalcraft.content.menu.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

/**
 * Variable-size item-backed container menu (owned by PC-3): the Stage 2 framework example.
 *
 * <p>Lays a {@link Container}'s slots out in a 9-wide grid above the player inventory. The backing
 * container is arbitrary -- a block inventory or (the intended Stage 2 use) an item's inventory, so
 * this is the shared base for the crystal bag / spirit tablet / spellbook menus. Registered as an
 * example {@link MenuType} in {@code registry/ModMenus} to prove the menu-registration path on both
 * loaders; the container size travels in the open buffer (written via
 * {@code platform/MenuCompat.open(player, provider, writer)}), so a screen can be opened for any size.
 *
 * <p>Fork-free (pure vanilla); reuses {@link ContainerMenuBase} for the player-inventory layout and
 * shift-click.
 */
public class ItemContainerMenu extends ContainerMenuBase {

    private static final int COLUMNS = 9;

    private final Container inventory;

    /** Server-side: bound to the real backing inventory (item- or block-owned). */
    public ItemContainerMenu(MenuType<?> type, int windowId, Inventory playerInv, Container inventory) {
        super(type, windowId, inventory.getContainerSize());
        this.inventory = inventory;
        inventory.startOpen(playerInv.player);

        int size = inventory.getContainerSize();
        int rows = Math.max(1, (size + COLUMNS - 1) / COLUMNS);
        for (int i = 0; i < size; i++)
            addSlot(new Slot(inventory, i, 8 + (i % COLUMNS) * 18, 18 + (i / COLUMNS) * 18));

        addPlayerInventory(playerInv, 18 + rows * 18 + 14);
    }

    /** Client-side factory: a dummy container of the size the server wrote into the open buffer. */
    public ItemContainerMenu(MenuType<?> type, int windowId, Inventory playerInv, FriendlyByteBuf data) {
        this(type, windowId, playerInv, new SimpleContainer(Math.max(1, data.readVarInt())));
    }

    public Container getContainer() {
        return inventory;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }
}

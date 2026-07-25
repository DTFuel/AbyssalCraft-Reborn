package com.shinoow.abyssalcraft.content.machine.researchtable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import com.shinoow.abyssalcraft.content.menu.base.ContainerMenuBase;

/**
 * Research Table menu (owned by PC-8, Stage C2a).
 *
 * <p>Ports the 1.12.2 {@code ContainerResearchTable}: no machine slots, only the player inventory (the
 * research interface itself is a knowledge browser drawn by the screen, deferred to Stage S-B). Built
 * on the PC-3 {@link ContainerMenuBase} with zero content slots.
 */
public class ResearchTableMenu extends ContainerMenuBase {

    public ResearchTableMenu(MenuType<?> type, int windowId, Inventory playerInv) {
        super(type, windowId, 0);
        addPlayerInventory(playerInv, 156);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

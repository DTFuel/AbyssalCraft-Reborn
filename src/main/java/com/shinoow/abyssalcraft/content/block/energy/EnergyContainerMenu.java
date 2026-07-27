package com.shinoow.abyssalcraft.content.block.energy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.MenuType;

public final class EnergyContainerMenu extends EnergyStorageMenu {

    public EnergyContainerMenu(MenuType<?> type, int windowId, Inventory inventory,
                               Container storage, ContainerData data) {
        super(type, windowId, inventory, storage, data);
        checkContainerDataCount(data, 2);
    }

    public EnergyContainerMenu(int windowId, Inventory inventory, FriendlyByteBuf ignored) {
        this(EnergyBlocks.ENERGY_CONTAINER_MENU.get(), windowId, inventory,
            new SimpleContainer(2), new SimpleContainerData(2));
    }
}
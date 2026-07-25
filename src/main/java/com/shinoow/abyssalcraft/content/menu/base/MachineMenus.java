package com.shinoow.abyssalcraft.content.menu.base;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Machine menu types (owned by PP-1). One example type built through {@link MenuCompat} to prove the
 * menu-registration path across both loaders; P2 machine screens reuse {@link MachineMenu}.
 */
public final class MachineMenus {

    private MachineMenus() {}

    /** {@code minecraft:menu} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    /** Example machine menu type (client factory backed by a dummy container until opened for real). */
    public static final Supplier<MenuType<MachineMenu>> MACHINE = MENUS.register("machine", () ->
        MenuCompat.create((windowId, inventory, extraData) ->
            new MachineMenu(MachineMenus.MACHINE.get(), windowId, inventory, extraData)));
}

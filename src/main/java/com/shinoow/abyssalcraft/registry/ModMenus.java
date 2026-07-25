package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.menu.base.ItemContainerMenu;
import com.shinoow.abyssalcraft.content.item.bag.CrystalBagMenu;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Canonical menu-type registry (owned by PC-3): the Stage 2 home for every {@link MenuType}.
 *
 * <p>The pilot machine menu ({@code content/menu/base/MachineMenus}, PP-1) stays as-is for the three
 * pilot machines; this is the generalized registrar that Stage 2 content (item containers, research
 * table, brewing stand) registers its menus into -- one line per menu, all built through the
 * loader-forked {@link MenuCompat#create}. Attached to the MOD bus via {@link ModRegistries#ALL}.
 */
public final class ModMenus {

    private ModMenus() {}

    /** {@code minecraft:menu} registrar in the AbyssalCraft namespace for Stage 2 menus. */
    public static final ModRegistrar<MenuType<?>> MENUS = ModRegistrar.of(Registries.MENU, AbyssalCraft.MODID);

    /**
     * Example variable-size item-container menu type (client factory reads the size from the open
     * buffer). Proves the menu-registration path on both loaders and is the shared type future
     * item-inventory containers (crystal bag / spirit tablet / spellbook) reuse.
     */
    public static final Supplier<MenuType<ItemContainerMenu>> ITEM_CONTAINER = MENUS.register("item_container", () ->
        MenuCompat.create((windowId, inventory, data) ->
            new ItemContainerMenu(ModMenus.ITEM_CONTAINER.get(), windowId, inventory, data)));

    public static final Supplier<MenuType<CrystalBagMenu>> CRYSTAL_BAG = MENUS.register("crystal_bag", () ->
        MenuCompat.create(CrystalBagMenu::new));
}

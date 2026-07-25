package com.shinoow.abyssalcraft.platform;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

//? if forge {
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.NetworkHooks;
//?} else {
/*import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
*///?}

/**
 * Compat: MenuType creation + server-side opening with a client-synced buffer (loader axis).
 *
 * <p>Forge uses {@code IForgeMenuType.create} / {@code NetworkHooks.openScreen}, NeoForge
 * {@code IMenuTypeExtension.create} / {@code Player.openMenu(provider, buffer)}; both take the same
 * {@code (windowId, inventory, buffer)} factory.
 */
public final class MenuCompat {

    private MenuCompat() {}

    /** Client-side menu factory receiving the extra data the server wrote when opening. */
    @FunctionalInterface
    public interface Factory<T extends AbstractContainerMenu> {
        T create(int windowId, Inventory inventory, FriendlyByteBuf data);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(Factory<T> factory) {
        //? if forge {
        return IForgeMenuType.create((windowId, inventory, data) -> factory.create(windowId, inventory, data));
        //?} else {
        /*return IMenuTypeExtension.create((windowId, inventory, data) -> factory.create(windowId, inventory, data));
        *///?}
    }

    /**
     * Open a container menu server-side, writing the block position into the sync buffer for the
     * client factory (block-backed menus such as the machines).
     */
    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        open(player, provider, buffer -> buffer.writeBlockPos(pos));
    }

    /**
     * Open a container menu server-side with an arbitrary sync-buffer writer, so item-backed containers
     * (crystal bag / spirit tablet / spellbook) can send their size / owner instead of a block pos.
     * Forge routes through {@code NetworkHooks.openScreen}; NeoForge through the
     * {@code Player.openMenu(MenuProvider, Consumer)} extension (its buffer is a {@code
     * RegistryFriendlyByteBuf}, a {@code FriendlyByteBuf} subtype, so the writer adapts by method ref).
     */
    public static void open(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        //? if forge {
        NetworkHooks.openScreen(player, provider, extraData);
        //?} else {
        /*player.openMenu(provider, extraData::accept);
        *///?}
    }
}

package com.shinoow.abyssalcraft.content.block.transfer;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.system.transfer.ItemTransfer;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferHost;

public final class SpiritAltarBlockEntity extends ACBlockEntity implements TickingBlockEntity {

    private static final int RANGE = 16;
    private final Set<BlockPos> hosts = new HashSet<>();
    private boolean enabled;

    public SpiritAltarBlockEntity(BlockPos pos, BlockState state) {
        super(TransferContent.SPIRIT_ALTAR_BE.get(), pos, state);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        long time = level.getGameTime();
        if (hosts.isEmpty() || time % 400L == 0L) {
            scan();
        }
        if (!enabled || time % 20L != 0L) return;
        hosts.removeIf(pos -> !level.isLoaded(pos));
        for (BlockPos hostPos : hosts) {
            BlockEntity blockEntity = level.getBlockEntity(hostPos);
            if (blockEntity == null) continue;
            ItemTransferHost host = ItemTransferAttachmentCompat.get(blockEntity);
            if (host != null && host.isTransferRunning()) {
                ItemTransfer.run(level, host);
            }
        }
    }

    public void handleTablet(Player player, ItemStack tablet) {
        int mode = SpiritTabletStorage.mode(tablet);
        if (player.isShiftKeyDown()) {
            scan();
            feedback(player, "message.abyssalcraft.spirit_altar.routes", routeCount());
            return;
        }
        if (mode == 0) {
            scan();
            feedback(player, "message.abyssalcraft.spirit_altar.scanned", hosts.size());
        } else if (mode == 1) {
            setEnabled(!enabled);
            forEachHost(host -> host.setTransferRunning(enabled));
            feedback(player, enabled ? "message.abyssalcraft.spirit_altar.enabled"
                : "message.abyssalcraft.spirit_altar.disabled", hosts.size());
        } else {
            forEachHost(ItemTransferHost::clearConfigurations);
            feedback(player, "message.abyssalcraft.spirit_altar.cleared", hosts.size());
        }
    }

    private void scan() {
        if (level == null) return;
        hosts.clear();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = worldPosition.getX() - RANGE; x <= worldPosition.getX() + RANGE; x++)
            for (int y = worldPosition.getY() - RANGE; y <= worldPosition.getY() + RANGE; y++)
                for (int z = worldPosition.getZ() - RANGE; z <= worldPosition.getZ() + RANGE; z++) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor)) continue;
                    BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (blockEntity == null || blockEntity == this) continue;
                    ItemTransferHost host = ItemTransferAttachmentCompat.get(blockEntity);
                    if (host != null && !host.getTransferConfigurations().isEmpty()) {
                        if (enabled) {
                            host.setTransferRunning(true);
                        }
                        hosts.add(cursor.immutable());
                    }
                }
    }

    private int routeCount() {
        final int[] count = {0};
        forEachHost(host -> count[0] += host.getTransferConfigurations().size());
        return count[0];
    }

    private void forEachHost(java.util.function.Consumer<ItemTransferHost> consumer) {
        if (level == null) return;
        hosts.removeIf(pos -> !level.isLoaded(pos));
        for (BlockPos pos : hosts) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            ItemTransferHost host = blockEntity == null ? null : ItemTransferAttachmentCompat.get(blockEntity);
            if (host != null) consumer.accept(host);
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            markUpdated();
        }
    }

    private static void feedback(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Enabled", enabled);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        enabled = tag.getBoolean("Enabled");
    }
}
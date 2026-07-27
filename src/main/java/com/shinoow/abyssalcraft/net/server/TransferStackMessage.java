package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server: move {@code stack} into slot {@code slot} of the open materializer bag menu
 * (owned by PS-1). Serialization is faithful for the item + count; the transfer effect is deferred
 * until the materializer bag menu is ported.
 *
 * <p><b>ItemStack wire format:</b> item registry id (UTF) + count (varint). {@code FriendlyByteBuf}'s
 * {@code writeItem/readItem} exist only on 1.20.1 (1.21 requires a registry-aware
 * {@code RegistryFriendlyByteBuf} + the stack STREAM_CODEC), so the stack is written fork-free as
 * id + count. This drops per-stack NBT/components -- acceptable for the plain material stacks the bag
 * moves; a component-carrying transfer would need a registry-aware buffer from the compat.
 */
public class TransferStackMessage implements NetworkChannel.ACPacket {

    private final int slot;
    private final ItemStack stack;

    public TransferStackMessage(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public TransferStackMessage(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
        String itemId = buf.readUtf();
        int count = buf.readVarInt();
        this.stack = new ItemStack(BuiltInRegistries.ITEM.get(ACRef.parse(itemId)), count);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
        buf.writeUtf(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        buf.writeVarInt(stack.getCount());
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Retired: modern Materializer result slots resolve and commit recipes on the server. Accepting
        // the client-provided stack here would reintroduce the legacy item-duplication trust boundary.
    }
}

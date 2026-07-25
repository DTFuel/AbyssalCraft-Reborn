package com.shinoow.abyssalcraft.content.item.bag;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.MenuCompat;

public final class CrystalBagItem extends Item {

    private final int capacity;

    public CrystalBagItem(int capacity) {
        super(new Properties().stacksTo(1));
        if (capacity <= 0) {
            throw new IllegalArgumentException("Crystal Bag capacity must be positive");
        }
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            SimpleMenuProvider provider = new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new CrystalBagMenu(windowId, inventory,
                    new CrystalBagInventory(serverPlayer, hand, bag), hand),
                Component.translatable("container.abyssalcraft.crystal_bag"));
            MenuCompat.open(serverPlayer, provider, buffer -> {
                buffer.writeVarInt(capacity);
                buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
            });
        }
        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide);
    }
}
package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.content.menu.facebook.BookOfManyFacesMenu;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.data.NecromancyData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Opens a read-only snapshot of the current world's twenty most recent fallen companions. */
public final class BookOfManyFacesItem extends TooltipCompat {

    public BookOfManyFacesItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            List<BookOfManyFacesMenu.FaceEntry> entries =
                BookOfManyFacesMenu.snapshot(NecromancyData.get(serverLevel));
            SimpleMenuProvider provider = new SimpleMenuProvider(
                (windowId, inventory, ignored) ->
                    new BookOfManyFacesMenu(windowId, inventory, hand, entries),
                Component.translatable("container.abyssalcraft.book_of_many_faces"));
            MenuCompat.open(serverPlayer, provider,
                buffer -> BookOfManyFacesMenu.writeOpenData(buffer, hand, entries));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.book_of_many_faces.1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.book_of_many_faces.2")
            .withStyle(ChatFormatting.GRAY));
    }
}
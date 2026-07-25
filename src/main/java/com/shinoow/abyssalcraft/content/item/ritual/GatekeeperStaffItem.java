package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.content.entity.misc.DimensionPortal;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.portal.DimensionData;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** The legacy Gatekeeper Staff: cycles any registered destination and places one-use portals. */
public final class GatekeeperStaffItem extends TooltipCompat {

    private static final String DESTINATION = "Destination";

    public GatekeeperStaffItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            List<DimensionData> choices = DimensionDataRegistry.instance().availableForGatewayTier(3, true);
            if (!choices.isEmpty()) {
                int index = selectedIndex(stack, choices);
                DimensionData next = choices.get((index + 1) % choices.size());
                ItemDataCompat.putString(stack, DESTINATION, next.dimension().location().toString());
                player.displayClientMessage(Component.translatable(next.displayKey()), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        ResourceKey<Level> target = DimensionDataRegistry.instance()
            .parseRegisteredDimension(ItemDataCompat.getString(context.getItemInHand(), DESTINATION))
            .orElse(null);
        if (target == null || target.equals(level.dimension())) {
            context.getPlayer().displayClientMessage(
                Component.translatable("message.portalplacer.error.2"), true);
            return InteractionResult.FAIL;
        }
        DimensionPortal portal = MiscEntities.SINGLE_PORTAL.get().create(level);
        if (portal == null) return InteractionResult.FAIL;
        portal.setDestination(target);
        portal.moveTo(context.getClickedPos().getX() + 0.5D,
            context.getClickedPos().getY() + 1.0D,
            context.getClickedPos().getZ() + 0.5D);
        return level.addFreshEntity(portal) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        DimensionDataRegistry.instance()
            .parseRegisteredDimension(ItemDataCompat.getString(stack, DESTINATION))
            .flatMap(DimensionDataRegistry.instance()::get)
            .ifPresent(data -> tooltip.add(Component.translatable(data.displayKey())));
    }

    private static int selectedIndex(ItemStack stack, List<DimensionData> choices) {
        String selected = ItemDataCompat.getString(stack, DESTINATION);
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).dimension().location().toString().equals(selected)) return i;
        }
        return -1;
    }
}
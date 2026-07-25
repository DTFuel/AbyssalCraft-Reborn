package com.shinoow.abyssalcraft.content.item.portal;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.portal.PortalAnchorBlockEntity;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.portal.DimensionData;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;

/** Tiered Gateway Key with a persistent, server-validated target dimension. */
public final class GatewayKeyItem extends TooltipCompat {

    private static final String DESTINATION_KEY = "GatewayDestination";

    private final int gatewayTier;

    public GatewayKeyItem(int gatewayTier) {
        super(new Item.Properties().stacksTo(1));
        if (gatewayTier < 0 || gatewayTier > 3) {
            throw new IllegalArgumentException("Gateway Key tier must be in [0, 3]");
        }
        this.gatewayTier = gatewayTier;
    }

    public int gatewayTier() {
        return gatewayTier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack key = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ResourceKey<Level> destination = cycleDestination(key, player.isShiftKeyDown());
            DimensionDataRegistry.instance().get(destination).ifPresent(data ->
                player.displayClientMessage(Component.translatable(data.displayKey()), true));
        }
        return InteractionResultHolder.sidedSuccess(key, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (gatewayTier != 3
            || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof PortalAnchorBlockEntity anchor)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            ResourceKey<Level> destination = selectedDestination(context.getItemInHand());
            PortalAnchorBlockEntity.ActivationResult result = anchor.toggle(
                context.getPlayer(), destination, gatewayTier);
            context.getPlayer().displayClientMessage(Component.translatable(result.translationKey()), true);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    public ResourceKey<Level> selectedDestination(ItemStack stack) {
        boolean includeVanilla = ACConfig.vanilla_handling.get();
        DimensionDataRegistry registry = DimensionDataRegistry.instance();
        return registry.parseRegisteredDimension(ItemDataCompat.getString(stack, DESTINATION_KEY))
            .filter(destination -> registry.isAvailableForGatewayTier(
                destination, gatewayTier, includeVanilla))
            .orElse(Level.OVERWORLD);
    }

    private ResourceKey<Level> cycleDestination(ItemStack stack, boolean backwards) {
        boolean includeVanilla = ACConfig.vanilla_handling.get();
        List<DimensionData> dimensions = DimensionDataRegistry.instance()
            .availableForGatewayTier(gatewayTier, includeVanilla);
        if (dimensions.isEmpty()) return Level.OVERWORLD;
        ResourceKey<Level> current = selectedDestination(stack);
        int currentIndex = -1;
        for (int i = 0; i < dimensions.size(); i++) {
            if (dimensions.get(i).dimension().equals(current)) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex;
        if (currentIndex < 0) {
            nextIndex = backwards ? dimensions.size() - 1 : 0;
        } else {
            int offset = backwards ? -1 : 1;
            nextIndex = Math.floorMod(currentIndex + offset, dimensions.size());
        }
        ResourceKey<Level> destination = dimensions.get(nextIndex).dimension();
        ItemDataCompat.putString(stack, DESTINATION_KEY, destination.location().toString());
        return destination;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.gateway_key.tier", gatewayTier)
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.gateway_key.cycle")
            .withStyle(ChatFormatting.GRAY));
        DimensionDataRegistry.instance().get(selectedDestination(stack)).ifPresent(data ->
            tooltip.add(Component.translatable("tooltip.abyssalcraft.gateway_key.destination",
                Component.translatable(data.displayKey())).withStyle(ChatFormatting.AQUA)));
    }
}
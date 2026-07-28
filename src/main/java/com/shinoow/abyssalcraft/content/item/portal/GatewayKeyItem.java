package com.shinoow.abyssalcraft.content.item.portal;

import java.util.List;

import net.minecraft.ChatFormatting;
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

import com.shinoow.abyssalcraft.content.entity.misc.DimensionPortal;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
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
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(key);
        if (!level.isClientSide) cycleAndNotify(key, player);
        return InteractionResultHolder.sidedSuccess(key, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) cycleAndNotify(context.getItemInHand(), player);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        if (!(context.getLevel() instanceof ServerLevel server)) {
            return InteractionResult.sidedSuccess(true);
        }
        ResourceKey<Level> destination = selectedDestination(context.getItemInHand());
        DimensionDataRegistry registry = DimensionDataRegistry.instance();
        if (destination.equals(server.dimension())
            || !registry.areDimensionsConnected(server.dimension(), destination, gatewayTier)) {
            player.displayClientMessage(Component.translatable("message.abyssalcraft.portal.incompatible"), true);
            return InteractionResult.FAIL;
        }
        DimensionPortal portal = MiscEntities.PORTAL.get().create(server);
        if (portal == null) return InteractionResult.FAIL;
        portal.setDestination(destination);
        portal.moveTo(context.getClickedPos().getX() + 0.5D,
            context.getClickedPos().getY() + 1.0D,
            context.getClickedPos().getZ() + 0.5D);
        return server.addFreshEntity(portal) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public ResourceKey<Level> selectedDestination(ItemStack stack) {
        DimensionDataRegistry registry = DimensionDataRegistry.instance();
        return registry.parseRegisteredDimension(ItemDataCompat.getString(stack, DESTINATION_KEY))
            .filter(destination -> registry.isGatewayKeyDestination(destination, gatewayTier))
            .orElseGet(() -> registry.gatewayKeyDestinations(gatewayTier).stream()
                .findFirst().map(DimensionData::dimension).orElse(Level.OVERWORLD));
    }

    private ResourceKey<Level> cycleDestination(ItemStack stack) {
        List<DimensionData> dimensions = DimensionDataRegistry.instance().gatewayKeyDestinations(gatewayTier);
        if (dimensions.isEmpty()) return Level.OVERWORLD;
        ResourceKey<Level> current = selectedDestination(stack);
        int currentIndex = -1;
        for (int i = 0; i < dimensions.size(); i++) {
            if (dimensions.get(i).dimension().equals(current)) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % dimensions.size();
        ResourceKey<Level> destination = dimensions.get(nextIndex).dimension();
        ItemDataCompat.putString(stack, DESTINATION_KEY, destination.location().toString());
        return destination;
    }

    private void cycleAndNotify(ItemStack stack, Player player) {
        ResourceKey<Level> destination = cycleDestination(stack);
        DimensionDataRegistry.instance().get(destination).ifPresent(data ->
            player.displayClientMessage(Component.translatable(data.displayKey()), true));
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
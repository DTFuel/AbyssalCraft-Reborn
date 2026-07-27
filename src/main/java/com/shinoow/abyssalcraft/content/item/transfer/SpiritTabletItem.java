package com.shinoow.abyssalcraft.content.item.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.shinoow.abyssalcraft.platform.CapabilityAccess;
import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.DisplayRoutesMessage;
import com.shinoow.abyssalcraft.net.server.SpiritTabletMessage;
import com.shinoow.abyssalcraft.net.server.ToggleStateMessage;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferConfiguration;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferHost;

public final class SpiritTabletItem extends TooltipCompat {

    public SpiritTabletItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tablet = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (player.isShiftKeyDown() && player.isSprinting()) {
                ACNetwork.sendToServer(new SpiritTabletMessage(-1, -1, false, true));
            } else if (player.isShiftKeyDown()) {
                int mainMode = player.getMainHandItem().getItem() instanceof SpiritTabletItem
                    ? SpiritTabletStorage.mode(player.getMainHandItem()) : -1;
                int offMode = player.getOffhandItem().getItem() instanceof SpiritTabletItem
                    ? SpiritTabletStorage.mode(player.getOffhandItem()) : -1;
                if (hand == InteractionHand.MAIN_HAND) mainMode = (mainMode + 1) % 3;
                else offMode = (offMode + 1) % 3;
                ACNetwork.sendToServer(new SpiritTabletMessage(mainMode, offMode, false, false));
            } else {
                ACNetwork.sendToServer(new SpiritTabletMessage(-1, -1, true, false));
            }
        }
        return InteractionResultHolder.sidedSuccess(tablet, level.isClientSide);
    }

    public static void openMenu(ServerPlayer player, InteractionHand hand, ItemStack tablet) {
        if (!(tablet.getItem() instanceof SpiritTabletItem) || player.getItemInHand(hand) != tablet) return;
        SimpleMenuProvider provider = new SimpleMenuProvider(
            (windowId, inventory, ignored) -> new SpiritTabletMenu(windowId, inventory,
                new SpiritTabletInventory(player, hand, tablet), hand, tablet),
            Component.translatable("container.abyssalcraft.spirit_tablet"));
        MenuCompat.open(player, provider, buffer -> buffer.writeBoolean(hand == InteractionHand.MAIN_HAND));
        List<BlockPos> route = SpiritTabletStorage.route(tablet);
        if (!route.isEmpty()) {
            CompoundTag root = new CompoundTag();
            ListTag routes = new ListTag();
            ListTag points = new ListTag();
            for (int index = 0; index < Math.min(route.size(), 64); index++) {
                points.add(LongTag.valueOf(route.get(index).asLong()));
            }
            routes.add(points);
            root.put("Routes", routes);
            ACNetwork.sendToPlayer(player, new DisplayRoutesMessage(root));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack tablet = context.getItemInHand();
        if (player == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockPos pos = context.getClickedPos();
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) ACNetwork.sendToServer(new ToggleStateMessage(pos));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        if (!player.mayUseItemAt(pos.relative(context.getClickedFace()), context.getClickedFace(), tablet)) {
            return InteractionResult.FAIL;
        }
        if (level.getBlockEntity(pos) instanceof com.shinoow.abyssalcraft.content.block.transfer.SpiritAltarBlockEntity altar) {
            altar.handleTablet(player, tablet);
            return InteractionResult.CONSUME;
        }
        int mode = SpiritTabletStorage.mode(tablet);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (mode == 0) {
            boolean inventory = blockEntity != null
                && CapabilityAccess.itemView(level, pos, context.getClickedFace()) != null;
            SpiritTabletStorage.appendRoute(tablet, inventory ? pos : pos.above(), context.getClickedFace(),
                level.dimension().location());
            feedback(player, inventory ? "message.abyssalcraft.spirit_tablet.path_added"
                : "message.abyssalcraft.spirit_tablet.waypoint_added");
            return InteractionResult.CONSUME;
        }
        if (blockEntity == null) {
            feedback(player, "message.abyssalcraft.spirit_tablet.no_host");
            return InteractionResult.FAIL;
        }
        if (mode == 1 && CapabilityAccess.itemView(level, pos, context.getClickedFace()) == null) {
            feedback(player, "message.abyssalcraft.spirit_tablet.no_inventory");
            return InteractionResult.FAIL;
        }
        ItemTransferHost host = ItemTransferAttachmentCompat.getOrCreate(blockEntity);
        if (mode == 2) {
            host.clearConfigurations();
            feedback(player, "message.abyssalcraft.spirit_tablet.cleared");
            return InteractionResult.CONSUME;
        }
        List<BlockPos> route = SpiritTabletStorage.route(tablet);
        if (route.isEmpty() || !SpiritTabletStorage.isRouteDimension(tablet, level.dimension().location())) {
            feedback(player, "message.abyssalcraft.spirit_tablet.bad_route");
            return InteractionResult.FAIL;
        }
        BlockPos destination = route.get(route.size() - 1);
        if (!level.isLoaded(destination)
                || CapabilityAccess.itemView(level, destination, SpiritTabletStorage.entrySide(tablet)) == null) {
            feedback(player, "message.abyssalcraft.spirit_tablet.no_destination");
            return InteractionResult.FAIL;
        }
        List<BlockPos> fullRoute = new ArrayList<>(route.size() + 1);
        fullRoute.add(pos);
        fullRoute.addAll(route);
        ItemTransferConfiguration configuration = new ItemTransferConfiguration(fullRoute)
            .exitSide(context.getClickedFace())
            .entrySide(SpiritTabletStorage.entrySide(tablet))
            .ignoreSubtypes(SpiritTabletStorage.ignoreSubtypes(tablet))
            .matchComponents(SpiritTabletStorage.matchComponents(tablet));
        NonNullListCopy.copyFilter(tablet, player, configuration);
        host.addTransferConfiguration(configuration);
        SpiritTabletStorage.clearRoute(tablet);
        feedback(player, "message.abyssalcraft.spirit_tablet.applied");
        return InteractionResult.CONSUME;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.spirit_tablet.mode",
            SpiritTabletStorage.mode(stack)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.spirit_tablet.route",
            SpiritTabletStorage.route(stack).size()).withStyle(ChatFormatting.GRAY));
    }

    private static void feedback(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private static final class NonNullListCopy {
        private static void copyFilter(ItemStack tablet, Player player, ItemTransferConfiguration configuration) {
            net.minecraft.core.NonNullList<ItemStack> filter = SpiritTabletStorage.loadFilter(tablet,
                player.level().registryAccess());
            for (int slot = 0; slot < filter.size(); slot++) {
                configuration.filterSlot(slot, filter.get(slot));
            }
        }
    }
}
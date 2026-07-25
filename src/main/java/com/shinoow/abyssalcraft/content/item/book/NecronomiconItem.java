package com.shinoow.abyssalcraft.content.item.book;

import java.util.List;

import com.shinoow.abyssalcraft.client.necronomicon.ACNecronomicon;
import com.shinoow.abyssalcraft.platform.SideExecutor;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyTransporterItem;
import com.shinoow.abyssalcraft.system.energy.EnergyItemInteractions;
import com.shinoow.abyssalcraft.system.energy.structure.StructureHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * The Necronomicon book item (upstream content that unblocks PH-5, so the death-book GUI is reachable
 * in-game). Faithful to the 1.12.2 {@code ItemNecronomicon}: a stack-of-one book of a given
 * {@code bookType} (0-4) that opens the Necronomicon GUI on a non-sneak right-click.
 *
 * <p>Ported: the open-on-use behaviour (the PH-5 {@link ACNecronomicon#open()} screen, dispatched
 * client-side through {@link SideExecutor} so the client-only screen never class-loads on a dedicated
 * server -- the same deferred-classload idiom the main class uses for client setup). Deferred with
 * their unported systems: the sneak-use {@code NecronomiconActionRegistry} actions and per-book entry
 * gating by {@code bookType} (PS-8b). As the faithful 1.12.2 {@code IEnergyTransporterItem}, the book
 * holds Potential Energy sized by its tier ({@link #getMaxEnergy}); the PE is stored in the stack's mod
 * data (PS-5 {@link IEnergyTransporterItem}) and shown in its tooltip. The deity-worship source that
 * fills it and the ritual / spell consumers that drain it land with the energy blocks / spell system.
 */
public class NecronomiconItem extends TooltipCompat implements IEnergyTransporterItem {

    /** Faithful PE capacity per book tier (1.12.2 {@code ItemNecronomicon.getMaxEnergy}). */
    private static final int[] MAX_ENERGY = {5000, 10000, 20000, 40000, 100000};

    private final int bookType;

    public NecronomiconItem(int bookType) {
        super(new Properties().stacksTo(1));
        this.bookType = bookType;
    }

    /** The book tier (0 = Necronomicon .. 4 = Abyssalnomicon); gates which entries show (PS-8b). */
    public int bookType() {
        return bookType;
    }

    @Override
    public int getMaxEnergy(ItemStack stack) {
        return bookType >= 0 && bookType < MAX_ENERGY.length ? MAX_ENERGY[bookType] : 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            // Sneak-use runs the NecronomiconActionRegistry action in 1.12.2 (deferred: unported actions).
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            SideExecutor.runWhenClient(() -> () -> ACNecronomicon.open(bookType));
        } else if (player instanceof ServerPlayer serverPlayer) {
            com.shinoow.abyssalcraft.common.handlers.KnowledgeHooks.onBookOpened(serverPlayer, bookType);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult energyInteraction = EnergyItemInteractions.placeInEnergyBlock(context);
        if (energyInteraction != InteractionResult.PASS) {
            return energyInteraction;
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        StructureHandler structures = StructureHandler.instance();
        if (!structures.canFormStructure(level, pos, bookType, player)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            structures.formStructure(level, pos, bookType, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.book_tier", bookType).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.potential_energy",
            (int) getContainedEnergy(stack), getMaxEnergy(stack)).withStyle(ChatFormatting.AQUA));
    }
}

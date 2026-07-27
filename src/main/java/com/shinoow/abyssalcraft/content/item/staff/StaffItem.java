package com.shinoow.abyssalcraft.content.item.staff;

import java.util.List;

import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyTransporterItem;
import com.shinoow.abyssalcraft.system.energy.EnergyItemInteractions;
import com.shinoow.abyssalcraft.system.spell.SpellUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

/**
 * A spell-casting staff (upstream content: the first spell consumer of Potential Energy, proving the PS-7
 * cast loop end-to-end). Like the Necronomicon, it is an {@link IEnergyTransporterItem} that holds PE
 * (charged at a deity statue, CR-59); a right-click ray-traces the mob the player is aiming at and casts a
 * fixed pilot spell ({@link StaffSpells#LIFE_DRAIN}) through {@link SpellUtils}, draining the staff's PE.
 *
 * <p>Simplification vs 1.12.2: it casts one hard-wired spell rather than the scroll-selected spell of the
 * Staff of the Gate (scroll inscription / selection is the PS-7b follow-up). The cast itself is faithful
 * (server-side raytrace + PE drain + effect) and fork-free.
 */
public class StaffItem extends TooltipCompat implements IEnergyTransporterItem {

    private static final int MAX_ENERGY = 20000;

    public StaffItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxEnergy(ItemStack stack) {
        return MAX_ENERGY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !SpellUtils.castManifest(level, player, StaffSpells.lifeDrain(), stack,
            com.shinoow.abyssalcraft.system.spell.ScrollType.BASIC, null)) {
            // No target in range or not enough PE: give the player audible/visual feedback rather than casting.
            player.displayClientMessage(Component.translatable("message.abyssalcraft.spell.fizzle"), true);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return EnergyItemInteractions.placeInEnergyBlock(context);
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.spell",
            Component.translatable(StaffSpells.lifeDrain().translationKey())).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.potential_energy",
            (int) getContainedEnergy(stack), getMaxEnergy(stack)).withStyle(ChatFormatting.AQUA));
    }
}

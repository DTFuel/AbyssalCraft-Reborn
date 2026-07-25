package com.shinoow.abyssalcraft.content.item.energy;

import java.util.List;

import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.IEnergyManipulator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** One of the legacy ritual charms that activates or clears a PE manipulator amplifier. */
public final class AmplifierCharmItem extends TooltipCompat {

    private final AmplifierType amplifier;
    private final DeityType deity;

    public AmplifierCharmItem(AmplifierType amplifier, DeityType deity) {
        super(new Item.Properties());
        this.amplifier = amplifier;
        this.deity = deity;
    }

    public AmplifierType amplifier() {
        return amplifier;
    }

    public DeityType deity() {
        return deity;
    }

    @Override
    public String getDescriptionId() {
        return "item.abyssalcraft." + familyId();
    }

    private String familyId() {
        if (deity == null) {
            return "charm";
        }
        return switch (deity) {
            case CTHULHU -> "cthulhucharm";
            case HASTUR -> "hasturcharm";
            case JZAHAR -> "jzaharcharm";
            case AZATHOTH -> "azathothcharm";
            case NYARLATHOTEP -> "nyarlathotepcharm";
            case SHUBNIGGURATH -> "shubniggurathcharm";
            case YOGSOTHOTH -> "yogsothothcharm";
        };
    }

    /** Apply this charm; returns whether the manipulator state changed. */
    public boolean applyTo(IEnergyManipulator manipulator) {
        return amplifier == null ? manipulator.clearActive() : manipulator.setActive(amplifier, deity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof IEnergyManipulator manipulator)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && applyTo(manipulator)) {
            if (amplifier != null && context.getPlayer() != null
                && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            level.playSound(null, context.getClickedPos(), SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS, 0.2F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.charm.amplifier",
            amplifier == null ? Component.translatable("tooltip.abyssalcraft.charm.none")
                : Component.translatable("tooltip.abyssalcraft.charm." + amplifier.name().toLowerCase()))
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.abyssalcraft.charm.deity",
            deity == null ? Component.translatable("tooltip.abyssalcraft.charm.none") : deity.displayName())
            .withStyle(ChatFormatting.GRAY));
    }
}
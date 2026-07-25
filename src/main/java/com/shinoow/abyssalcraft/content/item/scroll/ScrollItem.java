package com.shinoow.abyssalcraft.content.item.scroll;

import java.util.List;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;
import com.shinoow.abyssalcraft.system.spell.IScroll;
import com.shinoow.abyssalcraft.system.spell.ScrollType;
import com.shinoow.abyssalcraft.system.spell.Spell;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** A quality-gated spell scroll with its inscription stored in version-neutral custom data. */
public class ScrollItem extends TooltipCompat implements IScroll {

    public static final String SPELL_KEY = "Spell";
    private final ScrollType type;

    public ScrollItem(ScrollType type) {
        super(new Item.Properties().stacksTo(1));
        this.type = type;
    }

    @Override
    public ScrollType getScrollType(ItemStack stack) {
        return type;
    }

    public static String spellId(ItemStack stack) {
        return ItemDataCompat.getString(stack, SPELL_KEY);
    }

    public static ItemStack inscribe(ItemStack stack, Spell spell) {
        ItemStack result = stack.copyWithCount(1);
        ItemDataCompat.putString(result, SPELL_KEY, spell.id());
        return result;
    }

    //? if >=1.21 {
    /*@Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return useDuration(stack);
    }
    *///?} else {
    @Override
    public int getUseDuration(ItemStack stack) {
        return useDuration(stack);
    }
    //?}

    private int useDuration(ItemStack stack) {
        Spell spell = SpellRegistry.instance().getSpell(spellId(stack));
        return spell != null && spell.requiresCharging() ? 50 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return useDuration(stack) > 0 ? UseAnim.BOW : UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Spell spell = SpellRegistry.instance().getSpell(spellId(stack));
        if (spell != null && spell.requiresCharging()) player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // The server-authoritative cast pipeline is wired with the complete spell roster.
        return stack;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tiereditem.tier", type.quality()));
        Spell spell = SpellRegistry.instance().getSpell(spellId(stack));
        if (spell != null) tooltip.add(Component.translatable(spell.translationKey()));
    }
}
package com.shinoow.abyssalcraft.content.item.misc;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.TooltipCompat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Ten-dose drink that clears one plague and grants its antidote effect. */
public final class AntidoteItem extends TooltipCompat {

    private static final String CONTENT = "AbyssalCraftAntidoteUses";
    private final Supplier<MobEffect> plague;
    private final Supplier<MobEffect> antidote;

    public AntidoteItem(Supplier<MobEffect> plague, Supplier<MobEffect> antidote) {
        super(new Properties().stacksTo(1));
        this.plague = plague;
        this.antidote = antidote;
    }

    public static float visualContent(ItemStack stack) {
        int content = ItemDataCompat.getInt(stack, CONTENT, 10);
        if (content < 9 && content > 6) return 0.2F;
        if (content < 7 && content > 4) return 0.4F;
        if (content < 5 && content > 2) return 0.6F;
        if (content < 3 && content > 0) return 0.8F;
        return 0.0F;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    //? if >=1.21 {
    /*@Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }
    *///?} else {
    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }
    //?}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide) {
            MobEffectCompat.removeEffect(consumer, plague);
            consumer.addEffect(MobEffectCompat.effectInstance(antidote, 1200, 0));
        }
        if (consumer instanceof Player player && player.getAbilities().instabuild) return stack;
        int remaining = ItemDataCompat.getInt(stack, CONTENT, 10) - 1;
        if (remaining <= 0) return new ItemStack(Items.GLASS_BOTTLE);
        ItemDataCompat.putInt(stack, CONTENT, remaining);
        return stack;
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.abyssalcraft.antidote.contents",
            ItemDataCompat.getInt(stack, CONTENT, 10)));
    }
}
package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;

import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.StaffOfRendingMessage;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.platform.EnchantmentDataCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;
import com.shinoow.abyssalcraft.system.spell.SpellUtils;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Tiered Staff of Rending with per-recipe energy ledgers. */
public final class StaffOfRendingItem extends RitualEnergyItem {

    private final int tier;

    public StaffOfRendingItem(int tier) {
        super(100 * (tier + 1));
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            LivingEntity target = SpellUtils.rayTraceTarget(player, 50.0F);
            if (target instanceof Mob) {
                ACNetwork.sendToServer(new StaffOfRendingMessage(target.getId(), hand));
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            redeem(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public boolean rend(ServerPlayer player, LivingEntity target, ItemStack stack) {
        if (!(target instanceof Mob) || target.level() != player.level() || !target.isAlive()
            || player.distanceToSqr(target) > 2500.0D || !player.hasLineOfSight(target)) return false;
        List<RendingRecipe> matches = DataRecipeCompat.allOfType(player.level(), ModRecipes.RENDING.get()).stream()
            .filter(recipe -> RendingEnergyType.fromRecipe(recipe)
                .filter(type -> type.validates(recipe) && type.matches(target)).isPresent())
            .toList();
        if (matches.isEmpty()) return false;
        int amount = drainAmount(stack);
        if (!target.hurt(player.damageSources().playerAttack(player), amount)) return false;
        for (RendingRecipe recipe : matches) {
            RendingEnergyType.fromRecipe(recipe).ifPresent(type -> type.add(stack, amount));
        }
        return true;
    }

    public int multiRendLevel(ItemStack stack) {
        return EnchantmentDataCompat.read(stack).getOrDefault(ACRef.id("multi_rend"), 0);
    }

    public int drainAmount(ItemStack stack) {
        int sapping = EnchantmentDataCompat.read(stack).getOrDefault(ACRef.id("sapping"), 0);
        return tier + 1 + Math.max(0, Math.min(3, sapping));
    }

    public int getEnergy(ItemStack stack, RendingEnergyType type) {
        return type.get(stack);
    }

    public void setEnergy(ItemStack stack, RendingEnergyType type, int amount) {
        type.set(stack, amount);
    }

    private static void redeem(ServerPlayer player, ItemStack stack) {
        for (RendingRecipe recipe : DataRecipeCompat.allOfType(player.level(), ModRecipes.RENDING.get())) {
            RendingEnergyType type = RendingEnergyType.fromRecipe(recipe).orElse(null);
            if (type == null || !type.validates(recipe)) continue;
            int stored = type.get(stack);
            while (stored >= recipe.maxEnergy()) {
                ItemStack result = recipe.result().copy();
                if (!player.getInventory().add(result)) player.drop(result, false);
                stored -= recipe.maxEnergy();
            }
            type.set(stack, stored);
        }
    }

    @Override
    protected void appendTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tiereditem.tier", tier));
        for (RendingEnergyType type : RendingEnergyType.values()) {
            tooltip.add(Component.translatable("tooltip.abyssalcraft.rending_energy",
                Component.translatable(type.translationKey()), type.get(stack), type.threshold()));
        }
        super.appendTooltip(stack, tooltip, flag);
    }
}
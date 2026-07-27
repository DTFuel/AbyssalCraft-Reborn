package com.shinoow.abyssalcraft.system.ritual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.EnchantmentDataCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Combines the eight pedestal enchanted books onto the altar item. */
public final class MassEnchantBehavior implements RitualBehavior {

    @Override
    public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                            Player player, RitualHost host) {
        ItemStack target = host.ritualCenter();
        if (!EnchantmentDataCompat.canStore(target) || target.is(Items.BOOK) && !ACConfig.enchantBooks.get()) {
            return false;
        }
        List<ItemStack> books = host.ritualOfferingSnapshot().stream()
            .filter(stack -> !stack.isEmpty()).toList();
        return books.size() == 8 && books.stream().allMatch(book ->
            book.is(Items.ENCHANTED_BOOK)
                && !EnchantmentDataCompat.read(book).isEmpty()
                && (ACConfig.enchantMergedBooks.get() || !EnchantmentDataCompat.hasRepairCost(book)));
    }

    @Override
    public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                         Player player, RitualHost host) {
        if (!(level instanceof ServerLevel server)) return;
        int maximum = ACConfig.enchantmentMaxLevel.get();
        Map<ResourceLocation, Integer> combined = new LinkedHashMap<>();
        for (ItemStack book : host.ritualOfferingSnapshot()) {
            EnchantmentDataCompat.read(book).forEach((id, levelValue) ->
                combined.merge(id, Math.min(levelValue, maximum),
                    (left, right) -> Math.min(left + right, maximum)));
        }
        if (combined.isEmpty()) throw new IllegalStateException("Mass Enchanting has no enchantments to apply");
        host.setRitualCenter(EnchantmentDataCompat.apply(
            host.ritualCenter(), combined, server.registryAccess()));
    }
}
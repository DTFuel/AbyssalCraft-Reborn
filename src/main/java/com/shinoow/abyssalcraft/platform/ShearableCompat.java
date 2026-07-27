package com.shinoow.abyssalcraft.platform;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

//? if forge {
import net.minecraftforge.common.IForgeShearable;
//?} else {
/*import net.neoforged.neoforge.common.IShearable;
*///?}

/** Loader-neutral name for the loader shearing contract. */
//? if forge {
public interface ShearableCompat extends IForgeShearable {

	@Override
	default boolean isShearable(ItemStack stack, Level level, BlockPos pos) {
		return acIsShearable(null, stack, level, pos);
	}

	@Override
	default List<ItemStack> onSheared(Player player, ItemStack stack, Level level, BlockPos pos, int fortune) {
		return acOnSheared(player, stack, level, pos);
	}

	boolean acIsShearable(Player player, ItemStack stack, Level level, BlockPos pos);

	List<ItemStack> acOnSheared(Player player, ItemStack stack, Level level, BlockPos pos);
}
//?} else {
/*public interface ShearableCompat extends IShearable {

	@Override
	default boolean isShearable(Player player, ItemStack stack, Level level, BlockPos pos) {
		return acIsShearable(player, stack, level, pos);
	}

	@Override
	default List<ItemStack> onSheared(Player player, ItemStack stack, Level level, BlockPos pos) {
		return acOnSheared(player, stack, level, pos);
	}

	boolean acIsShearable(Player player, ItemStack stack, Level level, BlockPos pos);

	List<ItemStack> acOnSheared(Player player, ItemStack stack, Level level, BlockPos pos);
}
*///?}
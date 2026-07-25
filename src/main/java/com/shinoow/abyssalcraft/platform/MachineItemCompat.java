package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import com.shinoow.abyssalcraft.content.item.MachineRemainderItem;
//? if forge {
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
//?} else {
/*import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
*///?}

public final class MachineItemCompat {

    private MachineItemCompat() {}

    public static ItemStack craftingRemainder(ItemStack consumed) {
        if (!consumed.isEmpty() && consumed.getItem() instanceof MachineRemainderItem dynamic) {
            return dynamic.machineRemainder(consumed);
        }
        if (consumed.isEmpty() || !consumed.getItem().hasCraftingRemainingItem()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(consumed.getItem().getCraftingRemainingItem());
    }

    public static int fluidAmount(ItemStack stack, ResourceLocation fluidId) {
        if (stack.isEmpty()) {
            return 0;
        }
        //? if forge {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        //?} else {
        /*IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        *///?}
        if (handler == null) {
            return bucketAmount(stack, fluidId);
        }
        int amount = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (fluidId.equals(BuiltInRegistries.FLUID.getKey(handler.getFluidInTank(tank).getFluid()))) {
                amount += handler.getFluidInTank(tank).getAmount();
            }
        }
        if (amount > 0) {
            return amount;
        }
        return bucketAmount(stack, fluidId);
    }

    private static int bucketAmount(ItemStack stack, ResourceLocation fluidId) {
        if (!(stack.getItem() instanceof BucketItem bucket)) {
            return 0;
        }
        //? if >=1.21 {
        /*Fluid fluid = bucket.content;
        *///?} else {
        Fluid fluid = bucket.getFluid();
        //?}
        return fluidId.equals(BuiltInRegistries.FLUID.getKey(fluid)) ? 1000 : 0;
    }

    public static boolean is(ItemStack stack, String id) {
        ResourceLocation key = ACRef.parse(id);
        if (!BuiltInRegistries.ITEM.containsKey(key)) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.get(key);
        return stack.is(item);
    }
}
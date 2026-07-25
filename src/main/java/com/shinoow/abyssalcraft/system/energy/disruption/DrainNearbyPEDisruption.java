package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.IEnergyCollector;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.IEnergyManipulator;
import com.shinoow.abyssalcraft.system.energy.PEUtils;
import com.shinoow.abyssalcraft.system.energy.structure.IStructureComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Drain nearby collectors, falling back to players' charged PE items when no collector exists. */
public final class DrainNearbyPEDisruption extends Disruption {

    public DrainNearbyPEDisruption() {
        super("potentialEnergyDrain", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        int drainedCollectors = level.getBlockEntity(pos) instanceof IEnergyManipulator manipulator
            ? drainForManipulator(level, pos, manipulator)
            : drainDefaultArea(level, pos);
        if (drainedCollectors == 0) {
            drainPlayers(level, players);
        }
    }

    private static int drainForManipulator(Level level, BlockPos origin, IEnergyManipulator manipulator) {
        int rangeAmplifiers = PEUtils.getBlockAmplifiers(level, origin, AmplifierType.RANGE);
        if (manipulator instanceof IStructureComponent component) {
            rangeAmplifiers += (int) PEUtils.getStructureAmplifier(level, component, AmplifierType.RANGE);
        }
        int range = (int) (rangeAmplifiers + manipulator.getAmplifier(AmplifierType.RANGE) / 2.0F);
        return drainCollectors(level, origin, 3 + range, rangeAmplifiers, 4);
    }

    private static int drainDefaultArea(Level level, BlockPos origin) {
        return drainCollectors(level, origin, 7, 2, 2);
    }

    private static int drainCollectors(Level level, BlockPos origin, int horizontalRange,
                                       int depth, int normalDivisor) {
        int found = 0;
        for (int x = -horizontalRange; x <= horizontalRange; x++) {
            for (int y = 0; y <= depth; y++) {
                for (int z = -horizontalRange; z <= horizontalRange; z++) {
                    if (level.getBlockEntity(origin.offset(x, -y, z)) instanceof IEnergyCollector collector) {
                        int divisor = level.random.nextInt(4) == 0 ? 1 : normalDivisor;
                        collector.consumeEnergy(collector.getContainedEnergy() / divisor);
                        found++;
                    }
                }
            }
        }
        return found;
    }

    private static void drainPlayers(Level level, List<Player> players) {
        for (Player player : players) {
            int mainInventorySize = Math.min(36, player.getInventory().getContainerSize());
            for (int slot = 0; slot < mainInventorySize; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.getItem() instanceof IEnergyContainerItem energyItem) {
                    float contained = energyItem.getContainedEnergy(stack);
                    if (contained <= 0) {
                        continue;
                    }
                    int divisor = level.random.nextInt(4) == 0 ? 2 : 10;
                    energyItem.consumeEnergy(stack, contained / divisor);
                    if (BookItems.ALL.stream().anyMatch(book -> stack.is(book.get()))) {
                        player.hurt(level.damageSources().magic(), 2.0F);
                    }
                }
            }
        }
    }
}
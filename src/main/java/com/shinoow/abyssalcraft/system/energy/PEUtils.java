package com.shinoow.abyssalcraft.system.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.PEStreamMessage;
import com.shinoow.abyssalcraft.system.energy.structure.IStructureBase;
import com.shinoow.abyssalcraft.system.energy.structure.IStructureComponent;

/**
 * The Potential Energy network backbone (owned by PS-5), faithful to the 1.12.2 {@code api.energy.PEUtils}
 * core: the energy arithmetic and the manipulator&rarr;collector block-entity transfer. Fork-free (only
 * vanilla {@code Level}/{@code BlockEntity}/{@code BlockPos}).
 *
 * <p>The 1.12.2 transfer-to-players / transfer-to-dropped-items paths depend on the Necronomicon check +
 * energy-container items (unported), and the visual PE stream on PS-1's {@code PEStreamMessage}; those
 * land with the energy content + the Necronomicon (deferred).
 */
public final class PEUtils {

    private PEUtils() {}

    /** Add {@code energy} to {@code container}; returns the overflow that did not fit. */
    public static float addEnergy(IEnergyContainer container, float energy) {
        if (energy <= 0) {
            return 0;
        }
        float room = container.getMaxEnergy() - container.getContainedEnergy();
        float added = Math.min(room, energy);
        if (added > 0) {
            container.setEnergy(container.getContainedEnergy() + added);
        }
        return energy - added;
    }

    /** Consume up to {@code energy} from {@code container}; returns the amount actually consumed. */
    public static float consumeEnergy(IEnergyContainer container, float energy) {
        if (energy <= 0) {
            return 0;
        }
        float consumed = Math.min(container.getContainedEnergy(), energy);
        if (consumed > 0) {
            container.setEnergy(container.getContainedEnergy() - consumed);
        }
        return consumed;
    }

    /**
     * Move up to {@code amount} of Potential Energy from {@code from} to {@code to}, honouring the
     * target's remaining room. Returns the amount transferred.
     */
    public static float transfer(IEnergyContainer from, IEnergyContainer to, float amount) {
        if (!from.canTransferPE() || !to.canAcceptPE()) {
            return 0;
        }
        float pulled = consumeEnergy(from, Math.min(amount, from.getContainedEnergy()));
        float overflow = addEnergy(to, pulled);
        if (overflow > 0) {
            // Return the part that did not fit back to the source.
            addEnergy(from, overflow);
        }
        return pulled - overflow;
    }

    /** Move PE from an item into a block container, returning the amount transferred. */
    public static float transferFromItem(ItemStack stack, IEnergyContainer to, float amount) {
        if (!(stack.getItem() instanceof IEnergyContainerItem item) || !item.canTransferPE(stack)
            || !to.canAcceptPE()) {
            return 0;
        }
        float pulled = item.consumeEnergy(stack, amount);
        float overflow = addEnergy(to, pulled);
        if (overflow > 0) {
            item.addEnergy(stack, overflow);
        }
        return pulled - overflow;
    }

    /** Move PE from a block container into an item, returning the amount transferred. */
    public static float transferToItem(IEnergyContainer from, ItemStack stack, float amount) {
        if (!(stack.getItem() instanceof IEnergyContainerItem item) || !from.canTransferPE()
            || !item.canAcceptPE(stack)) {
            return 0;
        }
        float pulled = consumeEnergy(from, amount);
        float overflow = item.addEnergy(stack, pulled);
        if (overflow > 0) {
            addEnergy(from, overflow);
        }
        return pulled - overflow;
    }

    /** Pull PE from the container immediately in {@code direction} into {@code transporter}. */
    public static float collectAdjacent(Level level, BlockPos pos, Direction direction,
                                        IEnergyTransporter transporter) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
        if (blockEntity instanceof IEnergyContainer source && source != transporter) {
            return transfer(source, transporter, transporter.getDrainQuanta());
        }
        return 0;
    }

    /** Find the first PE container along an unobstructed ray. */
    public static IEnergyContainer findContainer(Level level, BlockPos pos, Direction direction, int range) {
        for (int distance = 1; distance <= range; distance++) {
            BlockPos targetPos = pos.relative(direction, distance);
            BlockEntity blockEntity = level.getBlockEntity(targetPos);
            if (blockEntity instanceof IEnergyContainer container) {
                return container;
            }
            var state = level.getBlockState(targetPos);
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, targetPos)) {
                return null;
            }
        }
        return null;
    }

    /** Send PE from {@code transporter} to the first visible container in {@code direction}. */
    public static float transferInDirection(Level level, BlockPos pos, Direction direction,
                                            IEnergyTransporter transporter) {
        IEnergyContainer target = findContainer(level, pos, direction, transporter.getTransferRange());
        if (target == null || target == transporter) return 0;
        float transferred = transfer(transporter, target, transporter.getTransferQuanta());
        if (transferred > 0.0F && target instanceof BlockEntity blockEntity) {
            broadcastPEStream(level, pos, blockEntity.getBlockPos());
        }
        return transferred;
    }

    /** Rescan the legacy horizontal collector ring, retaining at most twenty collectors. */
    public static void locateCollectors(Level level, BlockPos origin, IEnergyManipulator manipulator) {
        var collectors = manipulator.getEnergyCollectors();
        collectors.clear();
        int blockBoost = getBlockAmplifiers(level, origin, AmplifierType.RANGE);
        int structureBoost = manipulator instanceof IStructureComponent component
            ? (int) getStructureAmplifier(level, component, AmplifierType.RANGE)
            : 0;
        int verticalRange = blockBoost + structureBoost;
        int boost = Math.max(0, verticalRange
            + (int) (manipulator.getAmplifier(AmplifierType.RANGE) / 2.0F));
        int radius = 3 + boost;
        for (int x = -radius; x <= radius && collectors.size() < 20; x++) {
            for (int y = 0; y <= verticalRange && collectors.size() < 20; y++) {
                for (int z = -radius; z <= radius && collectors.size() < 20; z++) {
                    if (x >= -2 && x <= 2 && z >= -2 && z <= 2) {
                        continue;
                    }
                    BlockPos target = origin.offset(x, -y, z);
                    if (level.getBlockEntity(target) instanceof IEnergyCollector) {
                        collectors.add(target.immutable());
                    }
                }
            }
        }
    }

    /** Legacy gate: manipulators cannot operate beside another manipulator, including two blocks vertically. */
    public static boolean hasNoAdjacentManipulators(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof IEnergyManipulator) {
                return false;
            }
        }
        return !(level.getBlockEntity(pos.above(2)) instanceof IEnergyManipulator)
            && !(level.getBlockEntity(pos.below(2)) instanceof IEnergyManipulator);
    }

    /** Legacy gate: a collector adjacent to another collector is not fed by a manipulator. */
    public static boolean hasNoAdjacentCollectors(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof IEnergyCollector) {
                return false;
            }
        }
        return true;
    }

    /** Read a valid Place of Power bonus, clearing stale component membership. */
    public static float getStructureAmplifier(Level level, IStructureComponent component, AmplifierType type) {
        BlockPos basePos = component.getBasePosition();
        if (component.isInMultiblock() && basePos != null
            && level.getBlockEntity(basePos) instanceof IStructureBase base) {
            return base.getAmplifier(type);
        }
        if (component.isInMultiblock() || basePos != null) {
            component.setInMultiblock(false);
            component.setBasePosition(null);
        }
        return 0.0F;
    }

    /** Count up to two matching amplifier blocks directly below a manipulator. */
    public static int getBlockAmplifiers(Level level, BlockPos pos, AmplifierType type) {
        int amplifiers = 0;
        for (int distance = 1; distance <= 2; distance++) {
            if (level.getBlockState(pos.below(distance)).getBlock() instanceof IEnergyAmplifier amplifier
                && amplifier.getAmplifierType() == type) {
                amplifiers++;
            } else {
                break;
            }
        }
        return amplifiers;
    }

    /** Feed one transporter item from a manipulator. */
    public static float transferToItem(IEnergyManipulator manipulator, ItemStack stack, int tolerance) {
        if (!(stack.getItem() instanceof IEnergyTransporterItem item) || !item.canAcceptPE(stack)
            || !manipulator.canTransferPE()) {
            return 0.0F;
        }
        float offered = manipulator.getEnergyQuanta();
        float transferred = offered - item.addEnergy(stack, offered);
        if (transferred > 0) {
            manipulator.addTolerance(tolerance);
        }
        return transferred;
    }

    /** Preserve the legacy random dropped-item charging cadence around a manipulator. */
    public static int transferToDroppedItems(Level level, BlockPos origin, IEnergyManipulator manipulator,
                                             int range) {
        int interval = Math.max(1, 120 - (int) (20 * manipulator.getAmplifier(AmplifierType.DURATION)));
        int charged = 0;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(range))) {
            if (level.random.nextInt(interval) == 0
                && transferToItem(manipulator, itemEntity.getItem(), manipulator.isActive() ? 4 : 2) > 0) {
                broadcastPEStream(level, origin, itemEntity.blockPosition());
                charged++;
            }
        }
        return charged;
    }

    /**
     * Feed the manipulator's energy quanta to each collector block-entity it tracks. Returns the number
     * of collectors that received energy this call.
     */
    public static int transferToCollectors(Level level, IEnergyManipulator manipulator) {
        if (!manipulator.canTransferPE()) {
            return 0;
        }
        int fed = 0;
        int interval = Math.max(1, 120 - (int) (20 * manipulator.getAmplifier(AmplifierType.DURATION)));
        for (BlockPos pos : manipulator.getEnergyCollectors()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IEnergyCollector collector && collector.canAcceptPE()
                && hasNoAdjacentCollectors(level, pos) && level.random.nextInt(interval) == 0
                && manipulator.canTransferPE()) {
                float quanta = manipulator.getEnergyQuanta();
                if (quanta <= 0) {
                    continue;
                }
                collector.addEnergy(quanta);
                manipulator.addTolerance(manipulator.isActive() ? 2 : 1);
                if (manipulator instanceof BlockEntity origin) {
                    broadcastPEStream(level, origin.getBlockPos(), pos);
                }
                fed++;
            }
        }
        return fed;
    }

    /** Broadcast the legacy PE stream to clients within thirty blocks of its source. */
    public static void broadcastPEStream(Level level, BlockPos origin, BlockPos target) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        for (net.minecraft.server.level.ServerPlayer player : server.players()) {
            if (player.distanceToSqr(Vec3.atCenterOf(origin)) <= 900.0D) {
                ACNetwork.sendToPlayer(player, new PEStreamMessage(origin, target));
            }
        }
    }
}

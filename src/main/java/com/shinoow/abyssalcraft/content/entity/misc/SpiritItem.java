package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.CapabilityAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Spirit item (1.12.2 {@code spirititem}), the floating item that ferries an ItemStack along a route
 * during spirit-altar crafting. Route, progress and destination side survive save/reload; delivery
 * uses the loader-neutral item capability view shared by the modern transfer engine.
 */
public class SpiritItem extends ItemEntity {

    private final List<BlockPos> route = new ArrayList<>();
    private Direction facing = Direction.DOWN;
    private int pathIndex;

    public SpiritItem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setNeverPickUp();
        setUnlimitedLifetime();
        noPhysics = true;
    }

    public SpiritItem configure(ItemStack stack, List<BlockPos> positions, Direction destinationSide) {
        setItem(stack.copy());
        route.clear();
        positions.forEach(pos -> route.add(pos.immutable()));
        facing = destinationSide == null ? Direction.DOWN : destinationSide;
        pathIndex = 0;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (level().isClientSide || route.isEmpty() || getItem().isEmpty()) return;
        if (!ACConfig.spirit_items.get()) {
            dropAndDiscard();
            return;
        }

        pathIndex = Math.min(pathIndex, route.size() - 1);
        BlockPos target = route.get(pathIndex);
        Vec3 destination = Vec3.atLowerCornerOf(target).add(0.5D, 0.2D, 0.5D);
        Vec3 delta = destination.subtract(position());
        if (delta.lengthSqr() <= 1.0D) {
            if (pathIndex == route.size() - 1) {
                deliver(target);
                return;
            }
            pathIndex++;
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        setDeltaMovement(
            Math.max(-0.1D, Math.min(0.1D, delta.x)),
            Math.max(-0.05D, Math.min(0.05D, delta.y)),
            Math.max(-0.1D, Math.min(0.1D, delta.z)));
    }

    private void deliver(BlockPos target) {
        CapabilityAccess.ItemView inventory = CapabilityAccess.itemView(level(), target, facing);
        if (inventory == null) {
            dropAndDiscard();
            return;
        }
        ItemStack remaining = getItem().copy();
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            remaining = inventory.insert(slot, remaining, false);
        }
        if (!remaining.isEmpty()) spawnAtLocation(remaining);
        discard();
    }

    private void dropAndDiscard() {
        if (!getItem().isEmpty()) spawnAtLocation(getItem().copy());
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        long[] positions = new long[route.size()];
        for (int index = 0; index < route.size(); index++) positions[index] = route.get(index).asLong();
        tag.putLongArray("Route", positions);
        tag.putInt("PathIndex", pathIndex);
        tag.putInt("Facing", facing.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        route.clear();
        for (long position : tag.getLongArray("Route")) route.add(BlockPos.of(position));
        pathIndex = Math.max(0, tag.getInt("PathIndex"));
        facing = Direction.from3DDataValue(tag.getInt("Facing"));
        setNoGravity(true);
        setNeverPickUp();
        setUnlimitedLifetime();
        noPhysics = true;
    }
}

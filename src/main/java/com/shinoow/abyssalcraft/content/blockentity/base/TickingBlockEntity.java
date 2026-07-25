package com.shinoow.abyssalcraft.content.blockentity.base;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

/**
 * Framework interface for block entities that tick server-side (owned by PC-1) -- the modern
 * equivalent of the 1.12.2 {@code ITickable}.
 *
 * <p>A block hosting such a BE returns {@link #serverTicker()} from {@code EntityBlock.getTicker}
 * (guarded to the server side), and the BE implements {@link #serverTick()}. This generalises the
 * per-machine {@code MachineBlockEntity::serverTick} wiring so any non-machine block entity (energy,
 * spawner, ritual, ...) can tick through one shared ticker.
 */
public interface TickingBlockEntity {

    /** Called once per server tick while the chunk is loaded. */
    void serverTick();

    /**
     * A {@link BlockEntityTicker} that forwards to {@link #serverTick()} for any block entity
     * implementing this interface. Blocks should only return it on the logical server
     * ({@code level.isClientSide == false}).
     */
    static <T extends BlockEntity> BlockEntityTicker<T> serverTicker() {
        return (level, pos, state, be) -> {
            if (be instanceof TickingBlockEntity ticking) {
                ticking.serverTick();
            }
        };
    }
}

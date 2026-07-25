package com.shinoow.abyssalcraft.content.entity.misc;

import com.shinoow.abyssalcraft.content.item.misc.MiscItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Gatekeeper essence (1.12.2 {@code gatekeeperessence}), the stationary reward item dropped when the
 * Gatekeeper is defeated. It always carries the real essence item and remains stationary while the
 * vanilla ItemEntity age/lifespan and pickup behavior continue to apply.
 */
public class GatekeeperEssence extends ItemEntity {

    public GatekeeperEssence(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        setItem(new ItemStack(MiscItems.ESSENCE_OF_THE_GATEKEEPER.get()));
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        setDeltaMovement(Vec3.ZERO);
        super.tick();
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }
}

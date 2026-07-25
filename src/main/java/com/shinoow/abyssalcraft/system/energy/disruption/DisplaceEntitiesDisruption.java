package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Randomly swap nearby living entities' positions. */
public final class DisplaceEntitiesDisruption extends Disruption {

    public DisplaceEntitiesDisruption() {
        super("displace", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(16.0));
        for (LivingEntity entity : entities) {
            LivingEntity other = entities.get(level.random.nextInt(entities.size()));
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            entity.teleportTo(other.getX(), other.getY(), other.getZ());
            other.teleportTo(x, y, z);
        }
    }
}
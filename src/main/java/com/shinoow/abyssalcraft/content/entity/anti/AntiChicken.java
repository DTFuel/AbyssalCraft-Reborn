package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Chicken (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Chicken}, inheriting panic / mate / tempt-with-seeds behaviour and periodic
 * egg-laying. Drops {@code anti_chicken} + feathers (loot table {@code entities/antichicken}).
 */
public class AntiChicken extends Chicken implements AntiEntity {

    public AntiChicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    @Override
    public AntiChicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return AntiEntities.ANTI_CHICKEN.get().create(level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 8.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }
}

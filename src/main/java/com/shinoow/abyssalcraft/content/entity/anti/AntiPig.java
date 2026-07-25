package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Pig (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Pig}, inheriting panic / mate / tempt / saddle-ride behaviour. Drops
 * {@code anti_pork} (loot table {@code entities/antipig}).
 */
public class AntiPig extends Pig implements AntiEntity {

    public AntiPig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    @Override
    public AntiPig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return AntiEntities.ANTI_PIG.get().create(level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }
}

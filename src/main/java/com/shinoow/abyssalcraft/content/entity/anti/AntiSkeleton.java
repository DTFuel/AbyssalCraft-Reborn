package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Skeleton (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Skeleton}, inheriting the bow ranged-attack AI, default bow equipment, and
 * sun-burning. Drops arrows + {@code anti_bone} (loot table {@code entities/antiskeleton}).
 */
public class AntiSkeleton extends Skeleton implements AntiEntity {

    public AntiSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }
}

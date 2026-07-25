package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Creeper (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Creeper}, inheriting the full swell / prime / explode / powered-by-lightning
 * behaviour. Drops gunpowder (loot table {@code entities/anticreeper}). The signature anti-annihilation
 * (exploding on contact with a normal Creeper) is deferred with the config/{@code ExplosionUtil} port.
 */
public class AntiCreeper extends Creeper implements AntiEntity {

    public AntiCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }
}

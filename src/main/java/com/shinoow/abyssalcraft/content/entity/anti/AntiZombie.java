package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.base.HardcoreMeleeDamage;

/**
 * Anti-matter Zombie (owned by PD-3, Stage D2a).
 *
 * <p>1.12.2 {@code EntityAntiZombie} copied vanilla zombie AI onto {@code EntityMobBase}; the faithful
 * modern port extends vanilla {@link Zombie} directly, inheriting the correct behaviour (child, spawn
 * reinforcements, door-breaking, villager targeting) for free and layering on the anti drops (loot
 * table {@code entities/antizombie}) + {@link AntiEntity} marker. Attribute values track the 1.12.2
 * non-hardcore tuning (hardcore scaling awaits the ported config).
 */
public class AntiZombie extends Zombie implements AntiEntity {

    public AntiZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        HardcoreMeleeDamage.applyChip(this, target, 1.5F);
        return super.doHurtTarget(target);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.23D)
            .add(Attributes.FOLLOW_RANGE, 30.0D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }
}

package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.base.HardcoreMeleeDamage;

/**
 * Anti-matter Abyssal Zombie (owned by PD-3, Stage D2a).
 *
 * <p>Anti counterpart of the Abyssal Zombie (a coralium-plagued zombie). Extends vanilla {@link Zombie}
 * for faithful zombie behaviour; drops {@code anti_plagued_flesh} (loot table {@code entities/antiabyssalzombie}).
 * Attribute values track the 1.12.2 non-hardcore tuning.
 */
public class AntiAbyssalZombie extends Zombie implements AntiEntity {

    public AntiAbyssalZombie(EntityType<? extends Zombie> type, Level level) {
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
            .add(Attributes.MAX_HEALTH, 25.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23D)
            .add(Attributes.FOLLOW_RANGE, 42.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }
}

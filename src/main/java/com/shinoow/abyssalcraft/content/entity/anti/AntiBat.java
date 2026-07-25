package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Bat (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Bat}, inheriting hanging / erratic-flight ambient behaviour. Drops nothing
 * (like the vanilla bat), so it carries no loot table.
 */
public class AntiBat extends Bat implements AntiEntity {

    public AntiBat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 6.0D);
    }
}

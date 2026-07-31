package com.shinoow.abyssalcraft.content.entity.anti;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.base.HardcoreMeleeDamage;

/**
 * Anti-matter Spider (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Spider}, inheriting wall-climbing, leaping, day-neutral aggression, and the
 * skeleton jockey. Drops string + {@code anti_spider_eye} (loot table {@code entities/antispider}).
 */
public class AntiSpider extends Spider implements AntiEntity {

    public AntiSpider(EntityType<? extends Spider> type, Level level) {
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
        return Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 24.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.30D);
    }
}

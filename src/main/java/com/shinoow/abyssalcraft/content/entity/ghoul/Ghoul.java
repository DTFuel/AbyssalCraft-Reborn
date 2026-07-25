package com.shinoow.abyssalcraft.content.entity.ghoul;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The common Ghoul (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityGhoul}: 30 health, 5 attack damage, drops ghoul flesh
 * (loot table {@code entities/ghoul}).
 */
public class Ghoul extends AbstractGhoul {

    public Ghoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        burnInSunlightWhenConfigured();
        super.aiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ghoulAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }
}

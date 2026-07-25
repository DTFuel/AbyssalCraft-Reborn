package com.shinoow.abyssalcraft.content.entity.shoggoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Lesser Shoggoth (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityLesserShoggoth}: 25 health, 6 attack damage, the small
 * 0.9x1.3 hitbox (set on the registered {@link EntityType}). Drops shoggoth flesh (loot table
 * {@code entities/lesser_shoggoth}). Consuming eight food points grows it into an adult Shoggoth.
 */
public class LesserShoggoth extends AbstractShoggoth {

    public LesserShoggoth(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return shoggothAttributes()
            .add(Attributes.MAX_HEALTH, 25.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void performFoodAction() {
        if (spawnShoggoth(ShoggothEntities.SHOGGOTH.get(), net.minecraft.world.entity.MobSpawnType.CONVERSION) != null) {
            discard();
        }
    }

    @Override
    protected boolean isLargeShoggoth() {
        return false;
    }

    @Override
    protected PartProfile partProfile() {
        return new PartProfile(0.8F, 0.6F, 0.9F, 1.1F, 0.25D, 0.75D, 0.35D);
    }
}

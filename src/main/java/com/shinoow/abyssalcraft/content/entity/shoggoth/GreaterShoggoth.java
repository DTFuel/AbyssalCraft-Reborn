package com.shinoow.abyssalcraft.content.entity.shoggoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Greater Shoggoth (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityGreaterShoggoth}: 100 health, 10 attack damage, the large
 * 1.8x2.6 hitbox (set on the registered {@link EntityType}). Drops shoggoth flesh (loot table
 * {@code entities/greater_shoggoth}).
 */
public class GreaterShoggoth extends AbstractShoggoth {

    public GreaterShoggoth(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return shoggothAttributes()
            .add(Attributes.MAX_HEALTH, 100.0D)
            .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Override
    protected void performFoodAction() {
        AbstractShoggoth offspring = spawnShoggoth(ShoggothEntities.SHOGGOTH.get(),
            net.minecraft.world.entity.MobSpawnType.BREEDING);
        if (offspring != null) {
            playSound(com.shinoow.abyssalcraft.registry.ModSounds.event("shoggoth.birth"), 1.0F,
                (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 1.0F);
            launchOffspring(offspring);
        }
    }

    @Override
    protected PartProfile partProfile() {
        return new PartProfile(1.6F, 1.2F, 1.8F, 2.2F, 0.5D, 1.5D, 0.7D);
    }
}

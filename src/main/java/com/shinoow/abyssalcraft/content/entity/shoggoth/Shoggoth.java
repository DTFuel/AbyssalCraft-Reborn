package com.shinoow.abyssalcraft.content.entity.shoggoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Shoggoth (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityShoggoth}: 50 health, 8 attack damage, the 1.2x1.8 hitbox
 * (set on the registered {@link EntityType}). Drops shoggoth flesh (loot table {@code entities/shoggoth}).
 */
public class Shoggoth extends AbstractShoggoth {

    public Shoggoth(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return shoggothAttributes()
            .add(Attributes.MAX_HEALTH, 50.0D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void performFoodAction() {
        AbstractShoggoth offspring = spawnShoggoth(ShoggothEntities.LESSER_SHOGGOTH.get(),
            net.minecraft.world.entity.MobSpawnType.BREEDING);
        if (offspring != null) {
            playSound(com.shinoow.abyssalcraft.registry.ModSounds.event("shoggoth.birth"), 1.0F,
                (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 1.0F);
            launchOffspring(offspring);
        }
    }

    @Override
    protected PartProfile partProfile() {
        return new PartProfile(1.1F, 0.6F, 1.2F, 1.4F, 0.5D, 1.2D, 0.5D);
    }
}

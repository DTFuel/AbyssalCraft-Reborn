package com.shinoow.abyssalcraft.content.entity.anti;

import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Anti-matter Cow (owned by PD-3, Stage D2a).
 *
 * <p>Extends vanilla {@link Cow}, inheriting panic / mate / tempt-with-wheat / follow-parent behaviour.
 * Drops {@code anti_beef} + leather (loot table {@code entities/anticow}).
 */
public class AntiCow extends Cow implements AntiEntity {

    public AntiCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    @Override
    public AntiCow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return AntiEntities.ANTI_COW.get().create(level);
    }

    @Override
    public InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.BUCKET) && !player.getAbilities().instabuild && !isBaby()) {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            ItemStack filled = new ItemStack(LiquidAntimatterCompat.BUCKET.get());
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.20D);
    }
}

package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.EntityPartCompat;

/** Multipart flying Dragon Boss (Asorah successor). */
public final class DragonBoss extends BossMob implements EntityPartCompat.Parent {

    private final DragonFlightController<DragonBoss> dragon = new DragonFlightController<>(this, true);

    public DragonBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossKind.DRAGON_BOSS);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        dragon.tick();
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public EntityPartCompat.Part<?>[] getParts() {
        return dragon.parts();
    }

    @Override
    public boolean hurtPart(EntityPartCompat.Part<?> part, DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player) && !source.is(DamageTypes.EXPLOSION)
                && !source.is(DamageTypes.PLAYER_EXPLOSION)) return false;
        return super.hurt(source, dragon.adjustedPartDamage(part, amount));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) && getY() <= 0.0D && !level().isClientSide) discard();
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        dragon.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dragon.load(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

}
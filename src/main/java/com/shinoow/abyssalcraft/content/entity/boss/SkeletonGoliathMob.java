package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import com.shinoow.abyssalcraft.content.item.tool.ToolItems;
import com.shinoow.abyssalcraft.platform.IgniteCompat;

/** Skeleton Goliath with sunlight behavior and its real Cudgel equipment. */
public final class SkeletonGoliathMob extends EliteMob {

    public SkeletonGoliathMob(EntityType<? extends Monster> type, Level level) {
        super(type, level, EliteKind.SKELETON_GOLIATH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(5, new RestrictSunGoal(this));
        goalSelector.addGoal(6, new FleeSunGoal(this, 1.0D));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isSunBurnTick()) IgniteCompat.ignite(this, 8);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        net.minecraft.world.entity.MobSpawnType spawnType, SpawnGroupData spawnData
                                        //? if <1.21 {
                                        , CompoundTag tag
                                        //?}
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData
            //? if <1.21 {
            , tag
            //?}
        );
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ToolItems.CUDGEL.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.085F);
        setCanPickUpLoot(com.shinoow.abyssalcraft.config.ACConfig.hardcoreMode.get()
            || getRandom().nextFloat() < 0.55F * difficulty.getSpecialMultiplier());
        return result;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
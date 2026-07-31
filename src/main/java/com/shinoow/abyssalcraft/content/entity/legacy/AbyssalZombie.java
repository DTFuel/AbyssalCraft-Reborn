package com.shinoow.abyssalcraft.content.entity.legacy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.content.entity.base.HardcoreMeleeDamage;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.platform.IgniteCompat;
import net.minecraft.core.registries.BuiltInRegistries;

public final class AbyssalZombie extends Zombie {

    public AbyssalZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 25.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23D)
            .add(Attributes.FOLLOW_RANGE, 42.0D)
            .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        HardcoreMeleeDamage.applyChip(this, target, 1.5F);
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !EffectHooks.isCoraliumImmune(living)
            && (ACConfig.shouldInfect.get() || isInAbyssalWasteland())) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
        }
        if (hurt && getMainHandItem().isEmpty() && isOnFire()
                && getRandom().nextFloat() < level().getDifficulty().getId() * 0.3F) {
            IgniteCompat.ignite(target, 2 * level().getDifficulty().getId());
        }
        return hurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnData
                                        //? if <1.21 {
                                        , net.minecraft.nbt.CompoundTag tag
                                        //?}
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData
            //? if <1.21 {
            , tag
            //?}
        );
        setCanPickUpLoot(ACConfig.hardcoreMode.get()
            || getRandom().nextFloat() < 0.55F * difficulty.getSpecialMultiplier());
        return result;
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return !ComplexConfig.mobItemPickupBlacklist().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))
            && super.wantsToPickUp(stack);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        Difficulty difficulty = level.getDifficulty();
        boolean validVictim = victim instanceof Zombie || victim instanceof Player || victim instanceof Villager;
        if (!validVictim || difficulty != Difficulty.NORMAL && difficulty != Difficulty.HARD
            || difficulty == Difficulty.NORMAL && getRandom().nextBoolean()) return true;

        AbyssalZombie converted = LegacyEntities.ABYSSAL_ZOMBIE.get().create(level);
        if (converted == null) return true;
        converted.moveTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), victim.getXRot());
        //? if <1.21 {
        converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()),
            MobSpawnType.CONVERSION, null, null);
        //?} else {
        /*converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()),
            MobSpawnType.CONVERSION, null);
        *///?}
        if (victim.isBaby()) converted.setBaby(true);
        if (victim.hasCustomName()) converted.setCustomName(victim.getCustomName());
        if (!(victim instanceof Player)) victim.discard();
        level.addFreshEntity(converted);
        level.levelEvent(null, 1026, blockPosition(), 0);
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event("abyssalzombie.idle");
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return ModSounds.event("abyssalzombie.hit");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("abyssalzombie.death");
    }

    private boolean isInAbyssalWasteland() {
        return level().dimension().location().getNamespace().equals("abyssalcraft")
            && level().dimension().location().getPath().equals("abyssal_wasteland");
    }
}

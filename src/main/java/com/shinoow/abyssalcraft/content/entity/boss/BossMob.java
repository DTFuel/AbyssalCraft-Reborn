package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.registry.ModSounds;

// GeckoLib animatable integration (E-stage / PE-4 GeckoLib verification). GeckoLib 4.9 (1.21) dropped the
// `.core.` segment from these two packages that 4.8 (1.20.1) still uses, and they appear in GeoEntity's
// mandated @Override signatures -> the fork is unavoidable here (cannot be hidden in platform/). This is the
// documented GeckoLib exception to the "//? only in platform/ + main class" rule; see docs/spec/geckolib-model-porting.md.
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.util.GeckoLibUtil;
//? if forge {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}

import com.shinoow.abyssalcraft.config.ACConfig;

/**
 * Single class over the four boss-bar bosses (owned by PD-7, Stage D2b); the {@link BossKind} is baked
 * into each {@link EntityType} factory, mirroring the PD-4 demon-animal collapse. Supplies the boss's
 * faithful attributes + a standard hostile goal set + the {@link ACBossMob} health bar. Boss-specific
 * multi-phase attacks are deferred (see {@link BossKind}).
 */
public class BossMob extends ACBossMob implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final BossKind kind;

    public BossMob(EntityType<? extends Monster> type, Level level, BossKind kind) {
        super(type, level, kind.color());
        this.kind = kind;
        applyHardcoreAttributes();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model for the GeckoLib proof; boss animations land with the faithful mesh (PE-4b).
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public BossKind kind() {
        return kind;
    }

    public int getACDeathTime() {
        return getSyncedDeathTime();
    }

    public int getACDeathDuration() {
        return switch (kind) {
            case JZAHAR -> 800;
            case CHAGAROTH, SACTHOTH, DRAGON_BOSS -> 200;
        };
    }

    @Override
    protected void tickDeath() {
        int duration = getACDeathDuration();
        if (level().isClientSide) {
            deathTime = getACDeathTime();
            setDeltaMovement(0.0D, 0.0D, 0.0D);
            return;
        }
        int acDeathTime = getACDeathTime() + 1;
        setSyncedDeathTime(acDeathTime);
        deathTime = acDeathTime;
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        tickACDeath(acDeathTime);
        if (acDeathTime >= duration && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(Entity.RemovalReason.KILLED);
        }
    }

    protected void tickACDeath(int deathTick) {}

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getACDeathTime() > 0) tag.putInt("ACDeathTime", getACDeathTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int acDeathTime = tag.getInt("ACDeathTime");
        setSyncedDeathTime(acDeathTime);
        if (acDeathTime > 0) deathTime = acDeathTime;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes(BossKind kind) {
        return ACMob.createAttributes()
                .add(Attributes.MAX_HEALTH, kind.health())
                .add(Attributes.ATTACK_DAMAGE, kind.attack())
                .add(Attributes.MOVEMENT_SPEED, kind.speed())
                .add(Attributes.FOLLOW_RANGE, kind.followRange())
                .add(Attributes.ARMOR, kind.armor())
                .add(Attributes.KNOCKBACK_RESISTANCE, kind.knockbackResistance());
    }

    private void applyHardcoreAttributes() {
        if (!ACConfig.hardcoreMode.get()) return;
        var health = getAttribute(Attributes.MAX_HEALTH);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null) health.setBaseValue(kind.health() * 2.0D);
        if (attack != null) attack.setBaseValue(kind.attack() * 2.0D);
        setHealth(getMaxHealth());
    }

    // Faithful boss sounds (PH-4b). Sacthoth (the shadow boss) uses its shadow hurt cry + the dedicated
    // sacthoth death stinger; Jzahar's chants/ability shouts fire from the deferred multi-phase attacks,
    // and Chagaroth / the dragon boss keep the vanilla boss defaults.
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return kind == BossKind.SACTHOTH ? ModSounds.event("shadow.hit") : super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind == BossKind.SACTHOTH ? ModSounds.event("sacthoth.death") : super.getDeathSound();
    }
}

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.registry.ModSounds;

// GeckoLib animatable integration (E-stage / PE-4 GeckoLib verification). GeckoLib 4.9 (1.21) dropped the
// `.core.` segment from these two packages that 4.8 (1.20.1) still uses, and they appear in GeoEntity's
// mandated @Override signatures -> the fork is unavoidable here (cannot be hidden in platform/). This is the
// documented GeckoLib exception to the "//? only in platform/ + main class" rule; see docs/spec/geckolib-model-porting.md.
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.util.GeckoLibUtil;
//? if <1.21 {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ContentConfigMatrix;

/**
 * Single class over the four boss-bar bosses (owned by PD-7, Stage D2b); the {@link BossKind} is baked
 * into each {@link EntityType} factory, mirroring the PD-4 demon-animal collapse. Supplies the boss's
 * faithful attributes + a standard hostile goal set + the {@link ACBossMob} health bar. Boss-specific
 * multi-phase attacks are deferred (see {@link BossKind}).
 */
public class BossMob extends ACBossMob implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final BossKind kind;
    private int dialogPhase;

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
    public void tick() {
        super.tick();
        if (level().isClientSide || !ContentConfigMatrix.showBossDialogs()) return;
        int phase = getTarget() == null ? 0 : getHealth() <= getMaxHealth() * 0.5F ? 2 : 1;
        if (phase > dialogPhase) {
            dialogPhase = phase;
            broadcastDialog(phase == 1 ? "engage" : "phase");
        }
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
        tickLegacyDeathEffects((ServerLevel) level(), acDeathTime);
        tickACDeath(acDeathTime);
        if (acDeathTime >= duration && !isRemoved()) {
            if (ContentConfigMatrix.showBossDialogs()) broadcastDialog("defeated");
            level().broadcastEntityEvent(this, (byte) 60);
            remove(Entity.RemovalReason.KILLED);
        }
    }

    protected void tickACDeath(int deathTick) {}

    private void tickLegacyDeathEffects(ServerLevel server, int deathTick) {
        if (awardsLegacyExperience(deathTick)) {
            ExperienceOrb.award(server, position(), 500);
        }
        if (!ACConfig.particleEntity.get()) return;
        switch (kind) {
            case CHAGAROTH -> sendDeathParticles(server, deathTick, ParticleTypes.FLAME, ParticleTypes.LAVA,
                ParticleTypes.LARGE_SMOKE);
            case SACTHOTH -> sendDeathParticles(server, deathTick, ParticleTypes.SMOKE, ParticleTypes.LARGE_SMOKE,
                ParticleTypes.EXPLOSION);
            case DRAGON_BOSS -> {
                if (deathTick >= 180) sendDeathParticles(server, deathTick, ParticleTypes.EXPLOSION_EMITTER);
            }
            case JZAHAR -> tickJzaharDeathParticles(server, deathTick);
        }
    }

    private boolean awardsLegacyExperience(int deathTick) {
        return (kind == BossKind.JZAHAR ? deathTick > 750 : deathTick > 150)
            && deathTick % 5 == 0;
    }

    private void sendDeathParticles(ServerLevel server, int deathTick, ParticleOptions... particles) {
        if (deathTick > 200) return;
        double x = getX() + (getRandom().nextDouble() - 0.5D) * 8.0D;
        double y = getY() + 2.0D + (getRandom().nextDouble() - 0.5D) * 4.0D;
        double z = getZ() + (getRandom().nextDouble() - 0.5D) * 8.0D;
        for (ParticleOptions particle : particles) server.sendParticles(particle, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (deathTick >= 190) server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
            x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void sendDeathParticles(ServerLevel server, int deathTick, ParticleOptions particle) {
        double x = getX() + (getRandom().nextDouble() - 0.5D) * 8.0D;
        double y = getY() + 2.0D + (getRandom().nextDouble() - 0.5D) * 4.0D;
        double z = getZ() + (getRandom().nextDouble() - 0.5D) * 8.0D;
        server.sendParticles(particle, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void tickJzaharDeathParticles(ServerLevel server, int deathTick) {
        if (deathTick < 800) server.sendParticles(ParticleTypes.LARGE_SMOKE,
            getX(), getY() + 1.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (deathTick < 400) server.sendParticles(ParticleTypes.LARGE_SMOKE,
            getX(), getY() + 2.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (deathTick >= 100 && deathTick < 400) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 1, 1.5D, 1.0D, 1.5D, 0.0D);
        }
        if (deathTick >= 200 && deathTick < 400) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 1, 1.5D, 1.0D, 1.5D, 0.0D);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY() + 2.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (deathTick >= 790) server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
            getX(), getY() + 1.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getACDeathTime() > 0) tag.putInt("ACDeathTime", getACDeathTime());
        tag.putInt("DialogPhase", dialogPhase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int acDeathTime = tag.getInt("ACDeathTime");
        setSyncedDeathTime(acDeathTime);
        if (acDeathTime > 0) deathTime = acDeathTime;
        dialogPhase = Math.max(0, tag.getInt("DialogPhase"));
    }

    private void broadcastDialog(String phase) {
        Component message = Component.translatable("message.abyssalcraft.boss." + phase, getDisplayName());
        for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class,
                getBoundingBox().inflate(64.0D))) player.sendSystemMessage(message);
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

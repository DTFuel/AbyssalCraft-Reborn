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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.registry.ModSounds;

// GeckoLib animatable integration (PE-4b faithful meshes). Same `.core`-package fork as BossMob: GeckoLib
// 4.9 (1.21) dropped the `.core.` segment that 4.8 (1.20.1) uses, and these two appear in GeoEntity's
// mandated @Override signatures -> the fork is unavoidable here (documented GeckoLib exception to the
// "//? only in platform/ + main class" rule; see docs/spec/geckolib-model-porting.md).
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
 * Single class over the eight elite bosses / boss minions without a health bar (owned by PD-7, Stage
 * D2b); the {@link EliteKind} is baked into each {@link EntityType} factory (PD-4 collapse idiom).
 * Supplies faithful attributes + a standard hostile goal set. Trading / shearing / summon-by-boss /
 * multi-part behaviours are deferred (see {@link EliteKind}).
 */
public class EliteMob extends ACMob implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final EliteKind kind;

    public EliteMob(EntityType<? extends Monster> type, Level level, EliteKind kind) {
        super(type, level);
        this.kind = kind;
        applyHardcoreAttributes();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static faithful mesh (PE-4b); elite animations land later.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public EliteKind kind() {
        return kind;
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

    public static AttributeSupplier.Builder createAttributes(EliteKind kind) {
        return ACMob.createAttributes()
                .add(Attributes.MAX_HEALTH, kind.health())
                .add(Attributes.ATTACK_DAMAGE, kind.attack())
                .add(Attributes.MOVEMENT_SPEED, kind.speed())
                .add(Attributes.FOLLOW_RANGE, kind.followRange())
                .add(Attributes.ARMOR, kind.armor())
            .add(Attributes.ARMOR_TOUGHNESS, kind == EliteKind.DREADGUARD ? 4.0D : 0.0D)
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

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!super.checkSpawnRules(level, spawnType)) return false;
        if (kind != EliteKind.SHUB_OFFSPRING) return true;
        if (getY() < level.getSeaLevel()) return false;
        if (!(level instanceof Level actual)) return false;
        if (!actual.isNight()) return false;
        int phase = actual.dimensionType().moonPhase(actual.getDayTime());
        return phase == 0 || getRandom().nextFloat() + 0.01F > phase / 7.0F;
    }

    // Faithful per-kind elite sounds (PH-4b). Dreadguard keeps its full idle/hit/death set; the remnant's
    // scream is its ambient (its yes/no trade lines land with the deferred trading behaviour). Kinds without
    // a bespoke 1.12.2 sound fall through to the vanilla mob defaults.
    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind) {
            case DREADGUARD -> ModSounds.event("dreadguard.idle");
            case REMNANT -> ModSounds.event("remnant.scream");
            default -> super.getAmbientSound();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return kind == EliteKind.DREADGUARD ? ModSounds.event("dreadguard.hit") : super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind == EliteKind.DREADGUARD ? ModSounds.event("dreadguard.death") : super.getDeathSound();
    }
}

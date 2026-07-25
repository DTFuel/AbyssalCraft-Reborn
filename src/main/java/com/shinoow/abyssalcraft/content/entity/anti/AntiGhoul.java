package com.shinoow.abyssalcraft.content.entity.anti;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
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

/**
 * Anti-matter Ghoul (owned by PD-3, Stage D2a).
 *
 * <p>The anti member of the ghoul family. In 1.12.2 it extended {@code EntityGhoulBase} (owned by PD-5);
 * to avoid reaching into another family's package before it lands, this port extends the shared
 * {@link ACMob} monster base and wires a faithful melee AI. Drops {@code anti_ghoul_flesh} (loot table
 * {@code entities/antighoul}). The ghoul-specific transform-on-coralium/dread death (to Omothol Ghoul)
 * awaits the PD-5 ghoul family.
 */
public class AntiGhoul extends ACMob implements AntiEntity {

    public AntiGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void push(Entity other) {
        if (!annihilateOnContact(other)) super.push(other);
    }

    @Override
    public void die(DamageSource source) {
        boolean transform = !isDeadOrDying() && level() instanceof ServerLevel
            && (source.is(ACDamageTypes.CORALIUM) || source.is(ACDamageTypes.DREAD));
        super.die(source);
        if (transform) transformToOmothol((ServerLevel) level());
    }

    private void transformToOmothol(ServerLevel level) {
        var ghoul = GhoulEntities.OMOTHOL_GHOUL.get().create(level);
        if (ghoul == null) return;
        ghoul.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        //? if <1.21 {
        ghoul.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null, null);
        //?} else {
        /*ghoul.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        *///?}
        if (hasCustomName()) ghoul.setCustomName(getCustomName());
        level.addFreshEntity(ghoul);
        discard();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 45.0D)
            .add(Attributes.ATTACK_DAMAGE, 5.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.28D);
    }
}

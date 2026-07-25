package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.base.ACMob;

/**
 * Base for AbyssalCraft bosses that show a boss health bar (owned by PD-7, Stage D2b).
 *
 * <p>Faithful successor to the 1.12.2 bosses that held a {@code BossInfoServer} (Chagaroth / Jzahar /
 * Sacthoth / Dragon Boss). Manages a vanilla {@link ServerBossEvent}: the bar tracks the boss's health
 * every server AI tick and shifts colour by health fraction (blue &gt; 66% &rarr; green &gt; 33% &rarr;
 * red) as a lightweight stand-in for the 1.12.2 per-boss phase colouring, and players are added/removed
 * as they enter/leave tracking range (the vanilla {@code WitherBoss}/{@code EnderDragon} idiom). All of
 * this is loader/version-agnostic ({@code ServerBossEvent}, {@code startSeenByPlayer} and
 * {@code customServerAiStep} were javap-verified identical on both nodes).
 *
 * <p><b>Deferred</b> (each boss's signature multi-phase skills depend on not-yet-ported subsystems):
 * see the per-boss notes in {@code BossKind} / the porting docs.
 */
public abstract class ACBossMob extends ACMob {

    private static final EntityDataAccessor<Integer> AC_DEATH_TIME =
        SynchedEntityData.defineId(ACBossMob.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent;

    protected ACBossMob(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossEvent = (ServerBossEvent) new ServerBossEvent(
                getDisplayName(), color, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(AC_DEATH_TIME, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AC_DEATH_TIME, 0);
    }
    *///?}

    protected final int getSyncedDeathTime() {
        return entityData.get(AC_DEATH_TIME);
    }

    protected final void setSyncedDeathTime(int deathTime) {
        entityData.set(AC_DEATH_TIME, deathTime);
    }

    protected final void setBossBarColor(BossEvent.BossBarColor color) {
        if (bossEvent.getColor() != color) bossEvent.setColor(color);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        float f = getMaxHealth() > 0.0F ? getHealth() / getMaxHealth() : 0.0F;
        bossEvent.setProgress(f);
        BossEvent.BossBarColor color = f > 0.66F ? BossEvent.BossBarColor.BLUE
                : f > 0.33F ? BossEvent.BossBarColor.GREEN
                : BossEvent.BossBarColor.RED;
        if (bossEvent.getColor() != color) {
            bossEvent.setColor(color);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }
}

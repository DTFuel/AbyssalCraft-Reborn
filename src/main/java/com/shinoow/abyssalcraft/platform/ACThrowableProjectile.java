package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

/**
 * Compat base for AbyssalCraft thrown projectiles (acid / dread slug / ink) that faithfully extend
 * vanilla {@link ThrowableProjectile}. {@code ThrowableProjectile} does not implement {@link Entity}'s
 * abstract {@code defineSynchedData}, whose signature forks across loaders/versions (Forge no-arg vs
 * NeoForge 1.21 {@code (SynchedEntityData.Builder)}). None of these projectiles carry extra synched
 * data, so this base supplies the empty forked implementation and lets the business projectile classes
 * in {@code content/entity/projectile/**} stay fork-free. Owner ({@code Projectile}) NBT save/load and
 * the {@code onHit} auto-discard are inherited unchanged.
 */
public abstract class ACThrowableProjectile extends ThrowableProjectile {

    protected ACThrowableProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    //? if forge {
    @Override
    protected void defineSynchedData() {}
    //?} else {
    /*@Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}
    *///?}
}

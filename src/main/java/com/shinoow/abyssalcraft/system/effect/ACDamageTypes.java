package com.shinoow.abyssalcraft.system.effect;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

/** The three armor-piercing legacy AbyssalCraft damage sources. */
public final class ACDamageTypes {

    public static final ResourceKey<DamageType> CORALIUM = key("coralium");
    public static final ResourceKey<DamageType> DREAD = key("dread");
    public static final ResourceKey<DamageType> ANTIMATTER = key("antimatter");
    public static final ResourceKey<DamageType> ACID = key("acid");
    public static final ResourceKey<DamageType> SPELL = key("spell");

    private ACDamageTypes() {}

    public static DamageSource source(LivingEntity entity, ResourceKey<DamageType> key) {
        return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(key));
    }

    public static DamageSource attributedSource(LivingEntity caster, ResourceKey<DamageType> key) {
        return new DamageSource(caster.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(key), caster, caster);
    }

    public static DamageSource projectile(net.minecraft.world.entity.projectile.Projectile projectile,
                                          ResourceKey<DamageType> key) {
        return new DamageSource(projectile.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(key), projectile, projectile.getOwner());
    }

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ACRef.id(id));
    }
}
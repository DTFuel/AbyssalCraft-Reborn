package com.shinoow.abyssalcraft.integration.api;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** Typed sinks available to {@link IACPlugin} providers. */
public interface ACPluginContext {

    void registerShoggothFood(EntityType<? extends LivingEntity> entityType);

    void registerDreadPlagueImmunity(EntityType<? extends LivingEntity> entityType);

    void registerDreadPlagueCarrier(EntityType<? extends LivingEntity> entityType);

    void registerCoraliumPlagueImmunity(EntityType<? extends LivingEntity> entityType);

    void registerCoraliumPlagueCarrier(EntityType<? extends LivingEntity> entityType);
}
package com.shinoow.abyssalcraft.content.entity.anti;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.ghoul.AbstractGhoul;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Shared identity and normal-counterpart annihilation for all anti-matter entities. */
public interface AntiEntity {

	default boolean annihilateOnContact(Entity other) {
		Entity self = (Entity) this;
		if (self.level().isClientSide || !self.isAlive() || !isNormalCounterpart(self, other)) return false;

		float strength = ACConfig.nuclearAntimatterExplosions.get()
			? ACConfig.antimatterExplosionSize.get()
			: 5.0F;
		self.level().explode(self, self.getX(), self.getY(), self.getZ(), strength, Level.ExplosionInteraction.MOB);
		self.discard();
		return true;
	}

	private static boolean isNormalCounterpart(Entity self, Entity other) {
		EntityType<?> type = self.getType();
		if (type == AntiEntities.ANTI_ZOMBIE.get()) return other.getType() == EntityType.ZOMBIE;
		if (type == AntiEntities.ANTI_ABYSSAL_ZOMBIE.get()) return other.getType() == LegacyEntities.ABYSSAL_ZOMBIE.get();
		if (type == AntiEntities.ANTI_CREEPER.get()) return other.getType() == EntityType.CREEPER;
		if (type == AntiEntities.ANTI_SKELETON.get()) return other.getType() == EntityType.SKELETON;
		if (type == AntiEntities.ANTI_SPIDER.get()) return other.getType() == EntityType.SPIDER;
		if (type == AntiEntities.ANTI_GHOUL.get()) return other instanceof AbstractGhoul;
		if (type == AntiEntities.ANTI_PLAYER.get()) return other instanceof Player;
		if (type == AntiEntities.ANTI_COW.get()) return other.getType() == EntityType.COW;
		if (type == AntiEntities.ANTI_PIG.get()) return other.getType() == EntityType.PIG;
		if (type == AntiEntities.ANTI_CHICKEN.get()) return other.getType() == EntityType.CHICKEN;
		return type == AntiEntities.ANTI_BAT.get() && other.getType() == EntityType.BAT;
	}
}

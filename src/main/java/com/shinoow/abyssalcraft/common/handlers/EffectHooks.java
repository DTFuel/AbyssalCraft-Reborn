package com.shinoow.abyssalcraft.common.handlers;

import java.util.function.Supplier;
import java.util.UUID;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.integration.api.ACPluginRegistry;
import com.shinoow.abyssalcraft.content.entity.anti.AntiEntities;
import com.shinoow.abyssalcraft.content.entity.anti.AntiEntity;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;

/** Server-side plague, antidote, antimatter and death-conversion behavior. */
public final class EffectHooks {

    private static final UUID CORALIUM_PLAYER_1 = UUID.fromString("a5d8abca-0979-4bb0-825a-f1ccda0b350b");
    private static final UUID CORALIUM_PLAYER_2 = UUID.fromString("08f3211c-d425-47fd-afd8-f0e7f94152c4");

    private EffectHooks() {}

    public static void coraliumTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (entity instanceof ServerPlayer player && entity.tickCount % 200 == 0) {
            KnowledgeHooks.onPlagueTick(player, "coralium_plague");
        }
        if (entity.tickCount % 200 == 0 && entity.getRandom().nextFloat() > 0.7F && !isPurged(entity)) {
            spreadNearby(entity, ACEffects.CORALIUM_PLAGUE, 2.0D, EffectHooks::isCoraliumImmune);
        }
        if (!isCoraliumImmune(entity) && entity.tickCount % Math.max(1, 40 >> amplifier) == 0) {
            entity.hurt(ACDamageTypes.source(entity, ACDamageTypes.CORALIUM), 2.0F);
        }
    }

    public static void dreadTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (entity instanceof ServerPlayer player) {
            if (entity.tickCount % 200 == 0) KnowledgeHooks.onPlagueTick(player, "dread_plague");
            player.causeFoodExhaustion(0.025F * (amplifier + 2));
        }
        if (entity.tickCount % 100 == 0 && entity.getRandom().nextFloat() > 0.3F && !isPurged(entity)) {
            spreadNearby(entity, ACEffects.DREAD_PLAGUE, 3.0D, EffectHooks::isDreadImmune);
        }
        if (!isDreadImmune(entity) && entity.tickCount % Math.max(1, 25 >> amplifier) == 0) {
            entity.hurt(ACDamageTypes.source(entity, ACDamageTypes.DREAD), 1.0F);
        }
    }

    public static void antimatterTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && !isAntimatterImmune(entity)) {
            entity.hurt(ACDamageTypes.source(entity, ACDamageTypes.ANTIMATTER), 5.0F);
        }
    }

    public static void coraliumAntidoteTick(LivingEntity entity, int amplifier) {
        MobEffectCompat.removeEffect(entity, ACEffects.CORALIUM_PLAGUE);
    }

    public static void dreadAntidoteTick(LivingEntity entity, int amplifier) {
        MobEffectCompat.removeEffect(entity, ACEffects.DREAD_PLAGUE);
    }

    public static void onLivingHurt(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source) {
        if (victim.level().isClientSide || isPurged(victim) || !(source.getEntity() instanceof LivingEntity attacker)) return;
        if (MobEffectCompat.hasEffect(attacker, ACEffects.CORALIUM_PLAGUE)
                && !isCoraliumImmune(victim) && victim.getRandom().nextFloat() > 0.5F) {
            copyEffect(attacker, victim, ACEffects.CORALIUM_PLAGUE);
        } else if (isCoraliumCarrier(attacker) && !isCoraliumImmune(victim)
                && victim.getRandom().nextFloat() > 0.5F) {
            victim.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 600, 0));
        }
        if (MobEffectCompat.hasEffect(attacker, ACEffects.DREAD_PLAGUE)
                && !isDreadImmune(victim) && victim.getRandom().nextFloat() > 0.5F) {
            copyEffect(attacker, victim, ACEffects.DREAD_PLAGUE);
            } else if (isDreadCarrier(attacker) && !isDreadImmune(victim)
                && victim.getRandom().nextFloat() > 0.5F) {
                victim.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 600, 0));
        }
    }

    public static boolean onLivingDeath(ServerLevel level, LivingEntity victim) {
        if (isPurged(victim)) return false;
        boolean hasCoralium = MobEffectCompat.hasEffect(victim, ACEffects.CORALIUM_PLAGUE);
        boolean hasDread = MobEffectCompat.hasEffect(victim, ACEffects.DREAD_PLAGUE);
        boolean hasAntimatter = MobEffectCompat.hasEffect(victim, ACEffects.ANTIMATTER);
        if (hasCoralium) {
            spreadDeath(victim, ACEffects.CORALIUM_PLAGUE, EffectHooks::isCoraliumImmune);
        }
        if (hasDread) {
            spreadDeath(victim, ACEffects.DREAD_PLAGUE, EffectHooks::isDreadImmune);
        } else if (isDreadCarrier(victim)) {
            createCarrierCloud(victim, ACEffects.DREAD_PLAGUE);
        }
        if (!hasCoralium && isCoraliumCarrier(victim)) {
            createCarrierCloud(victim, ACEffects.CORALIUM_PLAGUE);
        }

        boolean spawned = hasCoralium && convertCoralium(level, victim);
        if (!spawned && hasDread) spawned = convertDread(level, victim);
        if (!spawned && hasAntimatter) spawned = convertAntimatter(level, victim);
        return spawned;
    }

    private static void createCarrierCloud(LivingEntity source, Supplier<MobEffect> effect) {
        if (ACConfig.no_potion_clouds.get()) return;
        AreaEffectCloud cloud = new AreaEffectCloud(source.level(), source.getX(), source.getY(), source.getZ());
        cloud.addEffect(MobEffectCompat.effectInstance(effect, 600, 0));
        cloud.setRadius(Math.max(1.0F, source.getBbWidth()));
        cloud.setDuration(100 + source.getRandom().nextInt(100));
        cloud.setRadiusPerTick((3.0F - cloud.getRadius()) / cloud.getDuration());
        source.level().addFreshEntity(cloud);
    }

    private static void spreadNearby(LivingEntity source, Supplier<MobEffect> effect, double radius,
                                     java.util.function.Predicate<LivingEntity> immune) {
        MobEffectInstance active = MobEffectCompat.getEffect(source, effect);
        if (active == null) return;
        for (LivingEntity target : source.level().getEntitiesOfClass(LivingEntity.class,
                source.getBoundingBox().inflate(radius), entity -> entity != source && !immune.test(entity))) {
            if (target.getRandom().nextBoolean()) copyEffect(active, target, effect);
        }
    }

    private static void spreadDeath(LivingEntity source, Supplier<MobEffect> effect,
                                    java.util.function.Predicate<LivingEntity> immune) {
        MobEffectInstance active = MobEffectCompat.getEffect(source, effect);
        if (active == null) return;
        if (!ACConfig.no_potion_clouds.get() && source.getRandom().nextFloat() > 0.1F) {
            AreaEffectCloud cloud = new AreaEffectCloud(source.level(), source.getX(), source.getY(), source.getZ());
            cloud.addEffect(MobEffectCompat.effectInstance(effect, 600, active.getAmplifier()));
            cloud.setRadius(Math.max(1.0F, source.getBbWidth()));
            cloud.setDuration(100 + source.getRandom().nextInt(100));
            cloud.setRadiusPerTick((3.0F - cloud.getRadius()) / cloud.getDuration());
            source.level().addFreshEntity(cloud);
            return;
        }
        AABB area = source.getBoundingBox().inflate(6.0D, 2.0D, 6.0D);
        for (LivingEntity target : source.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != source && !immune.test(entity))) {
            double distance = source.distanceToSqr(target);
            if (distance < 32.0D) {
                int duration = (int) ((1.0D - Math.sqrt(distance) / 8.0D) * active.getDuration() + 0.5D);
                if (duration > 20) target.addEffect(MobEffectCompat.effectInstance(effect, duration, active.getAmplifier()));
            }
        }
    }

    private static void copyEffect(LivingEntity source, LivingEntity target, Supplier<MobEffect> effect) {
        MobEffectInstance active = MobEffectCompat.getEffect(source, effect);
        if (active != null) copyEffect(active, target, effect);
    }

    private static void copyEffect(MobEffectInstance active, LivingEntity target, Supplier<MobEffect> effect) {
        target.addEffect(MobEffectCompat.effectInstance(effect, Math.max(600, active.getDuration()), active.getAmplifier()));
    }

    private static boolean isPurged(LivingEntity entity) {
        return PurgeHooks.isPurged(entity);
    }

    public static boolean isCoraliumImmune(LivingEntity entity) {
        ResourceLocation id = entityId(entity);
        return isCoraliumPlayer(entity) || LegacyEntities.isCoralium(entity)
            || ComplexConfig.coraliumImmunity().contains(id)
            || ComplexConfig.coraliumCarriers().contains(id)
            || ACPluginRegistry.isCoraliumImmune(id);
    }

    private static boolean isCoraliumPlayer(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.player.Player
            && (CORALIUM_PLAYER_1.equals(entity.getUUID()) || CORALIUM_PLAYER_2.equals(entity.getUUID()));
    }

    public static boolean isDreadImmune(LivingEntity entity) {
        ResourceLocation id = entityId(entity);
        return LegacyEntities.isDread(entity) || ComplexConfig.dreadImmunity().contains(id)
            || ComplexConfig.dreadCarriers().contains(id) || ACPluginRegistry.isDreadImmune(id);
    }

    public static boolean isAntimatterImmune(LivingEntity entity) {
        return entity instanceof AntiEntity
            || entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)
                .is(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("ethaxium_chestplate")));
    }

    public static boolean isCoraliumCarrier(LivingEntity entity) {
        ResourceLocation id = entityId(entity);
        return LegacyEntities.isCoralium(entity) || ComplexConfig.coraliumCarriers().contains(id)
            || ACPluginRegistry.isCoraliumCarrier(id);
    }

    public static boolean isDreadCarrier(LivingEntity entity) {
        ResourceLocation id = entityId(entity);
        return LegacyEntities.isDread(entity) || ComplexConfig.dreadCarriers().contains(id)
            || ACPluginRegistry.isDreadCarrier(id);
    }

    private static ResourceLocation entityId(LivingEntity entity) {
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    public static boolean applyConfiguredDemonTransformation(ServerLevel level, LivingEntity victim) {
        if (victim instanceof com.shinoow.abyssalcraft.content.entity.demon.DemonAnimal
                || victim instanceof com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal) return false;
        ComplexConfig.DemonTransformation mapping = ComplexConfig.demonTransformations().get(entityId(victim));
        if (mapping == null || level.random.nextFloat() >= mapping.chance()) return false;
        Supplier<? extends EntityType<? extends Mob>> target = switch (mapping.demonType()) {
            case 0 -> DemonEntities.DEMON_PIG;
            case 1 -> DemonEntities.DEMON_COW;
            case 2 -> DemonEntities.DEMON_CHICKEN;
            case 3 -> DemonEntities.DEMON_SHEEP;
            default -> null;
        };
        return spawnReplacement(level, victim, target, false);
    }

    private static boolean convertAntimatter(ServerLevel level, LivingEntity victim) {
        if (isAntimatterImmune(victim)) return false;
        Supplier<? extends EntityType<? extends Mob>> target = null;
        EntityType<?> type = victim.getType();
        if (type == LegacyEntities.ABYSSAL_ZOMBIE.get()) target = AntiEntities.ANTI_ABYSSAL_ZOMBIE;
        else if (isGhoul(type)) target = AntiEntities.ANTI_GHOUL;
        else if (victim instanceof Zombie) target = AntiEntities.ANTI_ZOMBIE;
        else if (victim instanceof Bat) target = AntiEntities.ANTI_BAT;
        else if (victim instanceof Chicken) target = AntiEntities.ANTI_CHICKEN;
        else if (victim instanceof Cow) target = AntiEntities.ANTI_COW;
        else if (victim instanceof Creeper) target = AntiEntities.ANTI_CREEPER;
        else if (victim instanceof Pig) target = AntiEntities.ANTI_PIG;
        else if (victim instanceof Skeleton) target = AntiEntities.ANTI_SKELETON;
        else if (victim instanceof Spider) target = AntiEntities.ANTI_SPIDER;
        else if (victim instanceof ServerPlayer) target = AntiEntities.ANTI_PLAYER;
        return spawnReplacement(level, victim, target, victim instanceof ServerPlayer);
    }

    private static boolean convertCoralium(ServerLevel level, LivingEntity victim) {
        if (isCoraliumImmune(victim)) return false;
        Supplier<? extends EntityType<? extends Mob>> target = null;
        if ((victim instanceof Zombie || victim instanceof ServerPlayer) && shouldTransform(level, 8)) target = LegacyEntities.ABYSSAL_ZOMBIE;
        else if (victim instanceof Squid && level.random.nextBoolean()) target = LegacyEntities.CORALIUM_SQUID;
        else if (isGhoul(victim.getType()) && shouldTransform(level, 8)) target = GhoulEntities.DEPTHS_GHOUL;
        return spawnReplacement(level, victim, target, victim instanceof ServerPlayer);
    }

    private static boolean convertDread(ServerLevel level, LivingEntity victim) {
        if (isDreadImmune(victim) || !level.random.nextBoolean()) return false;
        Supplier<? extends EntityType<? extends Mob>> target;
        if (victim.getType() == BossEntities.SKELETON_GOLIATH.get()) target = BossEntities.DREADGUARD;
        else if (victim instanceof Pig) target = DemonEntities.DEMON_PIG;
        else if (victim instanceof Cow) target = DemonEntities.DEMON_COW;
        else if (victim instanceof Chicken) target = DemonEntities.DEMON_CHICKEN;
        else if (victim instanceof Sheep) target = DemonEntities.DEMON_SHEEP;
        else if (isGhoul(victim.getType())) target = GhoulEntities.DREADED_GHOUL;
        else if (victim instanceof Zombie || victim instanceof Skeleton || victim instanceof ServerPlayer)
            target = LegacyEntities.DREADLING;
        else target = LegacyEntities.DREAD_SPAWN;
        return spawnReplacement(level, victim, target, victim instanceof ServerPlayer);
    }

    private static boolean shouldTransform(ServerLevel level, int bound) {
        return level.getDifficulty() == net.minecraft.world.Difficulty.HARD && level.random.nextBoolean()
            || level.random.nextInt(bound) == 0;
    }

    private static boolean isGhoul(EntityType<?> type) {
        return type == GhoulEntities.GHOUL.get() || type == GhoulEntities.DEPTHS_GHOUL.get()
            || type == GhoulEntities.DREADED_GHOUL.get() || type == GhoulEntities.OMOTHOL_GHOUL.get()
            || type == GhoulEntities.SHADOW_GHOUL.get();
    }

    private static boolean spawnReplacement(ServerLevel level, LivingEntity victim,
                                            Supplier<? extends EntityType<? extends Mob>> target,
                                            boolean retainVictim) {
        if (target == null) return false;
        Mob replacement = target.get().create(level);
        if (replacement == null) return false;
        replacement.copyPosition(victim);
        if (victim.hasCustomName()) replacement.setCustomName(victim.getCustomName());
        if (replacement.getType() == AntiEntities.ANTI_PLAYER.get() && victim instanceof ServerPlayer player) {
            replacement.setCustomName(net.minecraft.network.chat.Component.literal(invertName(player.getGameProfile().getName())));
        }
        replacement.setPersistenceRequired();
        if (!level.addFreshEntity(replacement)) return false;
        if (!retainVictim) victim.discard();
        MobEffectCompat.removeEffect(victim, ACEffects.CORALIUM_PLAGUE);
        MobEffectCompat.removeEffect(victim, ACEffects.DREAD_PLAGUE);
        MobEffectCompat.removeEffect(victim, ACEffects.ANTIMATTER);
        return true;
    }

    public static String invertName(String name) {
        StringBuilder result = new StringBuilder(name.length());
        for (int source = name.length() - 1, target = 0; source >= 0; source--, target++) {
            char character = name.charAt(source);
            result.append(Character.isUpperCase(name.charAt(target))
                ? Character.toUpperCase(character) : Character.toLowerCase(character));
        }
        return result.toString();
    }
}
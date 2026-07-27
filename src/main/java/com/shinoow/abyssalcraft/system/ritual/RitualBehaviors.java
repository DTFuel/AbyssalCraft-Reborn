package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.portal.PortalAnchorBlockEntity;
import com.shinoow.abyssalcraft.content.block.portal.PortalBlocks;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.item.portal.GatewayKeyItem;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;
import com.shinoow.abyssalcraft.world.structure.StructureKind;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Specialized ritual behavior implementations. */
public final class RitualBehaviors {

    private RitualBehaviors() {}

    static void bootstrap(RitualBehaviorRegistry registry) {
        registry.register("portal", new PortalBehavior());
        registry.register("summon_asorah", new SummonBehavior());
        registry.register("summon_sacthoth", new SummonBehavior());
        registry.register("respawn_jzahar", new JzaharRespawnBehavior());
        registry.register("breeding", new BreedingBehavior());
        registry.register("dread_spawn", new DreadSpawnBehavior());
        registry.register("coralium_plague_aoe", new PotionAoeBehavior(ACEffects.CORALIUM_PLAGUE));
        registry.register("dread_plague_aoe", new PotionAoeBehavior(ACEffects.DREAD_PLAGUE));
        registry.register("antimatter_aoe", new PotionAoeBehavior(ACEffects.ANTIMATTER));
        registry.register("weather", new WeatherBehavior());
        registry.register("resurrection", new ResurrectionBehavior());
        registry.register("house", new HouseBehavior());
        registry.register("cleansing", new BiomeBehavior(BiomeRitualTasks.Kind.CLEANSING));
        registry.register("corruption", new BiomeBehavior(BiomeRitualTasks.Kind.CORRUPTION));
        registry.register("infesting", new BiomeBehavior(BiomeRitualTasks.Kind.INFESTING));
        registry.register("curing", new BiomeBehavior(BiomeRitualTasks.Kind.CURING));
        registry.register("purging", new BiomeBehavior(BiomeRitualTasks.Kind.PURGING));
        registry.register("mass_enchanting", new MassEnchantBehavior());
    }

    private static final class PortalBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)
                || !(host.ritualCenter().getItem() instanceof GatewayKeyItem key)) return false;
            ResourceKey<Level> destination = key.selectedDestination(host.ritualCenter());
            return !destination.equals(server.dimension())
                && DimensionDataRegistry.instance().areDimensionsConnected(
                    server.dimension(), destination, key.gatewayTier());
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)
                || !(host.ritualCenter().getItem() instanceof GatewayKeyItem key)) {
                throw new IllegalStateException("Portal ritual lost its Gateway Key");
            }
            ItemStack returnedKey = host.ritualCenter().copy();
            ResourceKey<Level> destination = key.selectedDestination(returnedKey);
            int tier = key.gatewayTier();
            for (BlockPos pedestal : host.ritualPedestalPositions()) {
                server.setBlock(pedestal, EnergyBlocks.MONOLITH_PILLAR.get().defaultBlockState(), 3);
            }
            server.setBlock(altar, PortalBlocks.PORTAL_ANCHOR.get().defaultBlockState(), 3);
            if (!(server.getBlockEntity(altar) instanceof PortalAnchorBlockEntity anchor)
                || anchor.toggle(player, destination, tier) != PortalAnchorBlockEntity.ActivationResult.ACTIVATED) {
                throw new IllegalStateException("Portal Anchor failed to activate");
            }
            if (!player.addItem(returnedKey)) player.drop(returnedKey, false);
        }
    }

    private static final class SummonBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            return level instanceof ServerLevel && targetType(ritual) != null;
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            EntityType<?> type = targetType(ritual);
            if (type == null || !(type.create(server) instanceof Mob mob)) {
                throw new IllegalStateException("Unable to create summon target for " + ritual.name());
            }
            mob.moveTo(altar.getX() + 0.5D, altar.getY() + 1.0D, altar.getZ() + 0.5D,
                server.random.nextFloat() * 360.0F, 0.0F);
            MobSpawnCompat.finalizeTriggeredSpawn(server, mob);
            if (!server.addFreshEntity(mob)) throw new IllegalStateException("Unable to spawn " + ritual.name());
            mob.setPortalCooldown();
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, mob);
        }

        private static EntityType<?> targetType(ManifestRitual ritual) {
            if (ritual.manifest().actionTargets().size() != 1) return null;
            ResourceLocation id = ritual.manifest().actionTargets().get(0);
            return BuiltInRegistries.ENTITY_TYPE.containsKey(id) ? BuiltInRegistries.ENTITY_TYPE.get(id) : null;
        }
    }

    private static final class JzaharRespawnBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            return level instanceof ServerLevel && altar.equals(new BlockPos(4, 54, 85));
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            int pieces = RitualStructurePlacer.place(server, StructureKind.JZAHAR_TEMPLE,
                new BlockPos(4, 53, 7));
            if (pieces != 7) throw new IllegalStateException("Jzahar temple layout must contain seven pieces");
        }
    }

    private static final class HouseBehavior implements RitualBehavior {

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            int pieces = RitualStructurePlacer.place(server, StructureKind.ETHAXIUM_HOUSE, altar);
            if (pieces != 1) throw new IllegalStateException("House ritual layout must contain one piece");
            host.setRitualCenter(ItemStack.EMPTY);
        }
    }

    private static final class BreedingBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            return !animals(level, altar).isEmpty();
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            Map<EntityType<?>, List<Animal>> byType = new LinkedHashMap<>();
            for (Animal animal : animals(server, altar)) {
                byType.computeIfAbsent(animal.getType(), ignored -> new ArrayList<>()).add(animal);
            }
            for (List<Animal> group : byType.values()) {
                for (int index = 0; index + 1 < group.size(); index += 2) {
                    Animal parent = group.get(index);
                    AgeableMob child = parent.getBreedOffspring(server, group.get(index + 1));
                    if (child == null) continue;
                    child.setAge(-24000);
                    child.moveTo(parent.getX(), parent.getY(), parent.getZ(), parent.getYRot(), parent.getXRot());
                    server.addFreshEntity(child);
                }
                applyDensityRisk(server, group);
            }
        }

        private static List<Animal> animals(Level level, BlockPos altar) {
            return level.getEntitiesOfClass(Animal.class, new AABB(altar).inflate(16.0D, 3.0D, 16.0D),
                LivingEntity::isAlive);
        }

        private static void applyDensityRisk(ServerLevel level, List<Animal> animals) {
            if (animals.size() <= 2 || !deathChance(level, animals.size())) return;
            Animal victim = animals.get(level.random.nextInt(animals.size()));
            EntityType<? extends Mob> replacement = victim instanceof Cow ? DemonEntities.EVIL_COW.get()
                : victim instanceof Chicken ? DemonEntities.EVIL_CHICKEN.get()
                : victim instanceof Pig ? DemonEntities.EVIL_PIG.get()
                : victim instanceof Sheep ? DemonEntities.EVIL_SHEEP.get() : null;
            if (replacement != null) {
                Mob evil = replacement.create(level);
                if (evil == null) return;
                evil.moveTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), victim.getXRot());
                victim.discard();
                MobSpawnCompat.finalizeTriggeredSpawn(level, evil);
                level.addFreshEntity(evil);
            } else if (victim instanceof Horse || victim instanceof Ocelot
                || victim instanceof Wolf || victim instanceof Rabbit) {
                victim.hurt(level.damageSources().magic(), 200000.0F);
            }
        }

        private static boolean deathChance(ServerLevel level, int count) {
            if (count < 5) return level.random.nextInt(10) == 0;
            if (count > 5 && count < 10) return level.random.nextInt(9) == 0;
            if (count > 10 && count < 15) return level.random.nextInt(8) == 0;
            if (count > 15 && count < 20) return level.random.nextInt(7) == 0;
            if (count > 20 && count < 25) return level.random.nextInt(6) == 0;
            if (count > 25 && count < 30) return level.random.nextInt(5) == 0;
            if (count > 30 && count < 35) return level.random.nextInt(4) == 0;
            if (count > 35 && count < 40) return level.random.nextInt(3) == 0;
            if (count > 40 && count < 45) return level.random.nextBoolean();
            return true;
        }
    }

    private static final class DreadSpawnBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            return !targets(ritual, level, altar).isEmpty();
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            for (Mob original : List.copyOf(targets(ritual, server, altar))) {
                if (!(original.getType().create(server) instanceof Mob copy)) continue;
                copy.moveTo(original.getX(), original.getY(), original.getZ(),
                    original.getYRot(), original.getXRot());
                server.addFreshEntity(copy);
            }
        }

        private static List<Mob> targets(ManifestRitual ritual, Level level, BlockPos altar) {
            List<ResourceLocation> ids = ritual.manifest().actionTargets();
            return level.getEntitiesOfClass(Mob.class, new AABB(altar).inflate(16.0D, 3.0D, 16.0D),
                mob -> mob.isAlive() && ids.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType())));
        }
    }

    private record PotionAoeBehavior(Supplier<MobEffect> effect) implements RitualBehavior {

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            AABB area = new AABB(altar).inflate(16.0D, 3.0D, 16.0D);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player && entity.isAlive() && !immune(entity))) {
                target.addEffect(MobEffectCompat.effectInstance(effect, 400, 0));
            }
        }

        private boolean immune(LivingEntity entity) {
            return effect == ACEffects.CORALIUM_PLAGUE && EffectHooks.isCoraliumImmune(entity)
                || effect == ACEffects.DREAD_PLAGUE && EffectHooks.isDreadImmune(entity)
                || effect == ACEffects.ANTIMATTER && EffectHooks.isAntimatterImmune(entity);
        }
    }

    private static final class WeatherBehavior implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            return level instanceof ServerLevel && level.getBiome(altar).value().hasPrecipitation();
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            if (!server.isRaining()) server.setWeatherParameters(0, 6000, true, false);
            else if (!server.isThundering()) server.setWeatherParameters(0, 6000, true, true);
            else server.setWeatherParameters(6000, 0, false, false);
        }
    }

    private record BiomeBehavior(BiomeRitualTasks.Kind kind) implements RitualBehavior {

        @Override
        public boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                                Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return false;
            return kind != BiomeRitualTasks.Kind.CURING
                ? BiomeRitualTasks.canStart(kind, server, altar)
                : BiomeRitualTasks.canStart(kind, server, altar)
                    && BiomeRitualTasks.curingReplacement(server, altar) != null;
        }

        @Override
        public void complete(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
            if (!(level instanceof ServerLevel server)) return;
            ResourceKey<net.minecraft.world.level.biome.Biome> replacement =
                kind == BiomeRitualTasks.Kind.CURING
                    ? BiomeRitualTasks.curingReplacement(server, altar) : null;
            BiomeRitualTasks.get(server).enqueue(kind, altar, configuredRange(kind), replacement);
        }

        private static int configuredRange(BiomeRitualTasks.Kind kind) {
            return switch (kind) {
                case CLEANSING -> ACConfig.cleansingRitualRange.get();
                case CORRUPTION -> ACConfig.corruptionRitualRange.get();
                case INFESTING -> ACConfig.infestingRitualRange.get();
                case CURING -> ACConfig.curingRitualRange.get();
                case PURGING -> ACConfig.purgingRitualRange.get();
            };
        }
    }
}
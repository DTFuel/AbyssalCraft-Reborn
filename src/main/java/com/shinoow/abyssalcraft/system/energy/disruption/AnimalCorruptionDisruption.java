package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.platform.TamableCompat;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Replace mapped vanilla farm animals with their evil counterparts. */
public final class AnimalCorruptionDisruption extends Disruption {

    public AnimalCorruptionDisruption() {
        super("animalCorruption", DeityType.SHUBNIGGURATH);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (Animal animal : server.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(64.0))) {
            Mob replacement = CorruptionRegistry.createAnimalReplacement(server, animal);
            if (replacement != null) {
                replace(server, animal, replacement, true);
                continue;
            }
            if (animal instanceof AbstractHorse horse
                && !(horse instanceof ZombieHorse) && !(horse instanceof SkeletonHorse)) {
                EntityType<? extends AbstractHorse> type = server.random.nextBoolean()
                    ? EntityType.ZOMBIE_HORSE
                    : EntityType.SKELETON_HORSE;
                AbstractHorse corrupted = type.create(server);
                if (corrupted != null) {
                    corrupted.setAge(horse.getAge());
                    replace(server, horse, corrupted, false);
                }
            } else if (animal instanceof Wolf wolf && wolf.isTame() && !players.isEmpty()) {
                Player target = players.get(server.random.nextInt(players.size()));
                TamableCompat.untame(wolf);
                wolf.setPersistentAngerTarget(target.getUUID());
                wolf.startPersistentAngerTimer();
                wolf.setTarget(target);
            } else if (animal instanceof Cat cat && cat.isTame()) {
                TamableCompat.untame(cat);
            } else if (animal instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL) {
                rabbit.setVariant(Rabbit.Variant.EVIL);
            }
        }
    }

    private static void replace(ServerLevel server, Animal animal, Mob replacement, boolean finalizeSpawn) {
        replacement.copyPosition(animal);
        if (animal.hasCustomName()) {
            replacement.setCustomName(animal.getCustomName());
        }
        replacement.setPersistenceRequired();
        if (finalizeSpawn) {
            MobSpawnCompat.finalizeSpawnerSpawn(server, replacement);
        }
        if (server.addFreshEntity(replacement)) {
            animal.discard();
        }
    }
}
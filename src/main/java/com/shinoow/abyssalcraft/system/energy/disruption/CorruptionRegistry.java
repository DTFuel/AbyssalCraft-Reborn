package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.content.entity.demon.DemonAnimal;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.Villager;

/** Frozen corruption mappings and legacy sacrifice-target predicate. */
public final class CorruptionRegistry {

    private static final Map<EntityType<?>, Supplier<? extends EntityType<? extends Mob>>> ANIMALS =
        new LinkedHashMap<>();

    static {
        ANIMALS.put(EntityType.CHICKEN, DemonEntities.EVIL_CHICKEN);
        ANIMALS.put(EntityType.COW, DemonEntities.EVIL_COW);
        ANIMALS.put(EntityType.PIG, DemonEntities.EVIL_PIG);
        ANIMALS.put(EntityType.SHEEP, DemonEntities.EVIL_SHEEP);
    }

    private CorruptionRegistry() {}

    public static int animalMappings() {
        return ANIMALS.size();
    }

    public static Mob createAnimalReplacement(ServerLevel level, Animal animal) {
        Supplier<? extends EntityType<? extends Mob>> target = ANIMALS.get(animal.getType());
        return target == null ? null : target.get().create(level);
    }

    public static boolean isSacrifice(Mob mob) {
        return mob instanceof Animal || mob instanceof AmbientCreature || mob instanceof WaterAnimal
            || mob instanceof EvilAnimal || mob instanceof DemonAnimal || mob instanceof Spider
            || mob instanceof Villager;
    }
}
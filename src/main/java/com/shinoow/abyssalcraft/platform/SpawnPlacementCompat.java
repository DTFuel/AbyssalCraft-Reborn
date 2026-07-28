package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.levelgen.Heightmap;

//? if forge {
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
//?} else {
/*import net.minecraft.world.entity.SpawnPlacementTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
*///?}

/**
 * Compat: registering natural-spawn placement rules per {@link EntityType} (loader axis).
 *
 * <p>1.12.2 called {@code EntitySpawnPlacementRegistry.setPlacementType} directly; both modern loaders
 * moved this onto a MOD-bus event with an identical {@code register(EntityType, SpawnPlacements.Type,
 * Heightmap.Types, SpawnPredicate, Operation)} surface -- Forge {@code SpawnPlacementRegisterEvent}
 * vs NeoForge {@code RegisterSpawnPlacementsEvent}. Only the event class and the {@code Operation}
 * enum package differ, so business registrars (the D2a mob families) funnel through here and stay
 * fork-free, mirroring {@link EntityAttributeCompat}.
 *
 * <p>Usage: modules call {@link #register} from their registrar static init (guaranteed loaded via
 * {@code ModRegistries.ALL}); the main class calls {@link #attach} once to hook the event. Without a
 * placement rule a mob listed by a biome or biome modifier ignores light/ground checks, so this
 * remains the shared placement gate for all data-driven natural spawning.
 */
public final class SpawnPlacementCompat {

    private SpawnPlacementCompat() {}

    private record MonsterEntry<T extends Monster>(Supplier<EntityType<T>> type) {}
    private record AnimalEntry<T extends Animal>(Supplier<EntityType<T>> type) {}
    private record BatEntry<T extends Bat>(Supplier<EntityType<T>> type) {}
    private record WaterEntry<T extends WaterAnimal>(Supplier<EntityType<T>> type) {}

    private static final List<MonsterEntry<?>> MONSTER_ENTRIES = new ArrayList<>();
    private static final List<AnimalEntry<?>> ANIMAL_ENTRIES = new ArrayList<>();
    private static final List<BatEntry<?>> BAT_ENTRIES = new ArrayList<>();
    private static final List<WaterEntry<?>> WATER_ENTRIES = new ArrayList<>();

    public static Set<String> registeredIds() {
        Set<String> ids = new TreeSet<>();
        MONSTER_ENTRIES.forEach(entry -> ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(entry.type().get()).getPath()));
        ANIMAL_ENTRIES.forEach(entry -> ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(entry.type().get()).getPath()));
        BAT_ENTRIES.forEach(entry -> ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(entry.type().get()).getPath()));
        WATER_ENTRIES.forEach(entry -> ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(entry.type().get()).getPath()));
        return ids;
    }

    /**
     * Record a ground + darkness (vanilla hostile) spawn-placement rule to publish when the event
     * fires. All D2a mob families share the identical rule (ON_GROUND / MOTION_BLOCKING_NO_LEAVES /
     * {@code Monster.checkMonsterSpawnRules}); the loader-forked placement-type constant
     * ({@code SpawnPlacements.Type} on 1.20.1 vs {@code SpawnPlacementTypes} on 1.21) is applied
     * inside {@link #attach}, so caller registrars stay fork-free.
     */
    public static <T extends Monster> void registerGroundMonster(Supplier<EntityType<T>> type) {
        MONSTER_ENTRIES.add(new MonsterEntry<>(type));
    }

    public static <T extends Animal> void registerGroundAnimal(Supplier<EntityType<T>> type) {
        ANIMAL_ENTRIES.add(new AnimalEntry<>(type));
    }

    public static <T extends Bat> void registerAmbientBat(Supplier<EntityType<T>> type) {
        BAT_ENTRIES.add(new BatEntry<>(type));
    }

    public static <T extends WaterAnimal> void registerWaterAnimal(Supplier<EntityType<T>> type) {
        WATER_ENTRIES.add(new WaterEntry<>(type));
    }

    /** Attach the mod-bus listener that publishes every {@link #registerGroundMonster}ed rule. */
    public static void attach(IEventBus modBus) {
        //? if forge {
        modBus.addListener((SpawnPlacementRegisterEvent event) -> {
            for (MonsterEntry<?> e : MONSTER_ENTRIES) registerMonster(event, e);
            for (AnimalEntry<?> e : ANIMAL_ENTRIES) registerAnimal(event, e);
            for (BatEntry<?> e : BAT_ENTRIES) registerBat(event, e);
            for (WaterEntry<?> e : WATER_ENTRIES) registerWater(event, e);
        });
        //?} else {
        /*modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            for (MonsterEntry<?> e : MONSTER_ENTRIES) registerMonster(event, e);
            for (AnimalEntry<?> e : ANIMAL_ENTRIES) registerAnimal(event, e);
            for (BatEntry<?> e : BAT_ENTRIES) registerBat(event, e);
            for (WaterEntry<?> e : WATER_ENTRIES) registerWater(event, e);
        });
        *///?}
    }

    //? if forge {
    private static <T extends Monster> void registerMonster(SpawnPlacementRegisterEvent event, MonsterEntry<T> e) {
        event.register(e.type().get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }

    private static <T extends Animal> void registerAnimal(SpawnPlacementRegisterEvent event, AnimalEntry<T> e) {
        event.register(e.type().get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Bat> void registerBat(SpawnPlacementRegisterEvent event, BatEntry<T> e) {
        event.register(e.type().get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, level, spawnType, pos, random) -> Bat.checkBatSpawnRules((EntityType) type, level, spawnType, pos, random),
            SpawnPlacementRegisterEvent.Operation.AND);
    }

    private static <T extends WaterAnimal> void registerWater(SpawnPlacementRegisterEvent event, WaterEntry<T> e) {
        event.register(e.type().get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }
    //?} else {
    /*private static <T extends Monster> void registerMonster(RegisterSpawnPlacementsEvent event, MonsterEntry<T> e) {
        event.register(e.type().get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    private static <T extends Animal> void registerAnimal(RegisterSpawnPlacementsEvent event, AnimalEntry<T> e) {
        event.register(e.type().get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Bat> void registerBat(RegisterSpawnPlacementsEvent event, BatEntry<T> e) {
        event.register(e.type().get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, level, spawnType, pos, random) -> Bat.checkBatSpawnRules((EntityType) type, level, spawnType, pos, random),
            RegisterSpawnPlacementsEvent.Operation.AND);
    }

    private static <T extends WaterAnimal> void registerWater(RegisterSpawnPlacementsEvent event, WaterEntry<T> e) {
        event.register(e.type().get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }
    *///?}
}

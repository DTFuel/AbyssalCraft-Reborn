package com.shinoow.abyssalcraft.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Sound-event registry (owned by PH-4 / Stage H1). Registers every AbyssalCraft {@link SoundEvent}
 * faithfully to the 1.12.2 {@code MiscHandler.registerSoundEvent} list; the ids match the keys in
 * {@code assets/abyssalcraft/sounds.json} (which maps each to its {@code .ogg} variants).
 *
 * <p>Registration is fork-free: {@code SoundEvent.createVariableRangeEvent(ResourceLocation)} and
 * {@link Registries#SOUND_EVENT} are identical on 1.20.1 and 1.21, so this is a plain {@link ModRegistrar}
 * with no {@code platform/} compat. Attached via {@code ModRegistries.ALL} like every other registrar.
 *
 * <p>{@link #EVENTS} keeps each id&rarr;supplier so the ported entities/blocks can look their sound up when
 * they are wired to play it (mob ambient/hurt/death, shoggoth AI, boss abilities, ...). That wiring lives
 * in the owning entity/block classes and is done as those systems consume it; PH-4 delivers the registry
 * + assets so the sounds exist and load.
 */
public final class ModSounds {

    private ModSounds() {}

    /** {@code minecraft:sound_event} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<SoundEvent> SOUNDS = ModRegistrar.of(Registries.SOUND_EVENT, AbyssalCraft.MODID);

    /** Faithful 1.12.2 sound-event ids (match {@code sounds.json} keys). */
    private static final String[] IDS = {
        "dreadguard.death", "dreadguard.idle", "dreadguard.hit", "dreadguard.barf",
        "ghoul.normal.idle", "ghoul.hit", "ghoul.death",
        "ghoul.pete.idle", "ghoul.wilson.idle", "ghoul.orange.idle",
        "sacthoth.death", "shadow.death", "shadow.hit",
        "remnant.scream", "remnant.yes", "remnant.no", "remnant.priest.chant",
        "shoggoth.idle", "shoggoth.hit", "shoggoth.death", "shoggoth.step",
        "shoggoth.shoot", "shoggoth.consume", "shoggoth.birth",
        "jzahar.charge", "jzahar.blast", "jzahar.shout",
        "jzahar.earthquake", "jzahar.implosion", "jzahar.black_hole",
        "chant.cthulhu", "chant.yog_sothoth_1", "chant.yog_sothoth_2",
        "chant.hastur_1", "chant.hastur_2", "chant.sleeping", "chant.cthugha",
        "dreadspawn.idle", "dreadspawn.hit", "dreadspawn.death",
        "abyssalzombie.idle", "abyssalzombie.hit", "abyssalzombie.death",
        "antiplayer.hurt", "misc.compass"
    };

    /** Every registered sound event, keyed by its id (for downstream wiring). */
    public static final Map<String, Supplier<SoundEvent>> EVENTS = new LinkedHashMap<>();

    static {
        for (String id : IDS) {
            EVENTS.put(id, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(ACRef.id(id))));
        }
    }

    /** Resolve a registered {@link SoundEvent} by its id (PH-4b entity/block wiring). Id must be in {@link #IDS}. */
    public static SoundEvent event(String id) {
        return EVENTS.get(id).get();
    }
}

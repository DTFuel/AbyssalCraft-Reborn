package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.system.ritual.BiomeRitualTasks;

//? if forge {
import net.minecraftforge.event.TickEvent;
//?} else {
/*import net.neoforged.neoforge.event.tick.ServerTickEvent;
*///?}

/** Loader bridge for the persistent biome-ritual task queue. */
public final class RitualTaskCompat {

    private RitualTaskCompat() {}

    public static void attach() {
        //? if forge {
        EventBuses.game().addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) BiomeRitualTasks.tick(event.getServer());
        });
        //?} else {
        /*EventBuses.game().addListener((ServerTickEvent.Post event) ->
            BiomeRitualTasks.tick(event.getServer()));
        *///?}
    }
}
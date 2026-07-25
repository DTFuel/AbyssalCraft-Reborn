package com.shinoow.abyssalcraft.platform;

//? if forge {
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
*///?}

/**
 * Compat: the runtime / game event bus (loader axis).
 *
 * <p>Forge exposes {@code MinecraftForge.EVENT_BUS}; NeoForge {@code NeoForge.EVENT_BUS}. The MOD
 * bus is not exposed here - it is threaded from the main class into {@code init(IEventBus)}.
 */
public final class EventBuses {

    private EventBuses() {}

    /** The game/runtime event bus (gameplay events: entity join, player interactions, etc.). */
    public static IEventBus game() {
        //? if forge {
        return MinecraftForge.EVENT_BUS;
        //?} else {
        /*return NeoForge.EVENT_BUS;
        *///?}
    }
}

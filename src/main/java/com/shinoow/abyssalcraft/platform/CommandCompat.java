package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.system.command.ACCommands;

//? if forge {
import net.minecraftforge.event.RegisterCommandsEvent;
//?} else {
/*import net.neoforged.neoforge.event.RegisterCommandsEvent;
*///?}

/**
 * Compat: command registration (loader axis). Both loaders fire {@code RegisterCommandsEvent} on the
 * game/runtime bus with an identical {@code getDispatcher()}; only the package differs (Forge
 * {@code net.minecraftforge.event} vs NeoForge {@code net.neoforged.neoforge.event}). So only the import
 * forks -- the callback delegates to the fork-free {@link ACCommands}. Wired once from the main class
 * {@code init} (same as {@link GameHooksCompat#attach()}).
 */
public final class CommandCompat {

    private CommandCompat() {}

    /** Subscribe the command-registration listener to the game/runtime event bus. */
    public static void attach() {
        EventBuses.game().addListener((RegisterCommandsEvent event) -> ACCommands.register(event.getDispatcher()));
    }
}

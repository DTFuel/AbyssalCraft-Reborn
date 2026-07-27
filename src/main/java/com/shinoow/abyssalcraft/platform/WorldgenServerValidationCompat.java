package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.validation.server.ServerMatrixOrchestrator;

//? if forge {
import net.minecraftforge.event.server.ServerStartedEvent;
//?} else {
/*import net.neoforged.neoforge.event.server.ServerStartedEvent;
*///?}

/** Loader-local server-start hook for the unified RR-SERVER/T11.2 matrix. */
public final class WorldgenServerValidationCompat {

    private WorldgenServerValidationCompat() {}

    public static void attach() {
        EventBuses.game().addListener((ServerStartedEvent event) -> ServerMatrixOrchestrator.run(event.getServer()));
    }
}
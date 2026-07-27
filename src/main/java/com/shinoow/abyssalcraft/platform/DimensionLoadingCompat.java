package com.shinoow.abyssalcraft.platform;

import java.util.function.BooleanSupplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.world.ACDimensions;

//? if forge {
import net.minecraftforge.event.server.ServerStartedEvent;
//?} else {
/*import net.neoforged.neoforge.event.server.ServerStartedEvent;
*///?}

/** Synchronizes the legacy keep-loaded dimension options with a center-chunk ticket. */
public final class DimensionLoadingCompat {

    private DimensionLoadingCompat() {}

    public static void attach() {
        EventBuses.game().addListener((ServerStartedEvent event) -> sync(event.getServer()));
    }

    private static void sync(MinecraftServer server) {
        sync(server, ACDimensions.ABYSSAL_WASTELAND, ACConfig.keepLoaded1::get);
        sync(server, ACDimensions.DREADLANDS, ACConfig.keepLoaded2::get);
        sync(server, ACDimensions.OMOTHOL, ACConfig.keepLoaded3::get);
        sync(server, ACDimensions.DARK_REALM, ACConfig.keepLoaded4::get);
    }

    private static void sync(MinecraftServer server, ResourceKey<Level> dimension,
                             BooleanSupplier keepLoaded) {
        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            throw new IllegalStateException("Missing configured dimension " + dimension.location());
        }
        level.setChunkForced(0, 0, keepLoaded.getAsBoolean());
    }
}
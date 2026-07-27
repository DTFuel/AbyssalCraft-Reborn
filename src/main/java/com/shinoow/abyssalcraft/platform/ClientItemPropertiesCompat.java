package com.shinoow.abyssalcraft.platform;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
*///?}

/** Loader-neutral client setup hook for item property registration. */
public final class ClientItemPropertiesCompat {

    private ClientItemPropertiesCompat() {}

    public static void attach(IEventBus modBus, Runnable registration) {
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(registration));
    }
}
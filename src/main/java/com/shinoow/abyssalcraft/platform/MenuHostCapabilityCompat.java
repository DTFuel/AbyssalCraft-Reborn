package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStands;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
*///?}

/** Loader registration for sided item handlers owned by RR-MENU-HOST. */
public final class MenuHostCapabilityCompat {

    private static boolean attached;

    private MenuHostCapabilityCompat() {}

    public static void attach(IEventBus modBus) {
        attached = true;
        //? if >=1.21 {
        /*modBus.addListener((RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BrewingStands.BREWING_STAND_BE.get(),
                (stand, side) -> side == null ? new InvWrapper(stand) : new SidedInvWrapper(stand, side)));
        *///?}
    }

    public static boolean isAttached() {
        return attached;
    }
}
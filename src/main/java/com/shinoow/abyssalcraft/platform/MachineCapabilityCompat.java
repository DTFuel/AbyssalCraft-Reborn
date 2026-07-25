package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
*///?}

public final class MachineCapabilityCompat {

    private MachineCapabilityCompat() {}

    public static void attach(IEventBus modBus) {
        //? if >=1.21 {
        /*modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Crystallizers.CRYSTALLIZER_BE.get(),
                (machine, side) -> side == null ? new InvWrapper(machine) : new SidedInvWrapper(machine, side));
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Transmutators.TRANSMUTATOR_BE.get(),
                (machine, side) -> side == null ? new InvWrapper(machine) : new SidedInvWrapper(machine, side));
        });
        *///?}
    }
}
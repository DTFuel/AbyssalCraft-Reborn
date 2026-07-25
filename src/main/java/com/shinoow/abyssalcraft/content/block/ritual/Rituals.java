package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.system.ritual.InfusionRitual;
import com.shinoow.abyssalcraft.system.ritual.RitualRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The concrete rituals (owned by content/block/ritual), registered into the {@link RitualRegistry} (PS-6) at
 * mod init. The pilot infusion ritual proves the end-to-end loop (charge at a statue -> place offerings on
 * the pedestals -> right-click the altar with the Necronomicon -> product). The faithful ritual roster
 * (creation / summoning / breeding / potion / ... with their real offerings and costs) is the PS-6b
 * follow-up. Offerings are vanilla items (registered before init); the AC-item result is supplied lazily.
 */
public final class Rituals {

    private Rituals() {}

    public static void bootstrap() {
        // Pilot: nether star + diamond on the pedestals + a Necronomicon holding >= 100 PE -> oblivion catalyst.
        RitualRegistry.instance().register(new InfusionRitual(
            "oblivion_catalyst", 0, null, 100F,
            () -> new ItemStack(MiscItems.OBLIVION_CATALYST.get()),
            new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND)));
    }
}

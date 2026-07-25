package com.shinoow.abyssalcraft.content.item.energy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/** Item catalog for the Potential Energy subsystem. */
public final class EnergyItems {

    private static final List<Supplier<Item>> CHARMS_MUTABLE = new ArrayList<>();

    private EnergyItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    static {
        registerCharmFamily("charm", null);
        registerCharmFamily("cthulhucharm", DeityType.CTHULHU);
        registerCharmFamily("hasturcharm", DeityType.HASTUR);
        registerCharmFamily("jzaharcharm", DeityType.JZAHAR);
        registerCharmFamily("azathothcharm", DeityType.AZATHOTH);
        registerCharmFamily("nyarlathotepcharm", DeityType.NYARLATHOTEP);
        registerCharmFamily("yogsothothcharm", DeityType.YOGSOTHOTH);
        registerCharmFamily("shubniggurathcharm", DeityType.SHUBNIGGURATH);
    }

    public static final List<Supplier<Item>> CHARMS = Collections.unmodifiableList(CHARMS_MUTABLE);

    private static void registerCharmFamily(String baseId, DeityType deity) {
        registerCharm(baseId, null, deity);
        registerCharm(baseId + "_range", AmplifierType.RANGE, deity);
        registerCharm(baseId + "_duration", AmplifierType.DURATION, deity);
        registerCharm(baseId + "_power", AmplifierType.POWER, deity);
    }

    private static void registerCharm(String id, AmplifierType amplifier, DeityType deity) {
        CHARMS_MUTABLE.add(ITEMS.register(id, () -> new AmplifierCharmItem(amplifier, deity)));
    }
}
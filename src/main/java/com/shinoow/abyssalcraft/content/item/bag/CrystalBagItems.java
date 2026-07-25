package com.shinoow.abyssalcraft.content.item.bag;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

public final class CrystalBagItems {

    private CrystalBagItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<CrystalBagItem> SMALL = ITEMS.register("crystalbag_small", () -> new CrystalBagItem(18));
    public static final Supplier<CrystalBagItem> MEDIUM = ITEMS.register("crystalbag_medium", () -> new CrystalBagItem(36));
    public static final Supplier<CrystalBagItem> LARGE = ITEMS.register("crystalbag_large", () -> new CrystalBagItem(54));
    public static final Supplier<CrystalBagItem> HUGE = ITEMS.register("crystalbag_huge", () -> new CrystalBagItem(72));

    public static final List<Supplier<CrystalBagItem>> ALL = List.of(SMALL, MEDIUM, LARGE, HUGE);
}
package com.shinoow.abyssalcraft.content.item.tablet;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class TabletItems {

    private TabletItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final Supplier<StoneTabletItem> STONE_TABLET =
        ITEMS.register("stone_tablet", StoneTabletItem::new);
    public static final List<Supplier<? extends Item>> ALL = List.of(STONE_TABLET);
}
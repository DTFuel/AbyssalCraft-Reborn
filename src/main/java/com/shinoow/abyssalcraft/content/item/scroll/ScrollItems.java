package com.shinoow.abyssalcraft.content.item.scroll;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.system.spell.ScrollType;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/** The five legacy scroll quality tiers plus the two named unique scroll variants. */
public final class ScrollItems {

    private ScrollItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<ScrollItem> BASIC = scroll("basic_scroll", ScrollType.BASIC);
    public static final Supplier<ScrollItem> LESSER = scroll("lesser_scroll", ScrollType.LESSER);
    public static final Supplier<ScrollItem> MODERATE = scroll("moderate_scroll", ScrollType.MODERATE);
    public static final Supplier<ScrollItem> GREATER = scroll("greater_scroll", ScrollType.GREATER);
    public static final Supplier<ScrollItem> ANTIMATTER = scroll("antimatter_scroll", ScrollType.UNIQUE);
    public static final Supplier<ScrollItem> OBLIVION = scroll("oblivion_scroll", ScrollType.UNIQUE);

    public static final List<Supplier<ScrollItem>> ALL =
        List.of(BASIC, LESSER, MODERATE, GREATER, ANTIMATTER, OBLIVION);

    private static Supplier<ScrollItem> scroll(String id, ScrollType type) {
        return ITEMS.register(id, () -> new ScrollItem(type));
    }
}
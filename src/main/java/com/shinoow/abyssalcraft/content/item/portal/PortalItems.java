package com.shinoow.abyssalcraft.content.item.portal;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/** The four legacy Gateway Key tiers. */
public final class PortalItems {

    private PortalItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<GatewayKeyItem> GATEWAY_KEY = key("gatewaykey", 0);
    public static final Supplier<GatewayKeyItem> DREADLANDS_GATEWAY_KEY = key("gatewaykeydl", 1);
    public static final Supplier<GatewayKeyItem> OMOTHOL_GATEWAY_KEY = key("gatewaykeyjzh", 2);
    public static final Supplier<GatewayKeyItem> SILVER_KEY = key("silver_key", 3);
    public static final Supplier<Item> DREAD_PLAGUED_GATEWAY_KEY = ITEMS.register("dreadkey", () ->
        new Item(new Item.Properties()));

    public static final List<Supplier<GatewayKeyItem>> ALL = List.of(
        GATEWAY_KEY, DREADLANDS_GATEWAY_KEY, OMOTHOL_GATEWAY_KEY, SILVER_KEY);

    private static Supplier<GatewayKeyItem> key(String id, int tier) {
        return ITEMS.register(id, () -> new GatewayKeyItem(tier));
    }
}
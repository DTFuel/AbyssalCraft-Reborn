package com.shinoow.abyssalcraft.content.item.weapon;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.ToolCompat;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class SoulReaperItems {

    private SoulReaperItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<Item> SOUL_REAPER_BLADE = ITEMS.register("soulreaper", () ->
        ToolCompat.soulReaper(new Item.Properties()));
}

package com.shinoow.abyssalcraft.content.item.material;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

public final class MachineContentItems {

    private MachineContentItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final Supplier<Item> TRANSMUTATION_GEM =
        ITEMS.register("transmutation_gem", TransmutationGemItem::new);
    public static final List<Supplier<Item>> ALL = List.of(TRANSMUTATION_GEM);
}
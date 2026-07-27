package com.shinoow.abyssalcraft.content.item.ritual;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/** Missing legacy ritual centres/results that do not belong to another completed content system. */
public final class RitualItems {

    private RitualItems() {}

    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<Item> SEALING_KEY = plain("sealing_key");
    public static final Supplier<Item> INTERDIMENSIONAL_CAGE =
        ITEMS.register("interdimensional_cage", InterdimensionalCageItem::new);
    public static final Supplier<Item> BOOK_OF_MANY_FACES =
        ITEMS.register("book_of_many_faces", BookOfManyFacesItem::new);
    public static final Supplier<Item> STAFF_OF_THE_GATEKEEPER =
        ITEMS.register("staff_of_the_gatekeeper", GatekeeperStaffItem::new);

    public static final Supplier<Item> RING = plain("ring");
    public static final Supplier<Item> RING_OVERWORLD = tier("ring_overworld", 1);
    public static final Supplier<Item> RING_ABYSSAL_WASTELAND = tier("ring_abyssal_wasteland", 2);
    public static final Supplier<Item> RING_DREADLANDS = tier("ring_dreadlands", 3);
    public static final Supplier<Item> RING_OMOTHOL = tier("ring_omothol", 4);

    public static final Supplier<Item> STAFF_OF_RENDING = staff("staff_of_rending", 0);
    public static final Supplier<Item> ABYSSAL_WASTELAND_STAFF_OF_RENDING =
        staff("abyssal_wasteland_staff_of_rending", 1);
    public static final Supplier<Item> DREADLANDS_STAFF_OF_RENDING =
        staff("dreadlands_staff_of_rending", 2);
    public static final Supplier<Item> OMOTHOL_STAFF_OF_RENDING =
        staff("omothol_staff_of_rending", 3);

    public static final Supplier<Item> ABYSSAL_WASTELAND_ESSENCE = plain("abyssal_wasteland_essence");
    public static final Supplier<Item> DREADLANDS_ESSENCE = plain("dreadlands_essence");
    public static final Supplier<Item> OMOTHOL_ESSENCE = plain("omothol_essence");

    public static final List<Supplier<Item>> ALL = List.of(
        SEALING_KEY, INTERDIMENSIONAL_CAGE, BOOK_OF_MANY_FACES, STAFF_OF_THE_GATEKEEPER,
        RING, RING_OVERWORLD, RING_ABYSSAL_WASTELAND, RING_DREADLANDS, RING_OMOTHOL,
        STAFF_OF_RENDING, ABYSSAL_WASTELAND_STAFF_OF_RENDING,
        DREADLANDS_STAFF_OF_RENDING, OMOTHOL_STAFF_OF_RENDING,
        ABYSSAL_WASTELAND_ESSENCE, DREADLANDS_ESSENCE, OMOTHOL_ESSENCE);

    private static Supplier<Item> plain(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static Supplier<Item> tier(String id, int tier) {
        return ITEMS.register(id, () -> new TieredRitualItem(tier));
    }

    private static Supplier<Item> staff(String id, int tier) {
        return ITEMS.register(id, () -> new StaffOfRendingItem(tier));
    }
}
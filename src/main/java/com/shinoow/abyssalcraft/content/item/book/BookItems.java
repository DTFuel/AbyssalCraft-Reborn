package com.shinoow.abyssalcraft.content.item.book;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * The Necronomicon books (upstream content that unblocks PH-5 -- the death-book GUI becomes reachable
 * in-game via a real book item). Faithful to the 1.12.2 five-tier book roster ({@code ItemNecronomicon}
 * types 0-4), registered with clean snake_case ids. Item models/textures are asset-stage (PK) work;
 * this only registers + names the books. The registrar is attached to the MOD bus through
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class BookItems {

    private BookItems() {}

    /** {@code minecraft:item} registrar in the AbyssalCraft namespace for the Necronomicon books. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<Item> NECRONOMICON = book("necronomicon", 0);
    public static final Supplier<Item> ABYSSAL_WASTELAND_NECRONOMICON = book("abyssal_wasteland_necronomicon", 1);
    public static final Supplier<Item> DREADLANDS_NECRONOMICON = book("dreadlands_necronomicon", 2);
    public static final Supplier<Item> OMOTHOL_NECRONOMICON = book("omothol_necronomicon", 3);
    public static final Supplier<Item> ABYSSALNOMICON = book("abyssalnomicon", 4);

    /** Every Necronomicon book, in tier order (for the creative tab). */
    public static final List<Supplier<Item>> ALL = List.of(
        NECRONOMICON, ABYSSAL_WASTELAND_NECRONOMICON, DREADLANDS_NECRONOMICON, OMOTHOL_NECRONOMICON, ABYSSALNOMICON);

    private static Supplier<Item> book(String name, int bookType) {
        return ITEMS.register(name, () -> new NecronomiconItem(bookType));
    }
}

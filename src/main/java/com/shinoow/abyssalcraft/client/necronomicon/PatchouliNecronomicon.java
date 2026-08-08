package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.List;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.PatchouliAPI;

/** Client entry point for the five data-driven Patchouli Necronomicon editions. */
public final class PatchouliNecronomicon {

    private static final List<ResourceLocation> BOOKS = List.of(
        ACRef.id("necronomicon"),
        ACRef.id("abyssal_wasteland_necronomicon"),
        ACRef.id("dreadlands_necronomicon"),
        ACRef.id("omothol_necronomicon"),
        ACRef.id("abyssalnomicon"));

    private PatchouliNecronomicon() {}

    public static void open(int bookType) {
        if (bookType < 0 || bookType >= BOOKS.size()) {
            throw new IllegalArgumentException("invalid Necronomicon book type " + bookType);
        }
        PatchouliAPI.get().openBookGUI(BOOKS.get(bookType));
    }

    public static List<ResourceLocation> books() {
        return BOOKS;
    }
}
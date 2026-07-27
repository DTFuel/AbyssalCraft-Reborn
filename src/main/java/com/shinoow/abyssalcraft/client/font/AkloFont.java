package com.shinoow.abyssalcraft.client.font;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.resources.ResourceLocation;

/**
 * The Aklo font (owned by PH-6): the eldritch alphabet used for Necronomicon whispers / lore, faithful to the
 * 1.12.2 {@code AbyssalCraftAPI.getAkloFont()}. Modern Minecraft fonts are data-driven; this returns the font
 * id backed by {@code assets/abyssalcraft/font/aklo.json}. Apply it via
 * {@code Style.EMPTY.withFont(AkloFont.location())} on a {@link net.minecraft.network.chat.Component}.
 *
 * <p>The {@code aklo.json} shipped here maps printable ASCII through the migrated Aklo glyph bitmap.
 */
public final class AkloFont {

    private AkloFont() {}

    /** The Aklo font id ({@code abyssalcraft:aklo}). */
    public static ResourceLocation location() {
        return ACRef.id("aklo");
    }
}

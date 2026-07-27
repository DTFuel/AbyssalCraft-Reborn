package com.shinoow.abyssalcraft.client;

import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItems;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.client.renderer.item.ItemProperties;

/** Client item-model predicates retained from the legacy scroll renderer. */
public final class ClientItemProperties {

    private ClientItemProperties() {}

    public static void register() {
        var inscribed = ACRef.id("inscribed");
        ItemProperties.register(ScrollItems.BASIC.get(), inscribed,
            (stack, level, entity, seed) -> ScrollItem.spellId(stack).isEmpty() ? 0.0F : 1.0F);
        ItemProperties.register(ScrollItems.LESSER.get(), inscribed,
            (stack, level, entity, seed) -> ScrollItem.spellId(stack).isEmpty() ? 0.0F : 1.0F);
        ItemProperties.register(ScrollItems.MODERATE.get(), inscribed,
            (stack, level, entity, seed) -> ScrollItem.spellId(stack).isEmpty() ? 0.0F : 1.0F);
        ItemProperties.register(ScrollItems.GREATER.get(), inscribed,
            (stack, level, entity, seed) -> ScrollItem.spellId(stack).isEmpty() ? 0.0F : 1.0F);
        ItemProperties.register(TabletItems.STONE_TABLET.get(), ACRef.id("cursed"),
            (stack, level, entity, seed) -> StoneTabletStorage.isCursed(stack) ? 1.0F : 0.0F);
    }
}
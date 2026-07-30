package com.shinoow.abyssalcraft.client;

import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItems;
import com.shinoow.abyssalcraft.content.item.misc.AntidoteItem;
import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.item.tool.ToolItems;
import com.shinoow.abyssalcraft.content.item.weapon.SoulReaperItems;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;

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
        var content = ACRef.id("content");
        ItemProperties.register(MiscItems.CORALIUM_ANTIDOTE.get(), content,
            (stack, level, entity, seed) -> AntidoteItem.visualContent(stack));
        ItemProperties.register(MiscItems.DREAD_ANTIDOTE.get(), content,
            (stack, level, entity, seed) -> AntidoteItem.visualContent(stack));
        ItemProperties.register(RitualItems.INTERDIMENSIONAL_CAGE.get(), ACRef.id("captured"),
            (stack, level, entity, seed) -> InterdimensionalCageItem.hasCapturedEntity(stack) ? 1.0F : 0.0F);
        ItemProperties.register(SoulReaperItems.SOUL_REAPER_BLADE.get(), ACRef.id("level"),
            (stack, level, entity, seed) -> soulReaperLevel(ItemDataCompat.getInt(stack, "souls", 0)));
        ItemProperties.register(TransferContent.SPIRIT_TABLET.get(), ACRef.id("mode"),
            (stack, level, entity, seed) -> SpiritTabletStorage.mode(stack) / 2.0F);
        ItemProperties.register(ToolItems.CORALIUM_LONGBOW.get(), ACRef.vanilla("pull"),
            (stack, level, entity, seed) -> entity != null && entity.getUseItem() == stack
                ? entity.getTicksUsingItem() / 20.0F : 0.0F);
        ItemProperties.register(ToolItems.CORALIUM_LONGBOW.get(), ACRef.vanilla("pulling"),
            (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private static float soulReaperLevel(int souls) {
        if (souls >= 1000) return 1.0F;
        if (souls >= 500) return 0.8F;
        if (souls >= 250) return 0.6F;
        if (souls >= 125) return 0.4F;
        return souls >= 60 ? 0.2F : 0.0F;
    }
}
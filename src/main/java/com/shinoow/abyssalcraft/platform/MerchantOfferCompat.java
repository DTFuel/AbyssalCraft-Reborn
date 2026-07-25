package com.shinoow.abyssalcraft.platform;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
//? if >=1.21 {
/*import net.minecraft.world.item.trading.ItemCost;
*///?}

/** Cross-version MerchantOffer construction and neutral NBT persistence. */
public final class MerchantOfferCompat {

    private MerchantOfferCompat() {}

    public static MerchantOffer offer(ItemStack firstCost, ItemStack secondCost, ItemStack result,
                                      int maxUses, int xp, float priceMultiplier) {
        //? if >=1.21 {
        /*ItemCost first = new ItemCost(firstCost.getItem(), firstCost.getCount());
        Optional<ItemCost> second = secondCost.isEmpty()
            ? Optional.empty()
            : Optional.of(new ItemCost(secondCost.getItem(), secondCost.getCount()));
        return new MerchantOffer(first, second, result.copy(), 0, maxUses, xp, priceMultiplier);
        *///?} else {
        return new MerchantOffer(firstCost.copy(), secondCost.copy(), result.copy(), 0, maxUses, xp,
            priceMultiplier);
        //?}
    }

    public static CompoundTag save(MerchantOffers offers) {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (MerchantOffer offer : offers) {
            CompoundTag entry = new CompoundTag();
            putStack(entry, "First", offer.getBaseCostA());
            putStack(entry, "Second", offer.getCostB());
            putStack(entry, "Result", offer.getResult());
            entry.putInt("Uses", offer.getUses());
            entry.putInt("MaxUses", offer.getMaxUses());
            entry.putInt("Xp", offer.getXp());
            entry.putFloat("PriceMultiplier", offer.getPriceMultiplier());
            entries.add(entry);
        }
        root.put("Entries", entries);
        return root;
    }

    public static MerchantOffers load(CompoundTag root) {
        MerchantOffers offers = new MerchantOffers();
        ListTag entries = root.getList("Entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ItemStack first = getStack(entry, "First");
            ItemStack result = getStack(entry, "Result");
            if (first.isEmpty() || result.isEmpty()) continue;
            int maxUses = Math.max(1, entry.getInt("MaxUses"));
            MerchantOffer offer = offer(first, getStack(entry, "Second"), result, maxUses,
                Math.max(0, entry.getInt("Xp")), entry.getFloat("PriceMultiplier"));
            int uses = Math.min(maxUses, Math.max(0, entry.getInt("Uses")));
            for (int use = 0; use < uses; use++) offer.increaseUses();
            offers.add(offer);
        }
        return offers;
    }

    private static void putStack(CompoundTag tag, String key, ItemStack stack) {
        if (stack.isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tag.putString(key + "Id", id.toString());
        tag.putInt(key + "Count", stack.getCount());
    }

    private static ItemStack getStack(CompoundTag tag, String key) {
        if (!tag.contains(key + "Id")) return ItemStack.EMPTY;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(key + "Id"));
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, Math.max(1, tag.getInt(key + "Count")));
    }
}
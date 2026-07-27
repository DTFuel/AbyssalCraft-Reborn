package com.shinoow.abyssalcraft.content.menu.facebook;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.content.item.ritual.BookOfManyFacesItem;
import com.shinoow.abyssalcraft.registry.ModMenus;
import com.shinoow.abyssalcraft.system.data.NecromancyData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Slotless, read-only menu carrying a compact world-necromancy snapshot. */
public final class BookOfManyFacesMenu extends AbstractContainerMenu {

    public static final int MAX_ENTRIES = 20;
    public static final int PAGE_SIZE = 5;
    private static final int MAX_NAME_LENGTH = 128;

    public record FaceEntry(String name, int crystalSize) {
        public FaceEntry {
            name = normalizeName(name);
            crystalSize = Mth.clamp(crystalSize, 0, 2);
        }
    }

    private final Player owner;
    private final InteractionHand hand;
    private final List<FaceEntry> entries;

    public BookOfManyFacesMenu(int windowId, Inventory inventory, InteractionHand hand,
                               List<FaceEntry> entries) {
        super(ModMenus.BOOK_OF_MANY_FACES.get(), windowId);
        owner = inventory.player;
        this.hand = hand;
        this.entries = List.copyOf(entries.subList(0, Math.min(MAX_ENTRIES, entries.size())));
    }

    public BookOfManyFacesMenu(int windowId, Inventory inventory, FriendlyByteBuf data) {
        this(windowId, inventory, readHand(data), readEntries(data));
    }

    public static List<FaceEntry> snapshot(NecromancyData data) {
        return data.getData().stream().limit(MAX_ENTRIES)
            .map(entry -> new FaceEntry(entry.name(),
                entry.tag().getInt("ResurrectionRitualCrystalSize")))
            .toList();
    }

    public static void writeOpenData(FriendlyByteBuf buffer, InteractionHand hand,
                                     List<FaceEntry> entries) {
        buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
        int count = Math.min(MAX_ENTRIES, entries.size());
        buffer.writeVarInt(count);
        for (int index = 0; index < count; index++) {
            FaceEntry entry = entries.get(index);
            buffer.writeUtf(entry.name(), MAX_NAME_LENGTH);
            buffer.writeByte(entry.crystalSize());
        }
    }

    private static InteractionHand readHand(FriendlyByteBuf data) {
        return data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static List<FaceEntry> readEntries(FriendlyByteBuf data) {
        int count = Mth.clamp(data.readVarInt(), 0, MAX_ENTRIES);
        List<FaceEntry> result = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            result.add(new FaceEntry(data.readUtf(MAX_NAME_LENGTH), data.readByte()));
        }
        return List.copyOf(result);
    }

    public List<FaceEntry> entries() {
        return entries;
    }

    public List<FaceEntry> page(int page) {
        int clamped = Mth.clamp(page, 0, pageCount() - 1);
        int from = clamped * PAGE_SIZE;
        return entries.subList(from, Math.min(entries.size(), from + PAGE_SIZE));
    }

    public int pageCount() {
        return Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && player.getItemInHand(hand).getItem() instanceof BookOfManyFacesItem;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) return "?";
        String normalized = name.replace('\n', ' ').replace('\r', ' ');
        return normalized.length() <= MAX_NAME_LENGTH
            ? normalized : normalized.substring(0, MAX_NAME_LENGTH);
    }
}
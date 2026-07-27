package com.shinoow.abyssalcraft.content.menu.facebook;

import com.shinoow.abyssalcraft.system.data.NecromancyData;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public final class BookOfManyFacesSelfTest {

    private BookOfManyFacesSelfTest() {}

    public static void run() {
        NecromancyData data = new NecromancyData();
        for (int index = 0; index < 25; index++) {
            data.storeData("fallen companion " + index, new CompoundTag(), index % 3);
        }
        var entries = BookOfManyFacesMenu.snapshot(data);
        require(entries.size() == 20 && entries.get(0).name().equals("fallen companion 5")
            && entries.get(19).name().equals("fallen companion 24"),
            "Book of Many Faces did not preserve the capped world snapshot order");

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        BookOfManyFacesMenu.writeOpenData(buffer, InteractionHand.OFF_HAND, entries);
        require(!buffer.readBoolean() && buffer.readVarInt() == 20,
            "Book of Many Faces open data lost hand or count");
        for (int index = 0; index < 20; index++) {
            require(buffer.readUtf(128).equals(entries.get(index).name())
                && buffer.readByte() == entries.get(index).crystalSize(),
                "Book of Many Faces open data changed entry " + index);
        }
        require((entries.size() + BookOfManyFacesMenu.PAGE_SIZE - 1) / BookOfManyFacesMenu.PAGE_SIZE == 4,
            "Book of Many Faces page count changed");
        System.out.println("RR_FACEBOOK_SELF_TEST_OK entries=20 pages=4 pageSize=5");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
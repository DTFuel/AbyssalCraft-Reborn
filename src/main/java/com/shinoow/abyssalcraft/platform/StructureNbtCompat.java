package com.shinoow.abyssalcraft.platform;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
//? if >=1.21 {
/*import net.minecraft.nbt.NbtAccounter;
*///?}

/** Version boundary for reading compressed structure-template NBT. */
public final class StructureNbtCompat {

    private StructureNbtCompat() {}

    public static CompoundTag readCompressed(InputStream stream) throws IOException {
        //? if >=1.21 {
        /*return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
        *///?} else {
        return NbtIo.readCompressed(stream);
        //?}
    }
}
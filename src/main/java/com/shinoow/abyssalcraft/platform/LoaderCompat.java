package com.shinoow.abyssalcraft.platform;

/** Loader-neutral access to loader metadata. */
public final class LoaderCompat {

    private LoaderCompat() {}

    public static boolean isModLoaded(String modId) {
        //? if forge {
        return net.minecraftforge.fml.ModList.get().isLoaded(modId);
        //?} else {
        /*return net.neoforged.fml.ModList.get().isLoaded(modId);
        *///?}
    }
}
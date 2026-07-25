package com.shinoow.abyssalcraft.platform;

import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.AbyssalCraft;

/**
 * Compat: ResourceLocation construction (vanilla axis).
 *
 * <p>1.21 made the {@code ResourceLocation} constructors private-for-removal and added factory
 * methods. All mod code must build ResourceLocations through here so a version bump only touches
 * this class.
 */
// The 1.20.1 ResourceLocation(String,...) constructors are deprecated-for-removal; ACRef is the one
// place allowed to call them, so the removal warning is suppressed here intentionally.
@SuppressWarnings("removal")
public final class ACRef {

    private ACRef() {}

    /** ResourceLocation in the mod namespace ({@code abyssalcraft:<path>}). */
    public static ResourceLocation id(String path) {
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(AbyssalCraft.MODID, path);
        *///?} else {
        return new ResourceLocation(AbyssalCraft.MODID, path);
        //?}
    }

    /** ResourceLocation in an explicit namespace. */
    public static ResourceLocation of(String namespace, String path) {
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?} else {
        return new ResourceLocation(namespace, path);
        //?}
    }

    /** Parse a full {@code namespace:path} string. */
    public static ResourceLocation parse(String location) {
        //? if >=1.21 {
        /*return ResourceLocation.parse(location);
        *///?} else {
        return new ResourceLocation(location);
        //?}
    }

    /** ResourceLocation in the {@code minecraft} namespace. */
    public static ResourceLocation vanilla(String path) {
        //? if >=1.21 {
        /*return ResourceLocation.withDefaultNamespace(path);
        *///?} else {
        return new ResourceLocation("minecraft", path);
        //?}
    }
}

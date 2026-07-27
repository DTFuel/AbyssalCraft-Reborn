package com.shinoow.abyssalcraft.integration.api;

import net.minecraft.resources.ResourceLocation;

/** A discoverable third-party extension for AbyssalCraft's stable entity integration API. */
public interface IACPlugin {

    int API_VERSION = 1;

    /** Globally unique plugin id, normally in the provider mod's namespace. */
    ResourceLocation id();

    default int apiVersion() {
        return API_VERSION;
    }

    /** Register extensions during the server-start publication window. */
    void register(ACPluginContext context);
}
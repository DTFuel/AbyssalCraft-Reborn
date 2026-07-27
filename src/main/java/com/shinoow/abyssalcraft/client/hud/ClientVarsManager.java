package com.shinoow.abyssalcraft.client.hud;

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Loads + hot-reloads {@link ClientVars} from {@code assets/abyssalcraft/clientvars.json} (owned by PH-6),
 * faithful to the 1.12.2 {@code ClientVarsReloadListener}. Registered as a client resource-reload listener
 * (loader fork in {@code platform/ClientHooksCompat}); {@link #get()} returns the current values (defaults
 * until first load / on parse failure).
 */
public final class ClientVarsManager implements ResourceManagerReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FILE = ACRef.id("clientvars.json");
    private static final ClientVarsManager INSTANCE = new ClientVarsManager();

    private final AtomicReference<ClientVars> current = new AtomicReference<>(new ClientVars());

    private ClientVarsManager() {}

    public static ClientVarsManager instance() {
        return INSTANCE;
    }

    /** The current client vars (never {@code null}; defaults until loaded). */
    public static ClientVars get() {
        return INSTANCE.current.get();
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        manager.getResource(FILE).ifPresent(resource -> {
            try (BufferedReader reader = resource.openAsReader()) {
                ClientVars parsed = ClientVars.parse(JsonParser.parseReader(reader).getAsJsonObject());
                current.set(parsed);
            } catch (Exception e) {
                LOGGER.error("Failed to load clientvars.json, keeping defaults", e);
            }
        });
    }
}

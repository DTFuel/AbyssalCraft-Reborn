package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;

//? if forge {
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
*///?}

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Compat: client HUD overlay + resource-reload-listener registration (loader axis). <b>Client-only</b>
 * (attached through {@link SideExecutor#runWhenClient}).
 *
 * <p>Forge registers HUD overlays via {@code RegisterGuiOverlaysEvent} + {@code IGuiOverlay}; NeoForge 1.21
 * replaced them with {@code RegisterGuiLayersEvent} + {@code LayeredDraw.Layer} (different render signature).
 * Business HUD code supplies a version-neutral {@link HudRenderer}; both reload events share
 * {@code registerReloadListener(PreparableReloadListener)} so only the event import differs there.
 */
public final class ClientHooksCompat {

    /** A version-neutral HUD overlay: draw onto the in-game gui at the given screen size. */
    @FunctionalInterface
    public interface HudRenderer {
        void render(GuiGraphics graphics, int width, int height);
    }

    private record Overlay(String id, HudRenderer renderer) {}

    private static final List<Overlay> OVERLAYS = new ArrayList<>();
    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new ArrayList<>();

    private ClientHooksCompat() {}

    /** Queue a HUD overlay (client-side, before {@link #attach}). */
    public static void queueOverlay(String id, HudRenderer renderer) {
        OVERLAYS.add(new Overlay(id, renderer));
    }

    /** Queue a client resource-reload listener (client-side, before {@link #attach}). */
    public static void queueReloadListener(PreparableReloadListener listener) {
        RELOAD_LISTENERS.add(listener);
    }

    /** Attach the client-registration listeners to the MOD bus (client side only). */
    public static void attach(IEventBus modBus) {
        //? if forge {
        modBus.addListener((RegisterGuiOverlaysEvent event) -> {
            for (Overlay overlay : OVERLAYS) {
                event.registerAboveAll(overlay.id(), (gui, graphics, partialTick, width, height) -> overlay.renderer().render(graphics, width, height));
            }
        });
        //?} else {
        /*modBus.addListener((RegisterGuiLayersEvent event) -> {
            for (Overlay overlay : OVERLAYS) {
                event.registerAboveAll(ACRef.id(overlay.id()), (graphics, deltaTracker) -> overlay.renderer().render(graphics, graphics.guiWidth(), graphics.guiHeight()));
            }
        });
        *///?}
        modBus.addListener((RegisterClientReloadListenersEvent event) -> {
            for (PreparableReloadListener listener : RELOAD_LISTENERS) {
                event.registerReloadListener(listener);
            }
        });
    }
}

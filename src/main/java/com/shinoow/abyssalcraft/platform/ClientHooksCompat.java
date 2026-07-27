package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;

//? if forge {
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
*///?}

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
    private static final List<Runnable> CLIENT_TICKS = new ArrayList<>();
    private static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

    private ClientHooksCompat() {}

    /** Queue a HUD overlay (client-side, before {@link #attach}). */
    public static void queueOverlay(String id, HudRenderer renderer) {
        OVERLAYS.add(new Overlay(id, renderer));
    }

    /** Queue a client resource-reload listener (client-side, before {@link #attach}). */
    public static void queueReloadListener(PreparableReloadListener listener) {
        RELOAD_LISTENERS.add(listener);
    }

    /** Queue work that runs once after each client game tick. */
    public static void queueClientTick(Runnable tick) {
        CLIENT_TICKS.add(tick);
    }

    /** Queue a client key mapping for the loader's registration event. */
    public static void queueKeyMapping(KeyMapping mapping) {
        if (KEY_MAPPINGS.stream().anyMatch(existing -> existing.getName().equals(mapping.getName()))) {
            throw new IllegalStateException("Duplicate client key mapping " + mapping.getName());
        }
        KEY_MAPPINGS.add(mapping);
    }

    public static int queuedKeyMappingCount() {
        return KEY_MAPPINGS.size();
    }

    /** Draw a full-screen textured HUD layer with a clamped alpha. */
    public static void blitFullscreen(GuiGraphics graphics, ResourceLocation texture,
                                      int width, int height, float alpha) {
        float resolvedAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, resolvedAlpha);
        graphics.blit(texture, 0, 0, 0.0F, 0.0F, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    /** Attach the client-registration listeners to the MOD bus (client side only). */
    public static void attach(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) ->
            KEY_MAPPINGS.forEach(event::register));
        //? if forge {
        EventBuses.game().addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                CLIENT_TICKS.forEach(Runnable::run);
            }
        });
        //?} else {
        /*EventBuses.game().addListener((ClientTickEvent.Post event) ->
            CLIENT_TICKS.forEach(Runnable::run));
        *///?}
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

package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
*///?}

/**
 * Compat: client menu-screen registration + background rendering (loader + vanilla axes).
 *
 * <p><b>Client-only</b> -- never referenced on a dedicated server (the main class attaches it through
 * {@link SideExecutor#runWhenClient}, which defers class loading). Forge registers screens via
 * {@code MenuScreens.register} inside {@code FMLClientSetupEvent}; NeoForge removed that call and uses
 * {@code RegisterMenuScreensEvent}. 1.21 also added mouse/partialTick params to
 * {@code Screen.renderBackground}.
 */
public final class ClientScreenCompat {

    private ClientScreenCompat() {}

    private record Entry<M extends AbstractContainerMenu>(Supplier<MenuType<M>> type, MenuScreens.ScreenConstructor<M, ?> factory) {}

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();

    /** Queue a menu screen for registration. Call (client-side) before {@link #attach}. */
    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void queue(
            Supplier<MenuType<M>> type, MenuScreens.ScreenConstructor<M, S> factory) {
        if (ENTRIES.stream().anyMatch(entry -> entry.type().equals(type))) {
            throw new IllegalStateException("Duplicate menu screen supplier queued");
        }
        ENTRIES.add(new Entry<>(type, factory));
    }

    /** Attach the client-setup listener to the MOD bus (client side only). */
    public static void attach(IEventBus modBus, Runnable afterRegistration) {
        //? if forge {
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            registerAll();
            afterRegistration.run();
        }));
        //?} else {
        /*modBus.addListener((RegisterMenuScreensEvent event) -> {
            registerAll(event);
            afterRegistration.run();
        });
        *///?}
    }

    public static int queuedCount() {
        return ENTRIES.size();
    }

    public static boolean isQueued(Supplier<? extends MenuType<?>> type) {
        return ENTRIES.stream().anyMatch(entry -> entry.type().equals(type));
    }

    //? if forge {
    private static void registerAll() {
        for (Entry<?> entry : ENTRIES) {
            registerOne(entry);
        }
    }

    private static <M extends AbstractContainerMenu> void registerOne(Entry<M> entry) {
        MenuScreens.register(entry.type().get(), entry.factory());
    }
    //?} else {
    /*private static void registerAll(RegisterMenuScreensEvent event) {
        for (Entry<?> entry : ENTRIES) {
            registerOne(event, entry);
        }
    }

    private static <M extends AbstractContainerMenu> void registerOne(RegisterMenuScreensEvent event, Entry<M> entry) {
        event.register(entry.type().get(), entry.factory());
    }
    *///?}

    /** Draw the standard screen background, absorbing the 1.21 {@code renderBackground} signature change. */
    public static void background(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        /*screen.renderBackground(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        screen.renderBackground(graphics);
        //?}
    }
}

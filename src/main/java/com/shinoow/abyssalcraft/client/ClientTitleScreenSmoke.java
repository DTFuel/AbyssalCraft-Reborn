package com.shinoow.abyssalcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

/** Optional automated title-screen smoke used by the release runner. */
public final class ClientTitleScreenSmoke {

    private static final boolean ENABLED = Boolean.getBoolean("abyssalcraft.rrClientSmoke");
    private static int titleScreenTicks;
    private static boolean completed;

    private ClientTitleScreenSmoke() {}

    public static void register() {
        if (ENABLED) {
            com.shinoow.abyssalcraft.platform.ClientHooksCompat.queueClientTick(ClientTitleScreenSmoke::tick);
        }
    }

    private static void tick() {
        if (completed) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof TitleScreen)) {
            titleScreenTicks = 0;
            return;
        }
        if (++titleScreenTicks < 2) return;
        completed = true;
        System.out.println("RR_CLIENT_TITLE_SMOKE_OK screen=title ticks=" + titleScreenTicks);
        minecraft.stop();
    }
}
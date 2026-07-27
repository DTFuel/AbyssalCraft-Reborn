package com.shinoow.abyssalcraft.platform;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
//? if forge {
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
//?} else {
/*import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///?}

/** Client-only loader facade for the Mods-list config entry. */
public final class ConfigScreenCompat {

    private ConfigScreenCompat() {}

    @SuppressWarnings("removal")
    public static void register(Function<Screen, Screen> factory) {
        //? if forge {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((Minecraft minecraft, Screen parent) -> factory.apply(parent)));
        //?} else {
        /*ModLoadingContext.get().getActiveContainer().registerExtensionPoint(IConfigScreenFactory.class,
            (ModContainer container, Screen parent) -> factory.apply(parent));
        *///?}
    }
}
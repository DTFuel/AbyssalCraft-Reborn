package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.world.darklands.DarklandsRegion;
import com.shinoow.abyssalcraft.world.darklands.DarklandsSurfaceRules;

import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
*///?}

public final class DarklandsWorldgenCompat {

    private DarklandsWorldgenCompat() {}

    public static void attach(IEventBus modBus) {
        modBus.addListener(DarklandsWorldgenCompat::setup);
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Regions.register(new DarklandsRegion(ACRef.id("darklands_overworld"),
                ACConfig.darklandsRegionWeight.get()));
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD,
                AbyssalCraft.MODID, DarklandsSurfaceRules.overworld());
        });
    }
}

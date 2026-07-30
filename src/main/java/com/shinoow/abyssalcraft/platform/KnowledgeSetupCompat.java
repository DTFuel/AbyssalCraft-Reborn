package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.config.WorldgenConfigMigration;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
*///?}

/** Resolves registered item offerings after DeferredRegister has completed. */
public final class KnowledgeSetupCompat {

    private KnowledgeSetupCompat() {}

    public static void attach(IEventBus modBus) {
        modBus.addListener(KnowledgeSetupCompat::setup);
        modBus.addListener(KnowledgeSetupCompat::onConfigLoaded);
        modBus.addListener(KnowledgeSetupCompat::onConfigReloaded);
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComplexConfig.reload();
            KnowledgeContent.bootstrapOfferings();
        });
    }

    private static void onConfigLoaded(ModConfigEvent.Loading event) {
        reloadComplexConfig(event);
    }

    private static void onConfigReloaded(ModConfigEvent.Reloading event) {
        reloadComplexConfig(event);
    }

    private static void reloadComplexConfig(ModConfigEvent event) {
        if (ACConfig.isCommonConfig(event.getConfig().getSpec())) {
            if (WorldgenConfigMigration.migrate()) {
                ConfigCompat.save(event.getConfig().getSpec());
            }
            ComplexConfig.reload();
        }
    }
}
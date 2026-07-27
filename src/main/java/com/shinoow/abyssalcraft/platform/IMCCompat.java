package com.shinoow.abyssalcraft.platform;

import java.util.Set;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.integration.api.ACPluginRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

//? if forge {
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
*///?}

/** Cross-loader plugin lifecycle and Forge-only compatibility for five retained legacy IMC keys. */
public final class IMCCompat {

    private static final Set<String> RETAINED_KEYS = Set.of(
        "shoggothFood", "addDreadPlagueImmunity", "addDreadPlagueCarrier",
        "addCoraliumPlagueImmunity", "addCoraliumPlagueCarrier");
    private static final Set<String> RETIRED_KEYS = Set.of(
        "addCrystal", "addCrystallization", "addSingleCrystallization",
        "addOredictCrystallization", "addSingleOredictCrystallization", "addTransmutation",
        "addOredictTransmutation", "addMaterialization", "addGhoulArmor", "addGhoulHelmet",
        "addGhoulChestplate", "addGhoulLeggings", "addGhoulBoots");

    private IMCCompat() {}

    public static void attach(IEventBus modBus) {
        ACPluginRegistry.discover();
        EventBuses.game().addListener((ServerAboutToStartEvent event) -> ACPluginRegistry.publish());
        //? if forge {
        modBus.addListener(IMCCompat::process);
        //?}
    }

    public static int retainedKeyCount() {
        return RETAINED_KEYS.size();
    }

    public static int retiredKeyCount() {
        return RETIRED_KEYS.size();
    }

    //? if forge {
    private static void process(InterModProcessEvent event) {
        event.getIMCStream().forEach(IMCCompat::accept);
    }

    private static void accept(InterModComms.IMCMessage message) {
        String key = message.method();
        if (RETIRED_KEYS.contains(key)) {
            AbyssalCraft.LOGGER.warn("Ignoring retired AbyssalCraft IMC key '{}' from {}: use datapacks or resource packs",
                key, message.senderModId());
            return;
        }
        if (!RETAINED_KEYS.contains(key)) {
            AbyssalCraft.LOGGER.warn("Ignoring unknown AbyssalCraft IMC key '{}' from {}", key, message.senderModId());
            return;
        }
        try {
            ResourceLocation id = entityId(message.messageSupplier().get());
            ACPluginRegistry.registerLegacy(key, id);
        } catch (RuntimeException error) {
            AbyssalCraft.LOGGER.error("Invalid AbyssalCraft IMC '{}' from {}", key, message.senderModId(), error);
        }
    }
    //?}

    private static ResourceLocation entityId(Object payload) {
        if (payload instanceof ResourceLocation id) {
            return id;
        }
        if (payload instanceof EntityType<?> type) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(type);
        }
        if (payload instanceof String id) {
            return ACRef.parse(id);
        }
        throw new IllegalArgumentException("Expected EntityType, ResourceLocation, or namespaced String");
    }
}
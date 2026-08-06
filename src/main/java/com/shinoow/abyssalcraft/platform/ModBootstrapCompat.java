package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.client.ACClientSetup;
import com.shinoow.abyssalcraft.client.ClientItemProperties;
import com.shinoow.abyssalcraft.client.hud.ACHud;
import com.shinoow.abyssalcraft.client.render.ACEntityRenderers;
import com.shinoow.abyssalcraft.client.sky.ACDimensionSkies;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.data.ACDataGenerators;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.registry.ModRegistries;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
*///?}

/** Loader-owned mod entry point and lifecycle wiring. */
@Mod(AbyssalCraft.MODID)
public final class ModBootstrapCompat {

    //? if forge {
    @SuppressWarnings("removal")
    public ModBootstrapCompat() {
        init(FMLJavaModLoadingContext.get().getModEventBus());
    }
    //?} else {
    /*public ModBootstrapCompat(IEventBus modBus) {
        init(modBus);
    }
    *///?}

    private void init(IEventBus modBus) {
        AbyssalCraft.LOGGER.info("AbyssalCraft ({}) starting up", AbyssalCraft.MODID);
        ACConfig.register();
        ModRegistries.ALL.forEach(registrar -> registrar.attach(modBus));
        com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent.bootstrap();
        KnowledgeSetupCompat.attach(modBus);
        PotionBrewingCompat.attach(modBus);
        MachineCapabilityCompat.attach(modBus);
        MenuHostCapabilityCompat.attach(modBus);
        ItemTransferAttachmentCompat.attach(modBus);
        LiquidCoraliumCompat.attach(modBus);
        LiquidAntimatterCompat.attach(modBus);
        DarklandsWorldgenCompat.attach(modBus);
        DimensionLoadingCompat.attach();
        EntityAttributeCompat.attach(modBus);
        SpawnPlacementCompat.attach(modBus);
        ACNetwork.bootstrap(modBus);
        PlayerDataCompat.bootstrap(modBus);
        EnchantmentCompat.bootstrap(modBus);
        GameHooksCompat.attach();
        IMCCompat.attach(modBus);
        SpawnCandidateCompat.attach();
        EntityCatalogValidationCompat.attach();
        WorldgenServerValidationCompat.attach();
        RitualTaskCompat.attach();
        com.shinoow.abyssalcraft.system.energy.disruption.Disruptions.bootstrap();
        com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures.bootstrap();
        com.shinoow.abyssalcraft.content.block.ritual.Rituals.bootstrap();
        com.shinoow.abyssalcraft.system.spell.Spells.bootstrap();
        CommandCompat.attach();
        DataGenCompat.register(modBus, ACDataGenerators::gather);
        SideExecutor.runWhenClient(() -> () -> {
            ACClientSetup.registerScreens();
            ConfigScreenCompat.register(ACClientSetup::createConfigScreen);
            ClientItemPropertiesCompat.attach(modBus, ClientItemProperties::register);
            ClientScreenCompat.attach(modBus, ACClientSetup::validateR2GateScreens);
            ACClientSetup.registerItemColors();
            ClientColorCompat.attach(modBus);
            ArmorClientCompat.attach(modBus);
            EntityRendererCompat.attach(modBus, ACEntityRenderers::registerRenderers,
                ACEntityRenderers::registerLayers, ACEntityRenderers::registerPlayerLayers);
            DimensionEffectsCompat.attach(modBus, ACDimensionSkies::register);
            ParticleCompat.attach(modBus, ACClientSetup::registerParticles);
            ACHud.register();
            ACClientSetup.registerClientTicks();
            LineRenderCompat.attach(modBus);
            ClientHooksCompat.attach(modBus);
        });
    }
}
package com.shinoow.abyssalcraft;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import com.shinoow.abyssalcraft.client.ACClientSetup;
import com.shinoow.abyssalcraft.client.render.ACEntityRenderers;
import com.shinoow.abyssalcraft.client.sky.ACDimensionSkies;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.data.ACDataGenerators;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientColorCompat;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.platform.DarklandsWorldgenCompat;
import com.shinoow.abyssalcraft.platform.DimensionEffectsCompat;
import com.shinoow.abyssalcraft.platform.EnchantmentCompat;
import com.shinoow.abyssalcraft.platform.EntityAttributeCompat;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;
import com.shinoow.abyssalcraft.platform.ParticleCompat;
import com.shinoow.abyssalcraft.platform.MachineCapabilityCompat;
import com.shinoow.abyssalcraft.platform.MenuHostCapabilityCompat;
import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;
import com.shinoow.abyssalcraft.platform.KnowledgeSetupCompat;
import com.shinoow.abyssalcraft.platform.PlayerDataCompat;
import com.shinoow.abyssalcraft.platform.PotionBrewingCompat;
import com.shinoow.abyssalcraft.platform.SideExecutor;
import com.shinoow.abyssalcraft.platform.SpawnPlacementCompat;
import com.shinoow.abyssalcraft.registry.ModRegistries;
//? if forge {
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
*///?}

@Mod(AbyssalCraft.MODID)
public final class AbyssalCraft {

    public static final String MODID = "abyssalcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    //? if forge {
    // FMLJavaModLoadingContext.get() is deprecated-for-removal on 47.x but fully functional on 1.20.1.
    @SuppressWarnings("removal")
    public AbyssalCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        init(modBus);
    }
    //?} else {
    /*public AbyssalCraft(IEventBus modBus) {
        init(modBus);
    }
    *///?}

    // Shared bootstrap: attach DeferredRegisters and lifecycle listeners to the MOD bus here.
    private void init(IEventBus modBus) {
        LOGGER.info("AbyssalCraft ({}) starting up", MODID);
        // Register the mod config (COMMON spec) with the active mod container (via ModLoadingContext).
        ACConfig.register();
        // Attach every mod registrar to the MOD bus. The bus reference stays in this @Mod class (the
        // one allowed forked-API site), so registry/ business code carries no loader //? forks.
        ModRegistries.ALL.forEach(registrar -> registrar.attach(modBus));
        com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent.bootstrap();
        KnowledgeSetupCompat.attach(modBus);
        PotionBrewingCompat.attach(modBus);
        // Sided item automation for the Crystallizer/Transmutator. Forge exposes wrappers from the
        // block entity; NeoForge registers block capability providers on the MOD bus.
        MachineCapabilityCompat.attach(modBus);
        MenuHostCapabilityCompat.attach(modBus);
        ItemTransferAttachmentCompat.attach(modBus);
        LiquidCoraliumCompat.attach(modBus);
        LiquidAntimatterCompat.attach(modBus);
        DarklandsWorldgenCompat.attach(modBus);
        // Entity attributes: mod-bus EntityAttributeCreationEvent (loader fork lives in EntityAttributeCompat).
        // Suppliers are collected by ModEntities' static init, forced loaded via ModRegistries.ALL above.
        EntityAttributeCompat.attach(modBus);
        // Natural-spawn placement rules for the D2a mob families: mod-bus SpawnPlacementRegisterEvent
        // (loader fork lives in SpawnPlacementCompat). Rules are collected by family registrar static init.
        SpawnPlacementCompat.attach(modBus);
        // Network layer (PS-1): wire the multiplexed channel + register all 23 mod messages to the MOD
        // bus (loader fork lives in NetworkChannel). Server + client safe; must run before any send.
        ACNetwork.bootstrap(modBus);
        // Per-player necrodata capability (PS-2): attach a persistent tag to every player (loader fork
        // lives in PlayerDataCompat: Forge capability / NeoForge data attachment). Server + client safe.
        PlayerDataCompat.bootstrap(modBus);
        // Enchantments (PS-3): register the 5 AC enchantments (loader fork lives in EnchantmentCompat:
        // Forge registers Enchantment instances; NeoForge is datapack-driven so this is a no-op there).
        EnchantmentCompat.bootstrap(modBus);
        // Knowledge event hooks (PS-11): subscribe the game-bus listeners that record knowledge triggers
        // (entity kills, dimension changes) into the necrodata (PS-2). Loader fork lives in GameHooksCompat.
        com.shinoow.abyssalcraft.platform.GameHooksCompat.attach();
        com.shinoow.abyssalcraft.platform.SpawnCandidateCompat.attach();
        com.shinoow.abyssalcraft.platform.EntityCatalogValidationCompat.attach();
        // Disruptions (PS-9): register the concrete disruptions into the DisruptionHandler singleton (the
        // bad things a PE manipulator triggers without a Place of Power). Fork-free; server + client safe.
        com.shinoow.abyssalcraft.system.energy.disruption.Disruptions.bootstrap();
        com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures.bootstrap();
        // Rituals (PS-6 content): seed the RitualRegistry with the concrete rituals the altar can perform.
        // Fork-free; runs before any ritual is attempted. Server + client safe.
        com.shinoow.abyssalcraft.content.block.ritual.Rituals.bootstrap();
        // Spells (PS-7 content): register the concrete spells into the SpellRegistry (the pilot life-drain
        // spell the staff casts). Fork-free; holds no item reference, so safe to run at init.
        com.shinoow.abyssalcraft.content.item.staff.StaffSpells.bootstrap();
        // Commands (PJ-3 / Stage J): register /acunlockallknowledge on the game bus (RegisterCommandsEvent
        // fork lives in CommandCompat; the command builder is fork-free in system/command/ACCommands).
        com.shinoow.abyssalcraft.platform.CommandCompat.attach();
        // Datagen entry point (fires only during runData; the GatherDataEvent fork lives in DataGenCompat).
        DataGenCompat.register(modBus, ACDataGenerators::gather);
        // Client-only: queue machine screens + attach the client-setup listener. SideExecutor defers the
        // supplier so ACClientSetup/ClientScreenCompat (client classes) never load on a dedicated server.
        SideExecutor.runWhenClient(() -> () -> {
            ACClientSetup.registerScreens();
            ClientScreenCompat.attach(modBus, ACClientSetup::validateR2GateScreens);
            ACClientSetup.registerItemColors();
            ClientColorCompat.attach(modBus);
            com.shinoow.abyssalcraft.platform.ArmorClientCompat.attach(modBus);
            // Entity renderers + model layers (Stage E1 / PE-1): register a placeholder renderer for
            // every AC entity so runClient passes renderer validation (loader fork in EntityRendererCompat).
            EntityRendererCompat.attach(modBus, ACEntityRenderers::registerRenderers,
                ACEntityRenderers::registerLayers, ACEntityRenderers::registerPlayerLayers);
            // Dimension render effects (Stage H1 / PH-2): faithful per-dimension fog for AW/Dreadlands/
            // Dark Realm/Omothol (loader fork in DimensionEffectsCompat; custom skybox render deferred).
            DimensionEffectsCompat.attach(modBus, ACDimensionSkies::register);
            // Particle providers (Stage H1 / PH-3): bind the abyssal_fx sprite provider (loader fork in
            // ParticleCompat; coloured/item particles deferred to the ritual/PE content that emits them).
            ParticleCompat.attach(modBus, ACClientSetup::registerParticles);
            // HUD overlays + clientvars reload listener (Stage H2 / PH-6): register the AC HUD overlays and the
            // hot-reloadable clientvars loader (loader fork in ClientHooksCompat: GuiOverlay vs GuiLayer).
            com.shinoow.abyssalcraft.client.hud.ACHud.register();
            com.shinoow.abyssalcraft.platform.ClientHooksCompat.attach(modBus);
        });
    }
}

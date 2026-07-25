package com.shinoow.abyssalcraft.client;

import com.shinoow.abyssalcraft.client.particle.ACFadeParticle;
import com.shinoow.abyssalcraft.client.screen.machine.brewing.BrewingStandScreen;
import com.shinoow.abyssalcraft.client.screen.machine.crystallizer.CrystallizerScreen;
import com.shinoow.abyssalcraft.client.screen.machine.materializer.MaterializerScreen;
import com.shinoow.abyssalcraft.client.screen.machine.researchtable.ResearchTableScreen;
import com.shinoow.abyssalcraft.client.screen.machine.transmutator.TransmutatorScreen;
import com.shinoow.abyssalcraft.client.screen.item.CrystalBagScreen;
import com.shinoow.abyssalcraft.client.screen.item.SpiritTabletScreen;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.portal.PortalAnchorBlockEntity;
import com.shinoow.abyssalcraft.content.block.portal.PortalBlocks;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStands;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.platform.ClientColorCompat;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.ParticleCompat;
import com.shinoow.abyssalcraft.registry.ModParticles;
import com.shinoow.abyssalcraft.registry.ModMenus;

/**
 * Client-setup relay (skeleton).
 *
 * <p>Created early by the pilot (PP-3) so the Materializer has a screen; Stage E1 (PE-1) extends this
 * to also register entity renderers / model layers. Carries no loader fork -- it only queues screens
 * into {@link ClientScreenCompat}; the MOD-bus attach (which needs {@code IEventBus}) is threaded from
 * the {@code @Mod} main class, client-side only.
 */
public final class ACClientSetup {

    private ACClientSetup() {}

    /** Queue every machine screen with the compat registrar (called client-side during mod init). */
    public static void registerScreens() {
        ClientScreenCompat.queue(Materializers.MATERIALIZER_MENU, MaterializerScreen::new);
        ClientScreenCompat.queue(Crystallizers.CRYSTALLIZER_MENU, CrystallizerScreen::new);
        ClientScreenCompat.queue(Transmutators.TRANSMUTATOR_MENU, TransmutatorScreen::new);
        ClientScreenCompat.queue(ResearchTables.RESEARCH_TABLE_MENU, ResearchTableScreen::new);
        ClientScreenCompat.queue(BrewingStands.BREWING_STAND_MENU, BrewingStandScreen::new);
        ClientScreenCompat.queue(ModMenus.CRYSTAL_BAG, CrystalBagScreen::new);
        ClientScreenCompat.queue(TransferContent.SPIRIT_TABLET_MENU, SpiritTabletScreen::new);
    }

    /** Called by the real loader registration callback after every queued screen is installed. */
    public static void validateR2GateScreens() {
        if (ClientScreenCompat.queuedCount() != 7
            || !ClientScreenCompat.isQueued(ResearchTables.RESEARCH_TABLE_MENU)
            || !ClientScreenCompat.isQueued(BrewingStands.BREWING_STAND_MENU)
            || !ClientScreenCompat.isQueued(TransferContent.SPIRIT_TABLET_MENU)) {
            throw new IllegalStateException("R2 menu screen relay is incomplete or duplicated");
        }
        com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.info("R2_GATE_CLIENT_SCREENS_OK screens=7 r2Screens=3");
    }

    /** Queue per-element crystal tint colours (PB-1) with the colour compat (called client-side). */
    public static void registerItemColors() {
        for (int i = 0; i < MaterialItems.CRYSTAL_ELEMENTS.length; i++) {
            ClientColorCompat.queue(MaterialItems.CRYSTAL_COLORS[i],
                MaterialItems.CRYSTALS.get(i), MaterialItems.CRYSTAL_SHARDS.get(i), MaterialItems.CRYSTAL_FRAGMENTS.get(i),
                CrystalClusterBlocks.CLUSTERS.get(i));
            ClientColorCompat.queueBlocks(MaterialItems.CRYSTAL_COLORS[i], CrystalClusterBlocks.CLUSTERS.get(i));
        }
        ClientColorCompat.queue(0x910000, DecoBlocks.DREADLANDS_GRASS);
        ClientColorCompat.queueBlocks(0x910000, DecoBlocks.DREADLANDS_GRASS);
        ClientColorCompat.queueDynamicBlocks((state, level, pos, tintIndex) -> {
            if (tintIndex != 1 || level == null || pos == null) return 0xFFFFFF;
            return level.getBlockEntity(pos) instanceof PortalAnchorBlockEntity anchor
                ? anchor.getColor() & 0xFFFFFF : 0xFFFFFF;
        }, PortalBlocks.PORTAL_ANCHOR, PortalBlocks.UNCHAINED_PORTAL_ANCHOR);
    }

    /** Register the AC particle client providers (PH-3) with the particle compat (called client-side). */
    public static void registerParticles(ParticleCompat.Providers providers) {
        providers.registerSpriteSet(ModParticles.ABYSSAL_FX.get(), ACFadeParticle.Provider::new);
    }
}

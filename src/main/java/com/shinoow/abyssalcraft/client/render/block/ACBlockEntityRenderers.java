package com.shinoow.abyssalcraft.client.render.block;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.platform.EntityRendererCompat;

/**
 * BlockEntity-renderer dispatch (owned by PE-6, Stage E2). <b>Client-only</b> relay.
 *
 * <p>The single point where AbyssalCraft {@code BlockEntityRenderer}s are registered, through the
 * fork-free {@link EntityRendererCompat.Renderers#registerBlockEntity} sink (BE renderers share the same
 * {@code RegisterRenderers} event as entity renderers). Called from {@code ACEntityRenderers}.
 *
 * <p>The complete modern BER set is Research Table (legacy feather) plus Ritual, Rending and Energy
 * Pedestals (displayed inventory). Other registered block entities use static blockstate models,
 * screens, or server-side state only. Legacy severed-head TESRs are replaced by facing blockstate
 * models, and legacy Jzahar spawners are replaced by the vanilla spawner host; neither owns a modern
 * AbyssalCraft BlockEntityType.
 */
public final class ACBlockEntityRenderers {

    private ACBlockEntityRenderers() {}

    /** Register faithful BlockEntity renderers for existing modern hosts. */
    public static void register(EntityRendererCompat.Renderers renderers) {
        renderers.registerBlockEntity(ResearchTables.RESEARCH_TABLE_BE.get(), ResearchTableRenderer::new);
        renderers.registerBlockEntity(RitualBlocks.RITUAL_ALTAR_BE.get(), RitualAltarRenderer::new);
        renderers.registerBlockEntity(RitualBlocks.RITUAL_PEDESTAL_BE.get(), RitualPedestalRenderer::new);
        renderers.registerBlockEntity(RendingPedestals.RENDING_PEDESTAL_BE.get(), RendingPedestalRenderer::new);
        renderers.registerBlockEntity(EnergyBlocks.ENERGY_PEDESTAL_BE.get(), EnergyPedestalRenderer::new);
    }
}

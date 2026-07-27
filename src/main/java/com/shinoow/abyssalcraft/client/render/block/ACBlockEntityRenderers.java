package com.shinoow.abyssalcraft.client.render.block;

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
 * <p>Faithful BERs are registered when their modern BlockEntityType exists:
 * <ul>
 *   <li>the ritual / energy / sacrificial / rending <b>altars &amp; pedestals</b> (1.12.2
 *       {@code TileEntitySingletonInventoryBlockRenderer} -- a floating stored item) &rarr; Stage S
 *       (ritual / PE-energy / PoP / rending);</li>
 *   <li>the four severed <b>heads</b> (1.12.2 {@code TileEntityDirectionalRenderer} -- a directional head
 *       model) and the <b>Jzahar spawner</b> &rarr; later stages.</li>
 * </ul>
 * Research Table renders its legacy feather and Ritual Pedestal renders its stored offering. Remaining
 * TESRs stay deferred until their owning content provides a real BlockEntityType.
 */
public final class ACBlockEntityRenderers {

    private ACBlockEntityRenderers() {}

    /** Register faithful BlockEntity renderers for existing modern hosts. */
    public static void register(EntityRendererCompat.Renderers renderers) {
        renderers.registerBlockEntity(ResearchTables.RESEARCH_TABLE_BE.get(), ResearchTableRenderer::new);
        renderers.registerBlockEntity(RitualBlocks.RITUAL_PEDESTAL_BE.get(), RitualPedestalRenderer::new);
        renderers.registerBlockEntity(RendingPedestals.RENDING_PEDESTAL_BE.get(), RendingPedestalRenderer::new);
    }
}

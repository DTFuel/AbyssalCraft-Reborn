package com.shinoow.abyssalcraft.data.gen;

import java.util.Set;
import java.util.TreeSet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.shinoow.abyssalcraft.AbyssalCraft;

/** Headless-safe closure audit for modern BlockEntityRenderer hosts. */
public final class BlockEntityRendererHostAudit {

    private static final Set<String> BER_REQUIRED = Set.of(
        "energy_pedestal",
        "research_table",
        "rending_pedestal",
        "ritual_pedestal",
        "sacrificial_altar");

    private static final Set<String> NO_BER_REQUIRED = Set.of(
        "crate",
        "crystallizer",
        "deity_statue",
        "directional",
        "energy_collector",
        "energy_container",
        "energy_depositioner",
        "energy_relay",
        "idol_of_fading",
        "inventory",
        "machine",
        "materializer",
        "multi_block",
        "portal_anchor",
        "ritual_altar",
        "sealing_lock",
        "sequential_brewing_stand",
        "shoggoth_biomass",
        "spirit_altar",
        "state_transformer",
        "tombstone",
        "transmutator");

    private BlockEntityRendererHostAudit() {}

    @SuppressWarnings("deprecation")
    public static void validate() {
        Set<String> actual = new TreeSet<>();
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
            if (id != null && AbyssalCraft.MODID.equals(id.getNamespace())) actual.add(id.getPath());
        }
        Set<String> classified = new TreeSet<>(BER_REQUIRED);
        classified.addAll(NO_BER_REQUIRED);
        if (!actual.equals(classified)) {
            Set<String> missing = new TreeSet<>(actual);
            missing.removeAll(classified);
            Set<String> stale = new TreeSet<>(classified);
            stale.removeAll(actual);
            throw new IllegalStateException("BER host classification changed: unclassified=" + missing
                + ", stale=" + stale + ", actual=" + actual.size());
        }
        AbyssalCraft.LOGGER.info(
            "RR_BER_HOST_CLOSURE_OK registered={} noBer={} total={} replacedLegacy={} deferredLegacy={}",
            BER_REQUIRED.size(), NO_BER_REQUIRED.size(), actual.size(), 5, 0);
    }
}
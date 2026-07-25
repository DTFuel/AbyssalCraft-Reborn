package com.shinoow.abyssalcraft.system.ritual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Registry of Necronomicon rituals (owned by PS-6), faithful to the 1.12.2 {@code api.ritual.RitualRegistry}.
 * The altar block entity (deferred content) scans its surrounding pedestals, then asks this registry for the
 * ritual matching the offerings, book tier + dimension.
 */
public final class RitualRegistry {

    private static final RitualRegistry INSTANCE = new RitualRegistry();

    private final Map<String, Ritual> rituals = new LinkedHashMap<>();

    private RitualRegistry() {}

    public static RitualRegistry instance() {
        return INSTANCE;
    }

    public boolean register(Ritual ritual) {
        return rituals.putIfAbsent(ritual.name(), ritual) == null;
    }

    public List<Ritual> getRituals() {
        return Collections.unmodifiableList(new ArrayList<>(rituals.values()));
    }

    public Ritual getRitualById(String id) {
        return rituals.get(id);
    }

    /**
     * The first registered ritual whose offerings match {@code provided} and whose book-tier / dimension gate
     * is satisfied, or {@code null}. {@code dimension} is the current dimension (matched against a ritual's
     * nullable dimension key); {@code bookTier} is the Necronomicon tier the player holds.
     */
    public Ritual find(List<ItemStack> provided, ItemStack center, int bookTier, ResourceKey<Level> dimension) {
        for (Ritual ritual : rituals.values()) {
            if (bookTier >= ritual.bookType()
                    && (ritual.dimension() == null || ritual.dimension().equals(dimension))
                    && ritual.matchesCenter(center)
                    && ritual.matches(provided)) {
                return ritual;
            }
        }
        return null;
    }
}

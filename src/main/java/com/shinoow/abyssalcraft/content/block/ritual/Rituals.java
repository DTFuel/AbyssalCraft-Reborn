package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.system.ritual.ManifestRitual;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;
import com.shinoow.abyssalcraft.system.ritual.RitualRegistry;

/**
 * Mounts the source-derived 62-entry manifest into the runtime ritual registry in legacy order.
 */
public final class Rituals {

    private Rituals() {}

    public static void bootstrap() {
        RitualRegistry registry = RitualRegistry.instance();
        for (RitualManifest manifest : RitualManifestCatalog.entries()) {
            if (!registry.register(new ManifestRitual(manifest))
                && registry.getRitualById(manifest.id()) == null) {
                throw new IllegalStateException("Failed to register ritual " + manifest.id());
            }
        }
    }
}

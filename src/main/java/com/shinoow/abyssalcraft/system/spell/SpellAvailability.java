package com.shinoow.abyssalcraft.system.spell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.config.ACConfig;

/** Live server policy for the fourteen configurable spells. */
public final class SpellAvailability {

    private static final Map<String, Supplier<Boolean>> ENABLED = enabledById();

    private SpellAvailability() {}

    public static boolean isEnabled(Spell spell) {
        return spell != null && isEnabled(spell.id());
    }

    public static boolean isEnabled(SpellManifest manifest) {
        return manifest != null && isEnabled(manifest.id());
    }

    public static boolean isEnabled(String idOrAlias) {
        SpellManifest manifest = SpellManifestCatalog.get(idOrAlias);
        if (manifest == null) return true;
        Supplier<Boolean> configured = ENABLED.get(manifest.id());
        return configured != null && configured.get();
    }

    static Map<String, Supplier<Boolean>> enabledById() {
        Map<String, Supplier<Boolean>> enabled = new LinkedHashMap<>();
        enabled.put("entropy", () -> ACConfig.entropy_spell.get());
        enabled.put("lifedrain", () -> ACConfig.life_drain_spell.get());
        enabled.put("mining", () -> ACConfig.mining_spell.get());
        enabled.put("graspofcthulhu", () -> ACConfig.grasp_of_cthulhu_spell.get());
        enabled.put("invisibility", () -> ACConfig.invisibility_spell.get());
        enabled.put("detachment", () -> ACConfig.detachment_spell.get());
        enabled.put("stealvigor", () -> ACConfig.steal_vigor_spell.get());
        enabled.put("sirenssong", () -> ACConfig.sirens_song_spell.get());
        enabled.put("undeathtodust", () -> ACConfig.undeath_to_dust_spell.get());
        enabled.put("oozeremoval", () -> ACConfig.ooze_removal_spell.get());
        enabled.put("teleporthostiles", () -> ACConfig.teleport_hostile_spell.get());
        enabled.put("floating", () -> ACConfig.floating_spell.get());
        enabled.put("teleportHome", () -> ACConfig.teleport_home_spell.get());
        enabled.put("compass", () -> ACConfig.compass_spell.get());
        return Map.copyOf(enabled);
    }
}
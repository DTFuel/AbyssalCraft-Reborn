package com.shinoow.abyssalcraft.system.spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.ItemStack;

/**
 * Registry of Necronomicon spells (owned by PS-7), faithful to the 1.12.2 {@code api.spell.SpellRegistry}.
 * Content registers its spells here; the Necronomicon casting glue (deferred) asks this registry for the
 * spell matching the held book tier, scroll quality and inscription reagents.
 */
public final class SpellRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SpellRegistry INSTANCE = new SpellRegistry();

    private final List<Spell> spells = new ArrayList<>();
    private final Map<String, String> aliases = new HashMap<>();

    private SpellRegistry() {}

    public static SpellRegistry instance() {
        return INSTANCE;
    }

    /** Register a spell (book type must be 0..4; ids must be unique). */
    public void registerSpell(Spell spell) {
        if (spell.bookType() < 0 || spell.bookType() > 4) {
            LOGGER.error("Necronomicon book type does not exist: {}", spell.bookType());
            return;
        }
        for (Spell entry : spells) {
            if (entry.id().equals(spell.id())) {
                LOGGER.error("Necronomicon Spell already registered: {}", spell.id());
                return;
            }
        }
        spells.add(spell);
    }

    /** Register a legacy or transitional id that resolves to a canonical spell id. */
    public void registerAlias(String alias, String canonicalId) {
        if (alias.equals(canonicalId) || getSpellExact(canonicalId) == null) {
            throw new IllegalArgumentException("Invalid spell alias " + alias + " -> " + canonicalId);
        }
        aliases.put(alias, canonicalId);
    }

    public List<Spell> getSpells() {
        return Collections.unmodifiableList(spells);
    }

    public Spell getSpell(String id) {
        String canonicalId = aliases.getOrDefault(id, id);
        return getSpellExact(canonicalId);
    }

    private Spell getSpellExact(String id) {
        for (Spell spell : spells) {
            if (spell.id().equals(id)) {
                return spell;
            }
        }
        return null;
    }

    /**
     * The first registered spell whose reagents match {@code reagents} and whose book-tier / scroll-quality
     * gate is satisfied, or {@code null}. {@code bookTier} is the Necronomicon tier the player holds;
     * {@code held} is the quality of the scroll being used.
     */
    public Spell find(int bookTier, ScrollType held, List<ItemStack> reagents) {
        for (Spell spell : spells) {
            if (spell.bookType() <= bookTier
                    && held.quality() >= spell.scrollType().quality()
                    && spell.matches(reagents)) {
                return spell;
            }
        }
        return null;
    }
}

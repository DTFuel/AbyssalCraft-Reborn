package com.shinoow.abyssalcraft.system.spell;

/** One server-authoritative spell effect. */
public interface SpellBehavior {

    boolean canCast(ManifestSpell spell, SpellCastContext context);

    void cast(ManifestSpell spell, SpellCastContext context);
}
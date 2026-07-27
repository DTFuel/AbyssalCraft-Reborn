package com.shinoow.abyssalcraft.system.spell;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Runtime spell backed directly by one immutable {@link SpellManifest}. */
public final class ManifestSpell extends Spell {

    private final SpellManifest manifest;

    public ManifestSpell(SpellManifest manifest) {
        super(manifest.id(), manifest.bookType(), manifest.requiredEnergy(), manifest.reagentLayout());
        this.manifest = manifest;
        setColor(manifest.color());
        setScrollType(manifest.scrollType());
        if (manifest.requiresCharging()) setRequiresCharging();
        if (manifest.canOthersCast()) setCanBeCastByOthers(true);
        if (manifest.glyph() != null) setGlyph(manifest.glyph());
        if (manifest.research() != null) setResearch(manifest.research());
    }

    public SpellManifest manifest() {
        return manifest;
    }

    public boolean canCast(SpellCastContext context) {
        SpellBehavior behavior = SpellBehaviorRegistry.instance().get(id());
        return behavior != null && context.quality().quality() >= scrollType().quality()
            && behavior.canCast(this, context);
    }

    public void cast(SpellCastContext context) {
        SpellBehavior behavior = SpellBehaviorRegistry.instance().get(id());
        if (behavior == null) throw new IllegalStateException("Missing spell behavior: " + id());
        behavior.cast(this, context);
    }

    @Override
    public boolean canCastSpell(Level level, BlockPos pos, Player player, ScrollType scrollType) {
        return false;
    }

    @Override
    protected void castSpellServer(Level level, BlockPos pos, Player player, ScrollType scrollType) {}

    @Override
    protected void castSpellClient(Level level, BlockPos pos, Player player, ScrollType scrollType) {}
}
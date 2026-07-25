package com.shinoow.abyssalcraft.system.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base for spells aimed at a single living target (owned by PS-7), faithful to the 1.12.2
 * {@code api.spell.EntityTargetSpell}. Subtypes implement {@link #canCastSpellOnTarget} and
 * {@link #castSpellOnTarget}; the effect is applied server-side to the resolved target.
 *
 * <p><b>Deferred wiring:</b> in 1.12.2 the target is ray-traced client-side (within {@link #getRange()})
 * and the hit entity id is sent to the server to run {@link #castSpellOnTarget}. Here the client ray-trace
 * (an internal-method-handler concern) and the server round-trip (PS-1 {@code MobSpellMessage}, handler
 * currently a stub) are deferred content; the effect entry point {@link #castSpellOnTarget} is delivered so
 * concrete spells (e.g. {@link LifeDrainSpell}) compile and can be driven once that glue lands.
 */
public abstract class EntityTargetSpell extends Spell {

    private float range = 15;

    protected EntityTargetSpell(String id, int bookType, float requiredEnergy, ItemStack... reagents) {
        super(id, bookType, requiredEnergy, reagents);
    }

    protected void setRange(float range) {
        this.range = range;
    }

    public float getRange() {
        return range;
    }

    /** Checks if the spell may affect the given target. */
    protected abstract boolean canCastSpellOnTarget(LivingEntity target, ScrollType scrollType);

    /** Applies the spell effect to the resolved target (server-side). */
    public abstract void castSpellOnTarget(Level level, BlockPos pos, Player player, ScrollType scrollType, LivingEntity target);

    @Override
    public boolean canCastSpell(Level level, BlockPos pos, Player player, ScrollType scrollType) {
        // Targeting is resolved client-side (deferred: ray-trace via internal method handler within getRange()).
        return false;
    }

    @Override
    protected void castSpellClient(Level level, BlockPos pos, Player player, ScrollType scrollType) {
        // Deferred: ray-trace a target within getRange(), then send PS-1 MobSpellMessage to the server.
    }

    @Override
    protected void castSpellServer(Level level, BlockPos pos, Player player, ScrollType scrollType) {
        // Deferred: the server MobSpellMessage handler resolves the target id and calls castSpellOnTarget(...).
    }
}

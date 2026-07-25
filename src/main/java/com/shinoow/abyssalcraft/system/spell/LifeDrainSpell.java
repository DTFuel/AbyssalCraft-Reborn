package com.shinoow.abyssalcraft.system.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A life-drain spell (pilot, owned by PS-7): damages the target and heals the caster for the same amount.
 * Faithful to the 1.12.2 {@code LIFE_DRAIN} spell, a representative {@link EntityTargetSpell}. Fork-free
 * effect (vanilla {@code hurt}/{@code heal}/{@code damageSources().magic()}).
 */
public final class LifeDrainSpell extends EntityTargetSpell {

    public LifeDrainSpell(String id, int bookType, float requiredEnergy, ItemStack... reagents) {
        super(id, bookType, requiredEnergy, reagents);
    }

    public float amount(ScrollType scrollType) {
        return 5F + 2.5F * scrollType.quality();
    }

    @Override
    protected boolean canCastSpellOnTarget(LivingEntity target, ScrollType scrollType) {
        return target.isAlive();
    }

    @Override
    public void castSpellOnTarget(Level level, BlockPos pos, Player player, ScrollType scrollType, LivingEntity target) {
        if (level.isClientSide) {
            return;
        }
        float amount = amount(scrollType);
        target.hurt(level.damageSources().magic(), amount);
        player.heal(amount);
    }
}

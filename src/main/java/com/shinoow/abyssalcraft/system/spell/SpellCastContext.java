package com.shinoow.abyssalcraft.system.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/** Fully server-resolved cast request; clients never choose cost, quality or effect parameters. */
public record SpellCastContext(
    ServerLevel level,
    ServerPlayer caster,
    ItemStack source,
    ItemStack energySource,
    ScrollType quality,
    LivingEntity entityTarget,
    BlockHitResult blockTarget
) {

    public BlockPos origin() {
        return caster.blockPosition();
    }
}
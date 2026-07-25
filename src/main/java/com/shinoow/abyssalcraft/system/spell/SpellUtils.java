package com.shinoow.abyssalcraft.system.spell;

import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The spell casting glue (owned by PS-7), faithful to the 1.12.2 {@code api.spell.SpellUtils}: it draws the
 * spell's Potential Energy from a held energy item ({@link IEnergyContainerItem}, delivered CR-58) and
 * applies the effect. This is the piece the ItemStack-NBT fork (CR-58) unblocked.
 *
 * <p>Simplification vs 1.12.2: the target is ray-traced <b>server-side</b> from the caster's look vector
 * rather than client-side + a {@code MobSpellMessage} round-trip -- functionally equivalent and avoids the
 * (still-stub) network glue. Fork-free (vanilla {@code ProjectileUtil}/{@code Vec3}/{@code AABB}).
 */
public final class SpellUtils {

    private SpellUtils() {}

    /**
     * Cast an entity-target spell (server-side): ray-trace a living target within the spell's range, verify
     * the caster's {@code energyItem} holds enough PE, drain it, and apply the effect. Returns whether the
     * spell was cast.
     */
    public static boolean castOnTarget(Level level, Player player, EntityTargetSpell spell, ItemStack energyItem) {
        if (level.isClientSide || !(energyItem.getItem() instanceof IEnergyContainerItem energy)) {
            return false;
        }
        if (energy.getContainedEnergy(energyItem) < spell.requiredEnergy()) {
            return false;
        }
        LivingEntity target = rayTraceTarget(player, spell.getRange());
        if (target == null || !spell.canCastSpellOnTarget(target, spell.scrollType())) {
            return false;
        }
        energy.consumeEnergy(energyItem, spell.requiredEnergy());
        spell.castSpellOnTarget(level, target.blockPosition(), player, spell.scrollType(), target);
        return true;
    }

    /** The living entity the player is looking at within {@code range}, or {@code null}. */
    private static LivingEntity rayTraceTarget(Player player, float range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, search,
            entity -> entity instanceof LivingEntity && entity != player && !entity.isSpectator(),
            (double) (range * range));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }
}

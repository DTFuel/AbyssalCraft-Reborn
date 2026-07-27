package com.shinoow.abyssalcraft.system.spell;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The spell casting glue (owned by PS-7), faithful to the 1.12.2 {@code api.spell.SpellUtils}: it draws the
 * spell's Potential Energy from a held energy item ({@link IEnergyContainerItem}, delivered CR-58) and
 * applies the effect. This is the piece the ItemStack-NBT fork (CR-58) unblocked.
 *
 * <p>Entity scrolls may provide a client target through {@code MobSpellMessage}, but this class always
 * revalidates it server-side and falls back to a server ray trace. This keeps packet loss harmless and
 * prevents clients from choosing costs, quality, spell ids or out-of-range targets.
 */
public final class SpellUtils {

    private static final float DEFAULT_RANGE = 16.0F;

    private SpellUtils() {}

    /**
     * Cast an entity-target spell (server-side): ray-trace a living target within the spell's range, verify
     * the caster's {@code energyItem} holds enough PE, drain it, and apply the effect. Returns whether the
    * spell was cast.
     */
    public static boolean castOnTarget(Level level, Player player, EntityTargetSpell spell, ItemStack energyItem) {
        if (level.isClientSide || !SpellAvailability.isEnabled(spell)
            || !(energyItem.getItem() instanceof IEnergyContainerItem energy)) {
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

    /** Resolve and execute a manifest-backed spell entirely on the server. */
    public static boolean castManifest(Level level, Player player, ManifestSpell spell, ItemStack source,
                                       ScrollType quality, LivingEntity requestedTarget) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer caster)
            || spell == null || !SpellAvailability.isEnabled(spell) || quality == ScrollType.NONE
            || quality.quality() < spell.scrollType().quality()) {
            return false;
        }

        List<EnergySource> energySources = energySources(caster, source);
        float available = 0;
        for (EnergySource energySource : energySources) {
            available += energySource.container().getContainedEnergy(energySource.stack());
        }
        if (available + 0.001F < spell.requiredEnergy()) return false;

        LivingEntity entityTarget = null;
        BlockHitResult blockTarget = null;
        switch (spell.manifest().targetType()) {
            case ENTITY -> {
                entityTarget = validRequestedTarget(caster, requestedTarget, DEFAULT_RANGE)
                    ? requestedTarget : rayTraceTarget(caster, DEFAULT_RANGE);
                if (entityTarget == null) return false;
            }
            case ENTITY_OR_SELF -> {
                entityTarget = validRequestedTarget(caster, requestedTarget, DEFAULT_RANGE)
                    ? requestedTarget : rayTraceTarget(caster, DEFAULT_RANGE);
                if (entityTarget == null) entityTarget = caster;
            }
            case BLOCK -> {
                HitResult hit = rayTraceBlock(server, caster, DEFAULT_RANGE);
                if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return false;
                blockTarget = block;
            }
            case SELF -> {
                entityTarget = caster;
            }
        }

        ItemStack preferredEnergySource = energySources.stream()
            .filter(candidate -> candidate.container().canAcceptPE(candidate.stack()))
            .map(EnergySource::stack).findFirst()
            .orElse(energySources.isEmpty() ? ItemStack.EMPTY : energySources.get(0).stack());
        SpellCastContext context = new SpellCastContext(server, caster, source, preferredEnergySource, quality,
            entityTarget, blockTarget);
        if (!spell.canCast(context)
            && spell.manifest().targetType() == SpellManifest.TargetType.ENTITY_OR_SELF
            && entityTarget != caster) {
            context = new SpellCastContext(server, caster, source, preferredEnergySource, quality,
                caster, blockTarget);
        }
        if (!spell.canCast(context)) return false;

        List<EnergyDebit> debits = consumeEnergy(energySources, spell.requiredEnergy());
        float consumed = debits.stream().map(EnergyDebit::amount).reduce(0.0F, Float::sum);
        if (consumed + 0.001F < spell.requiredEnergy()) {
            refund(debits);
            return false;
        }
        try {
            spell.cast(context);
            return true;
        } catch (RuntimeException exception) {
            refund(debits);
            com.shinoow.abyssalcraft.AbyssalCraft.LOGGER.error(
                "Spell {} failed for {}", spell.id(), caster.getGameProfile().getName(), exception);
            return false;
        }
    }

    private static List<EnergySource> energySources(ServerPlayer caster, ItemStack source) {
        List<EnergySource> result = new ArrayList<>();
        addEnergySource(result, source);
        addEnergySource(result, caster.getMainHandItem());
        addEnergySource(result, caster.getOffhandItem());
        int size = Math.min(36, caster.getInventory().getContainerSize());
        for (int slot = 0; slot < size; slot++) {
            addEnergySource(result, caster.getInventory().getItem(slot));
        }
        return result;
    }

    private static void addEnergySource(List<EnergySource> sources, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IEnergyContainerItem energy)) return;
        for (EnergySource source : sources) if (source.stack() == stack) return;
        sources.add(new EnergySource(stack, energy));
    }

    private static List<EnergyDebit> consumeEnergy(List<EnergySource> sources, float requested) {
        List<EnergyDebit> result = new ArrayList<>();
        float remaining = requested;
        for (EnergySource source : sources) {
            if (remaining <= 0.001F) break;
            float consumed = source.container().consumeEnergy(source.stack(), remaining);
            if (consumed > 0) {
                result.add(new EnergyDebit(source.stack(), source.container(), consumed));
                remaining -= consumed;
            }
        }
        return result;
    }

    private static void refund(List<EnergyDebit> debits) {
        for (EnergyDebit debit : debits) debit.container().addEnergy(debit.stack(), debit.amount());
    }

    private record EnergySource(ItemStack stack, IEnergyContainerItem container) {}

    private record EnergyDebit(ItemStack stack, IEnergyContainerItem container, float amount) {}

    /** The living entity the player is looking at within {@code range}, or {@code null}. */
    public static LivingEntity rayTraceTarget(Player player, float range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, search,
            entity -> entity instanceof LivingEntity living && entity != player
                && canTarget(player, living),
            (double) (range * range));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private static boolean validRequestedTarget(ServerPlayer caster, LivingEntity target, float range) {
        return target != null && target.isAlive() && !target.isSpectator()
            && target.level() == caster.level() && target != caster
            && caster.distanceToSqr(target) <= range * range && canTarget(caster, target);
    }

    private static boolean canTarget(Player caster, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) return false;
        if (!(target instanceof Player targetPlayer)) return true;
        return !targetPlayer.isCreative() && caster.canHarmPlayer(targetPlayer);
    }

    private static HitResult rayTraceBlock(ServerLevel level, ServerPlayer player, float range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
            ClipContext.Fluid.ANY, player));
    }
}

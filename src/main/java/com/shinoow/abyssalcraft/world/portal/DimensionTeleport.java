package com.shinoow.abyssalcraft.world.portal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

import com.shinoow.abyssalcraft.platform.TeleportCompat;
import com.shinoow.abyssalcraft.system.portal.PortalAnchorIndex;

/**
 * Cross-dimension teleport helper (owned by PG-6 / Stage G2). The modern equivalent of the 1.12.2
 * {@code TeleporterAC}/{@code TeleporterSinglePortal} landing math, minus the portal-frame search: it
 * scales the entity's horizontal position by the ratio of the two dimensions' coordinate scales (the
 * old {@code WorldProvider.getMovementFactor()}), clamps inside the destination world border, drops the
 * entity onto the destination surface, and hands off to {@link TeleportCompat} for the actual
 * loader/version-forked {@code changeDimension} call.
 *
 * <p>The portal-frame/anchor <em>placement</em> side (1.12.2 {@code makePortal} + portal anchor block)
 * is deferred with the unported {@code portal_anchor} block and its Necronomicon activation ritual; this
 * helper is what those (and {@code world/portal} entities) call once a destination is chosen.
 */
public final class DimensionTeleport {

    private DimensionTeleport() {}

    /**
     * Send {@code entity} to the {@code destination} dimension, landing it on the surface at the
     * coordinate-scaled position. No-op (returns the entity unchanged) off-server, for an unknown
     * dimension, or when already in the destination.
     */
    public static Entity teleport(Entity entity, ResourceKey<Level> destination) {
        if (!(entity.level() instanceof ServerLevel src)) return entity;
        ServerLevel dest = src.getServer().getLevel(destination);
        if (dest == null || dest == src) return entity;

        double scale = src.dimensionType().coordinateScale() / dest.dimensionType().coordinateScale();
        WorldBorder border = dest.getWorldBorder();
        double x = Mth.clamp(entity.getX() * scale, border.getMinX() + 16.0, border.getMaxX() - 16.0);
        double z = Mth.clamp(entity.getZ() * scale, border.getMinZ() + 16.0, border.getMaxZ() - 16.0);
        BlockPos returnAnchor = PortalAnchorIndex.get(dest)
            .findNearestReturnAnchor(dest, src.dimension(), x, z).orElse(null);
        if (returnAnchor != null) {
            dest.getChunk(returnAnchor.getX() >> 4, returnAnchor.getZ() >> 4);
            Entity teleported = TeleportCompat.teleport(entity, dest, returnAnchor.getX() + 0.5D,
                returnAnchor.getY() + 1.0D, returnAnchor.getZ() + 0.5D);
            return teleported == null ? entity : teleported;
        }

        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        dest.getChunk(bx >> 4, bz >> 4);
        int y = dest.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        Entity teleported = TeleportCompat.teleport(entity, dest, bx + 0.5D, y, bz + 0.5D);
        return teleported == null ? entity : teleported;
    }
}

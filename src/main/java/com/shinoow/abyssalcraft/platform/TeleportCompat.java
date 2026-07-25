package com.shinoow.abyssalcraft.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

//? if <1.21 {
import java.util.function.Function;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;
//?}
//? if >=1.21 {
/*import net.minecraft.world.level.portal.DimensionTransition;
*///?}

/**
 * Compat: cross-dimension entity travel (vanilla axis, owned by PG-6 / Stage G2).
 *
 * <p>This is the single deep 1.20.1 &harr; 1.21 divergence of the portal subsystem, so the business
 * {@code world/portal/**} and portal entity stay fork-free. Mojang rewrote teleportation in 1.21:
 * <ul>
 *   <li><b>1.20.1 (Forge)</b> &mdash; {@code Entity.changeDimension(ServerLevel, ITeleporter)} where the
 *       Forge {@code ITeleporter} supplies a {@code PortalInfo(pos, speed, yRot, xRot)} landing spot.</li>
 *   <li><b>1.21 (NeoForge)</b> &mdash; {@code Entity.changeDimension(DimensionTransition)}, a record that
 *       carries the destination level, landing pos/speed/rotation and a post-transition hook.</li>
 * </ul>
 * Both place the entity at an explicit target position (the caller computes it), so no portal-frame
 * search is needed. Works for both mobs and players (the loader patches {@code ServerPlayer} too).
 */
public final class TeleportCompat {

    private TeleportCompat() {}

    /**
     * Move {@code entity} to {@code dest} at ({@code x},{@code y},{@code z}), preserving its rotation and
     * zeroing momentum. Returns the entity instance living in the destination level (a fresh copy for
     * most entities), or {@code null} if the travel was vetoed.
     */
    public static Entity teleport(Entity entity, ServerLevel dest, double x, double y, double z) {
        Vec3 pos = new Vec3(x, y, z);
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        //? if >=1.21 {
        /*return entity.changeDimension(new DimensionTransition(
            dest, pos, Vec3.ZERO, yRot, xRot, DimensionTransition.DO_NOTHING));
        *///?} else {
        return entity.changeDimension(dest, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity e, ServerLevel destWorld,
                                            Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(pos, Vec3.ZERO, yRot, xRot);
            }
        });
        //?}
    }
}

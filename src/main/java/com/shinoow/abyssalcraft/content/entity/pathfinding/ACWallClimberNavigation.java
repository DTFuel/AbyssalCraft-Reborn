package com.shinoow.abyssalcraft.content.entity.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;

/**
 * Canonical wall-climbing navigation for AbyssalCraft spider-like mobs (Stage D1 / PD-2).
 *
 * <p>Ports the role of 1.12.2's {@code PatchedPathNavigateClimber}. As with {@link ACGroundPathNavigation},
 * the historical "random spinning" patch (MinecraftForge PR #6091) is now native in vanilla
 * {@link WallClimberNavigation}, so this thin subclass simply gives AbyssalCraft climbing mobs a shared,
 * consistent navigation type and a single future customization point. The {@code (Mob, Level)} constructor
 * is identical across 1.20.1 and 1.21 (javap-verified), so this is fork-free.
 */
public class ACWallClimberNavigation extends WallClimberNavigation {

    public ACWallClimberNavigation(Mob mob, Level level) {
        super(mob, level);
    }
}

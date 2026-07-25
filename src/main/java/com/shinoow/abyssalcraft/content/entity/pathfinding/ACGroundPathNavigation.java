package com.shinoow.abyssalcraft.content.entity.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/**
 * Canonical ground navigation for AbyssalCraft mobs (Stage D1 / PD-2).
 *
 * <p>In 1.12.2 AbyssalCraft shipped a {@code PatchedPathNavigateGround} that overrode {@code pathFollow}
 * to stop the "random spinning while navigating" bug (MinecraftForge PR #6091). That fix is now native in
 * vanilla {@link GroundPathNavigation} ({@code followThePath}, 1.13+), so no override is required. This
 * thin subclass is the shared extension point AbyssalCraft ground mobs use, keeping a single place for any
 * future navigation tweaks (and preserving the old class's role in the port).
 */
public class ACGroundPathNavigation extends GroundPathNavigation {

    public ACGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }
}

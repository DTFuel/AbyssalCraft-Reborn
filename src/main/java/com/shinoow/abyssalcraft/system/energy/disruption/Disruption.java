package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A disruption (owned by PS-9): the bad thing that can happen while a manipulator draws Potential Energy
 * (PS-5) without a Place of Power (PS-10). Faithful to the 1.12.2 {@code api.energy.disruption.DisruptionEntry}:
 * a named effect optionally limited to a {@link DeityType} (the deity whose statue must be present). Concrete
 * subtypes decide the effect in {@link #disrupt(Level, BlockPos, List)}.
 */
public abstract class Disruption {

    private final String name;
    private final DeityType deity; // null = any deity

    protected Disruption(String name, DeityType deity) {
        this.name = name;
        this.deity = deity;
    }

    public String name() {
        return name;
    }

    /** The deity this disruption is limited to, or {@code null} for any. */
    public DeityType deity() {
        return deity;
    }

    public String translationKey() {
        return "ac.disruption." + name;
    }

    /** Where all the evil things happen. Runs server-side against players within ~16 blocks of {@code pos}. */
    public abstract void disrupt(Level level, BlockPos pos, List<Player> players);
}

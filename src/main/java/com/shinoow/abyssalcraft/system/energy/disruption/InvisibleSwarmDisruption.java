package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Spawns two-to-five Endermen with the legacy hidden 12000-tick invisibility effect. */
public final class InvisibleSwarmDisruption extends Disruption {

    public InvisibleSwarmDisruption(String name, DeityType deity) {
        super(name, deity);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) return;
        int count = server.random.nextInt(4) + 2;
        for (int index = 0; index < count; index++) {
            MobSpawnCompat.spawnNear(server, pos, EntityType.ENDERMAN,
                enderman -> enderman.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 12000, 1, false, false)));
        }
    }

    public static int minimumCount() {
        return 2;
    }

    public static int maximumCount() {
        return 5;
    }
}
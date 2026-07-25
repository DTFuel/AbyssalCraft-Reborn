package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Registry + generator of disruptions (owned by PS-9), faithful to the 1.12.2
 * {@code api.energy.disruption.DisruptionHandler}. A manipulator that draws PE without a Place of Power
 * (PS-10) asks this handler to {@link #generate} a random disruption matching the tied deity.
 */
public final class DisruptionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DisruptionHandler INSTANCE = new DisruptionHandler();

    private final List<Disruption> disruptions = new ArrayList<>();

    private DisruptionHandler() {}

    public static DisruptionHandler instance() {
        return INSTANCE;
    }

    public void registerDisruption(Disruption disruption) {
        for (Disruption entry : disruptions) {
            if (entry.name().equals(disruption.name())) {
                LOGGER.error("Disruption already registered: {}", disruption.name());
                return;
            }
        }
        disruptions.add(disruption);
    }

    public List<Disruption> getDisruptions() {
        return Collections.unmodifiableList(disruptions);
    }

    public Disruption find(String name) {
        for (Disruption disruption : disruptions) {
            if (disruption.name().equals(name)) {
                return disruption;
            }
        }
        return null;
    }

    /**
     * A random disruption eligible for {@code deity} (deity-less disruptions are always eligible; when
     * {@code deity} is {@code null} only deity-less ones qualify), or {@code null} if none.
     */
    public Disruption getRandom(DeityType deity, RandomSource random) {
        List<Disruption> eligible = new ArrayList<>();
        for (Disruption disruption : disruptions) {
            if (disruption.deity() == null || disruption.deity() == deity) {
                eligible.add(disruption);
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get(random.nextInt(eligible.size()));
    }

    /**
     * Pick and run a random disruption for {@code deity} (server-side). The Necronomicon feedback
     * (PS-1 {@code DisruptionMessage}, handler currently a stub) is deferred to that message's wiring.
     */
    public void generate(DeityType deity, Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        Disruption disruption = getRandom(deity, level.random);
        if (disruption != null) {
            disruption.disrupt(level, pos, players);
            Component name = Component.translatable(disruption.translationKey());
            players.forEach(player -> player.displayClientMessage(
                Component.translatable("message.abyssalcraft.disruption", name), false));
        }
    }
}

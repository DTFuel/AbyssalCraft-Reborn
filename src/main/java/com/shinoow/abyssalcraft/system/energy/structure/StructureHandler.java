package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Registry of Places of Power (owned by PS-10), faithful to the 1.12.2
 * {@code api.energy.structure.StructureHandler}. When a player uses a Necronomicon on a candidate block, this
 * handler finds the first registered {@link IPlaceOfPower} whose book-tier gate is met and which
 * {@code canConstruct} here, and forms it.
 */
public final class StructureHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StructureHandler INSTANCE = new StructureHandler();

    private final List<IPlaceOfPower> structures = new ArrayList<>();

    private StructureHandler() {}

    public static StructureHandler instance() {
        return INSTANCE;
    }

    public void registerStructure(IPlaceOfPower place) {
        for (IPlaceOfPower entry : structures) {
            if (entry.getIdentifier().equals(place.getIdentifier())) {
                LOGGER.error("Place of Power already registered: {}", place.getIdentifier());
                return;
            }
        }
        structures.add(place);
    }

    public List<IPlaceOfPower> getStructures() {
        return Collections.unmodifiableList(structures);
    }

    public IPlaceOfPower getStructureByName(String identifier) {
        for (IPlaceOfPower place : structures) {
            if (place.getIdentifier().equals(identifier)) {
                return place;
            }
        }
        return null;
    }

    /** Whether any registered Place of Power (book-tier met) can be formed here. */
    public boolean canFormStructure(Level level, BlockPos pos, int bookType, Player player) {
        for (IPlaceOfPower place : structures) {
            if (bookType >= place.getBookType() && place.canConstruct(level, pos, player)) {
                return true;
            }
        }
        return false;
    }

    /** Form the first eligible Place of Power here (server-side). */
    public void formStructure(Level level, BlockPos pos, int bookType, Player player) {
        if (level.isClientSide) {
            return;
        }
        for (IPlaceOfPower place : structures) {
            if (bookType >= place.getBookType() && place.canConstruct(level, pos, player)) {
                place.construct(level, pos);
                return;
            }
        }
    }
}

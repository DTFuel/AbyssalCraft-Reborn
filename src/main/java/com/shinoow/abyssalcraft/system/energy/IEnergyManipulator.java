package com.shinoow.abyssalcraft.system.energy;

import java.util.Set;

import net.minecraft.core.BlockPos;

/**
 * A block entity that manipulates (drains + distributes) Potential Energy (owned by PS-5), faithful to
 * the 1.12.2 {@code api.energy.IEnergyManipulator}. Each tick a manipulator feeds its energy quanta to
 * the collectors it tracks (via {@link PEUtils#transferToCollectors}); the player / dropped-item
 * transfer + amplifier-charm boosts land with the energy-item + Necronomicon content (deferred).
 */
public interface IEnergyManipulator {

    /** The quanta of Potential Energy this manipulator can move per transfer. */
    float getEnergyQuanta();

    /** Whether the manipulator currently has energy to transfer. */
    boolean canTransferPE();

    /** Positions of the collectors this manipulator feeds. */
    Set<BlockPos> getEnergyCollectors();

    /** Whether the amplifier boost is currently active. */
    boolean isActive();

    /** Accumulate transfer tolerance (wear) after a successful transfer. */
    void addTolerance(int amount);

    /** Current transfer tolerance; statues disrupt at 100 and depositioners at 200. */
    int getTolerance();

    /** Reset tolerance after a disruption or an administrative recovery. */
    default void resetTolerance() {
        setTolerance(0);
    }

    /** Restore tolerance from persistent data. */
    void setTolerance(int tolerance);

    /** The deity this manipulator is bound to, or {@code null}. */
    default DeityType getDeity() {
        return null;
    }

    /** Deity selected by the active charm, or {@code null}. */
    default DeityType getActiveDeity() {
        return null;
    }

    /** Amplifier selected by the active charm, or {@code null}. */
    default AmplifierType getActiveAmplifier() {
        return null;
    }

    /** Set or clear the charm deity. */
    default void setActiveDeity(DeityType deity) {}

    /** Set or clear the charm amplifier. */
    default void setActiveAmplifier(AmplifierType amplifier) {}

    /** Activate a charm once; an already-active manipulator ignores a second amplifier. */
    default boolean setActive(AmplifierType amplifier, DeityType deity) {
        if (isActive() || amplifier == null) {
            return false;
        }
        setActiveDeity(deity);
        setActiveAmplifier(amplifier);
        return true;
    }

    /** Clear the active charm state. */
    default boolean clearActive() {
        if (!isActive()) {
            return false;
        }
        setActiveDeity(null);
        setActiveAmplifier(null);
        return true;
    }

    /** Charm amplifier factor for {@code type} (0 = none); charms are deferred content. */
    default float getAmplifier(AmplifierType type) {
        return 0.0F;
    }
}

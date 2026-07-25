package com.shinoow.abyssalcraft.content.entity.ai;

/**
 * Hook interface for {@link SwellGoal} (Stage D1 / PD-2): a mob that swells like a creeper before
 * detonating, exposing its swell direction/state. Vanilla's {@code SwellGoal} is bound to
 * {@code Creeper}; AbyssalCraft's "anti-creeper" (and any other non-{@code Creeper} swelling mob)
 * implements this so it can reuse the shared {@link SwellGoal} instead.
 *
 * <p>State convention mirrors vanilla {@code Creeper}: positive = swelling toward detonation, negative =
 * winding back down.
 */
public interface SwellingMob {

    /** Current swell direction/state (positive = swelling, negative = de-swelling). */
    int getSwellState();

    /** Sets the swell direction/state. */
    void setSwellState(int state);
}

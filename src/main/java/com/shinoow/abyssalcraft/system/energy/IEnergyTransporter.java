package com.shinoow.abyssalcraft.system.energy;

/**
 * A block entity that relays Potential Energy between containers (owned by PS-5), faithful to the
 * 1.12.2 {@code api.energy.IEnergyTransporter}/{@code IEnergyRelayBlock}. A transporter is itself a
 * container that forwards energy along the network; the multi-block relay routing lands with the
 * energy blocks (deferred content).
 */
public interface IEnergyTransporter extends IEnergyContainer {

    /** Transfer range (in blocks) this transporter reaches. */
    int getTransferRange();

    /** PE pulled from the block directly behind the relay every collection cycle. */
    float getDrainQuanta();

    /** PE offered to the first visible container in front every transfer cycle. */
    float getTransferQuanta();
}

package com.shinoow.abyssalcraft.content.block.energy;

import java.util.List;
import java.util.Set;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.IEnergyManipulator;
import com.shinoow.abyssalcraft.system.energy.ManipulatorState;
import com.shinoow.abyssalcraft.system.energy.PEUtils;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Server-side PE routing core for the Energy Depositioner. */
public class EnergyDepositionerBlockEntity extends InventoryEnergyBlockEntity
    implements IEnergyManipulator, TickingBlockEntity {

    private static final int MAX_ENERGY = 10000;
    private static final int SCAN_INTERVAL = 200;
    private static final int DISRUPTION_TOLERANCE = 200;

    private final ManipulatorState manipulatorState = new ManipulatorState();

    public EnergyDepositionerBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.ENERGY_DEPOSITIONER_BE.get(), pos, state, 2, MAX_ENERGY);
    }

    @Override
    public void serverTick() {
        if (level == null || !PEUtils.hasNoAdjacentManipulators(level, worldPosition)) {
            return;
        }
        if (level.getGameTime() % SCAN_INTERVAL == 0) {
            PEUtils.locateCollectors(level, worldPosition, this);
        }
        PEUtils.transferToCollectors(level, this);
        if (getTolerance() >= DISRUPTION_TOLERANCE) {
            triggerDisruption();
        }
    }

    @Override
    public float getEnergyQuanta() {
        float quanta = isActive()
            ? 20.0F * Math.max(getAmplifier(AmplifierType.POWER), 1.0F)
            : 15.0F;
        return consumeEnergy(quanta);
    }

    @Override
    public boolean canTransferPE() {
        return getContainedEnergy() > 0;
    }

    @Override
    public Set<BlockPos> getEnergyCollectors() {
        return manipulatorState.collectors();
    }

    @Override
    public boolean isActive() {
        return getActiveAmplifier() != null;
    }

    @Override
    public void addTolerance(int amount) {
        setTolerance(getTolerance() + amount);
    }

    @Override
    public int getTolerance() {
        return manipulatorState.tolerance();
    }

    @Override
    public void setTolerance(int tolerance) {
        manipulatorState.setTolerance(tolerance);
        setChanged();
    }

    @Override
    public DeityType getActiveDeity() {
        return manipulatorState.activeDeity();
    }

    @Override
    public AmplifierType getActiveAmplifier() {
        return manipulatorState.activeAmplifier();
    }

    @Override
    public void setActiveDeity(DeityType deity) {
        manipulatorState.setActiveDeity(deity);
        setChanged();
    }

    @Override
    public void setActiveAmplifier(AmplifierType amplifier) {
        manipulatorState.setActiveAmplifier(amplifier);
        setChanged();
    }

    @Override
    public float getAmplifier(AmplifierType type) {
        if (type != getActiveAmplifier()) {
            return 0.0F;
        }
        return switch (type) {
            case DURATION -> 2.0F;
            case POWER -> 1.5F;
            case RANGE -> 4.0F;
        };
    }

    @Override
    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        manipulatorState.save(tag);
    }

    @Override
    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {
        manipulatorState.load(tag);
    }

    private void triggerDisruption() {
        resetTolerance();
        AABB bounds = new AABB(worldPosition).inflate(16.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, bounds);
        DeityType[] deities = DeityType.values();
        DeityType deity = deities[level.random.nextInt(deities.length)];
        DisruptionHandler.instance().generate(deity, level, worldPosition, players);
    }
}
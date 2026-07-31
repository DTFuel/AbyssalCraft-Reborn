package com.shinoow.abyssalcraft.content.block.energy;

import java.util.List;
import java.util.Set;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.IEnergyManipulator;
import com.shinoow.abyssalcraft.system.energy.IEnergyTransporterItem;
import com.shinoow.abyssalcraft.system.energy.ManipulatorState;
import com.shinoow.abyssalcraft.system.energy.PEUtils;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;
import com.shinoow.abyssalcraft.system.energy.structure.IStructureComponent;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Persistent PE source and manipulator shared by all functional deity statues. */
public class DeityStatueBlockEntity extends ACBlockEntity
    implements IEnergyManipulator, IStructureComponent, TickingBlockEntity {

    private static final int PLAYER_INTERVAL = 120;
    private static final int COLLECTOR_SCAN_INTERVAL = 200;
    private static final int DISRUPTION_TOLERANCE = 100;

    private final ManipulatorState manipulatorState = new ManipulatorState();
    private int timer;
    private boolean inMultiblock;
    private BlockPos basePosition;

    public DeityStatueBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.DEITY_STATUE_BE.get(), pos, state);
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }
        float structureRange = PEUtils.getStructureAmplifier(level, this, AmplifierType.RANGE);
        int blockRange = PEUtils.getBlockAmplifiers(level, worldPosition, AmplifierType.RANGE);
        boolean canOperate = canOperate(level.canSeeSky(worldPosition.above()),
            PEUtils.hasNoAdjacentManipulators(level, worldPosition), inMultiblock);
        if (canOperate) {
            int range = (int) (7 + (structureRange + blockRange) * 4 + getAmplifier(AmplifierType.RANGE));
            if (level.getGameTime() % COLLECTOR_SCAN_INTERVAL == 0) {
                PEUtils.locateCollectors(level, worldPosition, this);
            }
            chargeNearbyPlayers(range);
            PEUtils.transferToDroppedItems(level, worldPosition, this, range);
            PEUtils.transferToCollectors(level, this);
        }
        if (getTolerance() >= DISRUPTION_TOLERANCE) {
            triggerDisruption();
        }
    }

    static boolean canOperate(boolean skyVisible, boolean noAdjacentManipulators, boolean inMultiblock) {
        return inMultiblock || skyVisible && noAdjacentManipulators;
    }

    static boolean canDisrupt(boolean disruptionsDisabled, boolean inMultiblock, boolean inOmothol) {
        return !disruptionsDisabled && !inMultiblock && !inOmothol;
    }

    private void chargeNearbyPlayers(int range) {
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(range),
            player -> canAcceptPE(player.getMainHandItem()) || canAcceptPE(player.getOffhandItem()));
        if (players.isEmpty()) {
            return;
        }
        float duration = Math.max(getAmplifier(AmplifierType.DURATION), 1.0F)
            + PEUtils.getStructureAmplifier(level, this, AmplifierType.DURATION);
        if (++timer >= Math.max(1, (int) (PLAYER_INTERVAL / duration))) {
            timer = level.random.nextInt(10);
            for (Player player : players) {
                float transferred = PEUtils.transferToItem(this,
                    player.getItemInHand(InteractionHand.MAIN_HAND), isActive() ? 4 : 2);
                transferred += PEUtils.transferToItem(this,
                    player.getItemInHand(InteractionHand.OFF_HAND), isActive() ? 4 : 2);
                if (transferred > 0.0F) {
                    PEUtils.broadcastPEStream(level, worldPosition, player.blockPosition());
                }
            }
        }
    }

    private static boolean canAcceptPE(ItemStack stack) {
        return stack.getItem() instanceof IEnergyTransporterItem item && item.canAcceptPE(stack);
    }

    @Override
    public float getEnergyQuanta() {
        if (!isActive()) {
            return 5.0F;
        }
        return 10.0F * (Math.max(getAmplifier(AmplifierType.POWER), 1.0F)
            + PEUtils.getStructureAmplifier(level, this, AmplifierType.POWER));
    }

    @Override
    public boolean canTransferPE() {
        return true;
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
        if (!inMultiblock) {
            setTolerance(getTolerance() + amount);
        }
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
    public DeityType getDeity() {
        return getBlockState().getBlock() instanceof DeityStatueBlock statue ? statue.deity() : null;
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
        boolean matchingDeity = getActiveDeity() == getDeity();
        return switch (type) {
            case DURATION -> matchingDeity ? 4.0F : 2.0F;
            case POWER -> matchingDeity ? 2.5F : 1.5F;
            case RANGE -> matchingDeity ? 6.0F : 4.0F;
        };
    }

    @Override
    public boolean isInMultiblock() {
        return inMultiblock;
    }

    @Override
    public void setInMultiblock(boolean inMultiblock) {
        this.inMultiblock = inMultiblock;
        setChanged();
    }

    @Override
    public BlockPos getBasePosition() {
        return basePosition;
    }

    @Override
    public void setBasePosition(BlockPos basePosition) {
        this.basePosition = basePosition;
        setChanged();
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Timer", timer);
        manipulatorState.save(tag);
        tag.putBoolean("IsMultiblock", inMultiblock);
        if (basePosition != null) {
            tag.putLong("BasePosition", basePosition.asLong());
        }
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        timer = tag.getInt("Timer");
        manipulatorState.load(tag);
        inMultiblock = tag.getBoolean("IsMultiblock");
        basePosition = tag.contains("BasePosition") ? BlockPos.of(tag.getLong("BasePosition")) : null;
    }

    private void triggerDisruption() {
        resetTolerance();
        if (!canDisrupt(ACConfig.no_disruptions.get(), inMultiblock,
            level.dimension().equals(ACDimensions.OMOTHOL))) {
            return;
        }
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(16.0));
        DisruptionHandler.instance().generate(getDeity(), level, worldPosition, players);
    }
}

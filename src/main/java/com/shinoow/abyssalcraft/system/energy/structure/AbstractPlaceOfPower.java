package com.shinoow.abyssalcraft.system.energy.structure;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.energy.DeityStatueBlock;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.PlaceOfPowerBaseBlockEntity;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Shared formation and component-membership behavior for the three legacy Places of Power. */
abstract class AbstractPlaceOfPower implements IPlaceOfPower {

    private final String identifier;
    private final float rangeAmplifier;
    private final BlockPos renderActivationPoint;

    protected AbstractPlaceOfPower(String identifier, float rangeAmplifier, BlockPos renderActivationPoint) {
        this.identifier = identifier;
        this.rangeAmplifier = rangeAmplifier;
        this.renderActivationPoint = renderActivationPoint;
    }

    @Override
    public final String getIdentifier() {
        return identifier;
    }

    @Override
    public final int getBookType() {
        return 0;
    }

    @Override
    public final float getAmplifier(AmplifierType type) {
        return type == AmplifierType.RANGE ? rangeAmplifier : 0.0F;
    }

    @Override
    public final boolean canConstruct(Level level, BlockPos pos, Player player) {
        return isValid(level, pos, false);
    }

    @Override
    public final void construct(Level level, BlockPos pos) {
        if (level.isClientSide || !isValid(level, pos, false)) {
            return;
        }
        level.setBlock(pos, EnergyBlocks.PLACE_OF_POWER_BASE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof PlaceOfPowerBaseBlockEntity base) {
            base.setMultiblock(this);
        }
        setComponentMembership(level, pos, true);
    }

    @Override
    public final void validate(Level level, BlockPos pos) {
        setComponentMembership(level, pos, isValid(level, pos, true));
    }

    @Override
    public final void detach(Level level, BlockPos pos) {
        setComponentMembership(level, pos, false);
    }

    @Override
    public final BlockPos getActivationPointForRender() {
        return renderActivationPoint;
    }

    @Override
    public final int getAmbientEffectCooldown() {
        return 0;
    }

    @Override
    public final void triggerAmbientEffect(Level level, BlockPos pos) {
    }

    protected abstract boolean isValid(Level level, BlockPos basePos, boolean formed);

    protected abstract List<BlockPos> componentPositions(BlockPos basePos);

    protected final boolean isActivationBlock(Level level, BlockPos pos, boolean formed) {
        return level.getBlockState(pos).is(formed
            ? EnergyBlocks.PLACE_OF_POWER_BASE.get()
            : BaseBlocks.MONOLITH_STONE.get());
    }

    protected static boolean isStatue(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof DeityStatueBlock;
    }

    private void setComponentMembership(Level level, BlockPos basePos, boolean member) {
        for (BlockPos componentPos : componentPositions(basePos)) {
            if (level.getBlockEntity(componentPos) instanceof IStructureComponent component) {
                component.setInMultiblock(member);
                component.setBasePosition(member ? basePos : null);
            }
        }
    }
}
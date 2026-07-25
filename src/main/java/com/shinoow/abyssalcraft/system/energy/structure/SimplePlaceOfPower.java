package com.shinoow.abyssalcraft.system.energy.structure;

import com.shinoow.abyssalcraft.system.energy.AmplifierType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A minimal {@link IPlaceOfPower} (pilot, owned by PS-10): a book-tier-gated Place of Power whose formability
 * is a settable flag (so the framework can be exercised without a live world). Concrete Places of Power
 * (deferred content) supply real {@code canConstruct}/{@code construct}/render data against their energy
 * blocks (PS-5) and deity statues.
 */
public final class SimplePlaceOfPower implements IPlaceOfPower {

    private static final BlockState[][][] EMPTY = new BlockState[0][0][0];

    private final String identifier;
    private final int bookType;
    private boolean formable;

    public SimplePlaceOfPower(String identifier, int bookType) {
        this.identifier = identifier;
        this.bookType = bookType;
    }

    public SimplePlaceOfPower setFormable(boolean formable) {
        this.formable = formable;
        return this;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getBookType() {
        return bookType;
    }

    @Override
    public float getAmplifier(AmplifierType type) {
        return 0;
    }

    @Override
    public boolean canConstruct(Level level, BlockPos pos, Player player) {
        return formable;
    }

    @Override
    public void construct(Level level, BlockPos pos) {
    }

    @Override
    public void validate(Level level, BlockPos pos) {
    }

    @Override
    public BlockState[][][] getRenderData() {
        return EMPTY;
    }

    @Override
    public BlockPos getActivationPointForRender() {
        return BlockPos.ZERO;
    }

    @Override
    public int getAmbientEffectCooldown() {
        return 0;
    }

    @Override
    public void triggerAmbientEffect(Level level, BlockPos pos) {
    }
}

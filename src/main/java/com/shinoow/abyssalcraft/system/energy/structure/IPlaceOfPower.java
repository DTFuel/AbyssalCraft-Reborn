package com.shinoow.abyssalcraft.system.energy.structure;

import com.shinoow.abyssalcraft.system.energy.AmplifierType;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Place of Power (owned by PS-10), faithful to the 1.12.2 {@code api.energy.structure.IPlaceOfPower}: a
 * multiblock that lets an energy manipulator (PS-5) harvest Potential Energy <em>without</em> angering the
 * gods (i.e. without risking a disruption, PS-9). Register instances with {@link StructureHandler}.
 *
 * <p>The knowledge gate (1.12.2 {@code IResearchItem}) is decoupled here to an optional research id, so
 * PS-10 does not depend on the knowledge subsystem (PS-8); whoever forms the structure reads
 * {@link #getResearchId()} to gate it.
 */
public interface IPlaceOfPower {

    /** The identifier (unique key) of this Place of Power. */
    String getIdentifier();

    /** The Necronomicon book tier required to form this Place of Power. */
    int getBookType();

    /** Optional knowledge/research gate id (read by PS-8), or {@code null}. */
    default ResourceLocation getResearchId() {
        return null;
    }

    /** Bonus this structure grants to a manipulator's stat along {@code type}, or {@code 0}. */
    float getAmplifier(AmplifierType type);

    /** Whether the structure can be formed here (book-tier and research are checked before this). */
    boolean canConstruct(Level level, BlockPos pos, Player player);

    /** Build the Place of Power at {@code pos} (server-side). */
    void construct(Level level, BlockPos pos);

    /** Re-check the structure is still valid and act accordingly. */
    void validate(Level level, BlockPos pos);

    /** Clear membership from all structure components when the master block is removed. */
    default void detach(Level level, BlockPos pos) {
    }

    /** A block-state grid depicting the assembled structure (for Necronomicon rendering). */
    BlockState[][][] getRenderData();

    /** The position (within {@link #getRenderData()}) of the block that forms the structure. */
    BlockPos getActivationPointForRender();

    /** Cooldown in ticks between ambient-effect triggers. */
    int getAmbientEffectCooldown();

    /** Trigger the ambient effect of the Place of Power. */
    void triggerAmbientEffect(Level level, BlockPos pos);

    default String getDescriptionKey() {
        return "ac.structure." + getIdentifier() + ".description";
    }

    default String getRequiredBlockNamesKey() {
        return "ac.structure." + getIdentifier() + ".blocks";
    }
}

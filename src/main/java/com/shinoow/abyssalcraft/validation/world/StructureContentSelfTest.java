package com.shinoow.abyssalcraft.validation.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.shinoow.abyssalcraft.content.block.structure.CrateBlockEntity;
import com.shinoow.abyssalcraft.content.block.structure.SealingLockBlockEntity;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.IdolOfFadingBlockEntity;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBiomassBlockEntity;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

/** Permanent contract checks for converted structure marker hosts. */
public final class StructureContentSelfTest {

    private StructureContentSelfTest() {}

    public static void run() {
        require(BuiltInRegistries.BLOCK.getKey(StructureContent.CRATE.get()).getPath().equals("crate"),
            "crate block registration mismatch");
        require(BuiltInRegistries.ITEM.getKey(StructureContent.CRATE.get().asItem()).getPath().equals("crate"),
            "crate item is not the placeable crate");
        BlockEntity crateEntity = StructureContent.CRATE.get().newBlockEntity(BlockPos.ZERO,
            StructureContent.CRATE.get().defaultBlockState());
        require(crateEntity instanceof CrateBlockEntity, "crate did not create its own block entity");
        require(CrateBlockEntity.SLOT_COUNT == 27, "crate inventory is not 27 slots");

        SealingLockBlockEntity lock = new SealingLockBlockEntity(BlockPos.ZERO,
            StructureContent.SEALING_LOCK.get().defaultBlockState());
        lock.configureMarker("sealing_lock:locked");
        CompoundTag saved = new CompoundTag();
        lock.saveMarkerData(saved);
        SealingLockBlockEntity restored = new SealingLockBlockEntity(BlockPos.ZERO,
            StructureContent.SEALING_LOCK.get().defaultBlockState());
        restored.loadMarkerData(saved);
        require("locked".equals(restored.markerTarget()), "sealing lock marker target did not persist");
        require(!restored.isUnlocked(), "fresh sealing lock restored unlocked");
        restored.configureMarker("sealing_lock:unlocked");
        require(restored.isUnlocked(), "unlocked structure marker remained locked");

        IdolOfFadingBlockEntity idol = new IdolOfFadingBlockEntity(BlockPos.ZERO,
            EnergyBlocks.IDOL_OF_FADING.get().defaultBlockState());
        idol.setEnergy(idol.getMaxEnergy());
        require(idol.getContainedEnergy() == idol.getMaxEnergy(), "idol marker did not fill PE");
        require(EnergyBlocks.ENERGY_PEDESTALS.size() >= 4,
            "Omothol marker cannot select its four legacy pedestal tiers");
        ShoggothBiomassBlockEntity biomass = new ShoggothBiomassBlockEntity(BlockPos.ZERO,
            com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks.SHOGGOTH_BIOMASS.get().defaultBlockState());
        biomass.setInitialCooldown(99);
        require(!BaseBlocks.DEAD_TREE_LOG.get().defaultBlockState().isAir()
                && !BaseBlocks.DARKLANDS_OAK_LOG.get().defaultBlockState().isAir()
                && !BaseBlocks.DREADWOOD_LOG.get().defaultBlockState().isAir(),
            "graveyard tree marker lost one of its legacy tree species");

        System.out.println("RR_WORLD_STRUCTURE_CONTENT_OK crateSlots=27 markerHosts=5 lockTarget=locked treeSpecies=3");
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_WORLD_STRUCTURE_CONTENT_FAIL " + reason);
    }
}
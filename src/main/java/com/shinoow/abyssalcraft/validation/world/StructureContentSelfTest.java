package com.shinoow.abyssalcraft.validation.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.shinoow.abyssalcraft.content.block.structure.CrateBlockEntity;
import com.shinoow.abyssalcraft.content.block.structure.SealingLockBlockEntity;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;

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

        System.out.println("RR_WORLD_STRUCTURE_CONTENT_OK crateSlots=27 markerHosts=2 lockTarget=locked");
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_WORLD_STRUCTURE_CONTENT_FAIL " + reason);
    }
}
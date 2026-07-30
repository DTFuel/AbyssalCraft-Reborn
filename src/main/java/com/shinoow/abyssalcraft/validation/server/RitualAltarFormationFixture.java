package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.content.block.ritual.RitualAltarBlockEntity;
import com.shinoow.abyssalcraft.content.block.ritual.RitualAltarFormation;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualPedestalBlockEntity;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.knowledge.NecronomiconActions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;

/** Real-level regression for the Necronomicon create-altar action. */
public final class RitualAltarFormationFixture {

    private RitualAltarFormationFixture() {}

    public static void run(ServerLevel level) {
        run(level, Blocks.COBBLESTONE.defaultBlockState(), 0, "cobblestone");
    }

    public static void runAbyssalWasteland(ServerLevel level) {
        run(level, BaseBlocks.ABYSSAL_COBBLESTONE.get().defaultBlockState(), 1, "abyssal_cobblestone");
    }

    public static void runDreadlands(ServerLevel level) {
        run(level, BaseBlocks.DREADSTONE_COBBLESTONE.get().defaultBlockState(), 2, "dreadstone_cobblestone");
    }

    public static void runOmothol(ServerLevel level) {
        run(level, BaseBlocks.ETHAXIUM_BRICKS.get().defaultBlockState(), 3, "ethaxium_bricks");
    }

    public static void runDarkRealm(ServerLevel level) {
        run(level, Blocks.COBBLESTONE.defaultBlockState(), 4, "dark_realm_cobblestone");
    }

    private static void run(ServerLevel level, net.minecraft.world.level.block.state.BlockState material,
                            int bookType, String label) {
        int y = level.getMaxBuildHeight() - 2;
        BlockPos center = new BlockPos(520, y, 520);
        level.setBlockAndUpdate(center, material);
        for (BlockPos offset : RitualAltarFormation.PEDESTAL_OFFSETS) {
            level.setBlockAndUpdate(center.offset(offset), material);
        }
        try {
            require(RitualAltarFormation.canCreate(level, center, bookType),
                "Necronomicon did not recognize the " + label + " ring in " + level.dimension().location());
            require(RitualAltarFormation.canCreate(level, center, 4),
                "Abyssalnomicon did not recognize the lower-tier " + label + " ring");
            require(NecronomiconActions.execute(null, level, center, bookType, InteractionHand.MAIN_HAND)
                .consumesAction(), "Necronomicon action registry did not create the " + label + " altar");
            require(level.getBlockState(center).is(RitualBlocks.RITUAL_ALTAR.get())
                && level.getBlockEntity(center) instanceof RitualAltarBlockEntity,
                "center did not become a functional ritual altar");
            for (BlockPos offset : RitualAltarFormation.PEDESTAL_OFFSETS) {
                BlockPos pedestal = center.offset(offset);
                require(level.getBlockState(pedestal).is(RitualBlocks.RITUAL_PEDESTAL.get())
                    && level.getBlockEntity(pedestal) instanceof RitualPedestalBlockEntity,
                    "ring block did not become a functional ritual pedestal at " + offset);
            }
            System.out.printf("RR_RITUAL_ALTAR_FORMATION_OK dimension=%s material=%s pedestals=8 book=%d%n",
                level.dimension().location(), label, bookType);
        } finally {
            level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
            for (BlockPos offset : RitualAltarFormation.PEDESTAL_OFFSETS) {
                level.setBlockAndUpdate(center.offset(offset), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_RITUAL_ALTAR_FORMATION_FAIL " + reason);
    }
}
package com.shinoow.abyssalcraft.content.block;

import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

import com.shinoow.abyssalcraft.content.block.deco.DecoBehaviorSelfTest;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.ghoul.GhoulHeadBlocks;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.block.portal.PortalBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformers;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

public final class ContentSelfTest {

    private static final Map<String, ChatFormatting> COLORED_BLOCK_ITEMS = Map.ofEntries(
        Map.entry("abyssal_stone", ChatFormatting.BLUE),
        Map.entry("ethaxium", ChatFormatting.AQUA),
        Map.entry("abyssal_stone_brick", ChatFormatting.BLUE),
        Map.entry("chiseled_abyssal_stone_brick", ChatFormatting.BLUE),
        Map.entry("cracked_abyssal_stone_brick", ChatFormatting.BLUE),
        Map.entry("abyssal_stone_brick_stairs", ChatFormatting.BLUE),
        Map.entry("abyssal_stone_brick_fence", ChatFormatting.BLUE),
        Map.entry("block_of_abyssalnite", ChatFormatting.DARK_AQUA),
        Map.entry("block_of_refined_coralium", ChatFormatting.AQUA),
        Map.entry("block_of_dreadium", ChatFormatting.DARK_RED),
        Map.entry("block_of_ethaxium", ChatFormatting.AQUA),
        Map.entry("abyssal_stone_button", ChatFormatting.BLUE),
        Map.entry("abyssal_stone_pressure_plate", ChatFormatting.BLUE),
        Map.entry("ethaxium_bricks", ChatFormatting.AQUA),
        Map.entry("chiseled_ethaxium_brick", ChatFormatting.AQUA),
        Map.entry("cracked_ethaxium_brick", ChatFormatting.AQUA),
        Map.entry("ethaxium_pillar", ChatFormatting.AQUA),
        Map.entry("ethaxium_brick_stairs", ChatFormatting.AQUA),
        Map.entry("ethaxium_brick_fence", ChatFormatting.AQUA),
        Map.entry("dark_ethaxium_brick", ChatFormatting.DARK_RED),
        Map.entry("chiseled_dark_ethaxium_brick", ChatFormatting.DARK_RED),
        Map.entry("cracked_dark_ethaxium_brick", ChatFormatting.DARK_RED),
        Map.entry("dark_ethaxium_pillar", ChatFormatting.DARK_RED),
        Map.entry("dark_ethaxium_brick_stairs", ChatFormatting.DARK_RED),
        Map.entry("dark_ethaxium_brick_fence", ChatFormatting.DARK_RED),
        Map.entry("odb_core", ChatFormatting.DARK_RED));

    private ContentSelfTest() {}

    public static void run() {
        for (Map.Entry<String, ChatFormatting> entry : COLORED_BLOCK_ITEMS.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(ACRef.id(entry.getKey()));
            require(item != null && item != net.minecraft.world.item.Items.AIR,
                "missing colored block item: " + entry.getKey());
            Integer expected = entry.getValue().getColor();
            net.minecraft.network.chat.TextColor actualColor = new ItemStack(item).getHoverName().getStyle().getColor();
            require(actualColor != null, "block item has no name color: " + entry.getKey());
            int actual = actualColor.getValue();
            require(expected != null && actual == expected, "wrong block item color: " + entry.getKey());
        }
        requireShape(BaseBlocks.DARKSTONE.get(), 1.0D, 1.0D, 1.0D, "full block control");
        requireShape(DecoBlocks.DECORATIVE_CTHULHU_STATUE.get(), 0.5D, 1.0D, 0.5D, "statue");
        requireShape(DecoBlocks.MURAL.get(), 1.0D, 1.0D, 0.2D, "mural");
        requireShape(DecoBlocks.TOMBSTONE_STONE.get(), 0.8D, 1.0D, 0.4D, "tombstone");
        EnergyBlocks.ENERGY_COLLECTORS.forEach(block -> requireNonOccluding(block.get()));
        EnergyBlocks.ENERGY_CONTAINERS.forEach(block -> requireNonOccluding(block.get()));
        EnergyBlocks.ENERGY_PEDESTALS.forEach(block -> requireNonOccluding(block.get()));
        EnergyBlocks.ENERGY_RELAYS.forEach(block -> requireNonOccluding(block.get()));
        requireNonOccluding(EnergyBlocks.DEITY_STATUE.get());
        EnergyBlocks.DEITY_STATUES.forEach(block -> requireNonOccluding(block.get()));
        requireNonOccluding(EnergyBlocks.ENERGY_DEPOSITIONER.get());
        requireNonOccluding(EnergyBlocks.IDOL_OF_FADING.get());
        requireNonOccluding(EnergyBlocks.MONOLITH_PILLAR.get());
        GhoulHeadBlocks.ALL.forEach(block -> requireNonOccluding(block.get()));
        CrystalClusterBlocks.CLUSTERS.forEach(block -> requireNonOccluding(block.get()));
        CrystalClusterBlocks.MACHINE_COMPAT_CLUSTERS.forEach(block -> requireNonOccluding(block.get()));
        requireNonOccluding(DecoBlocks.DECORATIVE_CTHULHU_STATUE.get());
        requireNonOccluding(DecoBlocks.ABYSSAL_SAND_GLASS.get());
        requireNonOccluding(DecoBlocks.MURAL.get());
        requireNonOccluding(DecoBlocks.TOMBSTONE_STONE.get());
        requireNonOccluding(RitualBlocks.DREADLANDS_INFUSED_POWERSTONE.get());
        requireNonOccluding(RitualBlocks.ODB_CORE.get());
        requireNonOccluding(PortalBlocks.PORTAL_ANCHOR.get());
        requireNonOccluding(PortalBlocks.UNCHAINED_PORTAL_ANCHOR.get());
        requireNonOccluding(TransferContent.SPIRIT_ALTAR.get());
        requireNonOccluding(RendingPedestals.RENDING_PEDESTAL.get());
        requireNonOccluding(ResearchTables.RESEARCH_TABLE.get());
        requireNonOccluding(StateTransformers.STATE_TRANSFORMER.get());
        DecoBehaviorSelfTest.run();
        System.out.println("R1_CONTENT_SELF_TEST_OK nonOccludingCustomBlocks=ok");
    }

    private static void requireShape(Block block, double width, double height, double depth, String name) {
        AABB bounds = block.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO,
            CollisionContext.empty()).bounds();
        require(close(bounds.getXsize(), width) && close(bounds.getYsize(), height) && close(bounds.getZsize(), depth),
            name + " shape mismatch: " + bounds);
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) < 0.0001D;
    }

    private static void requireNonOccluding(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        require(!block.defaultBlockState().canOcclude(), "custom geometry occludes adjacent faces: " + id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
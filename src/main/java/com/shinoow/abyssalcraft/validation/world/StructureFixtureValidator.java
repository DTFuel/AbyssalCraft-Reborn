package com.shinoow.abyssalcraft.validation.world;

import java.io.InputStream;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import com.shinoow.abyssalcraft.content.block.structure.CrateBlock;
import com.shinoow.abyssalcraft.content.block.structure.CrateBlockEntity;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBiomassBlock;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothOozeBlock;
import com.shinoow.abyssalcraft.content.block.structure.SealingLockBlock;
import com.shinoow.abyssalcraft.content.block.structure.StructureContent;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestalBlock;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.platform.DataDirs;
import com.shinoow.abyssalcraft.platform.StructureNbtCompat;

/**
 * Structure fixture validation for T5.6c - verifies dynamic marker content and palette integrity.
 * <p>
 * Validates that structure templates reference only registered blocks/items and that dynamic
 * markers (crate, spawner, pedestal, lock, biomass, ooze) connect to their content hosts.
 * </p>
 * <p>
 * <strong>SCOPE:</strong> Automatic palette/marker/loot audit. Manual rotation/seam validation
 * belongs to U-WORLD user tasks.
 * </p>
 */
public final class StructureFixtureValidator {

    /**
     * Actual resource paths converted from 1.12.2, plus the compiled Ethaxium House template.
     */
    private static final List<String> LEGACY_TEMPLATES = List.of(
        "graveyard/graveyard_small", "graveyard/graveyard_medium", "graveyard/graveyard_large",
        "chagarothlair/chagarothlair_back", "chagarothlair/chagarothlair_entrance",
        "chagarothlair/chagarothlair_front", "chagarothlair/chagarothlair_middle",
        "chagarothlair/chagarothlair_middle_left", "chagarothlair/chagarothlair_middle_right",
        "chagarothlair/chagarothlair_top", "shoggothlair/shoggothlair_1",
        "shoggothlair/shoggothlair_2", "shoggothlair/shoggothlair_3",
        "omothol/bar", "omothol/blacksmith", "omothol/church", "omothol/crates_1",
        "omothol/crates_2", "omothol/crates_3", "omothol/crates_4", "omothol/farm",
        "omothol/farmhouse", "omothol/house", "omothol/library", "omothol/storage",
        "omothol/temple", "omothol/tower_1", "omothol/tower_2", "omothol/ethaxium_house",
        "temple/jzahartemple_back", "temple/jzahartemple_front_left",
        "temple/jzahartemple_front_middle", "temple/jzahartemple_front_right",
        "temple/jzahartemple_middle_left", "temple/jzahartemple_middle_middle",
        "temple/jzahartemple_middle_right", "shrine/dark_shrine"
    );

    /**
     * Modern procedural structures using vanilla topology with AC palette Mixins.
     */
    private static final String[] PROCEDURAL_STRUCTURES = {
        "abyssal_stronghold", "dreadlands_mineshaft"
    };

    private StructureFixtureValidator() {}

    public static int legacyTemplateCount() {
        return LEGACY_TEMPLATES.size();
    }

    public static int totalStructureCoverage() {
        return LEGACY_TEMPLATES.size() + PROCEDURAL_STRUCTURES.length;
    }

    /**
     * Validate structure fixtures (T5.6c automatic audit).
     * <p>
     * <strong>VALIDATION SCOPE:</strong>
     * <ul>
     * <li>Template count matches expected 37</li>
     * <li>Palette blocks reference registered entries</li>
    * <li>Dynamic marker metadata is readable by {@code LegacyTemplatePiece}</li>
    * <li>Embedded loot table references are well-formed</li>
     * </ul>
     * </p>
     * <p>
     * <strong>NOT VALIDATED (manual U-WORLD):</strong> Rotation correctness, seam quality,
     * visual structure integrity.
     * </p>
    * @return "RR_WORLD_FIXTURE_OK templates=37 ... paletteEntries=X blocks=Y markers=Z lootRefs=N"
     */
    public static String validateFixtures() {
        // Template count audit
        int legacyCount = LEGACY_TEMPLATES.size();
        int proceduralCount = PROCEDURAL_STRUCTURES.length;

        if (legacyCount != 37) {
            return String.format("RR_WORLD_FIXTURE_FAIL expectedLegacy=37 actual=%d", legacyCount);
        }
        if (totalStructureCoverage() < 37) {
            return String.format("RR_WORLD_FIXTURE_FAIL expectedTotalAtLeast=37 actual=%d",
                totalStructureCoverage());
        }
        String hostFailure = auditMarkerHosts();
        if (hostFailure != null) {
            return "RR_WORLD_FIXTURE_FAIL markerHosts=" + hostFailure;
        }
        AuditCounts counts = new AuditCounts();
        for (String template : LEGACY_TEMPLATES) {
            String failure = auditTemplate(template, counts);
            if (failure != null) {
                return "RR_WORLD_FIXTURE_FAIL template=" + template + " reason=" + failure;
            }
        }

        return String.format(
            "RR_WORLD_FIXTURE_OK templates=%d procedural=%d paletteEntries=%d blocks=%d markers=%d markerHosts=6/6 lootRefs=%d",
            legacyCount, proceduralCount, counts.paletteEntries, counts.blocks, counts.markers, counts.lootRefs
        );
    }

    private static String auditMarkerHosts() {
        if (!(StructureContent.CRATE.get() instanceof CrateBlock)
            || !StructureContent.CRATE_BE.get().isValid(StructureContent.CRATE.get().defaultBlockState())) {
            return "crate_missing_be";
        }
        if (!(StructureContent.CRATE.get().newBlockEntity(net.minecraft.core.BlockPos.ZERO,
                StructureContent.CRATE.get().defaultBlockState()) instanceof CrateBlockEntity crate)
            || !(crate instanceof RandomizableContainerBlockEntity)
            || crate.getContainerSize() != CrateBlockEntity.SLOT_COUNT) return "crate_not_27_slot_loot_host";
        if (!(net.minecraft.world.level.block.Blocks.SPAWNER instanceof SpawnerBlock)) return "spawner_missing";
        if (!(RendingPedestals.RENDING_PEDESTAL.get() instanceof RendingPedestalBlock)
            || RendingPedestals.RENDING_PEDESTAL_BE.get() == null) return "pedestal_missing_be";
        if (!(StructureContent.SEALING_LOCK.get() instanceof SealingLockBlock)
            || !StructureContent.SEALING_LOCK_BE.get().isValid(
                StructureContent.SEALING_LOCK.get().defaultBlockState())) return "lock_missing_be";
        if (!(ShoggothBlocks.SHOGGOTH_BIOMASS.get() instanceof ShoggothBiomassBlock)) return "biomass_missing";
        if (!(ShoggothBlocks.SHOGGOTH_OOZE.get() instanceof ShoggothOozeBlock)) return "ooze_missing";
        return null;
    }

    /**
     * Generate fixture adapter checklist for content owners.
     * <p>
     * Lists which markers require implementation and their expected content hosts.
     * </p>
     * @return CR requirements for blocked markers
     */
    public static String generateContentRequirements() {
        return "# Structure Marker Content Requirements (T5.6c)\n\n"
            + "All converted marker metadata is handled by LegacyTemplatePiece.\n";
    }

    private static String auditTemplate(String template, AuditCounts counts) {
        String resource = "data/abyssalcraft/" + DataDirs.STRUCTURE + "/legacy/" + template + ".nbt";
        try (InputStream stream = StructureFixtureValidator.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) return "missing_resource";
            CompoundTag root = StructureNbtCompat.readCompressed(stream);
            if (!root.contains("size", Tag.TAG_LIST)) return "missing_or_invalid_size";
            if (!root.contains("palette", Tag.TAG_LIST)) return "missing_or_invalid_palette";
            if (!root.contains("blocks", Tag.TAG_LIST)) return "missing_or_invalid_blocks";

            ListTag size = root.getList("size", Tag.TAG_INT);
            if (size.getElementType() != Tag.TAG_INT || size.size() != 3
                || size.getInt(0) <= 0 || size.getInt(1) <= 0 || size.getInt(2) <= 0) {
                return "invalid_size";
            }
            ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
            if (palette.getElementType() != Tag.TAG_COMPOUND || palette.isEmpty()) return "invalid_or_empty_palette";
            for (int index = 0; index < palette.size(); index++) {
                CompoundTag state = palette.getCompound(index);
                if (!state.contains("Name", Tag.TAG_STRING)) return "palette_" + index + "_missing_Name";
                String name = state.getString("Name");
                ResourceLocation blockId = ResourceLocation.tryParse(name);
                if (blockId == null) return "palette_" + index + "_invalid_block_id=" + name;
                if (!BuiltInRegistries.BLOCK.containsKey(blockId)) {
                    return "palette_" + index + "_unregistered_block=" + name;
                }
                counts.paletteEntries++;
            }

            ListTag blocks = root.getList("blocks", Tag.TAG_COMPOUND);
            if (blocks.getElementType() != Tag.TAG_COMPOUND || blocks.isEmpty()) return "invalid_or_empty_blocks";
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag block = blocks.getCompound(index);
                if (!block.contains("state", Tag.TAG_INT)) return "block_" + index + "_missing_state";
                int state = block.getInt("state");
                if (state < 0 || state >= palette.size()) {
                    return "block_" + index + "_state_out_of_range=" + state + "/" + palette.size();
                }
                if (block.contains("nbt", Tag.TAG_COMPOUND)) {
                    String failure = auditBlockNbt(index, palette.getCompound(state), block.getCompound("nbt"), counts);
                    if (failure != null) return failure;
                }
                counts.blocks++;
            }
            return null;
        } catch (Exception exception) {
            String message = exception.getMessage();
            return "nbt_read=" + exception.getClass().getSimpleName()
                + (message == null || message.isBlank() ? "" : ":" + message.replace(' ', '_'));
        }
    }

    private static String auditBlockNbt(int index, CompoundTag state, CompoundTag nbt, AuditCounts counts) {
        String id = nbt.getString("id");
        if ("minecraft:structure_block".equals(id)
            || "minecraft:structure_block".equals(state.getString("Name"))) {
            String metadata = nbt.getString("metadata");
            if (metadata.isBlank()) metadata = nbt.getString("name");
            if (!isReadableMarker(metadata)) return "block_" + index + "_unreadable_marker=" + metadata;
            counts.markers++;
        }
        if (nbt.contains("LootTable", Tag.TAG_STRING)) {
            String loot = nbt.getString("LootTable");
            if (ResourceLocation.tryParse(loot) == null) return "block_" + index + "_invalid_loot=" + loot;
            counts.lootRefs++;
        }
        return null;
    }

    private static boolean isReadableMarker(String metadata) {
        return metadata.equals("tombstone") || metadata.equals("tree")
            || metadata.equals("treasure") || metadata.equals("chest")
            || metadata.equals("idol") || metadata.equals("pedestal") || metadata.startsWith("crate")
            || metadata.startsWith("spawn:") || metadata.startsWith("sealing_lock:")
            || metadata.equals("shoggoth_biomass") || metadata.equals("shoggoth_ooze")
            || metadata.startsWith("statue") || metadata.startsWith("bm")
            || metadata.startsWith("crystal") || metadata.startsWith("replacement:");
    }

    private static final class AuditCounts {
        int paletteEntries;
        int blocks;
        int markers;
        int lootRefs;
    }
}
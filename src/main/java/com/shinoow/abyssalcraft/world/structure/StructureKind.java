package com.shinoow.abyssalcraft.world.structure;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * The AbyssalCraft programmatic-structure variants (owned by PG-5 / Stage G1).
 *
 * <p>Following the entity/dimension enum-collapse pattern (PD-4 {@code AnimalKind}), one
 * {@link ACStructure} class + one {@code StructureType} serves every code-built structure; the variant
 * is carried by this {@code kind} field (serialized into the structure JSON) and drives the block
 * palette + chest loot table in {@link ACStructurePiece}. These are the R2 "programmatic StructureType"
 * structures -- the 36 1.12.2 {@code .nbt} template structures (graveyard/chagaroth_lair/omothol city/
 * jzahar_temple/...) need binary format conversion + palette remap (in-game structure block) and are
 * deferred to a jigsaw pass.
 */
public enum StructureKind implements StringRepresentable {
    /** Small graveyard of tombstone pillars + a loot chest (1.12.2 {@code StructureGraveyard}). */
    GRAVEYARD("graveyard", "abyssalcraft:darkstone_brick", "graveyard"),
    /** Abyssal ruin of brick walls + a loot chest (1.12.2 {@code abyss/Abyruin}). */
    ABYRUIN("abyruin", "abyssalcraft:abyssal_stone_brick", "abyruin"),
    /** Dark shrine platform + a loot chest (1.12.2 {@code overworld/StructureDarkShrine}). */
    DARK_SHRINE("dark_shrine", "abyssalcraft:darkstone_brick", "dark_shrine"),
    /** One of the three converted Shoggoth pit templates. */
    SHOGGOTH_PIT("shoggoth_pit", "abyssalcraft:monolith_stone", "graveyard"),
    OMOTHOL_CITY("omothol_city", "abyssalcraft:omothol_stone", "omothol/house"),
    OMOTHOL_TEMPLE("omothol_temple", "abyssalcraft:omothol_stone", "omothol/house"),
    OMOTHOL_TOWER("omothol_tower", "abyssalcraft:omothol_stone", "omothol/house"),
    OMOTHOL_STORAGE("omothol_storage", "abyssalcraft:omothol_stone", "omothol/storage_junk"),
    ETHAXIUM_HOUSE("ethaxium_house", "abyssalcraft:ethaxium_bricks", "omothol/house"),
    CHAGAROTH_LAIR("chagaroth_lair", "abyssalcraft:dreadstone_brick", "chagaroth_lair"),
    JZAHAR_TEMPLE("jzahar_temple", "abyssalcraft:dark_ethaxium_brick", "omothol/house");

    public static final Codec<StructureKind> CODEC = StringRepresentable.fromEnum(StructureKind::values);

    private final String serializedName;
    private final ResourceLocation blockId;
    private final ResourceLocation lootTable;

    StructureKind(String serializedName, String blockId, String lootName) {
        this.serializedName = serializedName;
        this.blockId = ACRef.parse(blockId);
        this.lootTable = ACRef.id("chests/" + lootName);
    }

    /** The primary building block (resolved lazily against the frozen block registry). */
    public BlockState block() {
        return BuiltInRegistries.BLOCK.get(blockId).defaultBlockState();
    }

    /** The chest loot table id ({@code abyssalcraft:chests/<name>}). */
    public ResourceLocation lootTable() {
        return lootTable;
    }

    public boolean usesLegacyTemplate() {
        return this != ABYRUIN;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}

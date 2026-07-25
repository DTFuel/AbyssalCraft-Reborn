package com.shinoow.abyssalcraft.platform;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

//? if >=1.21 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?}

/**
 * Compat: programmatic {@code StructureType} codec + chest loot (vanilla axis, owned by PG-5).
 *
 * <p>Two 1.20.1 &harr; 1.21 divergences for code-built structures live here so the business
 * {@code world/structure/**} classes stay fork-free:
 * <ol>
 *   <li><b>StructureType codec.</b> {@code StructureType<S>} exposes {@code Codec<S>} on 1.20.1 but
 *       {@code MapCodec<S>} on 1.21 (the {@code Codec}&harr;{@code MapCodec} registry-value split flagged
 *       by Research R3). The structure builds its codec as a {@code MapCodec} on both versions (via
 *       {@code RecordCodecBuilder.mapCodec}); this wrapper adapts it to the loader's {@code StructureType}
 *       SAM.</li>
 *   <li><b>Chest loot.</b> {@code RandomizableContainerBlockEntity.setLootTable} takes a
 *       {@code ResourceLocation} on 1.20.1 but a {@code ResourceKey<LootTable>} on 1.21.</li>
 * </ol>
 */
public final class StructureCompat {

    private StructureCompat() {}

    /** Adapt a version-neutral {@code MapCodec} to the loader's {@code StructureType} SAM. */
    public static <S extends Structure> StructureType<S> structureType(MapCodec<S> codec) {
        //? if >=1.21 {
        /*return () -> codec;
        *///?} else {
        return () -> codec.codec();
        //?}
    }

    /** Attach a loot table to a chest {@code BlockEntity} placed during structure generation. */
    public static void setChestLoot(WorldGenLevel level, BlockPos pos, ResourceLocation lootId, long seed) {
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
            //? if >=1.21 {
            /*chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootId), seed);
            *///?} else {
            chest.setLootTable(lootId, seed);
            //?}
        }
    }
}

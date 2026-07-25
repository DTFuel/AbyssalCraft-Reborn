package com.shinoow.abyssalcraft.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import com.shinoow.abyssalcraft.world.structure.LegacyStructurePlacementContext;

//? if >=1.21 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?} else {
//?}

/** Loot-key compatibility for structures that retain vanilla topology with an AC theme. */
public final class StructureLootCompat {

    private StructureLootCompat() {}

    //? if >=1.21 {
    /*public static ResourceKey<LootTable> remap(ResourceKey<LootTable> lootTable) {
        ResourceLocation target = targetPath(lootTable);
        return target == null ? lootTable : ResourceKey.create(Registries.LOOT_TABLE, target);
    }

    private static ResourceLocation targetPath(ResourceKey<LootTable> lootTable) {
    *///?} else {
    public static ResourceLocation remap(ResourceLocation lootTable) {
        ResourceLocation target = targetPath(lootTable);
        return target == null ? lootTable : target;
    }

    private static ResourceLocation targetPath(ResourceLocation lootTable) {
    //?}
        return switch (LegacyStructurePlacementContext.active()) {
            case DREADLANDS_MINESHAFT -> lootTable.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
                ? ACRef.id("chests/mineshaft") : null;
            case ABYSSAL_STRONGHOLD -> {
                if (lootTable.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR)) {
                    yield ACRef.id("chests/stronghold_corridor");
                }
                if (lootTable.equals(BuiltInLootTables.STRONGHOLD_CROSSING)) {
                    yield ACRef.id("chests/stronghold_crossing");
                }
                yield null;
            }
            case NONE -> null;
        };
    }
}
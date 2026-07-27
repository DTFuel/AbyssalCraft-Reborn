package com.shinoow.abyssalcraft.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;

//? if >=1.21 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
*///?}

/** Version bridge for querying the live server's reloadable loot data. */
public final class ServerDataCompat {

    private ServerDataCompat() {}

    public static LootTable lootTable(MinecraftServer server, ResourceLocation id) {
        //? if >=1.21 {
        /*return server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
        *///?} else {
        return server.getLootData().getLootTable(id);
        //?}
    }
}
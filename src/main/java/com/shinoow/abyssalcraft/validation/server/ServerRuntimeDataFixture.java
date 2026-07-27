package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.content.entity.behavior.EntityLootAudit;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.platform.ServerDataCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootTable;

/** Runtime closure over the live RecipeManager, loot data and dynamic registries. */
public final class ServerRuntimeDataFixture {

    private ServerRuntimeDataFixture() {}

    public static void run(MinecraftServer server) {
        ServerLevel level = server.overworld();
        int recipes = DataRecipeCompat.entriesOfType(level, ModRecipes.CRYSTALLIZATION.get()).size()
            + DataRecipeCompat.entriesOfType(level, ModRecipes.MATERIALIZATION.get()).size()
            + DataRecipeCompat.entriesOfType(level, ModRecipes.TRANSMUTATION.get()).size();
        require(recipes == 27, "live RecipeManager machine recipe count=" + recipes);

        int legacyLoot = EntityLootAudit.entries().size();
        int logicalLoot = EntityLootAudit.logicalTables().size();
        require(legacyLoot == 69, "legacy entity loot catalog count=" + legacyLoot);
        require(logicalLoot == 97, "logical entity loot count=" + logicalLoot);
        for (String table : EntityLootAudit.logicalTables()) {
            LootTable resolved = ServerDataCompat.lootTable(server, ACRef.id("entities/" + table));
            require(resolved != LootTable.EMPTY, "live loot data is missing entities/" + table);
        }
        require(level.registryAccess() == server.registryAccess(), "ServerLevel registry access is not server-owned");
        System.out.println("RR_SERVER_RUNTIME_DATA_OK recipes=27 legacyLoot=69 resolvedLoot=97 registries=live");
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL " + reason);
    }
}
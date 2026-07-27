package com.shinoow.abyssalcraft.validation.server;

import java.util.HashSet;
import java.util.Set;

import com.shinoow.abyssalcraft.content.entity.behavior.EntityLootAudit;
import com.shinoow.abyssalcraft.data.gen.LegacyMachineRecipeCatalog;
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
        Set<String> recipes = new HashSet<>();
        DataRecipeCompat.entriesOfType(level, ModRecipes.CRYSTALLIZATION.get())
            .forEach(entry -> recipes.add(entry.id().toString()));
        DataRecipeCompat.entriesOfType(level, ModRecipes.MATERIALIZATION.get())
            .forEach(entry -> recipes.add(entry.id().toString()));
        DataRecipeCompat.entriesOfType(level, ModRecipes.TRANSMUTATION.get())
            .forEach(entry -> recipes.add(entry.id().toString()));
        Set<String> expectedRecipes = LegacyMachineRecipeCatalog.entries().stream()
            .filter(entry -> entry.status() == LegacyMachineRecipeCatalog.Status.MIGRATED
                || entry.status() == LegacyMachineRecipeCatalog.Status.REPLACED)
            .map(LegacyMachineRecipeCatalog.Entry::recipeId)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        require(recipes.equals(expectedRecipes), "live RecipeManager machine recipe closure=" + recipes.size()
            + " expected=" + expectedRecipes.size());

        int legacyLoot = EntityLootAudit.entries().size();
        int logicalLoot = EntityLootAudit.logicalTables().size();
        require(legacyLoot == 69, "legacy entity loot catalog count=" + legacyLoot);
        require(logicalLoot == 97, "logical entity loot count=" + logicalLoot);
        for (String table : EntityLootAudit.logicalTables()) {
            LootTable resolved = ServerDataCompat.lootTable(server, ACRef.id("entities/" + table));
            require(resolved != LootTable.EMPTY, "live loot data is missing entities/" + table);
        }
        require(level.registryAccess() == server.registryAccess(), "ServerLevel registry access is not server-owned");
        System.out.println("RR_SERVER_RUNTIME_DATA_OK recipes=" + recipes.size()
            + " legacyLoot=69 resolvedLoot=97 registries=live");
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL " + reason);
    }
}
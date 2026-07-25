package com.shinoow.abyssalcraft.data.gen;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStands;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.platform.MenuHostCapabilityCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.registry.ModRegistries;

/** Cross-relay invariants for the implementation-side R2 Gate. */
public final class R2GateSelfTest {

    private R2GateSelfTest() {}

    public static void run() {
        Set<ModRegistrar<?>> unique = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ModRegistrar<?> registrar : ModRegistries.ALL) {
            require(unique.add(registrar), "ModRegistries.ALL contains a duplicate registrar instance");
        }

        checkRegistrar("research blocks", ResearchTables.BLOCKS, 1);
        checkRegistrar("research items", ResearchTables.ITEMS, 1);
        checkRegistrar("research block entities", ResearchTables.BLOCK_ENTITIES, 1);
        checkRegistrar("research menus", ResearchTables.MENUS, 1);
        checkRegistrar("brewing blocks", BrewingStands.BLOCKS, 1);
        checkRegistrar("brewing items", BrewingStands.ITEMS, 1);
        checkRegistrar("brewing block entities", BrewingStands.BLOCK_ENTITIES, 1);
        checkRegistrar("brewing menus", BrewingStands.MENUS, 1);
        checkRegistrar("transfer blocks", TransferContent.BLOCKS, 1);
        checkRegistrar("transfer items", TransferContent.ITEMS, 2);
        checkRegistrar("transfer block entities", TransferContent.BLOCK_ENTITIES, 1);
        checkRegistrar("transfer menus", TransferContent.MENUS, 1);

        requireId(BuiltInRegistries.BLOCK, ResearchTables.RESEARCH_TABLE.get(), ACRef.id("research_table"));
        requireId(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResearchTables.RESEARCH_TABLE_BE.get(),
            ACRef.id("research_table"));
        requireId(BuiltInRegistries.MENU, ResearchTables.RESEARCH_TABLE_MENU.get(), ACRef.id("research_table"));
        requireId(BuiltInRegistries.BLOCK, BrewingStands.BREWING_STAND.get(),
            ACRef.id("sequential_brewing_stand"));
        requireId(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewingStands.BREWING_STAND_BE.get(),
            ACRef.id("sequential_brewing_stand"));
        requireId(BuiltInRegistries.MENU, BrewingStands.BREWING_STAND_MENU.get(),
            ACRef.id("sequential_brewing_stand"));
        requireId(BuiltInRegistries.ITEM, TransferContent.SPIRIT_TABLET.get(), ACRef.id("spirit_tablet"));
        requireId(BuiltInRegistries.BLOCK, TransferContent.SPIRIT_ALTAR.get(), ACRef.id("spirit_altar"));
        requireId(BuiltInRegistries.BLOCK_ENTITY_TYPE, TransferContent.SPIRIT_ALTAR_BE.get(),
            ACRef.id("spirit_altar"));
        requireId(BuiltInRegistries.MENU, TransferContent.SPIRIT_TABLET_MENU.get(), ACRef.id("spirit_tablet"));

        require(MenuHostCapabilityCompat.isAttached(), "MenuHostCapabilityCompat was not attached by mod init");
        require(ItemTransferAttachmentCompat.isAttached(),
            "ItemTransferAttachmentCompat was not attached by mod init");
        System.out.printf("R2_GATE_SELF_TEST_OK registrars=%d r2Registrars=12 menuHosts=3%n",
            ModRegistries.ALL.size());
    }

    private static void checkRegistrar(String label, ModRegistrar<?> registrar, int entries) {
        long mounts = ModRegistries.ALL.stream().filter(candidate -> candidate == registrar).count();
        require(mounts == 1, label + " registrar mount count changed: " + mounts);
        require(registrar.entries().size() == entries,
            label + " entry count changed: " + registrar.entries().size() + "/" + entries);
    }

    private static <T> void requireId(Registry<T> registry, T value, ResourceLocation expected) {
        ResourceLocation actual = registry.getKey(value);
        require(expected.equals(actual), "registry id changed: expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
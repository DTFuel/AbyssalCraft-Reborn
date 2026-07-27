package com.shinoow.abyssalcraft.system.command;

import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;

import net.minecraft.nbt.CompoundTag;

/** Permanent pure-state invariant for {@code /acunlockallknowledge}. */
public final class CommandSelfTest {

    private CommandSelfTest() {}

    public static void run() {
        NecroData data = new NecroData(new CompoundTag());
        require(ACCommands.toggleAllKnowledge(data) && data.hasUnlockedAllKnowledge(),
            "first command toggle did not unlock knowledge");
        require(!ACCommands.toggleAllKnowledge(data) && !data.hasUnlockedAllKnowledge(),
            "second command toggle did not re-lock knowledge");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("RR-ADV-API command self-test failed: " + message);
        }
    }
}
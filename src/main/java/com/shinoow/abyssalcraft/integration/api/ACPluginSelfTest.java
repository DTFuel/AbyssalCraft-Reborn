package com.shinoow.abyssalcraft.integration.api;

/** Permanent isolated lifecycle invariants for the public plugin API. */
public final class ACPluginSelfTest {

    private ACPluginSelfTest() {}

    public static void run() {
        ACPluginRegistry.selfTest();
    }
}
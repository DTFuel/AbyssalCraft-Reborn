package com.shinoow.abyssalcraft.platform;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

//? if forge {
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
//?} else {
/*import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
*///?}

/**
 * Compat: datagen entry point (loader axis - only the GatherDataEvent/IEventBus import forks).
 *
 * <p>The {@code GatherDataEvent} method surface is identical across Forge and NeoForge (only the
 * package differs), so {@link #register}'s body is shared. Business datagen code
 * ({@code data/ACDataGenerators}) receives a neutral {@link Gen} and never touches loader API,
 * keeping the datagen relay free of {@code //?} forks.
 */
public final class DataGenCompat {

    private DataGenCompat() {}

    /** Loader-neutral datagen context extracted from the loader-specific {@code GatherDataEvent}. */
    public static final class Gen {
        public final DataGenerator generator;
        public final PackOutput packOutput;
        public final ExistingFileHelper existingFiles;
        public final CompletableFuture<HolderLookup.Provider> lookup;
        public final boolean includeServer;
        public final boolean includeClient;

        Gen(DataGenerator generator, PackOutput packOutput, ExistingFileHelper existingFiles,
                CompletableFuture<HolderLookup.Provider> lookup, boolean includeServer, boolean includeClient) {
            this.generator = generator;
            this.packOutput = packOutput;
            this.existingFiles = existingFiles;
            this.lookup = lookup;
            this.includeServer = includeServer;
            this.includeClient = includeClient;
        }
    }

    /** Attach the datagen listener to the MOD bus; {@code action} runs when {@code runData} fires the event. */
    public static void register(IEventBus modBus, Consumer<Gen> action) {
        modBus.addListener((GatherDataEvent event) -> action.accept(new Gen(
            event.getGenerator(), event.getGenerator().getPackOutput(), event.getExistingFileHelper(),
            event.getLookupProvider(), event.includeServer(), event.includeClient())));
    }
}

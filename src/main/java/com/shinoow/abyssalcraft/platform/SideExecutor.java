package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;

//? if forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
*///?}

/**
 * Compat: physical side dispatch (loader axis).
 *
 * <p>Both loaders expose {@code FMLEnvironment.dist} but from different packages.
 * {@link #runWhenClient(Supplier)} defers instantiation of client-only code so it is never
 * class-loaded on a dedicated server.
 */
public final class SideExecutor {

    private SideExecutor() {}

    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    /** Run client-only code on the physical client; the supplier is only resolved there. */
    public static void runWhenClient(Supplier<Runnable> clientTask) {
        if (isClient()) {
            clientTask.get().run();
        }
    }
}

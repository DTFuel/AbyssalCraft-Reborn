package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

/**
 * Compat: DeferredRegister wrapper (loader axis).
 *
 * <p>Only the {@code DeferredRegister}/{@code IEventBus} import differs across loaders; the
 * create/register/attach surface is identical. Mod code holds a {@code ModRegistrar} instead of a
 * loader-specific {@code DeferredRegister}, keeping business code free of forked imports.
 *
 * @param <T> registry element type
 */
public final class ModRegistrar<T> {

    private final DeferredRegister<T> delegate;
    private final List<Supplier<? extends T>> entries = new ArrayList<>();

    private ModRegistrar(DeferredRegister<T> delegate) {
        this.delegate = delegate;
    }

    /** Create a registrar for a vanilla registry key, e.g. {@code Registries.BLOCK}. */
    public static <T> ModRegistrar<T> of(ResourceKey<? extends Registry<T>> registry, String modid) {
        return new ModRegistrar<>(DeferredRegister.create(registry, modid));
    }

    /** Register an entry; the returned Supplier resolves after registration completes. */
    public <U extends T> Supplier<U> register(String name, Supplier<? extends U> factory) {
        Supplier<U> registered = delegate.register(name, factory);
        entries.add(registered);
        return registered;
    }

    /**
     * Every entry registered through this wrapper, in registration order. Lets the creative-tab relay
     * ({@code registry/ModCreativeTabs}) fill category tabs without each content module exposing its
     * own list. Loader-agnostic (plain suppliers); read-only view.
     */
    public List<Supplier<? extends T>> entries() {
        return Collections.unmodifiableList(entries);
    }

    /** Attach to the MOD event bus (called once, from the registry aggregator). */
    public void attach(IEventBus modBus) {
        delegate.register(modBus);
    }
}

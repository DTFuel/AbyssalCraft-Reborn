package com.shinoow.abyssalcraft.integration.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** Service discovery, lifecycle validation, and immutable runtime state for {@link IACPlugin}. */
public final class ACPluginRegistry {

    private static final Object LOCK = new Object();
    private static final Map<ResourceLocation, IACPlugin> PENDING = new LinkedHashMap<>();
    private static final MutableExtensions LEGACY = new MutableExtensions();

    private static volatile Snapshot snapshot = Snapshot.EMPTY;
    private static boolean discovered;
    private static boolean publishing;
    private static boolean published;

    private ACPluginRegistry() {}

    /** Discover providers declared in {@code META-INF/services/...IACPlugin}. */
    public static void discover() {
        synchronized (LOCK) {
            if (discovered) {
                return;
            }
            discovered = true;
        }

        ServiceLoader<IACPlugin> loader = ServiceLoader.load(IACPlugin.class,
            Thread.currentThread().getContextClassLoader());
        Iterator<ServiceLoader.Provider<IACPlugin>> providers = loader.stream().iterator();
        while (true) {
            ServiceLoader.Provider<IACPlugin> provider;
            try {
                if (!providers.hasNext()) {
                    break;
                }
                provider = providers.next();
            } catch (ServiceConfigurationError error) {
                AbyssalCraft.LOGGER.error("Unable to inspect an AbyssalCraft plugin provider", error);
                continue;
            }
            try {
                registerDiscovered(provider.get());
            } catch (ServiceConfigurationError | RuntimeException error) {
                AbyssalCraft.LOGGER.error("Unable to load AbyssalCraft plugin provider {}", provider.type(), error);
            }
        }
    }

    /** Explicit alternative to ServiceLoader discovery. Must be called before the first server starts. */
    public static void register(IACPlugin plugin) {
        register(plugin, false);
    }

    private static void registerDiscovered(IACPlugin plugin) {
        register(plugin, true);
    }

    private static void register(IACPlugin plugin, boolean discoveredProvider) {
        Objects.requireNonNull(plugin, "plugin");
        ResourceLocation id = Objects.requireNonNull(plugin.id(), "plugin id");
        synchronized (LOCK) {
            ensureOpen();
            IACPlugin previous = PENDING.putIfAbsent(id, plugin);
            if (previous != null) {
                String message = "Duplicate AbyssalCraft plugin id " + id;
                if (discoveredProvider) {
                    AbyssalCraft.LOGGER.error(message);
                    return;
                }
                throw new IllegalArgumentException(message);
            }
        }
    }

    /** Accept one of the five retained Forge IMC entity extensions. */
    public static void registerLegacy(String key, ResourceLocation entityId) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(entityId, "entityId");
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            throw new IllegalArgumentException("Unknown entity type " + entityId);
        }
        synchronized (LOCK) {
            ensureOpen();
            switch (key) {
                case "shoggothFood" -> LEGACY.shoggothFoods.add(entityId);
                case "addDreadPlagueImmunity" -> LEGACY.dreadImmunity.add(entityId);
                case "addDreadPlagueCarrier" -> LEGACY.dreadCarriers.add(entityId);
                case "addCoraliumPlagueImmunity" -> LEGACY.coraliumImmunity.add(entityId);
                case "addCoraliumPlagueCarrier" -> LEGACY.coraliumCarriers.add(entityId);
                default -> throw new IllegalArgumentException("Unsupported legacy IMC key " + key);
            }
        }
    }

    /** Publish a deterministic immutable snapshot. Idempotent across repeated integrated-server starts. */
    public static void publish() {
        List<IACPlugin> plugins;
        MutableExtensions extensions;
        synchronized (LOCK) {
            if (published) {
                return;
            }
            ensureOpen();
            publishing = true;
            plugins = new ArrayList<>(PENDING.values());
            plugins.sort(Comparator.comparing(plugin -> plugin.id().toString()));
            extensions = LEGACY.copy();
        }

        extensions = applyPlugins(plugins, extensions, true);

        Snapshot result = extensions.snapshot();
        synchronized (LOCK) {
            snapshot = result;
            published = true;
            publishing = false;
        }
        String pluginIds = plugins.stream().map(plugin -> plugin.id().toString())
            .collect(java.util.stream.Collectors.joining(","));
        AbyssalCraft.LOGGER.info(
            "Published {} AbyssalCraft plugins [{}]: shoggothFood={} dreadImmunity={} dreadCarriers={} coraliumImmunity={} coraliumCarriers={}",
            plugins.size(), pluginIds, result.shoggothFoods.size(), result.dreadImmunity.size(),
            result.dreadCarriers.size(), result.coraliumImmunity.size(), result.coraliumCarriers.size());
    }

    public static boolean isPublished() {
        synchronized (LOCK) {
            return published;
        }
    }

    public static boolean isShoggothFood(EntityType<?> type) {
        return snapshot.shoggothFoods.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isDreadImmune(ResourceLocation entityId) {
        return snapshot.dreadImmunity.contains(entityId) || snapshot.dreadCarriers.contains(entityId);
    }

    public static boolean isDreadCarrier(ResourceLocation entityId) {
        return snapshot.dreadCarriers.contains(entityId);
    }

    public static boolean isCoraliumImmune(ResourceLocation entityId) {
        return snapshot.coraliumImmunity.contains(entityId) || snapshot.coraliumCarriers.contains(entityId);
    }

    public static boolean isCoraliumCarrier(ResourceLocation entityId) {
        return snapshot.coraliumCarriers.contains(entityId);
    }

    static void selfTest() {
        List<IACPlugin> plugins = List.of(
            testPlugin("valid", IACPlugin.API_VERSION, context -> {
                context.registerShoggothFood(EntityType.COW);
                context.registerDreadPlagueCarrier(EntityType.PIG);
                context.registerCoraliumPlagueImmunity(EntityType.SHEEP);
            }),
            testPlugin("wrong_version", IACPlugin.API_VERSION + 1,
                context -> context.registerDreadPlagueImmunity(EntityType.CHICKEN)),
            testPlugin("failing", IACPlugin.API_VERSION, context -> {
                context.registerCoraliumPlagueCarrier(EntityType.RABBIT);
                throw new IllegalStateException("expected test failure");
            }));

        Snapshot tested = applyPlugins(plugins, new MutableExtensions(), false).snapshot();
        require(tested.shoggothFoods.contains(ACRef.vanilla("cow")), "valid shoggoth food missing");
        require(tested.dreadCarriers.contains(ACRef.vanilla("pig")), "valid dread carrier missing");
        require(tested.isDreadImmune(ACRef.vanilla("pig")), "carrier is not immune");
        require(tested.coraliumImmunity.contains(ACRef.vanilla("sheep")), "valid immunity missing");
        require(!tested.dreadImmunity.contains(ACRef.vanilla("chicken")), "wrong API version was applied");
        require(!tested.coraliumCarriers.contains(ACRef.vanilla("rabbit")), "failed plugin was not rolled back");

        MutableContext context = new MutableContext(new MutableExtensions());
        context.close();
        boolean rejected = false;
        try {
            context.registerShoggothFood(EntityType.COW);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "closed plugin context accepted a registration");
    }

    private static void ensureOpen() {
        if (publishing || published) {
            throw new IllegalStateException("AbyssalCraft plugin registration is already closed");
        }
    }

    private static ResourceLocation entityId(EntityType<? extends LivingEntity> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(Objects.requireNonNull(type, "entityType"));
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            throw new IllegalArgumentException("Unregistered entity type " + type);
        }
        return id;
    }

    private static MutableExtensions applyPlugins(List<IACPlugin> plugins, MutableExtensions initial,
            boolean logFailures) {
        MutableExtensions extensions = initial;
        for (IACPlugin plugin : plugins.stream()
                .sorted(Comparator.comparing(value -> value.id().toString())).toList()) {
            if (plugin.apiVersion() != IACPlugin.API_VERSION) {
                if (logFailures) {
                    AbyssalCraft.LOGGER.error("Skipping AbyssalCraft plugin {}: API version {} != {}",
                        plugin.id(), plugin.apiVersion(), IACPlugin.API_VERSION);
                }
                continue;
            }
            MutableExtensions candidate = extensions.copy();
            MutableContext context = new MutableContext(candidate);
            try {
                plugin.register(context);
                context.close();
                extensions = candidate;
            } catch (RuntimeException error) {
                context.close();
                if (logFailures) {
                    AbyssalCraft.LOGGER.error("Skipping failed AbyssalCraft plugin {}", plugin.id(), error);
                }
            }
        }
        return extensions;
    }

    private static IACPlugin testPlugin(String path, int version,
            java.util.function.Consumer<ACPluginContext> registration) {
        return new IACPlugin() {
            @Override
            public ResourceLocation id() {
                return ACRef.id("self_test/" + path);
            }

            @Override
            public int apiVersion() {
                return version;
            }

            @Override
            public void register(ACPluginContext context) {
                registration.accept(context);
            }
        };
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("RR-ADV-API plugin self-test failed: " + message);
        }
    }

    private static final class MutableContext implements ACPluginContext {
        private final MutableExtensions extensions;
        private boolean open = true;

        private MutableContext(MutableExtensions extensions) {
            this.extensions = extensions;
        }

        @Override
        public void registerShoggothFood(EntityType<? extends LivingEntity> type) {
            requireOpen();
            extensions.shoggothFoods.add(entityId(type));
        }

        @Override
        public void registerDreadPlagueImmunity(EntityType<? extends LivingEntity> type) {
            requireOpen();
            extensions.dreadImmunity.add(entityId(type));
        }

        @Override
        public void registerDreadPlagueCarrier(EntityType<? extends LivingEntity> type) {
            requireOpen();
            extensions.dreadCarriers.add(entityId(type));
        }

        @Override
        public void registerCoraliumPlagueImmunity(EntityType<? extends LivingEntity> type) {
            requireOpen();
            extensions.coraliumImmunity.add(entityId(type));
        }

        @Override
        public void registerCoraliumPlagueCarrier(EntityType<? extends LivingEntity> type) {
            requireOpen();
            extensions.coraliumCarriers.add(entityId(type));
        }

        private void close() {
            open = false;
        }

        private void requireOpen() {
            if (!open) {
                throw new IllegalStateException("Plugin context is no longer writable");
            }
        }
    }

    private static final class MutableExtensions {
        private final Set<ResourceLocation> shoggothFoods = new LinkedHashSet<>();
        private final Set<ResourceLocation> dreadImmunity = new LinkedHashSet<>();
        private final Set<ResourceLocation> dreadCarriers = new LinkedHashSet<>();
        private final Set<ResourceLocation> coraliumImmunity = new LinkedHashSet<>();
        private final Set<ResourceLocation> coraliumCarriers = new LinkedHashSet<>();

        private MutableExtensions copy() {
            MutableExtensions copy = new MutableExtensions();
            copy.shoggothFoods.addAll(shoggothFoods);
            copy.dreadImmunity.addAll(dreadImmunity);
            copy.dreadCarriers.addAll(dreadCarriers);
            copy.coraliumImmunity.addAll(coraliumImmunity);
            copy.coraliumCarriers.addAll(coraliumCarriers);
            return copy;
        }

        private Snapshot snapshot() {
            return new Snapshot(immutable(shoggothFoods), immutable(dreadImmunity), immutable(dreadCarriers),
                immutable(coraliumImmunity), immutable(coraliumCarriers));
        }

        private static Set<ResourceLocation> immutable(Set<ResourceLocation> source) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(source));
        }
    }

    private record Snapshot(Set<ResourceLocation> shoggothFoods, Set<ResourceLocation> dreadImmunity,
                            Set<ResourceLocation> dreadCarriers, Set<ResourceLocation> coraliumImmunity,
                            Set<ResourceLocation> coraliumCarriers) {
        private static final Snapshot EMPTY = new MutableExtensions().snapshot();

        private int extensionCount() {
            return shoggothFoods.size() + dreadImmunity.size() + dreadCarriers.size()
                + coraliumImmunity.size() + coraliumCarriers.size();
        }

        private boolean isDreadImmune(ResourceLocation id) {
            return dreadImmunity.contains(id) || dreadCarriers.contains(id);
        }
    }
}
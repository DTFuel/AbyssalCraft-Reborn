package com.shinoow.abyssalcraft.system.portal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.ACDimensions;

/** Ordered registry of dimensions that can participate in AbyssalCraft gateway travel. */
public final class DimensionDataRegistry {

    private static final DimensionDataRegistry INSTANCE = new DimensionDataRegistry();

    private final Map<ResourceKey<Level>, DimensionData> dimensions = new LinkedHashMap<>();

    private DimensionDataRegistry() {
        registerDefaults();
    }

    public static DimensionDataRegistry instance() {
        return INSTANCE;
    }

    public synchronized void register(DimensionData data) {
        dimensions.put(data.dimension(), data);
    }

    public synchronized Optional<DimensionData> get(ResourceKey<Level> dimension) {
        return Optional.ofNullable(dimensions.get(dimension));
    }

    public synchronized List<DimensionData> values() {
        return List.copyOf(dimensions.values());
    }

    /** Server-authoritative Necronomicon tier for knowledge accessed in this dimension. */
    public synchronized OptionalInt requiredBookType(ResourceKey<Level> dimension) {
        ComplexConfig.DimensionBookMapping configured =
            ComplexConfig.dimensionBookTypeMappings().get(dimension.location());
        if (configured != null) return OptionalInt.of(configured.bookType());
        DimensionData data = dimensions.get(dimension);
        return data == null ? OptionalInt.empty() : OptionalInt.of(data.minimumBookType());
    }

    /** Stable legacy registration order used by Gateway Key target cycling. */
    public synchronized List<DimensionData> availableForGatewayTier(int gatewayTier,
                                                                     boolean includeVanillaDimensions) {
        if (gatewayTier < 0 || gatewayTier > 3) return List.of();
        return dimensions.values().stream()
            .filter(data -> data.minimumGatewayTier() <= gatewayTier)
            .filter(data -> includeVanillaDimensions
                || data.dimension() != Level.NETHER && data.dimension() != Level.END)
            .toList();
    }

    /** Gateway Key targets: only AbyssalCraft dimensions, in stable progression order. */
    public synchronized List<DimensionData> gatewayKeyDestinations(int gatewayTier) {
        if (gatewayTier < 0 || gatewayTier > 3) return List.of();
        return dimensions.values().stream()
            .filter(data -> data.dimension().location().getNamespace().equals("abyssalcraft"))
            .filter(data -> data.minimumGatewayTier() <= gatewayTier)
            .toList();
    }

    public synchronized boolean isGatewayKeyDestination(ResourceKey<Level> dimension, int gatewayTier) {
        return gatewayKeyDestinations(gatewayTier).stream()
            .anyMatch(data -> data.dimension().equals(dimension));
    }

    public synchronized boolean isAvailableForGatewayTier(ResourceKey<Level> dimension,
                                                            int gatewayTier,
                                                            boolean includeVanillaDimensions) {
        return availableForGatewayTier(gatewayTier, includeVanillaDimensions).stream()
            .anyMatch(data -> data.dimension().equals(dimension));
    }

    public synchronized Optional<ResourceKey<Level>> parseRegisteredDimension(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        try {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ACRef.parse(id));
            return dimensions.containsKey(key) ? Optional.of(key) : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public synchronized boolean areDimensionsConnected(ResourceKey<Level> from,
                                                        ResourceKey<Level> to,
                                                        int gatewayTier) {
        return areDimensionsConnected(from, to, gatewayTier, configuredStartDimension());
    }

    synchronized boolean areDimensionsConnected(ResourceKey<Level> from,
                                                ResourceKey<Level> to,
                                                int gatewayTier,
                                                ResourceKey<Level> startDimension) {
        if (gatewayTier < 0 || gatewayTier > 3 || from.equals(to)) return false;
        if (gatewayTier == 0 && to == ACDimensions.ABYSSAL_WASTELAND
            && from != startDimension) return false;
        DimensionData fromData = dimensions.get(from);
        DimensionData toData = dimensions.get(to);
        if (fromData == null || toData == null) return false;
        int requiredTier = Math.max(fromData.minimumGatewayTier(), toData.minimumGatewayTier());
        return gatewayTier >= requiredTier
            && fromData.isConnectedTo(to)
            && toData.isConnectedTo(from);
    }

    private static ResourceKey<Level> configuredStartDimension() {
        return switch (ACConfig.startDimension.get()) {
            case -1 -> Level.NETHER;
            case 0 -> Level.OVERWORLD;
            case 1 -> Level.END;
            case 50 -> ACDimensions.ABYSSAL_WASTELAND;
            case 51 -> ACDimensions.DREADLANDS;
            case 52 -> ACDimensions.OMOTHOL;
            case 53 -> ACDimensions.DARK_REALM;
            default -> null;
        };
    }

    public synchronized List<ResourceKey<Level>> reachableFrom(ResourceKey<Level> from,
                                                               int gatewayTier,
                                                               Predicate<ResourceKey<Level>> available) {
        List<ResourceKey<Level>> reachable = new ArrayList<>();
        for (ResourceKey<Level> candidate : dimensions.keySet()) {
            if (available.test(candidate) && areDimensionsConnected(from, candidate, gatewayTier)) {
                reachable.add(candidate);
            }
        }
        return List.copyOf(reachable);
    }

    private void registerDefaults() {
        register(DimensionData.builder(ACDimensions.ABYSSAL_WASTELAND,
                "dimension.abyssalcraft.abyssal_wasteland", 0xFF00FF00)
            .minimumBookType(1)
            .connectedTo(Level.OVERWORLD, ACDimensions.DREADLANDS)
            .portalMob(ACRef.id("abyssalzombie"))
            .build());
        register(DimensionData.builder(ACDimensions.DREADLANDS,
                "dimension.abyssalcraft.dreadlands", 0xFFFF0000)
            .minimumGatewayTier(1)
            .minimumBookType(2)
            .connectedTo(ACDimensions.ABYSSAL_WASTELAND, ACDimensions.OMOTHOL)
            .portalMob(ACRef.id("dreadling"))
            .build());
        register(DimensionData.builder(ACDimensions.OMOTHOL,
                "dimension.abyssalcraft.omothol", 0xFF00FFFF)
            .minimumGatewayTier(2)
            .minimumBookType(3)
            .connectedTo(ACDimensions.DREADLANDS, ACDimensions.DARK_REALM)
            .portalMob(ACRef.id("jzaharminion"))
            .overlay(ACRef.id("textures/model/omothol_portal.png"))
            .build());
        register(DimensionData.builder(ACDimensions.DARK_REALM,
                "dimension.abyssalcraft.dark_realm", 0xFF000000)
            .minimumGatewayTier(2)
            .minimumBookType(4)
            .connectedTo(ACDimensions.OMOTHOL)
            .portalMob(ACRef.id("shadowmonster"))
            .build());
        register(DimensionData.builder(Level.OVERWORLD, "dimension.minecraft.overworld", 0xFF0000FF)
            .connectedTo(ACDimensions.ABYSSAL_WASTELAND, Level.NETHER, Level.END)
            .build());
        register(DimensionData.builder(Level.NETHER, "dimension.minecraft.the_nether", 0xFFFC5700)
            .connectedTo(Level.OVERWORLD)
            .portalMob(ACRef.vanilla("zombified_piglin"))
            .build());
        register(DimensionData.builder(Level.END, "dimension.minecraft.the_end", 0xFFCC00FA)
            .connectedTo(Level.OVERWORLD)
            .build());
        validateDefaults();
    }

    private void validateDefaults() {
        ResourceKey<Level> defaultStart = Level.OVERWORLD;
        requireConnection(Level.OVERWORLD, ACDimensions.ABYSSAL_WASTELAND, 0, defaultStart);
        requireConnection(ACDimensions.ABYSSAL_WASTELAND, ACDimensions.DREADLANDS, 1, defaultStart);
        requireConnection(ACDimensions.DREADLANDS, ACDimensions.OMOTHOL, 2, defaultStart);
        requireConnection(ACDimensions.OMOTHOL, ACDimensions.DARK_REALM, 2, defaultStart);
        requireConnection(Level.OVERWORLD, Level.NETHER, 0, defaultStart);
        requireConnection(Level.OVERWORLD, Level.END, 0, defaultStart);
        if (areDimensionsConnected(ACDimensions.ABYSSAL_WASTELAND, ACDimensions.DREADLANDS, 0, defaultStart)
            || areDimensionsConnected(ACDimensions.DREADLANDS, ACDimensions.OMOTHOL, 1, defaultStart)
            || areDimensionsConnected(ACDimensions.ABYSSAL_WASTELAND, Level.NETHER, 3, defaultStart)) {
            throw new IllegalStateException("Portal dimension graph accepts an invalid connection");
        }
    }

    private void requireConnection(ResourceKey<Level> from, ResourceKey<Level> to, int gatewayTier,
                                   ResourceKey<Level> startDimension) {
        if (!areDimensionsConnected(from, to, gatewayTier, startDimension)) {
            throw new IllegalStateException("Missing portal connection " + from.location() + " -> "
                + to.location() + " at Gateway Key tier " + gatewayTier);
        }
    }
}
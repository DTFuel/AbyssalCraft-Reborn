package com.shinoow.abyssalcraft.system.portal;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Immutable metadata used by gateway keys, portal anchors and portal rendering. */
public record DimensionData(
    ResourceKey<Level> dimension,
    String displayKey,
    int color,
    int minimumGatewayTier,
    int minimumBookType,
    int ritualBookType,
    Set<ResourceKey<Level>> connectedDimensions,
    Optional<ResourceLocation> portalMob,
    Optional<ResourceLocation> overlay
) {

    public DimensionData {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(displayKey, "displayKey");
        Objects.requireNonNull(connectedDimensions, "connectedDimensions");
        Objects.requireNonNull(portalMob, "portalMob");
        Objects.requireNonNull(overlay, "overlay");
        if (minimumGatewayTier < 0 || minimumGatewayTier > 3) {
            throw new IllegalArgumentException("minimumGatewayTier must be between 0 and 3");
        }
        if (minimumBookType < 0 || minimumBookType > 4) {
            throw new IllegalArgumentException("minimumBookType must be between 0 and 4");
        }
        if (ritualBookType < 0 || ritualBookType > 4) {
            throw new IllegalArgumentException("ritualBookType must be between 0 and 4");
        }
        connectedDimensions = Set.copyOf(new LinkedHashSet<>(connectedDimensions));
    }

    public boolean isConnectedTo(ResourceKey<Level> other) {
        return connectedDimensions.contains(other);
    }

    public static Builder builder(ResourceKey<Level> dimension, String displayKey, int color) {
        return new Builder(dimension, displayKey, color);
    }

    public static final class Builder {

        private final ResourceKey<Level> dimension;
        private final String displayKey;
        private final int color;
        private final Set<ResourceKey<Level>> connectedDimensions = new LinkedHashSet<>();
        private int minimumGatewayTier;
        private int minimumBookType;
        private int ritualBookType = -1;
        private ResourceLocation portalMob;
        private ResourceLocation overlay;

        private Builder(ResourceKey<Level> dimension, String displayKey, int color) {
            this.dimension = dimension;
            this.displayKey = displayKey;
            this.color = color;
        }

        public Builder minimumGatewayTier(int tier) {
            minimumGatewayTier = tier;
            return this;
        }

        public Builder minimumBookType(int bookType) {
            minimumBookType = bookType;
            return this;
        }

        public Builder ritualBookType(int bookType) {
            ritualBookType = bookType;
            return this;
        }

        @SafeVarargs
        public final Builder connectedTo(ResourceKey<Level>... dimensions) {
            for (ResourceKey<Level> connected : dimensions) {
                connectedDimensions.add(Objects.requireNonNull(connected, "connected dimension"));
            }
            return this;
        }

        public Builder portalMob(ResourceLocation entityTypeId) {
            portalMob = Objects.requireNonNull(entityTypeId, "entityTypeId");
            return this;
        }

        public Builder overlay(ResourceLocation texture) {
            overlay = Objects.requireNonNull(texture, "texture");
            return this;
        }

        public DimensionData build() {
            int resolvedRitualBookType = ritualBookType < 0 ? minimumBookType : ritualBookType;
            return new DimensionData(dimension, displayKey, color, minimumGatewayTier, minimumBookType,
                resolvedRitualBookType,
                connectedDimensions, Optional.ofNullable(portalMob), Optional.ofNullable(overlay));
        }
    }
}
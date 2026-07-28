package com.shinoow.abyssalcraft.system.portal;

import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.misc.DimensionPortal;
import com.shinoow.abyssalcraft.world.ACDimensions;

/** Permanent invariants for the Gateway Key dimension graph. */
public final class PortalSelfTest {

    private PortalSelfTest() {}

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        DimensionDataRegistry registry = DimensionDataRegistry.instance();
        require(registry.values().size() == 7, "expected seven registered portal dimensions");
        require(registry.availableForGatewayTier(0, false).size() == 2,
            "tier-zero key must expose Overworld and Abyssal Wasteland");
        require(registry.availableForGatewayTier(1, false).size() == 3,
            "tier-one key must add the Dreadlands");
        require(registry.availableForGatewayTier(2, false).size() == 5,
            "tier-two key must add Omothol and the Dark Realm");
        require(registry.availableForGatewayTier(3, true).size() == 7,
            "Silver Key with vanilla handling must expose all registered dimensions");
        require(registry.gatewayKeyDestinations(0).size() == 1,
            "tier-zero Gateway Key must target only the Abyssal Wasteland");
        require(registry.gatewayKeyDestinations(1).size() == 2,
            "tier-one Gateway Key must add only the Dreadlands");
        require(registry.gatewayKeyDestinations(2).size() == 4
            && registry.gatewayKeyDestinations(3).size() == 4,
            "advanced Gateway Keys must expose exactly four AbyssalCraft dimensions");
        require(registry.gatewayKeyDestinations(3).stream().allMatch(data ->
            data.dimension().location().getNamespace().equals("abyssalcraft")),
            "Gateway Key target list contains a non-AbyssalCraft dimension");

        requireEdge(registry, Level.OVERWORLD, ACDimensions.ABYSSAL_WASTELAND, 0);
        requireEdge(registry, ACDimensions.ABYSSAL_WASTELAND, ACDimensions.DREADLANDS, 1);
        requireEdge(registry, ACDimensions.DREADLANDS, ACDimensions.OMOTHOL, 2);
        requireEdge(registry, ACDimensions.OMOTHOL, ACDimensions.DARK_REALM, 2);
        requireEdge(registry, Level.OVERWORLD, Level.NETHER, 0);
        requireEdge(registry, Level.OVERWORLD, Level.END, 0);

        require(!registry.areDimensionsConnected(ACDimensions.ABYSSAL_WASTELAND,
            ACDimensions.DREADLANDS, 0, Level.OVERWORLD), "tier-zero key reached the Dreadlands");
        require(!registry.areDimensionsConnected(ACDimensions.DREADLANDS,
            ACDimensions.OMOTHOL, 1, Level.OVERWORLD), "tier-one key reached Omothol");
        require(!registry.areDimensionsConnected(ACDimensions.ABYSSAL_WASTELAND,
            Level.NETHER, 3, Level.OVERWORLD), "unregistered cross-edge reached the Nether");
        require(registry.parseRegisteredDimension("not a resource location").isEmpty(),
            "invalid target string was accepted");
        require(registry.get(ACDimensions.OMOTHOL).orElseThrow().overlay().isPresent(),
            "Omothol portal overlay is missing");
        require(registry.get(Level.OVERWORLD).orElseThrow().color() == 0xFF0000FF,
            "Overworld portal color changed");
        require(registry.get(ACDimensions.DARK_REALM).orElseThrow().color() == 0xFF000000,
            "Dark Realm portal color changed");
        require(registry.get(ACDimensions.ABYSSAL_WASTELAND).orElseThrow().minimumBookType() == 1,
            "Abyssal Wasteland must require the Abyssal Wasteland Necronomicon");
        require(registry.get(ACDimensions.DREADLANDS).orElseThrow().minimumBookType() == 2,
            "Dreadlands must require the Dreadlands Necronomicon");
        require(registry.get(ACDimensions.OMOTHOL).orElseThrow().minimumBookType() == 3,
            "Omothol must require the Omothol Necronomicon");
        require(registry.get(ACDimensions.DARK_REALM).orElseThrow().minimumBookType() == 4,
            "Dark Realm must require the Abyssalnomicon");
        require(DimensionPortal.TRANSIENT_LIFETIME_TICKS == 1200
            && DimensionPortal.initialLifetime(false) == 1200
            && DimensionPortal.initialLifetime(true) < 0,
            "Portal lifetime contract changed");

        System.out.println("RR_PORTAL_SELF_TEST_OK dimensions=7 edges=6 keyTiers=4 transientLifetime=1200");
    }

    private static void requireEdge(DimensionDataRegistry registry,
                                    net.minecraft.resources.ResourceKey<Level> from,
                                    net.minecraft.resources.ResourceKey<Level> to,
                                    int tier) {
        require(registry.areDimensionsConnected(from, to, tier, Level.OVERWORLD),
            "missing portal edge " + from.location() + " -> " + to.location());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("RR-PORTAL self-test failed: " + message);
    }
}
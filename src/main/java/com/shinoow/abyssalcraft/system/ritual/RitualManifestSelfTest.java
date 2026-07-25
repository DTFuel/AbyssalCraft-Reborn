package com.shinoow.abyssalcraft.system.ritual;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.core.registries.BuiltInRegistries;

/** Permanent invariants for the source-derived 62-entry ritual catalog. */
public final class RitualManifestSelfTest {

    private RitualManifestSelfTest() {}

    public static void run() {
        List<RitualManifest> rituals = RitualManifestCatalog.entries();
        require(rituals.size() == 62, "legacy ritual catalog size changed: " + rituals.size());

        Set<String> ids = new HashSet<>();
        Set<String> legacyIds = new HashSet<>();
        for (int index = 0; index < rituals.size(); index++) {
            RitualManifest ritual = rituals.get(index);
            require(ritual.order() == index + 1, "ritual registration order is not contiguous at " + ritual.id());
            require(ids.add(ritual.id()), "duplicate ritual id: " + ritual.id());
            require(legacyIds.add(ritual.legacyId()), "duplicate legacy ritual id: " + ritual.legacyId());
            require(ritual.offeringLayout().size() == RitualManifest.PEDESTAL_COUNT,
                "ritual does not preserve eight pedestal slots: " + ritual.id());
        }

        require(count(rituals, RitualManifest.Kind.INFUSION) == 40, "expected 40 explicit infusion registrations");
        require(count(rituals, RitualManifest.Kind.CREATION) == 3, "expected 3 creation registrations");
        require(count(rituals, RitualManifest.Kind.TRANSFORMATION) == 1, "expected 1 transformation registration");
        require(rituals.stream().filter(RitualManifest::hidden).count() == 1, "expected one hidden ritual");
        require(rituals.stream().filter(ritual -> ritual.kind() != RitualManifest.Kind.INFUSION
            && ritual.kind() != RitualManifest.Kind.CREATION
            && ritual.kind() != RitualManifest.Kind.TRANSFORMATION).count() == 18,
            "expected 18 specialized registrations");

        RitualManifest portal = required("portal");
        require(portal.requiredEnergy() == 1000F && portal.offerings().size() == 4,
            "portal ritual legacy contract changed");
        RitualManifest respawn = required("respawn_jzahar");
        require(respawn.requiredEnergy() == 20000F && respawn.dimension() == ACDimensions.OMOTHOL,
            "Jzahar respawn must cost 20000 PE in Omothol");
        RitualManifest resurrection = required("resurrection");
        require(resurrection.requiresSacrifice() && resurrection.center().referencedItems().contains(ACRef.vanilla("name_tag"))
            && resurrection.offerings().size() == 6, "resurrection sacrifice/crystal contract changed");
        RitualManifest cleansing = required("cleansing");
        require(cleansing.requiredEnergy() == 10000F && cleansing.requiresSacrifice(),
            "cleansing ritual must cost the Abyssal Wasteland book capacity and require a living sacrifice");
        RitualManifest massEnchanting = required("mass_enchanting");
        require(massEnchanting.center().isAnyItem() && massEnchanting.requiredEnergy() == 50000F,
            "mass enchanting center/PE contract changed");
        require(required("house").hidden(), "house ritual must remain hidden");

        Set<String> missingItems = new TreeSet<>();
        Set<String> missingTargets = new TreeSet<>();
        for (RitualManifest ritual : rituals) {
            ritual.referencedItems().stream()
                .filter(id -> !BuiltInRegistries.ITEM.containsKey(id))
                .map(Object::toString)
                .forEach(missingItems::add);
            for (var target : ritual.actionTargets()) {
                boolean present = switch (ritual.kind()) {
                    case SUMMON, RESPAWN_JZAHAR, DREAD_SPAWN -> BuiltInRegistries.ENTITY_TYPE.containsKey(target);
                    case POTION_AOE -> BuiltInRegistries.MOB_EFFECT.containsKey(target);
                    case HOUSE -> true;
                    default -> false;
                };
                if (!present) missingTargets.add(ritual.id() + " -> " + target);
            }
        }
        require(missingItems.isEmpty(), "unresolved ritual item references: " + String.join(", ", missingItems));
        require(missingTargets.isEmpty(), "unresolved ritual action targets: " + String.join(", ", missingTargets));

        System.out.println("RR_RITUAL_MANIFEST_SELF_TEST_OK rituals=62 infusion=40 creation=3 transformation=1 special=18");
    }

    private static long count(List<RitualManifest> rituals, RitualManifest.Kind kind) {
        return rituals.stream().filter(ritual -> ritual.kind() == kind).count();
    }

    private static RitualManifest required(String id) {
        RitualManifest ritual = RitualManifestCatalog.get(id);
        require(ritual != null, "missing ritual manifest: " + id);
        return ritual;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
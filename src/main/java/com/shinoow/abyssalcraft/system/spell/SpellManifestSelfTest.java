package com.shinoow.abyssalcraft.system.spell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.core.registries.BuiltInRegistries;

/** Permanent source-derived invariants for the fourteen-spell roster. */
public final class SpellManifestSelfTest {

    private SpellManifestSelfTest() {}

    public static void run() {
        List<SpellManifest> spells = SpellManifestCatalog.entries();
        require(spells.size() == 14, "spell manifest size changed: " + spells.size());
        Set<String> ids = new HashSet<>();
        Set<String> aliases = new HashSet<>();
        for (int index = 0; index < spells.size(); index++) {
            SpellManifest spell = spells.get(index);
            require(spell.order() == index + 1, "spell order changed at " + spell.id());
            require(ids.add(spell.id()), "duplicate spell id: " + spell.id());
            for (String alias : spell.aliases()) require(aliases.add(alias) && !ids.contains(alias),
                "duplicate spell alias: " + alias);
        }
        require(spells.stream().filter(SpellManifest::requiresCharging).count() == 11,
            "expected eleven charging spells");
        require(count(spells, SpellManifest.TargetType.ENTITY) == 7,
            "expected seven strict entity-target spells");
        require(count(spells, SpellManifest.TargetType.ENTITY_OR_SELF) == 1,
            "expected one entity-or-self spell");
        require(count(spells, SpellManifest.TargetType.BLOCK) == 2,
            "expected two block-target spells");
        require(count(spells, SpellManifest.TargetType.SELF) == 4,
            "expected four self-target spells");

        SpellManifest lifeDrain = required("lifedrain");
        require(lifeDrain.requiredEnergy() == 100F && lifeDrain.requiresCharging()
            && lifeDrain.reagents().size() == 1, "Life Drain legacy parameters changed");
        require(required("life_drain") == lifeDrain, "Life Drain transitional alias is missing");
        SpellManifest teleportHome = required("teleportHome");
        require(teleportHome.bookType() == 0 && teleportHome.scrollType() == ScrollType.MODERATE
            && required("teleport_home") == teleportHome, "Teleport Home legacy id/quality changed");
        require(required("floating").bookType() == 3 && required("floating").requiredEnergy() == 15F,
            "Floating spell book tier/PE changed");

        Set<String> missing = new TreeSet<>();
        for (SpellManifest spell : spells) spell.referencedItems().stream()
            .filter(id -> !BuiltInRegistries.ITEM.containsKey(id))
            .map(Object::toString).forEach(missing::add);
        require(missing.isEmpty(), "unresolved spell reagent items: " + String.join(", ", missing));
        List<Spell> runtime = SpellRegistry.instance().getSpells();
        require(runtime.size() == spells.size(), "runtime spell registry does not mirror the manifest");
        for (int index = 0; index < spells.size(); index++) {
            require(runtime.get(index) instanceof ManifestSpell mounted && mounted.manifest() == spells.get(index),
                "runtime spell order diverged at index " + index);
            SpellManifest manifest = spells.get(index);
            if (manifest.id().equals("teleportHome")) {
                require(manifest.reagents().size() == 1
                    && net.minecraft.resources.ResourceLocation.parse("minecraft:beds")
                        .equals(manifest.reagents().get(0).tagId()),
                    "Teleport Home bed tag changed");
                continue;
            }
            List<net.minecraft.world.item.ItemStack> reagents = manifest.reagents().stream()
                .map(SpellIngredient::example).toList();
            require(SpellRegistry.instance().find(manifest.bookType(), manifest.scrollType(),
                manifest.parentId() == null ? "" : manifest.parentId(), reagents) == runtime.get(index),
                "Spellbook recipe does not resolve " + manifest.id());
        }
        Set<String> entityBehaviors = Set.of("lifedrain", "graspofcthulhu", "invisibility",
            "detachment", "stealvigor", "sirenssong", "undeathtodust", "teleporthostiles");
        Set<String> allBehaviors = new HashSet<>(entityBehaviors);
        allBehaviors.addAll(Set.of("entropy", "mining", "oozeremoval", "floating", "teleportHome", "compass"));
        require(SpellBehaviorRegistry.instance().size() == 14
            && SpellBehaviorRegistry.instance().ids().equals(allBehaviors),
            "spell behavior coverage changed");
        System.out.println("RR_SPELL_MANIFEST_SELF_TEST_OK spells=14 entity=7 entityOrSelf=1 block=2 self=4 charging=11 handlers=14 spellbook=14");
    }

    private static long count(List<SpellManifest> spells, SpellManifest.TargetType type) {
        return spells.stream().filter(spell -> spell.targetType() == type).count();
    }

    private static SpellManifest required(String id) {
        SpellManifest spell = SpellManifestCatalog.get(id);
        require(spell != null, "missing spell manifest: " + id);
        return spell;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
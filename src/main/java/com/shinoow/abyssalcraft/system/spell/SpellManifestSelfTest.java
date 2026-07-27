package com.shinoow.abyssalcraft.system.spell;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.config.ACConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

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
            require(SpellRegistry.instance().findIgnoringAvailability(manifest.bookType(), manifest.scrollType(),
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
        validateAvailabilityMatrix(spells, runtime);
        System.out.println("RR_SPELL_MANIFEST_SELF_TEST_OK spells=14 entity=7 entityOrSelf=1 block=2 self=4 charging=11 handlers=14 spellbook=14 availabilityMatrix=14x2");
    }

    private static void validateAvailabilityMatrix(List<SpellManifest> manifests, List<Spell> runtime) {
        Map<String, Supplier<Boolean>> policies = SpellAvailability.enabledById();
        require(policies.keySet().equals(manifests.stream().map(SpellManifest::id).collect(java.util.stream.Collectors.toSet())),
            "spell availability policy does not cover the manifest exactly");
        Map<Field, Object> originals = new LinkedHashMap<>();
        try {
            for (SpellManifest manifest : manifests) {
                Field field = ACConfig.class.getField(configField(manifest.id()));
                originals.put(field, field.get(null));
                field.set(null, (Supplier<Boolean>) () -> true);
            }
            for (int index = 0; index < manifests.size(); index++) {
                SpellManifest manifest = manifests.get(index);
                Spell spell = runtime.get(index);
                Field field = ACConfig.class.getField(configField(manifest.id()));
                    field.set(null, (Supplier<Boolean>) () -> false);
                    require(!SpellAvailability.isEnabled(manifest.id()) && !SpellAvailability.isEnabled(spell),
                        "disabled spell remains available: " + manifest.id());
                    for (String alias : manifest.aliases()) require(!SpellAvailability.isEnabled(alias),
                        "disabled spell alias remains available: " + alias);
                    require(SpellRegistry.instance().findForAvailabilityTest(
                        manifest.bookType(), manifest.scrollType(),
                        manifest.parentId() == null ? "" : manifest.parentId(), spell) == null,
                        "disabled spell remains inscribable: " + manifest.id());
                    field.set(null, (Supplier<Boolean>) () -> true);
                    require(SpellAvailability.isEnabled(manifest.id()) && SpellAvailability.isEnabled(spell),
                        "enabled spell remains unavailable: " + manifest.id());
                    for (String alias : manifest.aliases()) require(SpellAvailability.isEnabled(alias),
                        "enabled spell alias remains unavailable: " + alias);
                    require(SpellRegistry.instance().findForAvailabilityTest(
                        manifest.bookType(), manifest.scrollType(),
                        manifest.parentId() == null ? "" : manifest.parentId(), spell) == spell,
                        "enabled spell remains uninscribable: " + manifest.id());
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("missing spell config field", exception);
        } finally {
            originals.forEach((field, value) -> {
                try {
                    field.set(null, value);
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException("unable to restore spell config field " + field.getName(), exception);
                }
            });
        }
    }

    private static String configField(String id) {
        return switch (id) {
            case "entropy" -> "entropy_spell";
            case "lifedrain" -> "life_drain_spell";
            case "mining" -> "mining_spell";
            case "graspofcthulhu" -> "grasp_of_cthulhu_spell";
            case "invisibility" -> "invisibility_spell";
            case "detachment" -> "detachment_spell";
            case "stealvigor" -> "steal_vigor_spell";
            case "sirenssong" -> "sirens_song_spell";
            case "undeathtodust" -> "undeath_to_dust_spell";
            case "oozeremoval" -> "ooze_removal_spell";
            case "teleporthostiles" -> "teleport_hostile_spell";
            case "floating" -> "floating_spell";
            case "teleportHome" -> "teleport_home_spell";
            case "compass" -> "compass_spell";
            default -> throw new IllegalStateException("unknown spell id: " + id);
        };
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
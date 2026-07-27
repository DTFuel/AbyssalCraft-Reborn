package com.shinoow.abyssalcraft.system.knowledge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.RecipeDisplayCompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Strict legacy-page to modern-recipe catalog for Necronomicon recipe rendering. */
public final class NecronomiconRecipePages {

    private static final Map<String, Definition> DEFINITIONS = Map.ofEntries(
        active("CRAFTING_CORALIUM_INFUSED_STONE_1", "coraliumstone"), active("CRAFTING_SHADOW_GEM_1", "shadowgem"),
        active("CRAFTING_SHARD_OF_OBLIVION", "oblivionshard"), active("CRAFTING_GATEWAY_KEY", "gatewaykey"),
        active("CRAFTING_SKIN_OF_THE_ABYSSAL_WASTELAND", "skin_0"), active("CRAFTING_NECRONOMICON_C", "necronomicon_cor"),
        active("CRAFTING_STAFF_OF_RENDING_1", "drainstaff_0"), active("CRAFTING_SEQUENTIAL_BREWING_STAND_1", "sequential_brewing_stand"),
        active("CRAFTING_POWERSTONE_TRACKER", "powerstonetracker"),
        active("CRAFTING_TRANSMUTATOR_1", "transmutator"),
        active("CRAFTING_CORALIUM_CHUNK", "cchunk"), active("CRAFTING_CORALIUM_PLATE", "platec"),
        active("CRAFTING_SKIN_OF_THE_DREADLANDS", "skin_1"), active("CRAFTING_NECRONOMICON_D", "necronomicon_dre"),
        active("CRAFTING_PLATED_CORALIUM_HELMET", "corhelmetp"), active("CRAFTING_PLATED_CORALIUM_CHESTPLATE", "corplatep"),
        active("CRAFTING_PLATED_CORALIUM_LEGGINGS", "corlegsp"), active("CRAFTING_PLATED_CORALIUM_BOOTS", "corbootsp"),
        active("CRAFTING_CORALIUM_LONGBOW", "corbow"),
        active("CORALIUM_PLAGUE_INFO_2", "antidote_0"),
        active("CRAFTING_DREADIUM_1", "block_of_dreadium"), active("CRAFTING_CRYSTALLIZER_1", "crystallizer"),
        active("CRAFTING_DREAD_CLOTH", "dreadcloth"), active("CRAFTING_DREADIUM_PLATE", "dreadplate"),
        active("CRAFTING_DREADIUM_HILT", "dreadhilt"),
        active("CRAFTING_DREADIUM_BLADE", "dreadblade"),
        active("CRAFTING_SKIN_OF_OMOTHOL", "skin_2"), active("CRAFTING_NECRONOMICON_O", "necronomicon_omt"),
        active("CRAFTING_DREADIUM_SAMURAI_HELMET", "dreadiumsamuraihelmet"), active("CRAFTING_DREADIUM_SAMURAI_CHESTPLATE", "dreadiumsamuraiplate"),
        active("CRAFTING_DREADIUM_SAMURAI_LEGGINGS", "dreadiumsamurailegs"), active("CRAFTING_DREADIUM_SAMURAI_BOOTS", "dreadiumsamuraiboots"),
        active("CRAFTING_DREADIUM_KATANA", "dreadkatana"),
        active("DREAD_PLAGUE_INFO_2", "antidote_1"),
        active("CRAFTING_LIFE_CRYSTAL_1", "lifecrystal"), active("CRAFTING_ETHAXIUM_INGOT_1", "ethaxiumingot"),
        active("CRAFTING_ETHAXIUM_PILLAR", "ethaxiumpillar"), active("CRAFTING_DARK_ETHAXIUM_PILLAR", "darkethaxiumpillar"),
        active("CRAFTING_COIN", "coin_alt"), active("CRAFTING_CRYSTAL_BAG_1", "crystalbag_small"),
        active("CRAFTING_MATERIALIZER_1", "materializer"),
        active("CRAFTING_ABYSSALNOMICON_1", "abyssalnomicon"),
        active("CRAFTING_PORTAL_ANCHOR_1", "portal_anchor"), active("CRAFTING_ENERGY_PEDESTAL_1", "energypedestal"),
        active("CRAFTING_MONOLITH_PILLAR", "monolithpillar"), active("CRAFTING_RITUAL_CHARM", "charm_0"),
        active("CRAFTING_SACRIFICIAL_ALTAR_1", "sacrificialaltar"),
        active("CRAFTING_ENERGY_COLLECTOR", "energycollector"), active("CRAFTING_ENERGY_RELAY", "energyrelay"),
        active("CRAFTING_RENDING_PEDESTAL", "rendingpedestal"), active("CRAFTING_STONE_TABLET", "stonetablet"),
        active("CRAFTING_STATE_TRANSFORMER_1", "statetransformer"), active("CRAFTING_ENERGY_DEPOSITIONER_1", "energydepositioner"),
        active("CRAFTING_ODB_CORE", "odbcore"), active("CRAFTING_ODB", "odb"),
        active("CRAFTING_CARBON_CLUSTER", "carboncluster"), active("CRAFTING_DENSE_CARBON_CLUSTER", "densecarboncluster"),
        active("CRAFTING_CRATE", "crate"),
        active("CRAFTING_DECORATIVE_AZATHOTH_STATUE", "decorativestatue_0"), active("CRAFTING_DECORATIVE_CTHULHU_STATUE", "decorativestatue_1"),
        active("CRAFTING_DECORATIVE_HASTUR_STATUE", "decorativestatue_2"), active("CRAFTING_DECORATIVE_JZAHAR_STATUE", "decorativestatue_3"),
        active("CRAFTING_DECORATIVE_NYARLATHOTEP_STATUE", "decorativestatue_4"), active("CRAFTING_DECORATIVE_YOG_SOTHOTH_STATUE", "decorativestatue_5"),
        active("CRAFTING_DECORATIVE_SHUB_NIGGURATH_STATUE", "decorativestatue_6"), active("PE_UPGRADING_2", "ring")
    );

    private NecronomiconRecipePages() {}

    public static Optional<RecipePage> resolve(Level level, String legacyPageId) {
        Definition definition = DEFINITIONS.get(legacyPageId);
        if (definition == null || !definition.active()) return Optional.empty();
        List<RecipeDisplayCompat.DisplayRecipe> recipes = definition.recipeIds().stream()
            .map(id -> RecipeDisplayCompat.find(level, id).orElse(null))
            .toList();
        if (recipes.stream().anyMatch(java.util.Objects::isNull)) return Optional.empty();
        return Optional.of(new RecipePage(definition.recipeIds(), recipes));
    }

    public static Definition definition(String legacyPageId) { return DEFINITIONS.get(legacyPageId); }
    public static Map<String, Definition> definitions() { return DEFINITIONS; }

    private static Map.Entry<String, Definition> active(String legacyPageId, String... recipePaths) {
        return Map.entry(legacyPageId, new Definition(Arrays.stream(recipePaths).map(ACRef::id).toList(), ""));
    }

    private static Map.Entry<String, Definition> blocked(String legacyPageId, String reason) {
        return Map.entry(legacyPageId, new Definition(List.of(), reason));
    }

    public record Definition(List<ResourceLocation> recipeIds, String blockedReason) {
        public Definition {
            recipeIds = List.copyOf(recipeIds);
            blockedReason = blockedReason == null ? "" : blockedReason;
            if (recipeIds.isEmpty() == blockedReason.isBlank())
                throw new IllegalArgumentException("recipe page must be active or explicitly blocked");
        }
        public boolean active() { return !recipeIds.isEmpty(); }
    }

    public record RecipePage(List<ResourceLocation> ids, List<RecipeDisplayCompat.DisplayRecipe> recipes) {
        public RecipePage {
            ids = List.copyOf(ids);
            recipes = List.copyOf(recipes);
            if (ids.isEmpty() || ids.size() != recipes.size()) {
                throw new IllegalArgumentException("incomplete Necronomicon recipe page");
            }
        }
    }
}
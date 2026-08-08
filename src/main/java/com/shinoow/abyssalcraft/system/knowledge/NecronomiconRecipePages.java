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
        active("CRAFTING_CORALIUM_INFUSED_STONE_1", "coralium_infused_stone", "coraliumstone"),
        active("CRAFTING_SHADOW_GEM_1", "shadow_gem", "shadowgem"),
        active("CRAFTING_SHARD_OF_OBLIVION", "shard_of_oblivion", "oblivionshard"),
        active("CRAFTING_GATEWAY_KEY", "gatewaykey", "gatewaykey"),
        active("CRAFTING_SKIN_OF_THE_ABYSSAL_WASTELAND", "skin_of_the_abyssal_wasteland", "skin_0"),
        active("CRAFTING_NECRONOMICON_C", "abyssal_wasteland_necronomicon", "necronomicon_cor"),
        active("CRAFTING_STAFF_OF_RENDING_1", "staff_of_rending", "drainstaff_0"),
        active("CRAFTING_SEQUENTIAL_BREWING_STAND_1", "sequential_brewing_stand", "sequential_brewing_stand"),
        active("CRAFTING_POWERSTONE_TRACKER", "powerstone_tracker", "powerstonetracker"),
        active("CRAFTING_TRANSMUTATOR_1", "transmutator", "transmutator"),
        active("CRAFTING_CORALIUM_CHUNK", "chunk_of_coralium", "cchunk"),
        active("CRAFTING_CORALIUM_PLATE", "coralium_plate", "platec"),
        active("CRAFTING_SKIN_OF_THE_DREADLANDS", "skin_of_the_dreadlands", "skin_1"),
        active("CRAFTING_NECRONOMICON_D", "dreadlands_necronomicon", "necronomicon_dre"),
        active("CRAFTING_PLATED_CORALIUM_HELMET", "plated_coralium_helmet", "corhelmetp"),
        active("CRAFTING_PLATED_CORALIUM_CHESTPLATE", "plated_coralium_chestplate", "corplatep"),
        active("CRAFTING_PLATED_CORALIUM_LEGGINGS", "plated_coralium_leggings", "corlegsp"),
        active("CRAFTING_PLATED_CORALIUM_BOOTS", "plated_coralium_boots", "corbootsp"),
        active("CRAFTING_CORALIUM_LONGBOW", "coralium_longbow", "corbow"),
        active("CORALIUM_PLAGUE_INFO_2", "coralium_antidote", "antidote_0"),
        active("CRAFTING_DREADIUM_1", "block_of_dreadium", "block_of_dreadium"),
        active("CRAFTING_CRYSTALLIZER_1", "crystallizer", "crystallizer"),
        active("CRAFTING_DREAD_CLOTH", "dread_cloth", "dreadcloth"),
        active("CRAFTING_DREADIUM_PLATE", "dreadium_plate", "dreadplate"),
        active("CRAFTING_DREADIUM_HILT", "dreadium_katana_hilt", "dreadhilt"),
        active("CRAFTING_DREADIUM_BLADE", "dreadium_katana_blade", "dreadblade"),
        active("CRAFTING_SKIN_OF_OMOTHOL", "skin_of_omothol", "skin_2"),
        active("CRAFTING_NECRONOMICON_O", "omothol_necronomicon", "necronomicon_omt"),
        active("CRAFTING_DREADIUM_SAMURAI_HELMET", "dreadium_samurai_helmet", "dreadiumsamuraihelmet"),
        active("CRAFTING_DREADIUM_SAMURAI_CHESTPLATE", "dreadium_samurai_chestplate", "dreadiumsamuraiplate"),
        active("CRAFTING_DREADIUM_SAMURAI_LEGGINGS", "dreadium_samurai_leggings", "dreadiumsamurailegs"),
        active("CRAFTING_DREADIUM_SAMURAI_BOOTS", "dreadium_samurai_boots", "dreadiumsamuraiboots"),
        active("CRAFTING_DREADIUM_KATANA", "dreadium_katana", "dreadkatana"),
        active("DREAD_PLAGUE_INFO_2", "dread_antidote", "antidote_1"),
        active("CRAFTING_LIFE_CRYSTAL_1", "life_crystal", "lifecrystal"),
        active("CRAFTING_ETHAXIUM_INGOT_1", "ethaxium_ingot", "ethaxiumingot"),
        active("CRAFTING_ETHAXIUM_PILLAR", "ethaxium_pillar", "ethaxiumpillar"),
        active("CRAFTING_DARK_ETHAXIUM_PILLAR", "dark_ethaxium_pillar", "darkethaxiumpillar"),
        active("CRAFTING_COIN", "coin", "coin_alt"),
        active("CRAFTING_CRYSTAL_BAG_1", "crystalbag_small", "crystalbag_small"),
        active("CRAFTING_MATERIALIZER_1", "materializer", "materializer"),
        active("CRAFTING_ABYSSALNOMICON_1", "abyssalnomicon", "abyssalnomicon"),
        active("CRAFTING_PORTAL_ANCHOR_1", "portal_anchor", "portal_anchor"),
        active("CRAFTING_ENERGY_PEDESTAL_1", "energypedestal", "energypedestal"),
        active("CRAFTING_MONOLITH_PILLAR", "monolith_pillar", "monolithpillar"),
        active("CRAFTING_RITUAL_CHARM", "charm", "charm_0"),
        active("CRAFTING_SACRIFICIAL_ALTAR_1", "sacrificialaltar", "sacrificialaltar"),
        active("CRAFTING_ENERGY_COLLECTOR", "energycollector", "energycollector"),
        active("CRAFTING_ENERGY_RELAY", "energyrelay", "energyrelay"),
        active("CRAFTING_RENDING_PEDESTAL", "rending_pedestal", "rendingpedestal"),
        active("CRAFTING_STONE_TABLET", "stone_tablet", "stonetablet"),
        active("CRAFTING_STATE_TRANSFORMER_1", "state_transformer", "statetransformer"),
        active("CRAFTING_ENERGY_DEPOSITIONER_1", "energydepositioner", "energydepositioner"),
        active("CRAFTING_ODB_CORE", "odb_core", "odbcore"),
        active("CRAFTING_ODB", "oblivion_deathbomb", "odb"),
        active("CRAFTING_CARBON_CLUSTER", "carbon_cluster", "carboncluster"),
        active("CRAFTING_DENSE_CARBON_CLUSTER", "dense_carbon_cluster", "densecarboncluster"),
        active("CRAFTING_CRATE", "crate", "crate"),
        active("CRAFTING_DECORATIVE_AZATHOTH_STATUE", "decorative_cthulhu_statue", "decorativestatue_0"),
        active("CRAFTING_DECORATIVE_CTHULHU_STATUE", "decorative_hastur_statue", "decorativestatue_1"),
        active("CRAFTING_DECORATIVE_HASTUR_STATUE", "decorative_jzahar_statue", "decorativestatue_2"),
        active("CRAFTING_DECORATIVE_JZAHAR_STATUE", "decorative_azathoth_statue", "decorativestatue_3"),
        active("CRAFTING_DECORATIVE_NYARLATHOTEP_STATUE", "decorative_nyarlathotep_statue", "decorativestatue_4"),
        active("CRAFTING_DECORATIVE_YOG_SOTHOTH_STATUE", "decorative_yog_sothoth_statue", "decorativestatue_5"),
        active("CRAFTING_DECORATIVE_SHUB_NIGGURATH_STATUE", "decorative_shub_niggurath_statue", "decorativestatue_6"),
        active("PE_UPGRADING_2", "ring", "ring")
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

    private static Map.Entry<String, Definition> active(String legacyPageId, String resultPath,
                                                         String... recipePaths) {
        return Map.entry(legacyPageId, new Definition(Arrays.stream(recipePaths).map(ACRef::id).toList(),
            List.of(ACRef.id(resultPath)), ""));
    }

    private static Map.Entry<String, Definition> blocked(String legacyPageId, String reason) {
        return Map.entry(legacyPageId, new Definition(List.of(), List.of(), reason));
    }

    public record Definition(List<ResourceLocation> recipeIds, List<ResourceLocation> resultIds,
                             String blockedReason) {
        public Definition {
            recipeIds = List.copyOf(recipeIds);
            resultIds = List.copyOf(resultIds);
            blockedReason = blockedReason == null ? "" : blockedReason;
            if (recipeIds.isEmpty() == blockedReason.isBlank())
                throw new IllegalArgumentException("recipe page must be active or explicitly blocked");
            if (recipeIds.isEmpty() != resultIds.isEmpty())
                throw new IllegalArgumentException("active recipe page must declare a result");
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
package com.shinoow.abyssalcraft.system.knowledge;

import java.util.HashSet;
import java.util.Set;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.knowledge.condition.ConditionProcessorRegistry;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.KnowledgePredicate;
import com.shinoow.abyssalcraft.system.knowledge.condition.NecronomiconCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import com.shinoow.abyssalcraft.system.data.NecromancyData;
import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;

/** Permanent datagen invariants for the RR-KNOWLEDGE catalog and store. */
public final class KnowledgeSystemSelfTest {

    private KnowledgeSystemSelfTest() {}

    public static void run() {
        KnowledgeContent.bootstrap();
        KnowledgeContent.bootstrapOfferings();
        require(KnowledgeContent.researches().size() == 42, "research catalog is not 42");
        require(KnowledgeContent.conditions().size() == 42, "condition catalog is not 42");

        Set<String> researchIds = new HashSet<>();
        for (IResearchItem research : KnowledgeContent.researches()) {
            require(researchIds.add(research.getID().toString()), "duplicate research " + research.getID());
            require(research.getRequiredLevel() == 0 && research.getPointsCost() == 0,
                "legacy research economy changed for " + research.getID());
            require(research.getUnlockConditions().length == 1,
                "research must have one catalog condition " + research.getID());
        }

        ConditionProcessorRegistry processors = ConditionProcessorRegistry.instance();
        for (IUnlockCondition condition : KnowledgeContent.conditions()) {
            if (condition.getType() >= 0 && condition.getType() != NecronomiconCondition.TYPE) {
                require(processors.hasProcessor(condition.getType()), "missing processor " + condition.getType());
            }
            validateEntityIds(condition);
        }

        int offerings = 0;
        for (KnowledgeType type : KnowledgeType.values()) {
            offerings += ResearchRegistry.instance().getOfferingsForType(type).size();
        }
        require(offerings == 11, "offering catalog is not 11");

        CompoundTag tag = new CompoundTag();
        NecroData data = new NecroData(tag);
        require(data.triggerEntityUnlock("abyssalcraft:ghoul"), "first mutation was ignored");
        require(!data.triggerEntityUnlock("abyssalcraft:ghoul"), "duplicate mutation changed store");
        require(data.unlockAllKnowledge(true) && !data.unlockAllKnowledge(true), "boolean mutation is not idempotent");

        NecromancyData snapshots = new NecromancyData();
        for (int i = 0; i < 21; i++) snapshots.storeData("mob-" + i, new CompoundTag(), i % 3);
        require(snapshots.getData().size() == 20, "necromancy snapshot cap is not 20");
        require(snapshots.getDataForName("mob-0") == null && snapshots.getDataForName("mob-20") != null,
            "necromancy snapshot eviction order changed");
        CompoundTag saved = snapshots.serialize();
        require(NecromancyData.load(saved).getData().size() == 20, "necromancy snapshot round-trip failed");
        require(NecromancyData.crystalSize(0.74F) == 0 && NecromancyData.crystalSize(0.75F) == 1
            && NecromancyData.crystalSize(1.5F) == 2, "necromancy crystal-size boundaries changed");
        require(java.util.Arrays.equals(ComplexConfig.parsePortalColor(java.util.List.of(255, 255, 255)),
            new int[] {255, 255, 255}),
            "portal color parser changed");
        require(java.util.Arrays.equals(ComplexConfig.parseCoraliumOre(java.util.List.of(12, 8, 40)),
            new int[] {12, 8, 40}),
            "coralium ore parser changed");
        require("CbA".equals(EffectHooks.invertName("AbC")), "anti-player name inversion changed");

        System.out.printf("RR_KNOWLEDGE_SELF_TEST_OK research=%d conditions=%d offerings=%d%n",
            KnowledgeContent.researches().size(), KnowledgeContent.conditions().size(), offerings);
    }

    private static void validateEntityIds(IUnlockCondition condition) {
        Object value = condition.getConditionObject();
        if (condition.getType() == 1) {
            requireEntity((String) value);
        } else if (condition.getType() == 4 || condition.getType() == 11) {
            for (String id : (String[]) value) {
                requireEntity(id);
            }
        } else if (condition.getType() == 6) {
            for (String id : ((KnowledgePredicate) value).ids()) {
                requireEntity(id);
            }
        }
    }

    private static void requireEntity(String id) {
        require(BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.parse(id)), "missing entity " + id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
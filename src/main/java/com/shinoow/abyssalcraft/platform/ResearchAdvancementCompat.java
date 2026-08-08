package com.shinoow.abyssalcraft.platform;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSync;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.KnowledgePredicate;
import com.shinoow.abyssalcraft.system.knowledge.condition.NecronomiconCondition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Loader bridge for the visible advancements mirroring Necronomicon research. */
public final class ResearchAdvancementCompat {

    private static final String PREFIX = "research/";
    private static final Set<UUID> INTERNAL_UPDATES = ConcurrentHashMap.newKeySet();
    private static final Set<String> CATEGORIES = Set.of("biome", "dimension", "entity", "misc", "book");

    private ResearchAdvancementCompat() {}

    public static void award(ServerPlayer player, IResearchItem research) {
        internal(player, () -> {
            awardId(player, ACRef.id(PREFIX + "root"));
            awardId(player, ACRef.id(PREFIX + "category/" + category(research)));
            awardId(player, advancementId(research));
        });
    }

    public static void backfill(ServerPlayer player) {
        NecroData data = NecroDataCapability.get(player);
        boolean changed = false;
        for (IResearchItem research : KnowledgeContent.researches()) {
            if (isDone(player, advancementId(research))) {
                changed |= data.completeResearch(research.getID().toString());
            }
        }
        synchronize(player);
        if (changed) {
            KnowledgeSync.full(player);
        }
    }

    public static void synchronize(ServerPlayer player) {
        NecroData data = NecroDataCapability.get(player);
        boolean allKnowledge = data.hasUnlockedAllKnowledge();
        Set<String> completed = Set.copyOf(data.getCompletedResearches());
        Set<String> unlockedCategories = new java.util.HashSet<>();
        internal(player, () -> {
            for (IResearchItem research : KnowledgeContent.researches()) {
                boolean unlocked = allKnowledge || completed.contains(research.getID().toString());
                if (unlocked) {
                    awardId(player, advancementId(research));
                    unlockedCategories.add(category(research));
                } else {
                    revokeId(player, advancementId(research));
                }
            }
            for (String category : CATEGORIES) {
                ResourceLocation id = ACRef.id(PREFIX + "category/" + category);
                if (allKnowledge || unlockedCategories.contains(category)) awardId(player, id);
                else revokeId(player, id);
            }
            ResourceLocation root = ACRef.id(PREFIX + "root");
            if (allKnowledge || !completed.isEmpty()) awardId(player, root);
            else revokeId(player, root);
        });
    }

    public static boolean recordGranted(ServerPlayer player, ResourceLocation advancementId) {
        if (!"abyssalcraft".equals(advancementId.getNamespace())
            || !advancementId.getPath().startsWith(PREFIX)
            || advancementId.getPath().equals(PREFIX + "root")
            || advancementId.getPath().startsWith(PREFIX + "category/")) {
            return false;
        }
        if (INTERNAL_UPDATES.contains(player.getUUID())) {
            return true;
        }
        String researchPath = advancementId.getPath().substring(PREFIX.length());
        for (IResearchItem research : KnowledgeContent.researches()) {
            if (!research.getID().getPath().equals(researchPath)) continue;
            if (NecroDataCapability.get(player).completeResearch(research.getID().toString())) {
                award(player, research);
                KnowledgeSync.full(player);
            }
            return true;
        }
        return false;
    }

    public static ResourceLocation advancementId(IResearchItem research) {
        return ACRef.id(PREFIX + research.getID().getPath());
    }

    public static String category(IResearchItem research) {
        int type = research.getUnlockConditions()[0].getType();
        return switch (type) {
            case 0, 3, 5 -> "biome";
            case 2 -> "dimension";
            case 1, 4, 6, 11 -> "entity";
            case 12 -> "book";
            default -> "misc";
        };
    }

    public static String conditionTranslationKey(IResearchItem research) {
        int type = research.getUnlockConditions()[0].getType();
        String label = switch (type) {
            case 0 -> "visit_biome";
            case 3, 5 -> "visit_any_biome";
            case 1 -> "defeat_entity";
            case 4, 6 -> "defeat_any_entity";
            case 11 -> "defeat_all_entities";
            case 2 -> "visit_dimension";
            case 12 -> "open_book";
            default -> "experience";
        };
        return "gui.abyssalcraft.necronomicon.patchouli.research.condition." + label;
    }

    public static String conditionTarget(IResearchItem research) {
        IUnlockCondition condition = research.getUnlockConditions()[0];
        Object target = condition.getConditionObject();
        if (target instanceof String[] values) return String.join(", ", values);
        if (target instanceof KnowledgePredicate predicate) {
            return predicate.ids().stream().sorted().collect(java.util.stream.Collectors.joining(", "));
        }
        if (condition instanceof NecronomiconCondition book) {
            return Integer.toString(book.requiredBookType());
        }
        return String.valueOf(target);
    }

    private static void awardId(ServerPlayer player, ResourceLocation id) {
        var manager = player.getServer().getAdvancements();
        //? if forge {
        net.minecraft.advancements.Advancement advancement = manager.getAdvancement(id);
        if (advancement == null) return;
        net.minecraft.advancements.AdvancementProgress progress =
            player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
        //?} else {
        /*net.minecraft.advancements.AdvancementHolder advancement = manager.get(id);
        if (advancement == null) return;
        net.minecraft.advancements.AdvancementProgress progress =
            player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
        *///?}
    }

    private static void revokeId(ServerPlayer player, ResourceLocation id) {
        var manager = player.getServer().getAdvancements();
        //? if forge {
        net.minecraft.advancements.Advancement advancement = manager.getAdvancement(id);
        if (advancement == null) return;
        net.minecraft.advancements.AdvancementProgress progress =
            player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getCompletedCriteria()) {
            player.getAdvancements().revoke(advancement, criterion);
        }
        //?} else {
        /*net.minecraft.advancements.AdvancementHolder advancement = manager.get(id);
        if (advancement == null) return;
        net.minecraft.advancements.AdvancementProgress progress =
            player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getCompletedCriteria()) {
            player.getAdvancements().revoke(advancement, criterion);
        }
        *///?}
    }

    private static boolean isDone(ServerPlayer player, ResourceLocation id) {
        var manager = player.getServer().getAdvancements();
        //? if forge {
        net.minecraft.advancements.Advancement advancement = manager.getAdvancement(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
        //?} else {
        /*net.minecraft.advancements.AdvancementHolder advancement = manager.get(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
        *///?}
    }

    private static void internal(ServerPlayer player, Runnable action) {
        UUID id = player.getUUID();
        INTERNAL_UPDATES.add(id);
        try {
            action.run();
        } finally {
            INTERNAL_UPDATES.remove(id);
        }
    }
}
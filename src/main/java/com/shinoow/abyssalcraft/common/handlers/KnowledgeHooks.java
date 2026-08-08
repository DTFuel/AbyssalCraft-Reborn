package com.shinoow.abyssalcraft.common.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.ShouldSyncMessage;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.advancement.AdvancementKnowledge;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeSync;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/**
 * Knowledge event hooks (owned by PS-11): fork-free callbacks that record Necronomicon knowledge triggers
 * into a player's necrodata (PS-2's {@link NecroData}) as they play, faithful to the 1.12.2
 * {@code common.handlers.KnowledgeEventHandler}. The knowledge subsystem (PS-8) later reads these triggers to
 * unlock research. The loader-specific game-bus subscription lives in {@code platform/GameHooksCompat}, which
 * calls these methods.
 */
public final class KnowledgeHooks {

    private static final Map<UUID, Integer> DIMENSION_SYNC_TIMERS = new HashMap<>();

    private KnowledgeHooks() {}

    /** Record that {@code player} killed {@code victim} (entity-encounter trigger). */
    public static void onEntityKilled(Player player, LivingEntity victim) {
        if (player.level().isClientSide) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        NecroData data = NecroDataCapability.get(player);
        if (data.triggerEntityUnlock(id.toString()) && player instanceof ServerPlayer serverPlayer) {
            completeAndSync(serverPlayer, 1, id.toString(), -1);
        }
    }

    /** Record that {@code player} entered dimension {@code dimension}. */
    public static void onDimensionChanged(Player player, ResourceKey<Level> dimension) {
        if (player.level().isClientSide) {
            return;
        }
        String id = dimension.location().toString();
        NecroData data = NecroDataCapability.get(player);
        if (data.triggerDimensionUnlock(id) && player instanceof ServerPlayer serverPlayer) {
            com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent.completeAvailable(serverPlayer, -1);
            KnowledgeSync.unlock(serverPlayer, 2, id);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            scheduleSync(serverPlayer);
        }
    }

    /** Sample the current dynamic biome every 200 ticks. */
    public static void onPlayerTick(ServerPlayer player) {
        Integer remaining = DIMENSION_SYNC_TIMERS.get(player.getUUID());
        if (remaining != null) {
            if (remaining > 1) {
                DIMENSION_SYNC_TIMERS.put(player.getUUID(), remaining - 1);
            } else {
                DIMENSION_SYNC_TIMERS.remove(player.getUUID());
                KnowledgeSync.full(player);
            }
        }
        if (player.tickCount % 200 != 0) {
            if (player.tickCount % 20 == 0) {
                scanArtifacts(player);
            }
            return;
        }
        scanArtifacts(player);
        player.level().getBiome(BlockPos.containing(player.position())).unwrapKey().ifPresent(key -> {
            String id = key.location().toString();
            NecroData data = NecroDataCapability.get(player);
            if (data.triggerBiomeUnlock(id)) {
                completeAndSync(player, 0, id, -1);
            }
        });
    }

    /** Complete the four book-tier research entries when a real book is opened. */
    public static void onBookOpened(ServerPlayer player, int bookType) {
        String whisper = "book/" + bookType;
        NecroData data = NecroDataCapability.get(player);
        boolean unlockedWhisper = data.triggerWhisperUnlock(whisper);
        int completed = com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent
            .completeAvailable(player, bookType);
        if (unlockedWhisper) {
            KnowledgeSync.unlock(player, 5, whisper);
        }
        if (completed > 0) {
            KnowledgeSync.full(player);
        } else if (ACConfig.syncDataOnBookOpening.get()) {
            ACNetwork.sendToPlayer(player, new ShouldSyncMessage(0L));
        }
    }

    /** Record a completed AC progression advancement and synchronize its Necronomicon entry. */
    public static boolean onAdvancementEarned(ServerPlayer player, ResourceLocation id) {
        if (com.shinoow.abyssalcraft.platform.ResearchAdvancementCompat.recordGranted(player, id)) {
            return true;
        }
        if (!AdvancementKnowledge.contains(id)) {
            return false;
        }
        NecroData data = NecroDataCapability.get(player);
        if (!data.triggerAdvancementUnlock(id.toString())) {
            return false;
        }
        KnowledgeSync.trigger(player, 7, id.toString());
        return true;
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        DIMENSION_SYNC_TIMERS.remove(player.getUUID());
    }

    public static void scheduleSync(ServerPlayer player) {
        DIMENSION_SYNC_TIMERS.put(player.getUUID(), ACConfig.knowledgeSyncDelay.get());
    }

    public static void sync(ServerPlayer player) {
        KnowledgeSync.full(player);
    }

    private static void completeAndSync(ServerPlayer player, int type, String id, int bookType) {
        com.shinoow.abyssalcraft.system.knowledge.KnowledgeContent.completeAvailable(player, bookType);
        KnowledgeSync.trigger(player, type, id);
    }

    /** Record plague exposure as a misc trigger. */
    public static void onPlagueTick(ServerPlayer player, String plagueId) {
        NecroData data = NecroDataCapability.get(player);
        if (data.triggerMiscUnlock(plagueId)) {
            completeAndSync(player, 6, plagueId, -1);
        }
    }

    /** Record acquisition of an AbyssalCraft artifact from any inventory-producing game path. */
    public static boolean onArtifactAcquired(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"abyssalcraft".equals(id.getNamespace())) {
            return false;
        }
        NecroData data = NecroDataCapability.get(player);
        if (!data.triggerArtifactUnlock(id.toString())) {
            return false;
        }
        completeAndSync(player, 3, id.toString(), -1);
        return true;
    }

    /** Record a lost-page item action after its server-owned knowledge payload has been validated. */
    public static boolean onPageStudied(ServerPlayer player, String knowledge) {
        if (!validTrigger(knowledge)) {
            return false;
        }
        NecroData data = NecroDataCapability.get(player);
        if (!data.triggerPageUnlock(knowledge)) {
            return false;
        }
        completeAndSync(player, 4, knowledge, -1);
        return true;
    }

    /** Record a server-side Necronomicon action as a whisper event. */
    public static boolean onWhisper(ServerPlayer player, String whisper) {
        if (!validTrigger(whisper)) {
            return false;
        }
        NecroData data = NecroDataCapability.get(player);
        if (!data.triggerWhisperUnlock(whisper)) {
            return false;
        }
        completeAndSync(player, 5, whisper, -1);
        return true;
    }

    /** Record successful server-side Necronomicon action dispatch. */
    public static boolean onAction(ServerPlayer player, String action) {
        if (!validTrigger(action)) {
            return false;
        }
        NecroData data = NecroDataCapability.get(player);
        String trigger = "action/" + action;
        if (!data.triggerMiscUnlock(trigger)) {
            return false;
        }
        completeAndSync(player, 6, trigger, -1);
        return true;
    }

    private static void scanArtifacts(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            onArtifactAcquired(player, player.getInventory().getItem(slot));
        }
    }

    private static boolean validTrigger(String value) {
        return value != null && !value.isBlank() && value.length() <= 256;
    }
}

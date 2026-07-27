package com.shinoow.abyssalcraft.platform;

import com.shinoow.abyssalcraft.common.handlers.KnowledgeHooks;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyHostileMob;
import com.shinoow.abyssalcraft.system.advancement.AdvancementKnowledge;
import com.shinoow.abyssalcraft.world.ACDimensions;

//? if forge {
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.BlockEvent;
//?} else {
/*import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
*///?}

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Compat: game/runtime event-bus subscription for the knowledge hooks (loader axis).
 *
 * <p>The event classes ({@code LivingDeathEvent}, {@code PlayerEvent.PlayerChangedDimensionEvent}) live in
 * different packages on Forge and NeoForge; this class holds the forked imports + subscription and delegates
 * to the fork-free {@link KnowledgeHooks} (PS-11). Wired once from the main class {@code init}.
 */
public final class GameHooksCompat {

    private GameHooksCompat() {}

    /** Subscribe the knowledge event hooks to the game/runtime event bus. Server + client safe. */
    public static void attach() {
        EventBuses.game().addListener((LivingDeathEvent event) -> onLivingDeath(event));
        EventBuses.game().addListener((AdvancementEvent.AdvancementEarnEvent event) -> onAdvancementEarned(event));
        //? if forge {
        EventBuses.game().addListener((LivingHurtEvent event) ->
            com.shinoow.abyssalcraft.common.handlers.EffectHooks.onLivingHurt(event.getEntity(), event.getSource()));
        //?} else {
        /*EventBuses.game().addListener((LivingDamageEvent.Pre event) ->
            com.shinoow.abyssalcraft.common.handlers.EffectHooks.onLivingHurt(event.getEntity(), event.getSource()));
        *///?}
        EventBuses.game().addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> onChangedDimension(event));
        EventBuses.game().addListener((PlayerEvent.PlayerLoggedInEvent event) -> onLoggedIn(event));
        EventBuses.game().addListener((PlayerEvent.PlayerLoggedOutEvent event) -> onLoggedOut(event));
        EventBuses.game().addListener((PlayerEvent.PlayerRespawnEvent event) -> onRespawn(event));
        EventBuses.game().addListener((BabyEntitySpawnEvent event) -> onBabySpawn(event));
        EventBuses.game().addListener((EntityEvent.Size event) -> onEntitySize(event));
        EventBuses.game().addListener((AnimalTameEvent event) -> onAnimalTame(event));
        EventBuses.game().addListener((BonemealEvent event) -> onBonemeal(event));
        EventBuses.game().addListener((BlockEvent.BlockToolModificationEvent event) -> onToolModification(event));
        EventBuses.game().addListener((PlayerSetSpawnEvent event) -> onSetSpawn(event));
        EventBuses.game().addListener(GameHooksCompat::onSleep);
        //? if forge {
        EventBuses.game().addListener((TickEvent.PlayerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END && event.player instanceof net.minecraft.server.level.ServerPlayer player) {
                KnowledgeHooks.onPlayerTick(player);
                if (Boolean.getBoolean("abyssalcraft.rrNetValidation")) {
                    com.shinoow.abyssalcraft.net.RRNetValidation.serverTick(player);
                }
            }
        });
        //?} else {
        /*EventBuses.game().addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                KnowledgeHooks.onPlayerTick(player);
                if (Boolean.getBoolean("abyssalcraft.rrNetValidation")) {
                    com.shinoow.abyssalcraft.net.RRNetValidation.serverTick(player);
                }
            }
        });
        *///?}
    }

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        //? if forge {
        KnowledgeHooks.onAdvancementEarned(player, event.getAdvancement().getId());
        //?} else {
        /*KnowledgeHooks.onAdvancementEarned(player, event.getAdvancement().id());
        *///?}
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player) && event.getSource().getEntity() instanceof Player player) {
            KnowledgeHooks.onEntityKilled(player, event.getEntity());
        }
        if (victim.level() instanceof ServerLevel level) {
            storeNecromancySnapshot(level, victim);
            boolean spawned = com.shinoow.abyssalcraft.common.handlers.EffectHooks.onLivingDeath(level, victim);
            if (!spawned) {
                spawned = com.shinoow.abyssalcraft.common.handlers.EffectHooks
                    .applyConfiguredDemonTransformation(level, victim);
            }
            if (!spawned) convertToShadow(level, victim, event.getSource().getEntity());
        }
    }

    private static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (com.shinoow.abyssalcraft.common.handlers.PurgeHooks.isPurged(event.getParentA())) {
            event.setCanceled(true);
        }
    }

    private static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof com.shinoow.abyssalcraft.content.entity.ghoul.DepthsGhoul ghoul
                && ghoul.isBaby()) {
            event.setNewSize(event.getNewSize().scale(0.5F));
            //? if forge {
            event.setNewEyeHeight(event.getNewEyeHeight() * 0.5F);
            //?}
        }
    }

    private static void onAnimalTame(AnimalTameEvent event) {
        if (com.shinoow.abyssalcraft.common.handlers.PurgeHooks.isPurged(event.getTamer())) {
            event.setCanceled(true);
        }
    }

    private static void onBonemeal(BonemealEvent event) {
        if (!com.shinoow.abyssalcraft.common.handlers.PurgeHooks.isPurged(event.getLevel(), event.getPos())) return;
        if (!event.getLevel().isClientSide) {
            //? if forge {
            if (event.getBlock().getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock) {
            //?} else {
            /*if (event.isValidBonemealTarget()) {
            *///?}
                event.getStack().shrink(1);
            }
        }
        event.setCanceled(true);
    }

    private static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        //? if forge {
        if (event.getToolAction() != ToolActions.HOE_TILL) return;
        //?} else {
        /*if (event.getItemAbility() != ItemAbilities.HOE_TILL) return;
        *///?}
        if (!com.shinoow.abyssalcraft.common.handlers.PurgeHooks.isPurged(event.getLevel(), event.getPos())) return;
        if (!event.isSimulated() && !event.getContext().getLevel().isClientSide && event.getPlayer() != null) {
            //? if <1.21 {
            event.getHeldItemStack().hurtAndBreak(1, event.getPlayer(),
                player -> player.broadcastBreakEvent(event.getContext().getHand()));
            //?} else {
            /*event.getHeldItemStack().hurtAndBreak(1, event.getPlayer(),
                event.getContext().getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            *///?}
        }
        event.setCanceled(true);
    }

    private static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getNewSpawn() == null || !(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.getServer().getLevel(event.getSpawnLevel());
        if (level != null && com.shinoow.abyssalcraft.common.handlers.PurgeHooks
                .isPurged(level, event.getNewSpawn())) {
            event.setCanceled(true);
        }
    }

    //? if forge {
    private static void onSleep(PlayerSleepInBedEvent event) {
        event.getOptionalPos().filter(pos -> com.shinoow.abyssalcraft.common.handlers.PurgeHooks
            .isPurged(event.getEntity().level(), pos)).ifPresent(pos ->
                event.setResult(Player.BedSleepingProblem.NOT_POSSIBLE_HERE));
    }
    //?} else {
    /*private static void onSleep(CanPlayerSleepEvent event) {
        if (com.shinoow.abyssalcraft.common.handlers.PurgeHooks.isPurged(event.getLevel(), event.getPos())) {
            event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        }
    }
    *///?}

    private static void storeNecromancySnapshot(ServerLevel level, LivingEntity victim) {
        if (!(victim instanceof Mob) || isBoss(victim) || !victim.hasCustomName()) {
            return;
        }
        net.minecraft.nbt.CompoundTag tag = victim.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        tag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
            .getKey(victim.getType()).toString());
        com.shinoow.abyssalcraft.system.data.NecromancyData.get(level).storeData(
            victim.getName().getString(), tag,
            com.shinoow.abyssalcraft.system.data.NecromancyData.crystalSize(victim.getBbHeight()));
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof com.shinoow.abyssalcraft.content.entity.boss.ACBossMob
            || entity instanceof com.shinoow.abyssalcraft.content.entity.boss.EliteMob
            || entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // PlayerEvent.getEntity() already returns Player on both loaders.
        KnowledgeHooks.onDimensionChanged(event.getEntity(), event.getTo());
    }

    private static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            backfillAdvancementKnowledge(player);
            KnowledgeHooks.scheduleSync(player);
        }
    }

    private static void backfillAdvancementKnowledge(ServerPlayer player) {
        var manager = player.getServer().getAdvancements();
        for (AdvancementKnowledge.Entry entry : AdvancementKnowledge.ENTRIES) {
            //? if forge {
            net.minecraft.advancements.Advancement advancement = manager.getAdvancement(entry.id());
            if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                KnowledgeHooks.onAdvancementEarned(player, entry.id());
            }
            //?} else {
            /*net.minecraft.advancements.AdvancementHolder advancement = manager.get(entry.id());
            if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                KnowledgeHooks.onAdvancementEarned(player, entry.id());
            }
            *///?}
        }
    }

    private static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            KnowledgeHooks.onPlayerLoggedOut(player);
        }
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            KnowledgeHooks.scheduleSync(player);
        }
    }

    private static void convertToShadow(ServerLevel level, LivingEntity victim, net.minecraft.world.entity.Entity attacker) {
        if (isShadow(victim)) return;
        boolean shadowContext = level.dimension() == ACDimensions.DARK_REALM
            || attacker instanceof LivingEntity living && isShadow(living);
        if (!shadowContext || (!(victim instanceof Player) && !level.random.nextBoolean())) return;

        net.minecraft.world.entity.EntityType<? extends LegacyHostileMob> type;
        if (victim instanceof Player || victim.getBbHeight() >= 1.2F && victim.getBbHeight() < 2.2F) {
            type = LegacyEntities.SHADOW_MONSTER.get();
        } else if (victim.getBbHeight() >= 2.2F) {
            type = LegacyEntities.SHADOW_BEAST.get();
        } else {
            type = LegacyEntities.SHADOW_CREATURE.get();
        }
        LegacyHostileMob shadow = type.create(level);
        if (shadow == null) return;
        shadow.moveTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), victim.getXRot());
        if (victim.hasCustomName()) shadow.setCustomName(victim.getCustomName());
        shadow.setPersistenceRequired();
        level.addFreshEntity(shadow);
    }

    private static boolean isShadow(LivingEntity entity) {
        net.minecraft.world.entity.EntityType<?> type = entity.getType();
        return type == LegacyEntities.SHADOW_CREATURE.get() || type == LegacyEntities.SHADOW_MONSTER.get()
            || type == LegacyEntities.SHADOW_BEAST.get()
            || type == com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities.SHADOW_GHOUL.get()
            || type == com.shinoow.abyssalcraft.content.entity.boss.BossEntities.SACTHOTH.get();
    }
}

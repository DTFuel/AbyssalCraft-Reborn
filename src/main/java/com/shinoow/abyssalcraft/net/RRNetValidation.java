package com.shinoow.abyssalcraft.net;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.shinoow.abyssalcraft.content.block.demon.DemonBlocks;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.ShadowGhoul;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.ritual.GatekeeperStaffItem;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItems;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletItem;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.net.client.CleansingRitualMessage;
import com.shinoow.abyssalcraft.net.client.DisplayRoutesMessage;
import com.shinoow.abyssalcraft.net.client.DisruptionMessage;
import com.shinoow.abyssalcraft.net.client.EvilSheepMessage;
import com.shinoow.abyssalcraft.net.client.KnowledgeUnlockMessage;
import com.shinoow.abyssalcraft.net.client.NecroDataCapMessage;
import com.shinoow.abyssalcraft.net.client.PEStreamMessage;
import com.shinoow.abyssalcraft.net.client.RitualMessage;
import com.shinoow.abyssalcraft.net.client.RitualStartMessage;
import com.shinoow.abyssalcraft.net.client.ShouldSyncMessage;
import com.shinoow.abyssalcraft.net.client.SyncNecromancyDataMessage;
import com.shinoow.abyssalcraft.net.client.WindowPropertyMessage;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.spell.Spell;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Temporary real-network validation state. Removed after RR-NET live verification. */
public final class RRNetValidation {

    public static final BlockPos BASE = new BlockPos(8, 80, 8);
    public static final BlockPos FIRE = BASE.offset(0, 1, 0);
    public static final BlockPos HOST = BASE.offset(3, 1, 0);

    private static final Map<UUID, State> STATES = new HashMap<>();

    private RRNetValidation() {}

    public static void recordHandled(int id, net.minecraft.world.entity.player.Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.server.isSameThread()) throw new IllegalStateException("RR-NET server handler off-thread");
            STATES.computeIfAbsent(serverPlayer.getUUID(), ignored -> new State()).handled.set(id);
        } else {
            com.shinoow.abyssalcraft.platform.SideExecutor.runWhenClient(() -> () ->
                com.shinoow.abyssalcraft.client.network.RRNetClientValidation.recordHandled(id));
        }
    }

    public static void serverTick(ServerPlayer player) {
        if (!"RRNetClient".equals(player.getGameProfile().getName())) return;
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        if (state.player != player) {
            state = new State();
            state.player = player;
            STATES.put(player.getUUID(), state);
        }
        state.ticks++;
        if (!state.initialized) initialize(player, state);
        if (state.ticks % 5 != 0) return;
        advance(player, state);
    }

    private static void initialize(ServerPlayer player, State state) {
        ServerLevel level = player.serverLevel();
        level.getChunkAt(BASE);
        level.getEntitiesOfClass(LivingEntity.class, new AABB(BASE).inflate(32.0D), entity -> entity != player)
            .forEach(Entity::discard);
        for (int x = -5; x <= 12; x++) for (int z = -5; z <= 12; z++) {
            level.setBlock(BASE.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(BASE.offset(x, 0, z), Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlock(FIRE.below(), Blocks.NETHERRACK.defaultBlockState(), 3);
        level.setBlock(FIRE, DemonBlocks.MIMIC_FIRE.get().defaultBlockState(), 3);
        level.setBlock(HOST, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(HOST, Blocks.CHEST.defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(HOST);
        if (blockEntity != null) {
            com.shinoow.abyssalcraft.system.transfer.ItemTransferHost host =
                ItemTransferAttachmentCompat.getOrCreate(blockEntity);
            if (host != null) host.setTransferRunning(false);
        }

        state.zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
        state.shadowGhoul = GhoulEntities.SHADOW_GHOUL.get().create(level);
        state.pig = net.minecraft.world.entity.EntityType.PIG.create(level);
        state.evilSheep = DemonEntities.EVIL_SHEEP.get().create(level);
        spawn(level, state.zombie, BASE.offset(0, 1, 5));
        spawn(level, state.shadowGhoul, BASE.offset(-3, 1, 5));
        spawn(level, state.pig, BASE.offset(3, 1, 5));
        spawn(level, state.evilSheep, BASE.offset(6, 1, 5));
        state.evilSheep.setKilledPlayer(player.getUUID(), player.getGameProfile().getName());

        player.teleportTo(BASE.getX() + 0.5D, BASE.getY(), BASE.getZ() - 3.5D);
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        state.initialized = true;
        state.stageTick = state.ticks;
        System.out.println("RR_NET_SERVER_READY player=RRNetClient");
    }

    private static void spawn(ServerLevel level, Entity entity, BlockPos pos) {
        if (entity == null) throw new IllegalStateException("RR-NET entity setup failed");
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 180.0F, 0.0F);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            com.shinoow.abyssalcraft.platform.MobSpawnCompat.finalizeTriggeredSpawn(level, mob);
            mob.setPersistenceRequired();
        }
        level.addFreshEntity(entity);
    }

    private static void advance(ServerPlayer player, State state) {
        long age = state.ticks - state.stageTick;
        switch (state.stage) {
            case 0 -> {
                if (!has(state, 0, 2)) return;
                require(player.level().isEmptyBlock(FIRE), "FireMessage did not extinguish mimic fire");
                BlockEntity blockEntity = player.level().getBlockEntity(HOST);
                require(blockEntity != null && ItemTransferAttachmentCompat.get(blockEntity).isTransferRunning(),
                    "ToggleStateMessage did not start transfer host");
                ItemStack tablet = new ItemStack(TransferContent.SPIRIT_TABLET.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, tablet);
                SpiritTabletItem.openMenu(player, InteractionHand.MAIN_HAND, tablet);
                next(state);
            }
            case 1 -> {
                if (!has(state, 1, 5)) return;
                require(SpiritTabletStorage.mode(player.getMainHandItem()) == 2,
                    "SpiritTabletMessage did not set mode");
                player.closeContainer();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RitualItems.STAFF_OF_RENDING.get()));
                next(state);
            }
            case 2 -> {
                if (!has(state, 3)) return;
                require(state.shadowGhoul.getHealth() < state.shadowGhoul.getMaxHealth(),
                    "StaffOfRendingMessage did not damage target");
                require(ItemDataCompat.getInt(player.getMainHandItem(), "energyShadow", 0) > 0,
                    "StaffOfRendingMessage did not store rending energy");
                player.setItemInHand(InteractionHand.MAIN_HAND,
                    new ItemStack(RitualItems.STAFF_OF_THE_GATEKEEPER.get()));
                next(state);
            }
            case 3 -> {
                if (!has(state, 4)) return;
                require(((GatekeeperStaffItem) player.getMainHandItem().getItem()).mode(player.getMainHandItem()) == 1,
                    "StaffModeMessage did not toggle mode");
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BookItems.NECRONOMICON.get()));
                next(state);
            }
            case 4 -> {
                if (!has(state, 6, 7)) return;
                require(player.containerMenu instanceof com.shinoow.abyssalcraft.content.menu.spellbook.SpellbookMenu,
                    "OpenSpellbookMessage did not open Spellbook");
                player.closeContainer();
                ItemStack book = new ItemStack(BookItems.NECRONOMICON.get());
                ((IEnergyContainerItem) book.getItem()).setEnergy(book, 1000);
                player.getInventory().add(book);
                Spell spell = SpellRegistry.instance().getSpell("lifedrain");
                ItemStack scroll = ScrollItem.inscribe(new ItemStack(ScrollItems.BASIC.get()), spell);
                player.setItemInHand(InteractionHand.MAIN_HAND, scroll);
                player.startUsingItem(InteractionHand.MAIN_HAND);
                next(state);
            }
            case 5 -> {
                if (!has(state, 8) && age < 80) return;
                require(has(state, 8), "MobSpellMessage was not received after charging");
                ItemStack cage = new ItemStack(RitualItems.INTERDIMENSIONAL_CAGE.get());
                ((IEnergyContainerItem) cage.getItem()).setEnergy(cage, 1000);
                player.setItemInHand(InteractionHand.MAIN_HAND, cage);
                next(state);
            }
            case 6 -> {
                if (!has(state, 9)) return;
                require(state.pig.isRemoved(), "InterdimensionalCageMessage did not capture target");
                require(ItemDataCompat.copyData(player.getMainHandItem()).contains(InterdimensionalCageItem.ENTITY_KEY),
                    "Interdimensional cage did not store entity NBT");
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
                state.diamonds = count(player, Items.DIAMOND);
                next(state);
            }
            case 7 -> {
                if (!has(state, 10)) return;
                require(count(player, Items.DIAMOND) == state.diamonds,
                    "retired TransferStackMessage changed inventory");
                sendClientMatrix(player, state);
                next(state);
            }
            case 8 -> {
                if (age < 400) return;
                require(has(state, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                    "server did not receive every C2S packet: " + state.handled);
                require(state.sent.cardinality() == 12, "server did not send every S2C packet");
                System.out.println("RR_NET_SERVER_MATRIX_OK ids=23 c2s=11 s2c=12 delayed=20s threads=main");
                state.stage = 9;
            }
            default -> { }
        }
    }

    private static void sendClientMatrix(ServerPlayer player, State state) {
        CompoundTag data = new CompoundTag();
        data.putString("RRNet", "matrix");
        CompoundTag routeRoot = new CompoundTag();
        ListTag routes = new ListTag();
        ListTag route = new ListTag();
        route.add(LongTag.valueOf(BASE.asLong()));
        route.add(LongTag.valueOf(HOST.asLong()));
        routes.add(route);
        routeRoot.put("Routes", routes);
        send(player, state, 11, new WindowPropertyMessage(player.containerMenu.containerId, 0, 7));
        send(player, state, 12, new RitualMessage("cleansing", "", BASE, false));
        send(player, state, 13, new RitualStartMessage(BASE, "cleansing", 0, 80));
        send(player, state, 14, new CleansingRitualMessage(BASE.getX(), BASE.getZ(), 0, false));
        send(player, state, 15, new DisruptionMessage("CTHULHU", "lightning", BASE));
        send(player, state, 16, new EvilSheepMessage(player.getUUID(), player.getGameProfile().getName(), state.evilSheep.getId()));
        send(player, state, 17, new KnowledgeUnlockMessage(1, "abyssalcraft:ghoul"));
        send(player, state, 18, new NecroDataCapMessage(data.copy()));
        send(player, state, 19, new PEStreamMessage(BASE, HOST));
        send(player, state, 20, new ShouldSyncMessage(player.level().getGameTime()));
        send(player, state, 21, new SyncNecromancyDataMessage(data.copy()));
        send(player, state, 22, new DisplayRoutesMessage(routeRoot));
    }

    private static void send(ServerPlayer player, State state, int id,
                             com.shinoow.abyssalcraft.platform.NetworkChannel.ACPacket packet) {
        state.sent.set(id);
        ACNetwork.sendToPlayer(player, packet);
    }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static boolean has(State state, int... ids) {
        for (int id : ids) if (!state.handled.get(id)) return false;
        return true;
    }

    private static void next(State state) {
        state.stage++;
        state.stageTick = state.ticks;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class State {
        final BitSet handled = new BitSet(23);
        final BitSet sent = new BitSet(23);
        boolean initialized;
        int stage;
        long ticks;
        long stageTick;
        int diamonds;
        Zombie zombie;
        ShadowGhoul shadowGhoul;
        Pig pig;
        EvilAnimal evilSheep;
        ServerPlayer player;
    }
}
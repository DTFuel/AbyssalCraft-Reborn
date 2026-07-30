package com.shinoow.abyssalcraft.content.block.ritual;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.RitualMessage;
import com.shinoow.abyssalcraft.net.client.RitualStartMessage;
import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.disruption.CorruptionRegistry;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;
import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeGate;
import com.shinoow.abyssalcraft.system.knowledge.ResearchRegistry;
import com.shinoow.abyssalcraft.system.ritual.Ritual;
import com.shinoow.abyssalcraft.system.ritual.RitualHost;
import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.system.ritual.RitualRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
//? if <1.21 {
import net.minecraft.world.entity.MobType;
//?} else {
/*import net.minecraft.tags.EntityTypeTags;
*///?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

/**
 * Ritual altar block entity (owned by content/block/ritual): the ritual hub that consumes the PE it drains
 * from the held Necronomicon (CR-58) and the offerings on its surrounding pedestals to complete a
 * {@link Ritual} (PS-6). Faithful to the 1.12.2 {@code TileEntityRitualAltar} core: a right click with the
 * Necronomicon gathers the eight ring pedestals' offerings, resolves the ritual via {@link RitualRegistry},
 * checks the book holds enough Potential Energy, then consumes the PE + offerings and runs the ritual.
 *
 * <p>Faithful-simplified (deferred to PS-6b): the timed chant/particle ceremony with PE drained over the
 * ritual duration, the living sacrifice, the research gate, and the disruption-on-failure -- the pilot
 * completes instantly once the offerings + PE are satisfied.
 */
public class RitualAltarBlockEntity extends ACBlockEntity implements RitualHost {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RITUAL_TICKS = 200;

    /** The eight ring pedestal offsets relative to the altar (faithful 1.12.2 {@code RitualUtil.PEDESTAL_POSITIONS}). */
    private static final List<BlockPos> PEDESTAL_OFFSETS = List.of(
        new BlockPos(-3, 0, 0), new BlockPos(0, 0, -3),
        new BlockPos(3, 0, 0), new BlockPos(0, 0, 3),
        new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2),
        new BlockPos(2, 0, 2), new BlockPos(2, 0, -2));

    private final NonNullList<ItemStack> center = NonNullList.withSize(1, ItemStack.EMPTY);
    private final NonNullList<ItemStack> lockedCenter = NonNullList.withSize(1, ItemStack.EMPTY);
    private final NonNullList<ItemStack> offeringSnapshot = NonNullList.withSize(8, ItemStack.EMPTY);
    private CeremonyPhase phase = CeremonyPhase.IDLE;
    private String ritualId = "";
    private UUID userUuid;
    private UUID sacrificeUuid;
    private int ritualTicks;
    private int durationTicks;
    private float consumedEnergy;
    private float energyPerSecond;
    private boolean sacrificeDead;
    private boolean sacrificeSeenSinceLoad;

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RitualBlocks.RITUAL_ALTAR_BE.get(), pos, state);
    }

    /** Attempt a ritual on a right click (server-side). */
    public void tryRitual(Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return;
        if (isPerformingRitual()) {
            feedback(player, "message.abyssalcraft.ritual.busy");
            return;
        }
        ItemStack bookStack = player.getItemInHand(hand);
        if (!(bookStack.getItem() instanceof NecronomiconItem book)) {
            return;
        }
        List<RitualPedestal> pedestals = collectPedestals(level, pos);
        if (pedestals.size() < PEDESTAL_OFFSETS.size()) {
            feedback(player, "message.abyssalcraft.ritual.no_structure");
            return;
        }
        List<ItemStack> offerings = new ArrayList<>();
        for (RitualPedestal pedestal : pedestals) {
            ItemStack offering = pedestal.getOffering();
            if (!offering.isEmpty()) {
                offerings.add(offering);
            }
        }
        Ritual ritual = RitualRegistry.instance().find(offerings, getCenterItem(), book.bookType(), level.dimension());
        if (ritual == null) {
            feedback(player, "message.abyssalcraft.ritual.no_ritual");
            return;
        }
        if (!researchUnlocked(ritual, player, book.bookType())) {
            feedback(player, "message.abyssalcraft.ritual.locked");
            return;
        }
        if (!ritual.canStart(level, pos, player)) {
            feedback(player, "message.abyssalcraft.ritual.invalid");
            return;
        }
        Mob sacrifice = ritual.requiresSacrifice() ? findSacrifice(level, pos) : null;
        if (ritual.requiresSacrifice() && sacrifice == null) {
            feedback(player, "message.abyssalcraft.ritual.no_sacrifice");
            return;
        }
        if (availableEnergy(player) + 0.001F < ritual.requiredEnergy()) {
            feedback(player, "message.abyssalcraft.ritual.no_energy");
            return;
        }
        if (!consumeOfferings(ritual, pedestals)) {
            feedback(player, "message.abyssalcraft.ritual.no_ritual");
            return;
        }

        int seconds = ritualDurationTicks(book.getMaxEnergy(bookStack), ritual.requiredEnergy()) / 20;
        phase = CeremonyPhase.CHANT;
        ritualId = ritual.name();
        userUuid = player.getUUID();
        sacrificeUuid = sacrifice == null ? null : sacrifice.getUUID();
        ritualTicks = 0;
        durationTicks = seconds * 20;
        consumedEnergy = 0;
        energyPerSecond = ritual.requiredEnergy() / seconds;
        sacrificeDead = false;
        sacrificeSeenSinceLoad = sacrifice != null;
        lockedCenter.set(0, getCenterItem().copy());
        if (sacrifice != null) sacrifice.addEffect(new MobEffectInstance(MobEffects.GLOWING, MAX_RITUAL_TICKS));
        setChanged();

        level.playSound(null, pos, randomChant(level), SoundSource.PLAYERS, 1.0F, 1.0F);
        sendToLevel(server, new RitualStartMessage(pos, ritual.name(), sacrifice == null ? 0 : sacrifice.getId(), durationTicks));
        feedback(player, "message.abyssalcraft.ritual.started");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RitualAltarBlockEntity altar) {
        if (!level.isClientSide && level instanceof ServerLevel server) altar.tickCeremony(server, pos);
    }

    private void tickCeremony(ServerLevel level, BlockPos pos) {
        if (!isPerformingRitual()) return;
        Ritual ritual = RitualRegistry.instance().getRitualById(ritualId);
        ServerPlayer user = userUuid == null ? null : level.getServer().getPlayerList().getPlayer(userUuid);
        if (ritual == null || user == null || user.level() != level || collectPedestals(level, pos).size() != 8
            || !sameStack(getCenterItem(), lockedCenter.get(0))) {
            fail(level, pos, user, true);
            return;
        }

        if (sacrificeUuid != null && !sacrificeDead) {
            Entity entity = level.getEntity(sacrificeUuid);
            if (entity == null) {
                if (!sacrificeSeenSinceLoad) {
                    fail(level, pos, user, true);
                    return;
                }
                sacrificeDead = true;
            } else {
                sacrificeSeenSinceLoad = true;
                if (!entity.isAlive()) sacrificeDead = true;
            }
        }

        ritualTicks++;
        level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 1.0D,
            pos.getZ() + 0.5D, 1, 0.1D, 0.1D, 0.1D, 0.0D);

        if (phase == CeremonyPhase.CHANT && ritualTicks % 20 == 0
            && consumedEnergy + 0.001F < ritual.requiredEnergy()) {
            float requested = Math.min(energyPerSecond, ritual.requiredEnergy() - consumedEnergy);
            float drained = drainEnergy(user, requested);
            consumedEnergy += drained;
            if (drained + 0.001F < requested) {
                fail(level, pos, user, true);
                return;
            }
            setChanged();
        }

        if (ritualTicks >= durationTicks) {
            if (consumedEnergy + 0.01F < ritual.requiredEnergy()) {
                fail(level, pos, user, true);
            } else if (ritual.requiresSacrifice() && !sacrificeDead) {
                phase = CeremonyPhase.WAIT_SACRIFICE;
                if (ritualTicks >= MAX_RITUAL_TICKS) fail(level, pos, user, true);
            } else {
                complete(level, pos, user, ritual);
            }
        }
    }

    private void complete(ServerLevel level, BlockPos pos, ServerPlayer user, Ritual ritual) {
        try {
            ritual.complete(level, pos, user);
            feedback(user, "message.abyssalcraft.ritual.success");
            sendToLevel(level, new RitualMessage(ritual.name(), "", pos, false));
            resetCeremony();
        } catch (RuntimeException exception) {
            LOGGER.error("Ritual {} failed during completion at {}", ritual.name(), pos, exception);
            fail(level, pos, user, true);
        }
    }

    private void fail(ServerLevel level, BlockPos pos, Player user, boolean disrupt) {
        String failedRitual = ritualId;
        if (disrupt && !ACConfig.no_disruptions.get()) {
            List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(16.0D));
            DeityType deity = DeityType.values()[level.random.nextInt(DeityType.values().length)];
            DisruptionHandler.instance().generate(deity, level, pos, players);
        }
        if (user != null) feedback(user, "message.abyssalcraft.ritual.failed");
        sendToLevel(level, new RitualMessage(failedRitual, "", pos, true));
        resetCeremony();
    }

    private static void sendToLevel(ServerLevel level, com.shinoow.abyssalcraft.platform.NetworkChannel.ACPacket message) {
        for (ServerPlayer player : level.players()) ACNetwork.sendToPlayer(player, message);
    }

    private void resetCeremony() {
        phase = CeremonyPhase.IDLE;
        ritualId = "";
        userUuid = null;
        sacrificeUuid = null;
        ritualTicks = 0;
        durationTicks = 0;
        consumedEnergy = 0;
        energyPerSecond = 0;
        sacrificeDead = false;
        sacrificeSeenSinceLoad = false;
        lockedCenter.set(0, ItemStack.EMPTY);
        for (int i = 0; i < offeringSnapshot.size(); i++) offeringSnapshot.set(i, ItemStack.EMPTY);
        setChanged();
    }

    private boolean consumeOfferings(Ritual ritual, List<RitualPedestal> pedestals) {
        List<Integer> available = new ArrayList<>();
        List<OfferingUse> planned = new ArrayList<>();
        for (int index = 0; index < pedestals.size(); index++) {
            ItemStack stack = pedestals.get(index).getOffering();
            offeringSnapshot.set(index, stack.copy());
            if (!stack.isEmpty()) available.add(index);
        }
        for (RitualIngredient ingredient : ritual.offerings()) {
            int matched = -1;
            for (int index : available) {
                if (ingredient.matches(pedestals.get(index).getOffering())) {
                    matched = index;
                    break;
                }
            }
            if (matched < 0) return false;
            planned.add(new OfferingUse(matched, ingredient.count()));
            available.remove(Integer.valueOf(matched));
        }
        for (OfferingUse use : planned) pedestals.get(use.pedestal()).consumeOffering(use.count());
        return true;
    }

    public static int ritualDurationTicks(int bookCapacity, float requiredEnergy) {
        float tenth = Math.max(1.0F, bookCapacity / 10.0F);
        int seconds = Math.max(1, Math.min(10, (int) Math.ceil(requiredEnergy / tenth)));
        return seconds * 20;
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) return first.isEmpty() && second.isEmpty();
        return ContainerCompat.canStack(first, second) && first.getCount() == second.getCount();
    }

    private static boolean researchUnlocked(Ritual ritual, Player player, int bookType) {
        if (ritual.researchId() == null) return true;
        IResearchItem research = ResearchRegistry.instance().getResearchItemById(ritual.researchId());
        return research != null && KnowledgeGate.isUnlocked(NecroDataCapability.get(player), research, player, bookType);
    }

    private static Mob findSacrifice(Level level, BlockPos pos) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(4.0D))) {
            if (CorruptionRegistry.isSacrifice(mob) && mob.isAlive() && !mob.isBaby()
                //? if <1.21 {
                && mob.getMobType() != MobType.UNDEAD
                //?} else {
                /*&& !mob.getType().is(EntityTypeTags.UNDEAD)
                *///?}
            ) return mob;
        }
        return null;
    }

    private static float availableEnergy(Player player) {
        float available = 0;
        ItemStack main = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (main.getItem() instanceof IEnergyContainerItem energy) available += energy.getContainedEnergy(main);
        if (offhand != main && offhand.getItem() instanceof IEnergyContainerItem energy) {
            available += energy.getContainedEnergy(offhand);
        }
        int size = Math.min(36, player.getInventory().getContainerSize());
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == main || stack == offhand) continue;
            if (stack.getItem() instanceof IEnergyContainerItem energy) available += energy.getContainedEnergy(stack);
        }
        return available;
    }

    private static float drainEnergy(Player player, float requested) {
        float drained = 0;
        ItemStack main = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (main.getItem() instanceof IEnergyContainerItem energy) {
            drained += energy.consumeEnergy(main, requested);
        }
        if (offhand != main && drained + 0.001F < requested
            && offhand.getItem() instanceof IEnergyContainerItem energy) {
            drained += energy.consumeEnergy(offhand, requested - drained);
        }
        int size = Math.min(36, player.getInventory().getContainerSize());
        for (int slot = 0; slot < size && drained + 0.001F < requested; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == main || stack == offhand) continue;
            if (stack.getItem() instanceof IEnergyContainerItem energy) {
                drained += energy.consumeEnergy(stack, requested - drained);
            }
        }
        return drained;
    }

    private static net.minecraft.sounds.SoundEvent randomChant(Level level) {
        String[] chants = {"chant.cthulhu", "chant.yog_sothoth_1", "chant.yog_sothoth_2",
            "chant.hastur_1", "chant.hastur_2", "chant.sleeping", "chant.cthugha"};
        return ModSounds.event(chants[level.random.nextInt(chants.length)]);
    }

    public boolean isPerformingRitual() {
        return phase != CeremonyPhase.IDLE;
    }

    public ItemStack getCenterItem() {
        return center.get(0);
    }

    public void setCenterItem(ItemStack stack) {
        if (isPerformingRitual()) return;
        center.set(0, stack.copyWithCount(1));
        markUpdated();
    }

    public ItemStack takeCenterItem() {
        if (isPerformingRitual()) return ItemStack.EMPTY;
        ItemStack stack = center.get(0);
        center.set(0, ItemStack.EMPTY);
        markUpdated();
        return stack;
    }

    @Override
    public ItemStack ritualCenter() {
        return getCenterItem();
    }

    @Override
    public void setRitualCenter(ItemStack stack) {
        center.set(0, stack.copy());
        markUpdated();
    }

    @Override
    public List<ItemStack> ritualOfferingSnapshot() {
        if (!isPerformingRitual() && level != null) {
            return collectPedestals(level, worldPosition).stream()
                .map(RitualPedestal::getOffering).map(ItemStack::copy).toList();
        }
        return offeringSnapshot.stream().map(ItemStack::copy).toList();
    }

    @Override
    public List<BlockPos> ritualPedestalPositions() {
        return PEDESTAL_OFFSETS.stream().map(worldPosition::offset).toList();
    }

    @Override
    public void fillRitualPedestals(ItemStack stack) {
        if (level == null) return;
        for (RitualPedestal pedestal : collectPedestals(level, worldPosition)) {
            if (pedestal instanceof RitualPedestalBlockEntity blockEntity) blockEntity.setOffering(stack.copy());
        }
    }

    private static List<RitualPedestal> collectPedestals(Level level, BlockPos pos) {
        List<RitualPedestal> pedestals = new ArrayList<>();
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            if (level.getBlockEntity(pos.offset(offset)) instanceof RitualPedestal pedestal) {
                pedestals.add(pedestal);
            }
        }
        return pedestals;
    }

    private static void feedback(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.saveItems(tag, center, registries);
        CompoundTag snapshot = new CompoundTag();
        ContainerCompat.saveItems(snapshot, offeringSnapshot, registries);
        tag.put("OfferingSnapshot", snapshot);
        CompoundTag locked = new CompoundTag();
        ContainerCompat.saveItems(locked, lockedCenter, registries);
        tag.put("LockedCenter", locked);
        tag.putString("CeremonyPhase", phase.name());
        tag.putString("Ritual", ritualId);
        if (userUuid != null) tag.putUUID("RitualUser", userUuid);
        if (sacrificeUuid != null) tag.putUUID("RitualSacrifice", sacrificeUuid);
        tag.putInt("RitualTicks", ritualTicks);
        tag.putInt("RitualDuration", durationTicks);
        tag.putFloat("ConsumedEnergy", consumedEnergy);
        tag.putFloat("EnergyPerSecond", energyPerSecond);
        tag.putBoolean("SacrificeDead", sacrificeDead);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.loadItems(tag, center, registries);
        if (tag.contains("OfferingSnapshot")) {
            ContainerCompat.loadItems(tag.getCompound("OfferingSnapshot"), offeringSnapshot, registries);
        }
        if (tag.contains("LockedCenter")) {
            ContainerCompat.loadItems(tag.getCompound("LockedCenter"), lockedCenter, registries);
        }
        try {
            phase = CeremonyPhase.valueOf(tag.getString("CeremonyPhase"));
        } catch (IllegalArgumentException exception) {
            phase = CeremonyPhase.IDLE;
        }
        ritualId = tag.getString("Ritual");
        userUuid = tag.hasUUID("RitualUser") ? tag.getUUID("RitualUser") : null;
        sacrificeUuid = tag.hasUUID("RitualSacrifice") ? tag.getUUID("RitualSacrifice") : null;
        ritualTicks = tag.getInt("RitualTicks");
        durationTicks = tag.getInt("RitualDuration");
        consumedEnergy = tag.getFloat("ConsumedEnergy");
        energyPerSecond = tag.getFloat("EnergyPerSecond");
        sacrificeDead = tag.getBoolean("SacrificeDead");
        sacrificeSeenSinceLoad = false;
        if (phase != CeremonyPhase.IDLE && (ritualId.isBlank() || userUuid == null || durationTicks < 1)) {
            resetCeremony();
        }
    }

    private enum CeremonyPhase {
        IDLE,
        CHANT,
        WAIT_SACRIFICE
    }

    private record OfferingUse(int pedestal, int count) {}
}

package com.shinoow.abyssalcraft.content.block.ritual;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.system.ritual.Ritual;
import com.shinoow.abyssalcraft.system.ritual.RitualRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
public class RitualAltarBlockEntity extends BlockEntityCompat {

    /** The eight ring pedestal offsets relative to the altar (faithful 1.12.2 {@code RitualUtil.PEDESTAL_POSITIONS}). */
    private static final List<BlockPos> PEDESTAL_OFFSETS = List.of(
        new BlockPos(-3, 0, 0), new BlockPos(0, 0, -3),
        new BlockPos(3, 0, 0), new BlockPos(0, 0, 3),
        new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2),
        new BlockPos(2, 0, 2), new BlockPos(2, 0, -2));

    private final NonNullList<ItemStack> center = NonNullList.withSize(1, ItemStack.EMPTY);

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RitualBlocks.RITUAL_ALTAR_BE.get(), pos, state);
    }

    /** Attempt a ritual on a right click (server-side). {@code book} is the player's main-hand item. */
    public void tryRitual(Level level, BlockPos pos, Player player) {
        if (!(player.getMainHandItem().getItem() instanceof NecronomiconItem book)) {
            return;
        }
        ItemStack bookStack = player.getMainHandItem();
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
        if (book.getContainedEnergy(bookStack) < ritual.requiredEnergy()) {
            feedback(player, "message.abyssalcraft.ritual.no_energy");
            return;
        }
        book.consumeEnergy(bookStack, ritual.requiredEnergy());
        pedestals.forEach(RitualPedestal::consumeOffering);
        ritual.complete(level, pos, player);
        feedback(player, "message.abyssalcraft.ritual.success");
    }

    public ItemStack getCenterItem() {
        return center.get(0);
    }

    public void setCenterItem(ItemStack stack) {
        center.set(0, stack.copyWithCount(1));
        setChanged();
    }

    public ItemStack takeCenterItem() {
        ItemStack stack = center.get(0);
        center.set(0, ItemStack.EMPTY);
        setChanged();
        return stack;
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
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerCompat.loadItems(tag, center, registries);
    }
}

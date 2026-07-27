package com.shinoow.abyssalcraft.content.menu.spellbook;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.registry.ModMenus;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;
import com.shinoow.abyssalcraft.system.knowledge.IResearchItem;
import com.shinoow.abyssalcraft.system.knowledge.KnowledgeGate;
import com.shinoow.abyssalcraft.system.knowledge.ResearchRegistry;
import com.shinoow.abyssalcraft.system.spell.IScroll;
import com.shinoow.abyssalcraft.system.spell.Spell;
import com.shinoow.abyssalcraft.system.spell.SpellIngredient;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Seven-slot, server-authoritative Necronomicon spell inscription menu. */
public final class SpellbookMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOTS = 6;
    private static final int RESULT_SLOT = 6;
    private static final int PLAYER_START = 7;

    private final SimpleContainer inputs;
    private final SimpleContainer result = new SimpleContainer(1);
    private final InteractionHand hand;
    private final int sourceHotbarSlot;
    private final ItemStack book;
    private final Player user;
    private Spell currentSpell;

    public SpellbookMenu(int windowId, Inventory inventory, InteractionHand hand, ItemStack book) {
        this(windowId, inventory, new SimpleContainer(INPUT_SLOTS), hand, book, inventory.selected);
    }

    private SpellbookMenu(int windowId, Inventory inventory, SimpleContainer inputs, InteractionHand hand,
                          ItemStack book, int sourceHotbarSlot) {
        super(ModMenus.SPELLBOOK.get(), windowId);
        this.inputs = inputs;
        this.hand = hand;
        this.sourceHotbarSlot = sourceHotbarSlot;
        this.book = book;
        this.user = inventory.player;
        inputs.addListener(container -> refreshResult());

        addSlot(new Slot(inputs, 0, 51, 73) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof IScroll; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(inputs, 1, 51, 48));
        addSlot(new Slot(inputs, 2, 76, 69));
        addSlot(new Slot(inputs, 3, 65, 98));
        addSlot(new Slot(inputs, 4, 37, 98));
        addSlot(new Slot(inputs, 5, 26, 69));
        addSlot(new ResultSlot(result, 0, 134, 73));

        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 156 + row * 18));
        }
        for (int column = 0; column < 9; column++) {
            int hotbarSlot = column;
            addSlot(new Slot(inventory, column, 8 + column * 18, 214) {
                @Override public boolean mayPickup(Player player) { return !isSourceSlot(hotbarSlot); }
                @Override public boolean mayPlace(ItemStack stack) { return !isSourceSlot(hotbarSlot); }
            });
        }
        refreshResult();
    }

    public SpellbookMenu(int windowId, Inventory inventory, FriendlyByteBuf data) {
        this(windowId, inventory, readHand(data));
    }

    private SpellbookMenu(int windowId, Inventory inventory, InteractionHand hand) {
        this(windowId, inventory, new SimpleContainer(INPUT_SLOTS), hand,
            inventory.player.getItemInHand(hand), inventory.selected);
    }

    private static InteractionHand readHand(FriendlyByteBuf data) {
        return data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private boolean isSourceSlot(int hotbarSlot) {
        return hand == InteractionHand.MAIN_HAND && hotbarSlot == sourceHotbarSlot;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP
            && (hand == InteractionHand.OFF_HAND && button == Inventory.SLOT_OFFHAND
                || hand == InteractionHand.MAIN_HAND && button == sourceHotbarSlot)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void refreshResult() {
        currentSpell = resolveSpell();
        ItemStack scroll = inputs.getItem(0);
        ItemStack output = currentSpell == null ? ItemStack.EMPTY : ScrollItem.inscribe(scroll, currentSpell);
        result.setItem(0, output);
        broadcastChanges();
    }

    private Spell resolveSpell() {
        if (!(book.getItem() instanceof NecronomiconItem necronomicon)) return null;
        ItemStack scroll = inputs.getItem(0);
        if (!(scroll.getItem() instanceof IScroll parchment)) return null;
        List<ItemStack> reagents = new ArrayList<>(5);
        for (int index = 1; index < INPUT_SLOTS; index++) {
            if (!inputs.getItem(index).isEmpty()) reagents.add(inputs.getItem(index));
        }
        Spell spell = SpellRegistry.instance().find(necronomicon.bookType(), parchment.getScrollType(scroll),
            ScrollItem.spellId(scroll), reagents);
        return spell != null && researchUnlocked(spell, necronomicon.bookType()) ? spell : null;
    }

    private boolean researchUnlocked(Spell spell, int bookType) {
        if (spell.researchId() == null) return true;
        IResearchItem research = ResearchRegistry.instance().getResearchItemById(spell.researchId());
        return research != null && KnowledgeGate.isUnlocked(NecroDataCapability.get(user), research, user, bookType);
    }

    private boolean canCommit(Player player) {
        Spell spell = resolveSpell();
        return spell != null && spell == currentSpell && stillValid(player)
            && !result.getItem(0).isEmpty();
    }

    private ItemStack commit(Player player) {
        if (!canCommit(player)) return ItemStack.EMPTY;
        Spell spell = currentSpell;
        List<ReagentUse> planned = planReagents(spell);
        if (planned == null) return ItemStack.EMPTY;
        ItemStack output = ScrollItem.inscribe(inputs.getItem(0), spell);
        inputs.removeItem(0, 1);
        for (ReagentUse use : planned) inputs.removeItem(use.slot(), use.count());
        refreshResult();
        return output;
    }

    private List<ReagentUse> planReagents(Spell spell) {
        List<Integer> available = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        List<ReagentUse> planned = new ArrayList<>();
        for (SpellIngredient ingredient : spell.reagentLayout()) {
            if (ingredient.isEmpty()) continue;
            int matched = -1;
            for (int slot : available) {
                if (ingredient.matches(inputs.getItem(slot))) {
                    matched = slot;
                    break;
                }
            }
            if (matched < 0) return null;
            planned.add(new ReagentUse(matched, ingredient.count()));
            available.remove(Integer.valueOf(matched));
        }
        return planned;
    }

    @Override
    public boolean stillValid(Player player) {
        return !book.isEmpty() && player.getItemInHand(hand) == book
            && book.getItem() instanceof NecronomiconItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).mayPickup(player)) return ItemStack.EMPTY;
        if (index == RESULT_SLOT) return quickTakeResult(player);
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < INPUT_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof IScroll) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 1, INPUT_SLOTS, false)) {
            int hotbarStart = PLAYER_START + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(stack, hotbarStart, slots.size(), false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, PLAYER_START, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    private ItemStack quickTakeResult(Player player) {
        ItemStack output = result.getItem(0).copy();
        if (output.isEmpty() || !canFullyMove(output)) return ItemStack.EMPTY;
        ItemStack crafted = commit(player);
        if (crafted.isEmpty()) return ItemStack.EMPTY;
        ItemStack transfer = crafted.copy();
        if (!moveItemStackTo(transfer, PLAYER_START, slots.size(), true) || !transfer.isEmpty()) {
            player.drop(transfer, false);
        }
        return crafted;
    }

    private boolean canFullyMove(ItemStack stack) {
        for (int index = PLAYER_START; index < slots.size(); index++) {
            ItemStack target = slots.get(index).getItem();
            if (target.isEmpty()) return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, inputs);
    }

    private final class ResultSlot extends Slot {
        private ResultSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return canCommit(player); }
        @Override public ItemStack remove(int amount) { return amount > 0 ? commit(user) : ItemStack.EMPTY; }
    }

    private record ReagentUse(int slot, int count) {}
}
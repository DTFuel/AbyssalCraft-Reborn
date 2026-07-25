package com.shinoow.abyssalcraft.content.machine.materializer;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagStorage;
import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

public class MaterializerBlockEntity extends BlockEntityCompat implements Container, MenuProvider {

    public static final int SLOT_BAG = 0;
    public static final int SLOT_BOOK = 1;
    public static final int SLOT_COUNT = 2;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final NonNullList<ItemStack> migrationDrops = NonNullList.withSize(3, ItemStack.EMPTY);

    public MaterializerBlockEntity(BlockPos pos, BlockState state) {
        super(Materializers.MATERIALIZER_BE.get(), pos, state);
    }

    public List<DataRecipeCompat.Entry<MaterializationRecipe>> availableRecipes() {
        if (level == null || !hasRequiredItems()) return List.of();
        NonNullList<ItemStack> bagContents = CrystalBagStorage.load(items.get(SLOT_BAG), level.registryAccess());
        return recipes().stream()
            .filter(entry -> CrystalBagStorage.canConsume(bagContents, entry.value().inputs()))
            .toList();
    }

    public ItemStack craft(ResourceLocation recipeId) {
        if (level == null || level.isClientSide || !hasRequiredItems()) return ItemStack.EMPTY;
        MaterializationRecipe recipe = recipes().stream()
            .filter(entry -> entry.id().equals(recipeId)).map(DataRecipeCompat.Entry::value).findFirst().orElse(null);
        if (recipe == null || !CrystalBagStorage.consume(items.get(SLOT_BAG), recipe.inputs(), level.registryAccess())) {
            return ItemStack.EMPTY;
        }
        setChanged();
        return recipe.result().copy();
    }

    private boolean hasRequiredItems() {
        return CrystalBagStorage.isBag(items.get(SLOT_BAG))
            && items.get(SLOT_BOOK).getItem() instanceof NecronomiconItem;
    }

    private List<DataRecipeCompat.Entry<MaterializationRecipe>> recipes() {
        return level == null ? List.of() : DataRecipeCompat.entriesOfType(level, ModRecipes.MATERIALIZATION.get()).stream()
            .sorted(Comparator.comparing(entry -> entry.id().toString())).toList();
    }

    public NonNullList<ItemStack> migrationDrops() {
        return migrationDrops;
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) { ItemStack result = ContainerHelper.removeItem(items, slot, count); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_BAG ? CrystalBagStorage.isBag(stack)
            : slot == SLOT_BOOK && stack.getItem() instanceof NecronomiconItem;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
        items.set(slot, stack);
        if (stack.getCount() > 1) stack.setCount(1);
        setChanged();
    }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override public Component getDisplayName() { return Component.translatable("container.abyssalcraft.materializer"); }
    @Override public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new MaterializerMenu(windowId, inventory, this);
    }

    @Override protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("MachineDataVersion", 2);
        ContainerCompat.saveItems(tag, items, registries);
        CompoundTag pending = new CompoundTag();
        ContainerCompat.saveItems(pending, migrationDrops, registries);
        tag.put("PendingMigration", pending);
    }

    @Override protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        NonNullList<ItemStack> legacy = NonNullList.withSize(3, ItemStack.EMPTY);
        ContainerCompat.loadItems(tag, legacy, registries);
        items.clear();
        migrationDrops.clear();
        if (CrystalBagStorage.isBag(legacy.get(0))) items.set(SLOT_BAG, legacy.get(0));
        else migrationDrops.set(0, legacy.get(0));
        if (legacy.get(1).getItem() instanceof NecronomiconItem) items.set(SLOT_BOOK, legacy.get(1));
        else migrationDrops.set(1, legacy.get(1));
        migrationDrops.set(2, legacy.get(2));
        if (tag.contains("PendingMigration")) {
            ContainerCompat.loadItems(tag.getCompound("PendingMigration"), migrationDrops, registries);
        }
    }
}

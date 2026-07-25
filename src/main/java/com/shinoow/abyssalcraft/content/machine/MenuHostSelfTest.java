package com.shinoow.abyssalcraft.content.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletMenu;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStandBlockEntity;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStandMenu;
import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStands;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTableMenu;
import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTables;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferConfiguration;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferHost;

public final class MenuHostSelfTest {

    private MenuHostSelfTest() {}

    public static void run(HolderLookup.Provider registries) {
        testResearchLayout();
        testBrewingInventory(registries);
        testTabletStorage(registries);
        testTransferRoundTrip(registries);
        System.out.println("RR_MENU_HOST_SELF_TEST_OK");
    }

    private static void testResearchLayout() {
        ResearchTableMenu menu = new ResearchTableMenu(ResearchTables.RESEARCH_TABLE_MENU.get(), 1,
            new Inventory(null));
        require(menu.slots.size() == 36, "research table must expose only the player inventory");
        require(menu.slots.get(0).x == 8 && menu.slots.get(0).y == 156,
            "research table inventory did not move down by 72 pixels");
        require(menu.slots.get(35).x == 152 && menu.slots.get(35).y == 214,
            "research table hotbar layout is not the faithful 238-high layout");
    }

    private static void testBrewingInventory(HolderLookup.Provider registries) {
        TestBrewing original = new TestBrewing();
        original.setItem(BrewingStandBlockEntity.SLOT_FUEL, new ItemStack(Items.BLAZE_POWDER, 3));
        require(original.canPlaceItem(BrewingStandBlockEntity.SLOT_FUEL, new ItemStack(Items.BLAZE_POWDER)),
            "brewing fuel slot rejected blaze powder");
        require(!original.canPlaceItem(BrewingStandBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL)),
            "brewing fuel slot accepted coal");
        require(java.util.Arrays.equals(original.getSlotsForFace(Direction.UP), new int[] {3})
            && java.util.Arrays.equals(original.getSlotsForFace(Direction.DOWN), new int[] {5, 6, 7})
            && java.util.Arrays.equals(original.getSlotsForFace(Direction.NORTH), new int[] {0, 1, 2, 4}),
            "brewing sided automation slot map changed");
        original.seedTimers();
        CompoundTag saved = original.write(registries);
        TestBrewing restored = new TestBrewing();
        restored.read(saved, registries);
        require(restored.getItem(BrewingStandBlockEntity.SLOT_FUEL).getCount() == 3
            && saved.getShort("BrewTime") == 123 && saved.getByte("Fuel") == 7,
            "brewing inventory/timers did not round-trip");

        Inventory playerInventory = new Inventory(null);
        playerInventory.setItem(9, new ItemStack(Items.BLAZE_POWDER));
        TestBrewing shiftTarget = new TestBrewing();
        BrewingStandMenu menu = new BrewingStandMenu(BrewingStands.BREWING_STAND_MENU.get(), 2,
            playerInventory, shiftTarget, new SimpleContainerData(BrewingStandBlockEntity.DATA_COUNT));
        require(menu.quickMoveStack(null, 8).is(Items.BLAZE_POWDER)
            && shiftTarget.getItem(BrewingStandBlockEntity.SLOT_FUEL).is(Items.BLAZE_POWDER),
            "brewing shift-click did not prioritize the fuel slot");
    }

    private static void testTabletStorage(HolderLookup.Provider registries) {
        ItemStack tablet = new ItemStack(TransferContent.SPIRIT_TABLET.get());
        SpiritTabletStorage.setMode(tablet, 2);
        ResourceLocation dimension = ACRef.parse("minecraft:overworld");
        SpiritTabletStorage.appendRoute(tablet, new BlockPos(3, 4, 5), Direction.WEST, dimension);
        SpiritTabletStorage.appendRoute(tablet, new BlockPos(8, 9, 10), Direction.DOWN, dimension);
        NonNullList<ItemStack> filter = NonNullList.withSize(SpiritTabletStorage.FILTER_SIZE, ItemStack.EMPTY);
        filter.set(0, new ItemStack(Items.DIAMOND));
        SpiritTabletStorage.saveFilter(tablet, filter, registries);
        require(SpiritTabletStorage.mode(tablet) == 2
            && SpiritTabletStorage.route(tablet).equals(List.of(new BlockPos(3, 4, 5), new BlockPos(8, 9, 10)))
            && SpiritTabletStorage.entrySide(tablet) == Direction.DOWN
            && SpiritTabletStorage.isRouteDimension(tablet, dimension)
            && SpiritTabletStorage.loadFilter(tablet, registries).get(0).is(Items.DIAMOND),
            "spirit tablet data did not round-trip");
        SpiritTabletStorage.toggleFilter(tablet, 0);
        SpiritTabletStorage.toggleFilter(tablet, 1);
        require(SpiritTabletStorage.ignoreSubtypes(tablet) && SpiritTabletStorage.matchComponents(tablet),
            "spirit tablet filter toggles did not persist");

        Inventory offhandInventory = new Inventory(null);
        SpiritTabletMenu offhandMenu = new SpiritTabletMenu(3, offhandInventory,
            new SimpleContainer(SpiritTabletStorage.FILTER_SIZE), InteractionHand.OFF_HAND, tablet);
        require(offhandMenu.slots.size() == 41 && offhandMenu.ignoreSubtypes()
            && offhandMenu.matchComponents(),
            "offhand spirit tablet menu did not bind the persisted filter flags");
    }

    private static void testTransferRoundTrip(HolderLookup.Provider registries) {
        ItemTransferConfiguration configuration = new ItemTransferConfiguration(List.of(
            new BlockPos(1, 2, 3), new BlockPos(4, 5, 6), new BlockPos(7, 8, 9)))
            .exitSide(Direction.EAST).entrySide(Direction.DOWN)
            .ignoreSubtypes(true).matchComponents(false)
            .filterSlot(0, new ItemStack(Items.DIAMOND));
        ItemTransferHost original = new ItemTransferHost(() -> { });
        original.addTransferConfiguration(configuration);
        original.setTransferRunning(true);
        CompoundTag saved = original.save(registries);
        ItemTransferHost restored = new ItemTransferHost(() -> { });
        restored.load(saved, registries);
        require(restored.isTransferRunning() && restored.transferRate() == 1
            && restored.getTransferConfigurations().size() == 1,
            "transfer holder state did not round-trip");
        ItemTransferConfiguration route = restored.getTransferConfigurations().get(0);
        require(route.route().equals(configuration.route()) && route.exitSide() == Direction.EAST
            && route.entrySide() == Direction.DOWN && route.ignoreSubtypes() && !route.matchComponents()
            && route.matches(new ItemStack(Items.DIAMOND)) && !route.matches(new ItemStack(Items.COAL)),
            "transfer route/filter/facing did not round-trip");

        CompoundTag legacy = new CompoundTag();
        legacy.putLong("Origin", new BlockPos(11, 12, 13).asLong());
        legacy.putLong("Destination", new BlockPos(14, 15, 16).asLong());
        legacy.putInt("ExitSide", Direction.NORTH.get3DDataValue());
        legacy.putInt("EntrySide", Direction.SOUTH.get3DDataValue());
        ItemTransferConfiguration migrated = new ItemTransferConfiguration();
        migrated.load(legacy, registries);
        require(migrated.isValid() && migrated.origin().equals(new BlockPos(11, 12, 13))
            && migrated.destination().equals(new BlockPos(14, 15, 16)),
            "legacy origin/destination transfer data did not migrate");

        ItemStack filterPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        filterPickaxe.setDamageValue(1);
        ItemStack candidatePickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        candidatePickaxe.setDamageValue(20);
        ItemTransferConfiguration wildcardDamage = new ItemTransferConfiguration(
            BlockPos.ZERO, BlockPos.ZERO.east()).ignoreSubtypes(true).matchComponents(true)
            .filterSlot(0, filterPickaxe);
        require(wildcardDamage.matches(candidatePickaxe),
            "ignore-subtypes filter still compared the damage component");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class TestBrewing extends BrewingStandBlockEntity {
        private TestBrewing() {
            super(BlockPos.ZERO, BrewingStands.BREWING_STAND.get().defaultBlockState());
        }

        private void seedTimers() {
            CompoundTag tag = new CompoundTag();
            tag.putShort("BrewTime", (short) 123);
            tag.putByte("Fuel", (byte) 7);
            loadData(tag, null);
            setItem(SLOT_FUEL, new ItemStack(Items.BLAZE_POWDER, 3));
        }

        private CompoundTag write(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            saveData(tag, registries);
            return tag;
        }

        private void read(CompoundTag tag, HolderLookup.Provider registries) {
            loadData(tag, registries);
        }
    }
}
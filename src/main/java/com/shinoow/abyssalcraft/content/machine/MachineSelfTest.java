package com.shinoow.abyssalcraft.content.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagMenu;
import com.shinoow.abyssalcraft.content.item.bag.CrystalBagItems;
import com.shinoow.abyssalcraft.content.item.bag.CrystalBagStorage;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.item.material.MachineContentItems;
import com.shinoow.abyssalcraft.content.menu.base.MachineResultSlot;
import com.shinoow.abyssalcraft.content.machine.crystallizer.CrystallizerBlockEntity;
import com.shinoow.abyssalcraft.content.machine.crystallizer.CrystallizerMenu;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.materializer.MaterializerBlockEntity;
import com.shinoow.abyssalcraft.content.machine.materializer.MaterializerMenu;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.transmutator.TransmutatorBlockEntity;
import com.shinoow.abyssalcraft.content.machine.transmutator.TransmutatorMenu;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.content.recipe.materialization.CountedIngredient;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.MachineItemCompat;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;

public final class MachineSelfTest {

    private MachineSelfTest() {}

    public static void run(HolderLookup.Provider registries) {
        ItemStack bag = new ItemStack(CrystalBagItems.SMALL.get());
        NonNullList<ItemStack> contents = NonNullList.withSize(18, ItemStack.EMPTY);
        contents.set(0, new ItemStack(MaterialItems.CRYSTALS.get(3).get(), 2));
        contents.set(1, new ItemStack(MaterialItems.CRYSTALS.get(3).get(), 3));
        CrystalBagStorage.save(bag, contents, registries);

        CountedIngredient fiveShards = new CountedIngredient(Ingredient.of(MaterialItems.CRYSTALS.get(3).get()), 5);
        require(CrystalBagStorage.canConsume(bag, List.of(fiveShards), registries), "bag simulation failed");
        require(CrystalBagStorage.consume(bag, List.of(fiveShards), registries), "bag commit failed");
        require(CrystalBagStorage.load(bag, registries).stream().allMatch(ItemStack::isEmpty), "bag did not consume across slots");
        require(!CrystalBagStorage.consume(bag, List.of(fiveShards), registries), "failed consume mutated empty bag");
        ItemStack upgradedBag = new ItemStack(CrystalBagItems.MEDIUM.get());
        CompoundTag oldBagData = ItemDataCompat.copyData(bag);
        oldBagData.putInt("InvSize", 18);
        ItemDataCompat.setData(upgradedBag, oldBagData);
        require(CrystalBagStorage.load(upgradedBag, registries).size() == 36,
            "upgraded bag did not adopt its new capacity");

        require(CrystallizerBlockEntity.fuelBurnTime(new ItemStack(Items.BLAZE_POWDER)) == 1200,
            "crystallizer fuel mismatch");
        require(TransmutatorBlockEntity.fuelBurnTime(new ItemStack(Items.BLAZE_ROD)) == 2400,
            "transmutator fuel mismatch");
        require(TransmutatorBlockEntity.fuelBurnTime(new ItemStack(Items.COAL)) == 0,
            "coal must not be a transmutator fuel");
        ItemStack transmutationGem = new ItemStack(MachineContentItems.TRANSMUTATION_GEM.get());
        require(TransmutatorBlockEntity.fuelBurnTime(transmutationGem) == 10000,
            "transmutation gem fuel mismatch");
        ItemStack usedGem = MachineItemCompat.craftingRemainder(transmutationGem);
        require(usedGem.is(MachineContentItems.TRANSMUTATION_GEM.get()) && usedGem.getDamageValue() == 1,
            "transmutation gem did not return with one use consumed");
        usedGem.setDamageValue(usedGem.getMaxDamage());
        require(MachineItemCompat.craftingRemainder(usedGem).isEmpty(),
            "spent transmutation gem returned from the machine");
        require(CrystallizerBlockEntity.fuelBurnTime(new ItemStack(
            com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks.CLUSTERS.get(3).get())) == 1800,
            "carbon crystal cluster fuel mismatch");
        ItemStack liquidCoraliumBucket = new ItemStack(LiquidCoraliumCompat.BUCKET.get());
        require(MachineItemCompat.fluidAmount(liquidCoraliumBucket, ACRef.id("liquid_coralium")) == 1000,
            "liquid coralium bucket capability did not expose 1000 mB");
        require(TransmutatorBlockEntity.fuelBurnTime(liquidCoraliumBucket) == 20000,
            "liquid coralium bucket fuel mismatch");
        require(MachineItemCompat.craftingRemainder(liquidCoraliumBucket).is(Items.BUCKET),
            "liquid coralium bucket did not return an empty bucket");

        CrystallizationRecipe crystallization = new CrystallizationRecipe(
            Ingredient.of(Items.DIAMOND), new ItemStack(Items.AMETHYST_SHARD),
            new ItemStack(Items.QUARTZ), 0.2F, 200);
        require(!crystallization.secondaryResult().isEmpty() && crystallization.time() == 200,
            "crystallization full shape failed");

        MaterializationRecipe materialization = new MaterializationRecipe(List.of(
            new CountedIngredient(Ingredient.of(Items.AMETHYST_SHARD), 1),
            new CountedIngredient(Ingredient.of(Items.QUARTZ), 2),
            new CountedIngredient(Ingredient.of(Items.REDSTONE), 3),
            new CountedIngredient(Ingredient.of(Items.GLOWSTONE_DUST), 4),
            new CountedIngredient(Ingredient.of(Items.PRISMARINE_CRYSTALS), 5)),
            new ItemStack(Items.DIAMOND));
        require(materialization.inputs().size() == 5, "materialization five-input shape failed");

        TransmutationRecipe transmutation = new TransmutationRecipe(
            Ingredient.of(Items.STONE), new ItemStack(Items.DEEPSLATE), 0.1F, 200);
        require(transmutation.experience() == 0.1F && transmutation.time() == 200,
            "transmutation full shape failed");

        TestCrystallizer original = new TestCrystallizer();
        original.seed();
        CompoundTag saved = original.write(registries);
        require(saved.getInt("Progress") == 80 && saved.getInt("BurnTime") == 2321,
            "machine save omitted live timers");
        require(saved.getList("OutputExperience", Tag.TAG_COMPOUND).size() == 1,
            "machine save omitted XP ledger");
        TestCrystallizer restored = new TestCrystallizer();
        restored.read(saved, registries);
        require(restored.matchesSeed(), "machine NBT round-trip changed slots or timers");
        require(!restored.canPlaceItem(CrystallizerBlockEntity.SLOT_SECONDARY_OUTPUT, new ItemStack(Items.QUARTZ)),
            "secondary output accepted insertion");
        require(restored.getSlotsForFace(Direction.UP).length == 1
            && restored.getSlotsForFace(Direction.DOWN).length == 3
            && restored.getSlotsForFace(Direction.NORTH).length == 1,
            "sided automation slot map changed");

        TestMaterializer materializer = new TestMaterializer();
        CompoundTag legacyMaterializer = new CompoundTag();
        NonNullList<ItemStack> legacyItems = NonNullList.withSize(3, ItemStack.EMPTY);
        legacyItems.set(0, new ItemStack(CrystalBagItems.SMALL.get()));
        legacyItems.set(1, new ItemStack(Items.BLAZE_ROD));
        legacyItems.set(2, new ItemStack(Items.DIAMOND));
        com.shinoow.abyssalcraft.platform.ContainerCompat.saveItems(legacyMaterializer, legacyItems, registries);
        materializer.read(legacyMaterializer, registries);
        require(CrystalBagStorage.isBag(materializer.getItem(MaterializerBlockEntity.SLOT_BAG)),
            "materializer did not preserve its bag slot");
        require(materializer.getItem(MaterializerBlockEntity.SLOT_BOOK).isEmpty(),
            "materializer accepted a legacy fuel as a book");
        require(materializer.migrationDrops().get(1).is(Items.BLAZE_ROD)
            && materializer.migrationDrops().get(2).is(Items.DIAMOND),
            "materializer lost legacy fuel/output during migration");
        CompoundTag materializerSaved = materializer.write(registries);
        TestMaterializer materializerRestored = new TestMaterializer();
        materializerRestored.read(materializerSaved, registries);
        require(CrystalBagStorage.isBag(materializerRestored.getItem(MaterializerBlockEntity.SLOT_BAG))
            && materializerRestored.migrationDrops().get(1).is(Items.BLAZE_ROD)
            && materializerRestored.migrationDrops().get(2).is(Items.DIAMOND),
            "materializer migration data did not round-trip");

        testMenuTransfers();
        testResultExtraction(registries);

        System.out.println("RR_MACHINE_SELF_TEST_OK");
    }

    private static void testMenuTransfers() {
        Inventory crystallizerInventory = new Inventory(null);
        crystallizerInventory.setItem(9, new ItemStack(Items.BLAZE_POWDER));
        TestCrystallizer crystallizer = new TestCrystallizer();
        CrystallizerMenu crystallizerMenu = new CrystallizerMenu(1, crystallizerInventory, crystallizer,
            new SimpleContainerData(com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity.DATA_COUNT));
        require(crystallizerMenu.quickMoveStack(null, 4).is(Items.BLAZE_POWDER)
            && crystallizer.getItem(CrystallizerBlockEntity.SLOT_FUEL).is(Items.BLAZE_POWDER),
            "crystallizer shift-click did not route fuel");

        Inventory transmutatorInventory = new Inventory(null);
        transmutatorInventory.setItem(9, new ItemStack(Items.BLAZE_ROD));
        TestTransmutator transmutator = new TestTransmutator();
        TransmutatorMenu transmutatorMenu = new TransmutatorMenu(2, transmutatorInventory, transmutator,
            new SimpleContainerData(com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity.DATA_COUNT));
        require(transmutatorMenu.quickMoveStack(null, 3).is(Items.BLAZE_ROD)
            && transmutator.getItem(TransmutatorBlockEntity.SLOT_FUEL).is(Items.BLAZE_ROD),
            "transmutator shift-click did not route fuel");

        Inventory materializerInventory = new Inventory(null);
        materializerInventory.setItem(9, new ItemStack(CrystalBagItems.SMALL.get()));
        materializerInventory.setItem(10, new ItemStack(BookItems.NECRONOMICON.get()));
        TestMaterializer materializer = new TestMaterializer();
        MaterializerMenu materializerMenu = new MaterializerMenu(3, materializerInventory, materializer);
        require(materializerMenu.quickMoveStack(null, 20).is(CrystalBagItems.SMALL.get())
            && CrystalBagStorage.isBag(materializer.getItem(MaterializerBlockEntity.SLOT_BAG)),
            "materializer shift-click did not route the crystal bag");
        require(materializerMenu.quickMoveStack(null, 21).is(BookItems.NECRONOMICON.get())
            && materializer.getItem(MaterializerBlockEntity.SLOT_BOOK).is(BookItems.NECRONOMICON.get()),
            "materializer shift-click did not route the Necronomicon");

        Inventory bagInventory = new Inventory(null);
        bagInventory.selected = 0;
        bagInventory.setItem(0, new ItemStack(CrystalBagItems.SMALL.get()));
        bagInventory.setItem(9, new ItemStack(MaterialItems.CRYSTALS.get(3).get()));
        SimpleContainer bagContents = new SimpleContainer(18);
        CrystalBagMenu bagMenu = new CrystalBagMenu(4, bagInventory, bagContents, InteractionHand.MAIN_HAND);
        require(bagMenu.quickMoveStack(null, 18).is(MaterialItems.CRYSTALS.get(3).get())
            && bagContents.getItem(0).is(MaterialItems.CRYSTALS.get(3).get()),
            "crystal bag shift-click did not route a crystal");
        require(bagMenu.quickMoveStack(null, 45).isEmpty()
            && bagInventory.getItem(0).is(CrystalBagItems.SMALL.get()),
            "crystal bag menu allowed moving its host stack");
    }

    private static void testResultExtraction(HolderLookup.Provider registries) {
        TestCrystallizer playerExtraction = new TestCrystallizer();
        playerExtraction.seed();
        MachineResultSlot resultSlot = new MachineResultSlot(playerExtraction,
            CrystallizerBlockEntity.SLOT_OUTPUT, 0, 0);
        ItemStack extracted = resultSlot.remove(1);
        resultSlot.onTake(null, extracted);
        require(extracted.is(Items.AMETHYST_SHARD)
            && playerExtraction.write(registries).getList("OutputExperience", Tag.TAG_COMPOUND).isEmpty(),
            "player result extraction left stale XP ledger entries");

        TestCrystallizer automatedExtraction = new TestCrystallizer();
        automatedExtraction.seed();
        automatedExtraction.removeItem(CrystallizerBlockEntity.SLOT_OUTPUT, 1);
        require(automatedExtraction.write(registries).getList("OutputExperience", Tag.TAG_COMPOUND).isEmpty(),
            "automated output extraction left claimable player XP");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class TestCrystallizer extends CrystallizerBlockEntity {

        private static final ResourceLocation TEST_RECIPE = ACRef.id("self_test");

        private TestCrystallizer() {
            super(BlockPos.ZERO, Crystallizers.CRYSTALLIZER.get().defaultBlockState());
        }

        private void seed() {
            items.set(SLOT_INPUT, new ItemStack(Items.DIAMOND));
            items.set(SLOT_OUTPUT, new ItemStack(Items.AMETHYST_SHARD));
            progress = 80;
            maxProgress = 200;
            burnTime = 2321;
            maxBurnTime = 2400;
            activeRecipeId = TEST_RECIPE;
            recordExperience(SLOT_OUTPUT, TEST_RECIPE, 1, 0.2F);
        }

        private CompoundTag write(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            saveData(tag, registries);
            return tag;
        }

        private void read(CompoundTag tag, HolderLookup.Provider registries) {
            loadData(tag, registries);
        }

        private boolean matchesSeed() {
            return getItem(SLOT_INPUT).is(Items.DIAMOND)
                && getItem(SLOT_OUTPUT).is(Items.AMETHYST_SHARD)
                && progress == 80 && maxProgress == 200
                && burnTime == 2321 && maxBurnTime == 2400
                && TEST_RECIPE.equals(activeRecipeId);
        }
    }

    private static final class TestMaterializer extends MaterializerBlockEntity {

        private TestMaterializer() {
            super(BlockPos.ZERO, Materializers.MATERIALIZER.get().defaultBlockState());
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

    private static final class TestTransmutator extends TransmutatorBlockEntity {

        private TestTransmutator() {
            super(BlockPos.ZERO, Transmutators.TRANSMUTATOR.get().defaultBlockState());
        }
    }
}
package com.shinoow.abyssalcraft.system.energy;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.DeityStatueBlock;
import com.shinoow.abyssalcraft.content.block.energy.IdolOfFadingBlockEntity;
import com.shinoow.abyssalcraft.content.block.energy.EnergyDropBlock;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlockItem;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.energy.AmplifierCharmItem;
import com.shinoow.abyssalcraft.content.item.energy.EnergyItems;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.energy.structure.EnergyStructures;
import com.shinoow.abyssalcraft.system.energy.structure.IPlaceOfPower;
import com.shinoow.abyssalcraft.system.energy.structure.StructureHandler;
import com.shinoow.abyssalcraft.system.energy.disruption.CorruptionRegistry;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionAudit;
import com.shinoow.abyssalcraft.system.energy.disruption.DisruptionHandler;
import com.shinoow.abyssalcraft.system.energy.disruption.FireRainDisruption;
import com.shinoow.abyssalcraft.system.energy.disruption.InvisibleSwarmDisruption;
import com.shinoow.abyssalcraft.system.effect.DreadPlagueSelfTest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Permanent implementation-side invariants for the PE network. */
public final class EnergySelfTest {

    private EnergySelfTest() {}

    public static void run() {
        require(EnergyBlocks.ENERGY_COLLECTORS.size() == 5, "collector catalog size changed");
        require(EnergyBlocks.ENERGY_CONTAINERS.size() == 5, "container catalog size changed");
        require(EnergyBlocks.ENERGY_PEDESTALS.size() == 5, "pedestal catalog size changed");
        require(EnergyBlocks.ENERGY_RELAYS.size() == 5, "relay catalog size changed");

        checkIds(EnergyBlocks.ENERGY_COLLECTORS, EnergyBlockKind.COLLECTOR);
        checkIds(EnergyBlocks.ENERGY_CONTAINERS, EnergyBlockKind.CONTAINER);
        checkIds(EnergyBlocks.ENERGY_PEDESTALS, EnergyBlockKind.PEDESTAL);
        checkIds(EnergyBlocks.ENERGY_RELAYS, EnergyBlockKind.RELAY);
        require(ACRef.id("energydepositioner").equals(
            BuiltInRegistries.BLOCK.getKey(EnergyBlocks.ENERGY_DEPOSITIONER.get())),
            "energy depositioner registry id changed");

        String[] statueIds = {"cthulhu_statue", "hastur_statue", "jzahar_statue", "azathoth_statue",
            "nyarlathotep_statue", "shub_niggurath_statue", "yog_sothoth_statue"};
        require(EnergyBlocks.DEITY_STATUES.size() == statueIds.length, "functional statue catalog size changed");
        for (int index = 0; index < statueIds.length; index++) {
            Block block = EnergyBlocks.DEITY_STATUES.get(index).get();
            require(ACRef.id(statueIds[index]).equals(BuiltInRegistries.BLOCK.getKey(block)),
                "functional statue registry id changed: " + statueIds[index]);
            require(block instanceof DeityStatueBlock statue && statue.deity() == DeityType.values()[index],
                "functional statue deity mapping changed: " + statueIds[index]);
        }
        require(ACRef.id("deity_statue").equals(BuiltInRegistries.BLOCK.getKey(EnergyBlocks.DEITY_STATUE.get())),
            "legacy deity_statue compatibility entry changed");
        require(ACRef.id("idol_of_fading").equals(BuiltInRegistries.BLOCK.getKey(EnergyBlocks.IDOL_OF_FADING.get())),
            "idol of fading registry id changed");
        require(IdolOfFadingBlockEntity.MAX_ENERGY == 1000
            && IdolOfFadingBlockEntity.ACTIVATION_INTERVAL == 200
            && IdolOfFadingBlockEntity.ACTIVATION_GATE == 100.0F
            && IdolOfFadingBlockEntity.selectVariant(0, 2) == 2
            && IdolOfFadingBlockEntity.selectVariant(1, 0) == 1
            && IdolOfFadingBlockEntity.selectVariant(1, 1) == 0
            && IdolOfFadingBlockEntity.energyCost(0) == 25.0F
            && IdolOfFadingBlockEntity.energyCost(1) == 50.0F
            && IdolOfFadingBlockEntity.energyCost(2) == 100.0F,
            "idol of fading parameters changed");
        require(ACRef.id("monolith_pillar").equals(
            BuiltInRegistries.BLOCK.getKey(EnergyBlocks.MONOLITH_PILLAR.get())),
            "monolith pillar registry id changed");
        require(ACRef.id("multi_block").equals(
            BuiltInRegistries.BLOCK.getKey(EnergyBlocks.PLACE_OF_POWER_BASE.get())),
            "Place of Power master registry id changed");

        EnergyStructures.bootstrap();
        String[] structureIds = {"basic", "totempole", "archway"};
        float[] structureRanges = {2.0F, 3.0F, 1.0F};
        BlockPos[] activationPoints = {new BlockPos(1, 2, 1), BlockPos.ZERO, new BlockPos(2, 0, 0)};
        require(EnergyStructures.ALL.size() == 3, "Place of Power catalog size changed");
        for (int index = 0; index < structureIds.length; index++) {
            IPlaceOfPower place = EnergyStructures.ALL.get(index);
            require(place.getIdentifier().equals(structureIds[index]) && place.getBookType() == 0
                && place.getAmplifier(AmplifierType.RANGE) == structureRanges[index]
                && place.getAmplifier(AmplifierType.POWER) == 0.0F
                && place.getActivationPointForRender().equals(activationPoints[index])
                && place.getRenderData().length > 0
                && StructureHandler.instance().getStructureByName(structureIds[index]) == place,
                "Place of Power contract changed: " + structureIds[index]);
        }

            DisruptionAudit.validate(DisruptionHandler.instance());
            require(CorruptionRegistry.animalMappings() == 4, "animal corruption mapping count changed");
            require(FireRainDisruption.candidateCount() == 8, "fire-rain candidate count changed");
            require(InvisibleSwarmDisruption.minimumCount() == 2 && InvisibleSwarmDisruption.maximumCount() == 5,
                "invisible swarm bounds changed");
            DreadPlagueSelfTest.run();

        String[] charmFamilies = {"charm", "cthulhucharm", "hasturcharm", "jzaharcharm",
            "azathothcharm", "nyarlathotepcharm", "yogsothothcharm", "shubniggurathcharm"};
        DeityType[] charmDeities = {null, DeityType.CTHULHU, DeityType.HASTUR, DeityType.JZAHAR,
            DeityType.AZATHOTH, DeityType.NYARLATHOTEP, DeityType.YOGSOTHOTH, DeityType.SHUBNIGGURATH};
        AmplifierType[] charmTypes = {null, AmplifierType.RANGE, AmplifierType.DURATION, AmplifierType.POWER};
        require(EnergyItems.CHARMS.size() == 32, "ritual charm catalog size changed");
        for (int family = 0; family < charmFamilies.length; family++) {
            for (int variant = 0; variant < charmTypes.length; variant++) {
                int index = family * 4 + variant;
                String id = charmFamilies[family] + (variant == 0 ? "" : "_" + charmTypes[variant].name().toLowerCase());
                ItemStack stack = new ItemStack(EnergyItems.CHARMS.get(index).get());
                require(ACRef.id(id).equals(BuiltInRegistries.ITEM.getKey(stack.getItem())),
                    "ritual charm registry id changed: " + id);
                require(stack.getItem() instanceof AmplifierCharmItem charm && charm.amplifier() == charmTypes[variant]
                    && charm.deity() == charmDeities[family],
                    "ritual charm mapping changed: " + id);
            }
        }

        int[][] expected = {
            {1000, 10000, 5000, 500, 4, 10, 20},
            {1500, 20000, 7500, 600, 6, 20, 30},
            {2000, 60000, 10000, 700, 8, 30, 40},
            {2500, 240000, 12500, 800, 10, 40, 50},
            {3000, 1200000, 15000, 900, 12, 50, 60}
        };
        EnergyTier[] tiers = EnergyTier.values();
        for (int index = 0; index < tiers.length; index++) {
            EnergyTier tier = tiers[index];
            int[] row = expected[index];
            require(tier.collectorCapacity() == row[0]
                && tier.containerCapacity() == row[1]
                && tier.pedestalCapacity() == row[2]
                && tier.relayCapacity() == row[3]
                && tier.relayRange() == row[4]
                && tier.relayDrainQuanta() == row[5]
                && tier.transferQuanta() == row[6],
                "energy tier parameters changed: " + tier);
        }

        TestContainer source = new TestContainer(100, 30);
        TestContainer target = new TestContainer(10, 5);
        require(PEUtils.transfer(source, target, 20) == 5
            && source.getContainedEnergy() == 25 && target.getContainedEnergy() == 10,
            "container overflow was not returned to the source");

        ItemStack book = new ItemStack(BookItems.NECRONOMICON.get());
        IEnergyContainerItem bookEnergy = (IEnergyContainerItem) book.getItem();
        bookEnergy.setEnergy(book, 40);
        TestContainer buffer = new TestContainer(30, 0);
        require(PEUtils.transferFromItem(book, buffer, 20) == 20
            && bookEnergy.getContainedEnergy(book) == 20 && buffer.getContainedEnergy() == 20,
            "item-to-container PE transfer changed");
        require(PEUtils.transferToItem(buffer, book, 15) == 15
            && bookEnergy.getContainedEnergy(book) == 35 && buffer.getContainedEnergy() == 5,
            "container-to-item PE transfer changed");
        ItemStack storedBlock = EnergyDropBlock.stackWithEnergy(
            EnergyBlocks.ENERGY_COLLECTORS.get(0).get().defaultBlockState(), 321.5F);
        require(storedBlock.getItem() instanceof EnergyBlockItem
            && com.shinoow.abyssalcraft.platform.ItemDataCompat.getFloat(
                storedBlock, EnergyBlockItem.ENERGY_KEY) == 321.5F,
            "energy block drop did not retain PotEnergy");

        ManipulatorState state = new ManipulatorState();
        state.setTolerance(73);
        state.setActiveDeity(DeityType.HASTUR);
        state.setActiveAmplifier(AmplifierType.RANGE);
        state.collectors().addAll(List.of(new BlockPos(1, 2, 3), new BlockPos(-4, 5, -6)));
        CompoundTag saved = new CompoundTag();
        state.save(saved);
        ManipulatorState restored = new ManipulatorState();
        restored.load(saved);
        require(restored.tolerance() == 73 && restored.activeDeity() == DeityType.HASTUR
            && restored.activeAmplifier() == AmplifierType.RANGE
            && restored.collectors().equals(state.collectors()),
            "manipulator legacy-state round-trip changed");

        System.out.println("RR_ENERGY_SELF_TEST_OK blocks=21 statues=7 charms=32 idol=1 pop=3 disruptions=27 blocked=0");
    }

    private static void checkIds(List<Supplier<Block>> blocks, EnergyBlockKind kind) {
        EnergyTier[] tiers = EnergyTier.values();
        for (int index = 0; index < tiers.length; index++) {
            require(ACRef.id(kind.id(tiers[index])).equals(BuiltInRegistries.BLOCK.getKey(blocks.get(index).get())),
                "energy block registry id changed: " + kind + "/" + tiers[index]);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class TestContainer implements IEnergyContainer {

        private final int maxEnergy;
        private float energy;

        private TestContainer(int maxEnergy, float energy) {
            this.maxEnergy = maxEnergy;
            this.energy = energy;
        }

        @Override
        public float getContainedEnergy() {
            return energy;
        }

        @Override
        public int getMaxEnergy() {
            return maxEnergy;
        }

        @Override
        public void setEnergy(float energy) {
            this.energy = energy;
        }
    }
}
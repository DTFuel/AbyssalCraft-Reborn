package com.shinoow.abyssalcraft.data.gen;

import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.platform.BlockModelGen;
import com.shinoow.abyssalcraft.platform.DataGenCompat;

/** Blockstates and stable baseline models for all RR-ENERGY blocks. */
public final class EnergyBlockData extends BlockModelGen {

    public EnergyBlockData(DataGenCompat.Gen gen) {
        super(gen);
    }

    @Override
    public String getName() {
        return "AbyssalCraft Energy Block States";
    }

    @Override
    protected void generate() {
        energyCollector(EnergyBlocks.ENERGY_COLLECTORS.get(0).get(), null);
        energyCollector(EnergyBlocks.ENERGY_COLLECTORS.get(1).get(), mcLoc("block/stone"));
        energyCollector(EnergyBlocks.ENERGY_COLLECTORS.get(2).get(), modLoc("block/abyssal_stone"));
        energyCollector(EnergyBlocks.ENERGY_COLLECTORS.get(3).get(), modLoc("block/dreadstone"));
        energyCollector(EnergyBlocks.ENERGY_COLLECTORS.get(4).get(), modLoc("block/omothol_stone"));
        energyContainer(EnergyBlocks.ENERGY_CONTAINERS.get(0).get(), null);
        energyContainer(EnergyBlocks.ENERGY_CONTAINERS.get(1).get(), mcLoc("block/stone"));
        energyContainer(EnergyBlocks.ENERGY_CONTAINERS.get(2).get(), modLoc("block/abyssal_stone"));
        energyContainer(EnergyBlocks.ENERGY_CONTAINERS.get(3).get(), modLoc("block/dreadstone"));
        energyContainer(EnergyBlocks.ENERGY_CONTAINERS.get(4).get(), modLoc("block/omothol_stone"));
        energyPedestal(EnergyBlocks.ENERGY_PEDESTALS.get(0).get(), null);
        energyPedestal(EnergyBlocks.ENERGY_PEDESTALS.get(1).get(), mcLoc("block/stone"));
        energyPedestal(EnergyBlocks.ENERGY_PEDESTALS.get(2).get(), modLoc("block/abyssal_stone"));
        energyPedestal(EnergyBlocks.ENERGY_PEDESTALS.get(3).get(), modLoc("block/dreadstone"));
        energyPedestal(EnergyBlocks.ENERGY_PEDESTALS.get(4).get(), modLoc("block/omothol_stone"));
        energyRelay(EnergyBlocks.ENERGY_RELAYS.get(0).get(), null);
        energyRelay(EnergyBlocks.ENERGY_RELAYS.get(1).get(), mcLoc("block/stone"));
        energyRelay(EnergyBlocks.ENERGY_RELAYS.get(2).get(), modLoc("block/abyssal_stone"));
        energyRelay(EnergyBlocks.ENERGY_RELAYS.get(3).get(), modLoc("block/dreadstone"));
        energyRelay(EnergyBlocks.ENERGY_RELAYS.get(4).get(), modLoc("block/omothol_stone"));
        sacrificialAltar(EnergyBlocks.SACRIFICIAL_ALTARS.get(0).get(), null);
        sacrificialAltar(EnergyBlocks.SACRIFICIAL_ALTARS.get(1).get(), mcLoc("block/stone"));
        sacrificialAltar(EnergyBlocks.SACRIFICIAL_ALTARS.get(2).get(), modLoc("block/abyssal_stone"));
        sacrificialAltar(EnergyBlocks.SACRIFICIAL_ALTARS.get(3).get(), modLoc("block/dreadstone"));
        sacrificialAltar(EnergyBlocks.SACRIFICIAL_ALTARS.get(4).get(), modLoc("block/omothol_stone"));

        energyDepositioner(EnergyBlocks.ENERGY_DEPOSITIONER.get());
        parentModel(EnergyBlocks.IDOL_OF_FADING.get(), "deco_statue", "monolith_stone");
        parentModel(EnergyBlocks.MONOLITH_PILLAR.get(), "deco_statue", "monolith_pillar");
        cubeWithoutItem(EnergyBlocks.PLACE_OF_POWER_BASE.get(), "multi_block");

    }
}
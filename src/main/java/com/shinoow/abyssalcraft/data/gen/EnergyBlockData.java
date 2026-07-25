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
        horizontalSharedModel(EnergyBlocks.DEITY_STATUE.get(), "deco_statue");
        EnergyBlocks.ENERGY_COLLECTORS.forEach(block -> cube(block.get(), "energycollector"));
        EnergyBlocks.ENERGY_CONTAINERS.forEach(block -> cube(block.get(), "energycontainer"));
        EnergyBlocks.ENERGY_PEDESTALS.forEach(block -> parentModel(block.get(), "deco_statue", "monolith_stone"));
        EnergyBlocks.ENERGY_RELAYS.forEach(block -> directionalCube(block.get(), "energy_glow"));

        cube(EnergyBlocks.ENERGY_DEPOSITIONER.get(), "energydepositioner");
        parentModel(EnergyBlocks.IDOL_OF_FADING.get(), "deco_statue", "monolith_stone");
        parentModel(EnergyBlocks.MONOLITH_PILLAR.get(), "deco_statue", "monolith_pillar");
        cubeWithoutItem(EnergyBlocks.PLACE_OF_POWER_BASE.get(), "monolith_stone");

        EnergyBlocks.DEITY_STATUES.forEach(block ->
            horizontalParent(block.get(), "deco_statue", "monolith_stone"));
    }
}
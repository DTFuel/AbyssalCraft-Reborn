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
        EnergyBlocks.ENERGY_COLLECTORS.forEach(block ->
            layeredCube(block.get(), "energy_glow", "energycollector"));
        EnergyBlocks.ENERGY_CONTAINERS.forEach(block ->
            layeredCube(block.get(), "energy_glow", "energycontainer"));
        EnergyBlocks.ENERGY_PEDESTALS.forEach(block ->
            energyPedestal(block.get(), "energy_glow", "energy_trim"));
        EnergyBlocks.ENERGY_RELAYS.forEach(block -> directionalCube(block.get(), "energy_glow"));

        layeredCube(EnergyBlocks.ENERGY_DEPOSITIONER.get(), "energy_glow", "energydepositioner");
        parentModel(EnergyBlocks.IDOL_OF_FADING.get(), "deco_statue", "monolith_stone");
        parentModel(EnergyBlocks.MONOLITH_PILLAR.get(), "deco_statue", "monolith_pillar");
        cubeWithoutItem(EnergyBlocks.PLACE_OF_POWER_BASE.get(), "monolith_stone");

    }
}
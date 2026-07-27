package com.shinoow.abyssalcraft.content.block.demon;

import java.util.function.Supplier;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Blocks owned by the demon/evil-animal content family. */
public final class DemonBlocks {

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);

    public static final Supplier<Block> MIMIC_FIRE = BLOCKS.register("mimic_fire", () ->
        new MimicFireBlock(BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks()
            .lightLevel(state -> 15).sound(SoundType.WOOL).noLootTable()));

    private DemonBlocks() {}
}
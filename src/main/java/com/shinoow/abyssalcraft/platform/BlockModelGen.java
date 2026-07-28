package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

//? if forge {
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;

/**
 * Compat: block state + block-model + item-model datagen facade (loader axis).
 *
 * <p>Only the {@code BlockStateProvider} package forks between Forge and NeoForge; its helper surface
 * is identical, so business providers ({@code data/gen/BaseBlockData}) extend this and call the neutral
 * helpers below, staying free of {@code //?}. All textures resolve to mod block textures
 * ({@code abyssalcraft:block/<name>}). Every helper also emits the item model so a placed block has an
 * inventory icon (the PP-4/CR-9 lesson: never ship a block without its item model).
 */
public abstract class BlockModelGen extends BlockStateProvider {

    protected BlockModelGen(DataGenCompat.Gen gen) {
        super(gen.packOutput, AbyssalCraft.MODID, gen.existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        generate();
    }

    /** Business hook: define every block's states / models / item-models here. */
    protected abstract void generate();

    private String path(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private ResourceLocation tex(String texture) {
        return modLoc("block/" + texture);
    }

    /** Full cube (stone / brick / cobble / plank) + matching item model. */
    protected void cube(Block block, String texture) {
        ModelFile model = models().cubeAll(path(block), tex(texture));
        simpleBlockWithItem(block, model);
    }

    /** Full cube world model without an item model (for internal, unobtainable blocks). */
    protected void cubeWithoutItem(Block block, String texture) {
        simpleBlock(block, models().cubeAll(path(block), tex(texture)));
    }

    /** Six-way directional block using one texture on every face. */
    protected void directionalCube(Block block, String texture) {
        ModelFile model = models().cubeAll(path(block), tex(texture));
        directionalBlock(block, model);
        simpleBlockItem(block, model);
    }

    /** Opaque full-cube base with a cutout overlay expanded by the shared layered model. */
    protected void layeredCube(Block block, String base, String overlay) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/layered_ore"))
            .texture("all", tex(base))
            .texture("overlay", tex(overlay))
            .texture("particle", tex(base))
            .renderType("cutout");
        simpleBlockWithItem(block, model);
    }

    /** Pedestal geometry with independent body and top-emblem textures. */
    protected void energyPedestal(Block block, String body, String emblem) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/rending_pedestal"))
            .texture("0", tex(body))
            .texture("1", tex(emblem))
            .texture("particle", tex(body))
            .renderType("cutout");
        simpleBlockWithItem(block, model);
    }

    /** Child of an existing geometry with a replaceable {@code #all} texture. */
    protected void parentModel(Block block, String parent, String texture) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/" + parent))
            .texture("all", tex(texture))
            .texture("particle", tex(texture));
        simpleBlockWithItem(block, model);
    }

    /** Horizontally-facing block using one texture on every face. */
    protected void horizontalCube(Block block, String texture) {
        ModelFile model = models().cubeAll(path(block), tex(texture));
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }

    /** Horizontally-facing block using an existing shared model. */
    protected void horizontalSharedModel(Block block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/" + modelName));
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }

    /** Horizontally-facing child model that supplies one texture to a shared parent geometry. */
    protected void horizontalParent(Block block, String parent, String texture) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/" + parent))
            .texture("all", tex(texture))
            .texture("particle", tex(texture));
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }

    /** Grass-style bottom/side/top model with the vanilla tinted grass top. */
    protected void grass(Block block, String side, String bottom) {
        ModelFile model = models().withExistingParent(path(block), mcLoc("block/grass_block"))
            .texture("particle", tex(bottom))
            .texture("bottom", tex(bottom))
            .texture("side", tex(side))
            .texture("top", mcLoc("block/grass_block_top"))
            .texture("overlay", mcLoc("block/grass_block_side_overlay"));
        simpleBlockWithItem(block, model);
    }

    /** Grass-style bottom/side/top model whose textures are all supplied by the mod. */
    protected void layeredGround(Block block, String side, String bottom, String top) {
        ModelFile model = models().cubeBottomTop(path(block), tex(side), tex(bottom), tex(top));
        simpleBlockWithItem(block, model);
    }

    /** Translucent full cube + matching item model. */
    protected void translucentCube(Block block, String texture) {
        ModelFile model = models().cubeAll(path(block), tex(texture)).renderType("translucent");
        simpleBlockWithItem(block, model);
    }

    /** Blockstate + item model that both reference an existing shared block model. */
    protected void sharedModel(Block block, String model) {
        ModelFile file = new ModelFile.UncheckedModelFile(modLoc("block/" + model));
        simpleBlockWithItem(block, file);
    }

    /** Column / pillar (log, ethaxium pillar): {@code <side>} side + {@code <end>} end textures. */
    protected void pillar(RotatedPillarBlock block, String side, String end) {
        ModelFile model = models().cubeColumn(path(block), tex(side), tex(end));
        axisBlock(block, model, model);
        simpleBlockItem(block, model);
    }

    /** Fixed (non-axis) column-textured full block (e.g. glowing bricks): {@code side} + {@code end}. */
    protected void column(Block block, String side, String end) {
        ModelFile model = models().cubeColumn(path(block), tex(side), tex(end));
        simpleBlockWithItem(block, model);
    }

    /** Leaves (full cube). */
    protected void leaves(Block block, String texture) {
        ModelFile model = models().singleTexture(path(block), mcLoc("block/leaves"), "all", tex(texture));
        simpleBlockWithItem(block, model);
    }

    /** Cross / sapling model (cutout) + a flat item icon. */
    protected void cross(Block block, String texture) {
        ModelFile model = models().cross(path(block), tex(texture)).renderType("cutout");
        simpleBlock(block, model);
        itemModels().singleTexture(path(block), mcLoc("item/generated"), "layer0", tex(texture));
    }

    /** Slab (bottom / top / double) + item model. */
    protected void slab(SlabBlock block, String texture) {
        ResourceLocation t = tex(texture);
        ModelFile doubleModel = models().cubeAll(path(block) + "_double", t);
        slabBlock(block, doubleModel.getLocation(), t);
        simpleBlockItem(block, models().slab(path(block), t, t, t));
    }

    /** Stairs + item model. */
    protected void stairs(StairBlock block, String texture) {
        ResourceLocation t = tex(texture);
        stairsBlock(block, t);
        simpleBlockItem(block, models().stairs(path(block), t, t, t));
    }

    /** Wall (multipart) + inventory item model. */
    protected void wall(WallBlock block, String texture) {
        ResourceLocation t = tex(texture);
        wallBlock(block, t);
        simpleBlockItem(block, models().wallInventory(path(block) + "_inventory", t));
    }

    /** Fence (multipart) + inventory item model. */
    protected void fence(FenceBlock block, String texture) {
        ResourceLocation t = tex(texture);
        fenceBlock(block, t);
        simpleBlockItem(block, models().fenceInventory(path(block) + "_inventory", t));
    }

    /** Button + inventory item model. */
    protected void button(ButtonBlock block, String texture) {
        ResourceLocation t = tex(texture);
        buttonBlock(block, t);
        simpleBlockItem(block, models().buttonInventory(path(block) + "_inventory", t));
    }

    /** Pressure plate + item model. */
    protected void pressurePlate(PressurePlateBlock block, String texture) {
        ResourceLocation t = tex(texture);
        pressurePlateBlock(block, t);
        simpleBlockItem(block, models().pressurePlate(path(block), t));
    }

    /** Door (all facing/hinge/open states, cutout render) + a flat {@code item/<name>} icon. */
    protected void door(DoorBlock block, String bottomTexture, String topTexture) {
        doorBlockWithRenderType(block, tex(bottomTexture), tex(topTexture), "cutout");
        itemModels().singleTexture(path(block), mcLoc("item/generated"), "layer0", modLoc("item/" + path(block)));
    }

    /** Fence gate (open/closed/wall states) + inventory item model (the closed gate). */
    protected void fenceGate(FenceGateBlock block, String texture) {
        fenceGateBlock(block, tex(texture));
        simpleBlockItem(block, new ModelFile.UncheckedModelFile(modLoc("block/" + path(block))));
    }
}

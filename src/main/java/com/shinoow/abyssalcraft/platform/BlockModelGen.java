package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

//? if forge {
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.energy.EnergyPedestalBlock;

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

    /** Axis-aware log with the legacy transparent bark overlay on its four side faces. */
    protected void layeredPillar(RotatedPillarBlock block, String side, String end, String overlay) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/darklands_oak_log_layered"))
            .texture("side", tex(side))
            .texture("end", tex(end))
            .texture("overlay", tex(overlay))
            .texture("particle", tex(side))
            .renderType("cutout");
        axisBlock(block, model, model);
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

    /** Legacy Energy Pedestal geometry, including placement-driven tilt and tier host-stone bands. */
    protected void energyPedestal(Block block, ResourceLocation hostStone) {
        boolean tiered = hostStone != null;
        ModelFile upright = tiered
            ? models().withExistingParent(path(block), modLoc("block/tiered_energy_pedestal"))
                .texture("2", hostStone)
            : new ModelFile.UncheckedModelFile(modLoc("block/energy_pedestal"));
        ModelFile tilted = tiered
            ? models().withExistingParent(path(block) + "_tilted",
                modLoc("block/tiered_energy_pedestal_tilted")).texture("2", hostStone)
            : new ModelFile.UncheckedModelFile(modLoc("block/energy_pedestal_tilted"));
        var states = getVariantBuilder(block);
        states.partialState().with(EnergyPedestalBlock.TILTED, false)
            .modelForState().modelFile(upright).addModel();
        states.partialState().with(EnergyPedestalBlock.TILTED, true)
            .modelForState().modelFile(tilted).addModel();
        simpleBlockItem(block, upright);
    }

    /** Legacy Sacrificial Altar geometry with tier host-stone bands. */
    protected void sacrificialAltar(Block block, ResourceLocation hostStone) {
        ModelFile model = hostStone == null
            ? new ModelFile.UncheckedModelFile(modLoc("block/sacrificial_altar"))
            : models().withExistingParent(path(block), modLoc("block/tiered_sacrificial_altar"))
                .texture("host", hostStone);
        simpleBlockWithItem(block, model);
    }

    /** Legacy open-frame Energy Container geometry; tiered variants add host-stone bands. */
    protected void energyContainer(Block block, ResourceLocation hostStone) {
        boolean tiered = hostStone != null;
        var builder = models().withExistingParent(path(block),
                modLoc(tiered ? "block/tiered_energy_container" : "block/energy_container"))
            .texture("0", tex("monolith_stone"))
            .texture("2", tex("energycontainer"))
            .texture("3", tex("energy_glow"))
            .texture("particle", tex("monolith_stone"))
            .renderType("cutout");
        if (tiered) {
            builder.texture("4", tex("energy_trim"))
                .texture("5", hostStone);
        }
        simpleBlockWithItem(block, builder);
    }

    /** Legacy open-frame collector geometry; tiered variants add host-stone bands. */
    protected void energyCollector(Block block, ResourceLocation hostStone) {
        boolean tiered = hostStone != null;
        var builder = models().withExistingParent(path(block),
                modLoc(tiered ? "block/tiered_energy_collector" : "block/energy_collector"))
            .texture("2", tex("energycollector"))
            .texture("3", tex("energy_glow"))
            .texture("particle", tex("monolith_stone"))
            .texture("side", tex("monolith_stone"))
            .renderType("cutout");
        if (tiered) {
            builder.texture("4", hostStone)
                .texture("5", tex("energy_trim"));
        }
        simpleBlockWithItem(block, builder);
    }

    /** Legacy six-way relay geometry with its directional pointer and tier bands. */
    protected void energyRelay(Block block, ResourceLocation hostStone) {
        boolean tiered = hostStone != null;
        var builder = models().withExistingParent(path(block),
                modLoc(tiered ? "block/tiered_energy_relay" : "block/energy_relay"))
            .texture("0", tex("monolith_stone"))
            .texture("2", tex("energy_glow"))
            .texture("particle", tex("monolith_stone"))
            .renderType("cutout");
        if (tiered) {
            builder.texture("3", tex("energy_trim"))
                .texture("4", hostStone);
        }
        directionalBlock(block, builder);
        simpleBlockItem(block, builder);
    }

    /** Legacy Energy Depositioner frame, ooze columns and central overlay. */
    protected void energyDepositioner(Block block) {
        ModelFile model = models().withExistingParent(path(block), modLoc("block/energy_depositioner"))
            .texture("0", tex("monolith_stone"))
            .texture("1", tex("shoggoth_ooze"))
            .texture("2", tex("energydepositioner"))
            .texture("particle", tex("monolith_stone"))
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

    /** Slab whose vertical sides differ from its upper and lower faces. */
    protected void distinctSlab(SlabBlock block, String side, String bottom, String top) {
        ResourceLocation sideTexture = tex(side);
        ResourceLocation bottomTexture = tex(bottom);
        ResourceLocation topTexture = tex(top);
        ModelFile lower = models().slab(path(block), sideTexture, bottomTexture, topTexture);
        ModelFile upper = models().slabTop(path(block) + "_top", sideTexture, bottomTexture, topTexture);
        ModelFile doubled = models().cubeBottomTop(path(block) + "_double",
            sideTexture, bottomTexture, topTexture);
        var states = getVariantBuilder(block);
        states.partialState().with(SlabBlock.TYPE, SlabType.BOTTOM)
            .modelForState().modelFile(lower).addModel();
        states.partialState().with(SlabBlock.TYPE, SlabType.TOP)
            .modelForState().modelFile(upper).addModel();
        states.partialState().with(SlabBlock.TYPE, SlabType.DOUBLE)
            .modelForState().modelFile(doubled).addModel();
        simpleBlockItem(block, lower);
    }

    /** Slab preserving the legacy 2:1:1 random face distribution. */
    protected void weightedSlab(SlabBlock block, String texturePrefix) {
        ModelFile[] lower = new ModelFile[3];
        ModelFile[] upper = new ModelFile[3];
        ModelFile[] doubled = new ModelFile[3];
        for (int index = 0; index < 3; index++) {
            int variant = index + 1;
            ResourceLocation texture = tex(texturePrefix + "_" + variant);
            String suffix = variant == 1 ? "" : "_" + variant;
            lower[index] = models().slab(path(block) + suffix, texture, texture, texture);
            upper[index] = models().slabTop(path(block) + "_top" + suffix, texture, texture, texture);
            doubled[index] = models().cubeAll(path(block) + "_double" + suffix, texture);
        }
        var states = getVariantBuilder(block);
        states.partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).setModels(weighted(lower, 0, 0, false));
        states.partialState().with(SlabBlock.TYPE, SlabType.TOP).setModels(weighted(upper, 0, 0, false));
        states.partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).setModels(weighted(doubled, 0, 0, false));
        simpleBlockItem(block, lower[0]);
    }

    /** Stairs + item model. */
    protected void stairs(StairBlock block, String texture) {
        ResourceLocation t = tex(texture);
        stairsBlock(block, t);
        simpleBlockItem(block, models().stairs(path(block), t, t, t));
    }

    /** Stairs preserving the legacy 2:1:1 random face distribution for every shape and orientation. */
    protected void weightedStairs(StairBlock block, String texturePrefix) {
        ModelFile[] straight = new ModelFile[3];
        ModelFile[] inner = new ModelFile[3];
        ModelFile[] outer = new ModelFile[3];
        for (int index = 0; index < 3; index++) {
            int variant = index + 1;
            ResourceLocation texture = tex(texturePrefix + "_" + variant);
            String suffix = variant == 1 ? "" : "_" + variant;
            straight[index] = models().stairs(path(block) + suffix, texture, texture, texture);
            inner[index] = models().stairsInner(path(block) + "_inner" + suffix, texture, texture, texture);
            outer[index] = models().stairsOuter(path(block) + "_outer" + suffix, texture, texture, texture);
        }
        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction facing = state.getValue(StairBlock.FACING);
            Half half = state.getValue(StairBlock.HALF);
            StairsShape shape = state.getValue(StairBlock.SHAPE);
            int rotationY = (int) facing.getClockWise().toYRot();
            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) rotationY += 270;
            if (shape != StairsShape.STRAIGHT && half == Half.TOP) rotationY += 90;
            rotationY %= 360;
            ModelFile[] selected = switch (shape) {
                case STRAIGHT -> straight;
                case INNER_LEFT, INNER_RIGHT -> inner;
                case OUTER_LEFT, OUTER_RIGHT -> outer;
            };
            int rotationX = half == Half.TOP ? 180 : 0;
            return weighted(selected, rotationX, rotationY, rotationX != 0 || rotationY != 0);
        }, StairBlock.WATERLOGGED);
        simpleBlockItem(block, straight[0]);
    }

    private ConfiguredModel[] weighted(ModelFile[] models, int rotationX, int rotationY, boolean uvLock) {
        return ConfiguredModel.builder()
            .modelFile(models[0]).rotationX(rotationX).rotationY(rotationY).uvLock(uvLock).weight(2)
            .nextModel().modelFile(models[1]).rotationX(rotationX).rotationY(rotationY).uvLock(uvLock)
            .nextModel().modelFile(models[2]).rotationX(rotationX).rotationY(rotationY).uvLock(uvLock)
            .build();
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

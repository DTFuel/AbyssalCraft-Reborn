package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.ChatFormatting;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.BlockFactory;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.content.block.item.ColoredBlockItem;

/**
 * Building-material base blocks (owned by PB-3).
 *
 * <p>The AbyssalCraft building families -- Darkstone, Abyssal stone, Dreadstone, Elysian stone,
 * Coralium stone, Ethaxium, Dark ethaxium, Omothol/Monolith stone, Darklands oak and Dreadwood --
 * mapped onto vanilla block classes. Each entry registers the block and its {@link BlockItem};
 * states/models/item-models are datagen'd by {@code data/gen/BaseBlockData}. Attached to the MOD bus
 * via {@code ModRegistries.ALL}.
 *
 * <p>Fork-free: every block uses a CONCRETE vanilla class ({@link Block}, {@link SlabBlock},
 * {@link StairBlock}, {@link WallBlock}, {@link FenceBlock}, {@link RotatedPillarBlock},
 * {@link LeavesBlock}) whose constructor is identical on 1.20.1-forge and 1.21.1-neoforge. Blocks with
 * loader-forked constructors (buttons / pressure plates -> {@code BlockSetType}/{@code Sensitivity};
 * saplings -> {@code TreeGrower}) are intentionally deferred until a {@code platform} block factory
 * hosts those forks. {@link StairBlock} base states reference {@code <base>.get().defaultBlockState()},
 * so every base block is declared BEFORE the stairs that build on it (DeferredRegister constructs in
 * declaration order).
 */
public final class BaseBlocks {

    private BaseBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    private static BlockBehaviour.Properties rock() {
        return BlockBehaviour.Properties.of().strength(1.5F, 6.0F).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of().strength(2.0F, 3.0F);
    }

    private static BlockBehaviour.Properties leaves() {
        return BlockBehaviour.Properties.of().strength(0.2F).randomTicks().noOcclusion();
    }

    /** Register a block + its {@link BlockItem} under the same name. */
    private static <T extends Block> Supplier<T> reg(String name, Supplier<T> factory) {
        Supplier<T> block = BLOCKS.register(name, factory);
        ChatFormatting color = colorFor(name);
        ITEMS.register(name, () -> color == null
            ? new BlockItem(block.get(), new Item.Properties())
            : new ColoredBlockItem(block.get(), new Item.Properties(), color));
        return block;
    }

    private static ChatFormatting colorFor(String name) {
        if (name.contains("dark_ethaxium")) return ChatFormatting.DARK_RED;
        if (name.contains("ethaxium")) return ChatFormatting.AQUA;
        if (name.equals("abyssal_stone") || name.startsWith("abyssal_stone_brick")
                || name.startsWith("chiseled_abyssal_stone_brick")
            || name.startsWith("cracked_abyssal_stone_brick")
            || name.equals("abyssal_stone_button")
            || name.equals("abyssal_stone_pressure_plate")) return ChatFormatting.BLUE;
        return null;
    }

    private static Supplier<Block> stone(String name) {
        return reg(name, () -> new Block(rock()));
    }

    private static Supplier<SlabBlock> stoneSlab(String name) {
        return reg(name, () -> new SlabBlock(rock()));
    }

    private static Supplier<StairBlock> stoneStairs(String name, Supplier<? extends Block> base) {
        return reg(name, () -> new StairBlock(base.get().defaultBlockState(), rock()));
    }

    private static Supplier<WallBlock> stoneWall(String name) {
        return reg(name, () -> new WallBlock(rock()));
    }

    private static Supplier<FenceBlock> stoneFence(String name) {
        return reg(name, () -> new FenceBlock(rock()));
    }

    // ==== Darkstone ====
    public static final Supplier<Block> DARKSTONE = stone("darkstone");
    public static final Supplier<Block> DARKSTONE_COBBLESTONE = stone("darkstone_cobblestone");
    public static final Supplier<Block> DARKSTONE_BRICK = stone("darkstone_brick");
    public static final Supplier<Block> CHISELED_DARKSTONE_BRICK = stone("chiseled_darkstone_brick");
    public static final Supplier<Block> CRACKED_DARKSTONE_BRICK = stone("cracked_darkstone_brick");
    public static final Supplier<Block> GLOWING_DARKSTONE_BRICKS =
        reg("glowing_darkstone_bricks", () -> new Block(rock().lightLevel(s -> 15)));
    public static final Supplier<SlabBlock> DARKSTONE_SLAB = stoneSlab("darkstone_slab");
    public static final Supplier<SlabBlock> DARKSTONE_BRICK_SLAB = stoneSlab("darkstone_brick_slab");
    public static final Supplier<SlabBlock> DARKSTONE_COBBLESTONE_SLAB = stoneSlab("darkstone_cobblestone_slab");
    public static final Supplier<StairBlock> DARKSTONE_BRICK_STAIRS = stoneStairs("darkstone_brick_stairs", DARKSTONE_BRICK);
    public static final Supplier<StairBlock> DARKSTONE_COBBLESTONE_STAIRS = stoneStairs("darkstone_cobblestone_stairs", DARKSTONE_COBBLESTONE);
    public static final Supplier<WallBlock> DARKSTONE_COBBLESTONE_WALL = stoneWall("darkstone_cobblestone_wall");
    public static final Supplier<FenceBlock> DARKSTONE_BRICK_FENCE = stoneFence("darkstone_brick_fence");

    // ==== Abyssal stone ====
    public static final Supplier<Block> ABYSSAL_STONE = stone("abyssal_stone");
    public static final Supplier<Block> ABYSSAL_COBBLESTONE = stone("abyssal_cobblestone");
    public static final Supplier<Block> ABYSSAL_STONE_BRICK = stone("abyssal_stone_brick");
    public static final Supplier<Block> CHISELED_ABYSSAL_STONE_BRICK = stone("chiseled_abyssal_stone_brick");
    public static final Supplier<Block> CRACKED_ABYSSAL_STONE_BRICK = stone("cracked_abyssal_stone_brick");
    public static final Supplier<SlabBlock> ABYSSAL_STONE_BRICK_SLAB = stoneSlab("abyssal_stone_brick_slab");
    public static final Supplier<SlabBlock> ABYSSAL_COBBLESTONE_SLAB = stoneSlab("abyssal_cobblestone_slab");
    public static final Supplier<StairBlock> ABYSSAL_STONE_BRICK_STAIRS = stoneStairs("abyssal_stone_brick_stairs", ABYSSAL_STONE_BRICK);
    public static final Supplier<StairBlock> ABYSSAL_COBBLESTONE_STAIRS = stoneStairs("abyssal_cobblestone_stairs", ABYSSAL_COBBLESTONE);
    public static final Supplier<WallBlock> ABYSSAL_COBBLESTONE_WALL = stoneWall("abyssal_cobblestone_wall");
    public static final Supplier<FenceBlock> ABYSSAL_STONE_BRICK_FENCE = stoneFence("abyssal_stone_brick_fence");

    // ==== Dreadstone ====
    public static final Supplier<Block> DREADSTONE = stone("dreadstone");
    public static final Supplier<Block> DREADSTONE_COBBLESTONE = stone("dreadstone_cobblestone");
    public static final Supplier<Block> DREADSTONE_BRICK = stone("dreadstone_brick");
    public static final Supplier<Block> CHISELED_DREADSTONE_BRICK = stone("chiseled_dreadstone_brick");
    public static final Supplier<Block> CRACKED_DREADSTONE_BRICK = stone("cracked_dreadstone_brick");
    public static final Supplier<SlabBlock> DREADSTONE_BRICK_SLAB = stoneSlab("dreadstone_brick_slab");
    public static final Supplier<SlabBlock> DREADSTONE_COBBLESTONE_SLAB = stoneSlab("dreadstone_cobblestone_slab");
    public static final Supplier<StairBlock> DREADSTONE_BRICK_STAIRS = stoneStairs("dreadstone_brick_stairs", DREADSTONE_BRICK);
    public static final Supplier<StairBlock> DREADSTONE_COBBLESTONE_STAIRS = stoneStairs("dreadstone_cobblestone_stairs", DREADSTONE_COBBLESTONE);
    public static final Supplier<WallBlock> DREADSTONE_COBBLESTONE_WALL = stoneWall("dreadstone_cobblestone_wall");
    public static final Supplier<FenceBlock> DREADSTONE_BRICK_FENCE = stoneFence("dreadstone_brick_fence");

    // ==== Elysian stone ====
    public static final Supplier<Block> ELYSIAN_STONE = stone("elysian_stone");
    public static final Supplier<Block> ELYSIAN_COBBLESTONE = stone("elysian_cobblestone");
    public static final Supplier<Block> ELYSIAN_STONE_BRICK = stone("elysian_stone_brick");
    public static final Supplier<Block> CHISELED_ELYSIAN_STONE_BRICK = stone("chiseled_elysian_stone_brick");
    public static final Supplier<Block> CRACKED_ELYSIAN_STONE_BRICK = stone("cracked_elysian_stone_brick");
    public static final Supplier<SlabBlock> ELYSIAN_STONE_BRICK_SLAB = stoneSlab("elysian_stone_brick_slab");
    public static final Supplier<SlabBlock> ELYSIAN_COBBLESTONE_SLAB = stoneSlab("elysian_cobblestone_slab");
    public static final Supplier<StairBlock> ELYSIAN_STONE_BRICK_STAIRS = stoneStairs("elysian_stone_brick_stairs", ELYSIAN_STONE_BRICK);
    public static final Supplier<StairBlock> ELYSIAN_COBBLESTONE_STAIRS = stoneStairs("elysian_cobblestone_stairs", ELYSIAN_COBBLESTONE);
    public static final Supplier<WallBlock> ELYSIAN_COBBLESTONE_WALL = stoneWall("elysian_cobblestone_wall");
    public static final Supplier<FenceBlock> ELYSIAN_STONE_BRICK_FENCE = stoneFence("elysian_stone_brick_fence");

    // ==== Coralium stone ====
    public static final Supplier<Block> CORALIUM_STONE = stone("coralium_stone");
    public static final Supplier<Block> CORALIUM_COBBLESTONE = stone("coralium_cobblestone");
    public static final Supplier<Block> CORALIUM_STONE_BRICK = stone("coralium_stone_brick");
    public static final Supplier<Block> CHISELED_CORALIUM_STONE_BRICK = stone("chiseled_coralium_stone_brick");
    public static final Supplier<Block> CRACKED_CORALIUM_STONE_BRICK = stone("cracked_coralium_stone_brick");
    public static final Supplier<SlabBlock> CORALIUM_STONE_BRICK_SLAB = stoneSlab("coralium_stone_brick_slab");
    public static final Supplier<SlabBlock> CORALIUM_COBBLESTONE_SLAB = stoneSlab("coralium_cobblestone_slab");
    public static final Supplier<StairBlock> CORALIUM_STONE_BRICK_STAIRS = stoneStairs("coralium_stone_brick_stairs", CORALIUM_STONE_BRICK);
    public static final Supplier<StairBlock> CORALIUM_COBBLESTONE_STAIRS = stoneStairs("coralium_cobblestone_stairs", CORALIUM_COBBLESTONE);
    public static final Supplier<WallBlock> CORALIUM_COBBLESTONE_WALL = stoneWall("coralium_cobblestone_wall");
    public static final Supplier<FenceBlock> CORALIUM_STONE_BRICK_FENCE = stoneFence("coralium_stone_brick_fence");

    // ==== Ethaxium ====
    public static final Supplier<Block> ETHAXIUM = stone("ethaxium");
    // NB: the BLOCK is plural "ethaxium_bricks" (vanilla convention, cf. minecraft:bricks) to avoid
    // colliding with the singular crafting-ingredient ITEM "ethaxium_brick" owned by PB-1 MaterialItems.
    public static final Supplier<Block> ETHAXIUM_BRICKS = stone("ethaxium_bricks");
    public static final Supplier<Block> CHISELED_ETHAXIUM_BRICK = stone("chiseled_ethaxium_brick");
    public static final Supplier<Block> CRACKED_ETHAXIUM_BRICK = stone("cracked_ethaxium_brick");
    public static final Supplier<RotatedPillarBlock> ETHAXIUM_PILLAR = reg("ethaxium_pillar", () -> new RotatedPillarBlock(rock()));
    public static final Supplier<SlabBlock> ETHAXIUM_BRICK_SLAB = stoneSlab("ethaxium_brick_slab");
    public static final Supplier<StairBlock> ETHAXIUM_BRICK_STAIRS = stoneStairs("ethaxium_brick_stairs", ETHAXIUM_BRICKS);
    public static final Supplier<FenceBlock> ETHAXIUM_BRICK_FENCE = stoneFence("ethaxium_brick_fence");

    // ==== Dark ethaxium ====
    public static final Supplier<Block> DARK_ETHAXIUM_BRICK = stone("dark_ethaxium_brick");
    public static final Supplier<Block> CHISELED_DARK_ETHAXIUM_BRICK = stone("chiseled_dark_ethaxium_brick");
    public static final Supplier<Block> CRACKED_DARK_ETHAXIUM_BRICK = stone("cracked_dark_ethaxium_brick");
    public static final Supplier<RotatedPillarBlock> DARK_ETHAXIUM_PILLAR = reg("dark_ethaxium_pillar", () -> new RotatedPillarBlock(rock()));
    public static final Supplier<SlabBlock> DARK_ETHAXIUM_BRICK_SLAB = stoneSlab("dark_ethaxium_brick_slab");
    public static final Supplier<StairBlock> DARK_ETHAXIUM_BRICK_STAIRS = stoneStairs("dark_ethaxium_brick_stairs", DARK_ETHAXIUM_BRICK);
    public static final Supplier<FenceBlock> DARK_ETHAXIUM_BRICK_FENCE = stoneFence("dark_ethaxium_brick_fence");

    // ==== Omothol / Monolith stone ====
    public static final Supplier<Block> OMOTHOL_STONE = stone("omothol_stone");
    public static final Supplier<Block> MONOLITH_STONE = stone("monolith_stone");

    // ==== Darklands oak (wood) ====
    public static final Supplier<LeavesBlock> DARKLANDS_OAK_LEAVES = reg("darklands_oak_leaves", () -> new LeavesBlock(leaves()));
    public static final Supplier<RotatedPillarBlock> DARKLANDS_OAK_LOG = reg("darklands_oak_log", () -> new RotatedPillarBlock(wood()));
    public static final Supplier<Block> DARKLANDS_OAK_PLANKS = reg("darklands_oak_planks", () -> new Block(wood()));
    public static final Supplier<SlabBlock> DARKLANDS_OAK_SLAB = reg("darklands_oak_slab", () -> new SlabBlock(wood()));
    public static final Supplier<StairBlock> DARKLANDS_OAK_STAIRS =
        reg("darklands_oak_stairs", () -> new StairBlock(DARKLANDS_OAK_PLANKS.get().defaultBlockState(), wood()));
    public static final Supplier<FenceBlock> DARKLANDS_OAK_FENCE = reg("darklands_oak_fence", () -> new FenceBlock(wood()));

    // ==== Dreadwood ====
    public static final Supplier<LeavesBlock> DREADWOOD_LEAVES = reg("dreadwood_leaves", () -> new LeavesBlock(leaves()));
    public static final Supplier<RotatedPillarBlock> DREADWOOD_LOG = reg("dreadwood_log", () -> new RotatedPillarBlock(wood()));
    public static final Supplier<Block> DREADWOOD_PLANKS = reg("dreadwood_planks", () -> new Block(wood()));
    public static final Supplier<SlabBlock> DREADWOOD_SLAB = reg("dreadwood_slab", () -> new SlabBlock(wood()));
    public static final Supplier<StairBlock> DREADWOOD_STAIRS =
        reg("dreadwood_stairs", () -> new StairBlock(DREADWOOD_PLANKS.get().defaultBlockState(), wood()));
    public static final Supplier<FenceBlock> DREADWOOD_FENCE = reg("dreadwood_fence", () -> new FenceBlock(wood()));

    // ==== Abyssal Swamp dead tree ====
    public static final Supplier<RotatedPillarBlock> DEAD_TREE_LOG =
        reg("dead_tree_log", () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of().strength(2.0F, 1.0F).sound(SoundType.WOOD)));

    // ===== PB-8: forked-construction variants (ctors diverge 1.20.1<->1.21 -> platform/BlockFactory) =====
    // Stone families (darkstone/abyssal_stone/coralium_stone) use BlockSetType.STONE; wood families
    // (darklands_oak/dreadwood) use OAK. Buttons/plates are non-solid; doors are non-occluding; the
    // sapling grower is a vanilla-oak placeholder (real tree growth is a world-stage concern).

    private static BlockBehaviour.Properties redstoneProps(SoundType sound) {
        return BlockBehaviour.Properties.of().noCollission().strength(0.5F).sound(sound);
    }

    private static BlockBehaviour.Properties doorProps() {
        return BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties saplingProps() {
        return BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(SoundType.GRASS);
    }

    // ---- Buttons (stone: 20 ticks, arrows can't press; wood: 30 ticks, arrows can) ----
    public static final Supplier<ButtonBlock> DARKSTONE_BUTTON =
        reg("darkstone_button", () -> BlockFactory.button(redstoneProps(SoundType.STONE), BlockSetType.STONE, 20, false));
    public static final Supplier<ButtonBlock> ABYSSAL_STONE_BUTTON =
        reg("abyssal_stone_button", () -> BlockFactory.button(redstoneProps(SoundType.STONE), BlockSetType.STONE, 20, false));
    public static final Supplier<ButtonBlock> CORALIUM_STONE_BUTTON =
        reg("coralium_stone_button", () -> BlockFactory.button(redstoneProps(SoundType.STONE), BlockSetType.STONE, 20, false));
    public static final Supplier<ButtonBlock> DARKLANDS_OAK_BUTTON =
        reg("darklands_oak_button", () -> BlockFactory.button(redstoneProps(SoundType.WOOD), BlockSetType.OAK, 30, true));
    public static final Supplier<ButtonBlock> DREADWOOD_BUTTON =
        reg("dreadwood_button", () -> BlockFactory.button(redstoneProps(SoundType.WOOD), BlockSetType.OAK, 30, true));

    // ---- Pressure plates ----
    public static final Supplier<PressurePlateBlock> DARKSTONE_PRESSURE_PLATE =
        reg("darkstone_pressure_plate", () -> BlockFactory.pressurePlate(redstoneProps(SoundType.STONE), BlockSetType.STONE));
    public static final Supplier<PressurePlateBlock> ABYSSAL_STONE_PRESSURE_PLATE =
        reg("abyssal_stone_pressure_plate", () -> BlockFactory.pressurePlate(redstoneProps(SoundType.STONE), BlockSetType.STONE));
    public static final Supplier<PressurePlateBlock> CORALIUM_STONE_PRESSURE_PLATE =
        reg("coralium_stone_pressure_plate", () -> BlockFactory.pressurePlate(redstoneProps(SoundType.STONE), BlockSetType.STONE));
    public static final Supplier<PressurePlateBlock> DARKLANDS_OAK_PRESSURE_PLATE =
        reg("darklands_oak_pressure_plate", () -> BlockFactory.pressurePlate(redstoneProps(SoundType.WOOD), BlockSetType.OAK));
    public static final Supplier<PressurePlateBlock> DREADWOOD_PRESSURE_PLATE =
        reg("dreadwood_pressure_plate", () -> BlockFactory.pressurePlate(redstoneProps(SoundType.WOOD), BlockSetType.OAK));

    // ---- Saplings (placeholder oak grower) ----
    public static final Supplier<SaplingBlock> DARKLANDS_OAK_SAPLING =
        reg("darklands_oak_sapling", () -> BlockFactory.sapling(saplingProps(), "darklands_tree"));
    public static final Supplier<SaplingBlock> DREADWOOD_SAPLING =
        reg("dreadwood_sapling", () -> BlockFactory.sapling(saplingProps(), "dreadlands_tree"));

    // ---- Doors ----
    public static final Supplier<DoorBlock> DARKLANDS_OAK_DOOR =
        reg("darklands_oak_door", () -> BlockFactory.door(doorProps(), BlockSetType.OAK));
    public static final Supplier<DoorBlock> DREADWOOD_DOOR =
        reg("dreadwood_door", () -> BlockFactory.door(doorProps(), BlockSetType.OAK));

    // ---- Fence gates ----
    public static final Supplier<FenceGateBlock> DARKLANDS_OAK_FENCE_GATE =
        reg("darklands_oak_fence_gate", () -> BlockFactory.fenceGate(wood(), WoodType.OAK));
    public static final Supplier<FenceGateBlock> DREADWOOD_FENCE_GATE =
        reg("dreadwood_fence_gate", () -> BlockFactory.fenceGate(wood(), WoodType.OAK));
}

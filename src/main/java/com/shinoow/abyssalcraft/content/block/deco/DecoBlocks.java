package com.shinoow.abyssalcraft.content.block.deco;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.ChatFormatting;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.content.block.item.ColoredBlockItem;

/**
 * Decorative / plain-function blocks (owned by PB-5).
 *
 * <p>Ports the 1.12.2 decoration set: the decorative deity statues, mural and tombstones (all
 * horizontally facing), the ingot storage blocks, the Dreadlands ground blocks, Abyssal sand/glass,
 * and the two wasteland plants. Every entry is a vanilla {@link Block} or one of the two thin local
 * subclasses ({@link DecoFacingBlock}, {@link DecoPlantBlock}); nothing here touches loader/version
 * forked API, so the file carries no {@code //?}. Registry ids reuse the clean snake_case names.
 *
 * <p>Deliberately out of scope here (deferred to the owning stage):
 * <ul>
 *   <li>Creative-tab placement -&gt; Gate B integration on {@link com.shinoow.abyssalcraft.registry.ModCreativeTabs}.</li>
 *   <li>Final high-fidelity statue/tombstone/mural geometry -&gt; asset stage.</li>
 *   <li>{@code monolith_pillar} (a PE energy amplifier) and the functional/summoning statues -&gt; system stages.</li>
 * </ul>
 * {@link #BLOCKS} and {@link #ITEMS} are attached to the MOD bus by the main class through
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class DecoBlocks {

    private DecoBlocks() {}

    /** {@code minecraft:block} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);

    /** {@code minecraft:item} registrar for the block items. */
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    // --- Decorative deity statues (facing; strength 6.0 / 12.0, from 1.12.2) ---
    public static final Supplier<Block> DECORATIVE_CTHULHU_STATUE = facing("decorative_cthulhu_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_HASTUR_STATUE = facing("decorative_hastur_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_JZAHAR_STATUE = facing("decorative_jzahar_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_AZATHOTH_STATUE = facing("decorative_azathoth_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_NYARLATHOTEP_STATUE = facing("decorative_nyarlathotep_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_YOG_SOTHOTH_STATUE = facing("decorative_yog_sothoth_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);
    public static final Supplier<Block> DECORATIVE_SHUB_NIGGURATH_STATUE = facing("decorative_shub_niggurath_statue", 6.0F, 12.0F, DecoFacingBlock.ShapeKind.STATUE);

    // --- Mural (facing; 5.0 / 10.0) ---
    public static final Supplier<Block> MURAL = facing("mural", 5.0F, 10.0F, DecoFacingBlock.ShapeKind.MURAL);

    // --- Tombstones (facing; 2.5 / 20.0) ---
    public static final Supplier<Block> TOMBSTONE_STONE = facing("tombstone_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_ABYSSAL_STONE = facing("tombstone_abyssal_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_CORALIUM_STONE = facing("tombstone_coralium_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_DARKSTONE = facing("tombstone_darkstone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_DREADSTONE = facing("tombstone_dreadstone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_ELYSIAN_STONE = facing("tombstone_elysian_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_ETHAXIUM = facing("tombstone_ethaxium", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_MONOLITH_STONE = facing("tombstone_monolith_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<Block> TOMBSTONE_OMOTHOL_STONE = facing("tombstone_omothol_stone", 2.5F, 20.0F, DecoFacingBlock.ShapeKind.TOMBSTONE);
    public static final Supplier<BlockEntityType<TombstoneBlockEntity>> TOMBSTONE_BE =
        BLOCK_ENTITIES.register("tombstone", () -> BlockEntityType.Builder.of(TombstoneBlockEntity::new,
            TOMBSTONE_STONE.get(), TOMBSTONE_ABYSSAL_STONE.get(), TOMBSTONE_CORALIUM_STONE.get(),
            TOMBSTONE_DARKSTONE.get(), TOMBSTONE_DREADSTONE.get(), TOMBSTONE_ELYSIAN_STONE.get(),
            TOMBSTONE_ETHAXIUM.get(), TOMBSTONE_MONOLITH_STONE.get(), TOMBSTONE_OMOTHOL_STONE.get()).build(null));

    // --- Ingot / storage blocks (metal; 5.0 / 6.0) ---
    public static final Supplier<Block> BLOCK_OF_ABYSSALNITE = solid("block_of_abyssalnite", 5.0F, 6.0F, SoundType.METAL);
    public static final Supplier<Block> BLOCK_OF_REFINED_CORALIUM = solid("block_of_refined_coralium", 5.0F, 6.0F, SoundType.METAL);
    public static final Supplier<Block> BLOCK_OF_DREADIUM = solid("block_of_dreadium", 5.0F, 6.0F, SoundType.METAL);
    public static final Supplier<Block> BLOCK_OF_ETHAXIUM = solid("block_of_ethaxium", 5.0F, 6.0F, SoundType.METAL);

    // --- Ground blocks ---
    public static final Supplier<Block> DREADLANDS_DIRT = blocked("dreadlands_dirt", () -> new DreadlandsGroundBlock(
        BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GRAVEL).randomTicks(), false));
    public static final Supplier<Block> DREADLANDS_GRASS = blocked("dreadlands_grass", () -> new DreadlandsGroundBlock(
        BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRASS).randomTicks(), true));
    public static final Supplier<Block> DREADLANDS_MUCK = blocked("dreadlands_muck", () -> new DreadlandsMuckBlock(
        BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.SLIME_BLOCK)));
    public static final Supplier<Block> ABYSSAL_SAND = solid("abyssal_sand", 0.5F, 0.5F, SoundType.SAND);
    public static final Supplier<Block> FUSED_ABYSSAL_SAND = solid("fused_abyssal_sand", 0.5F, 0.5F, SoundType.STONE);

    // --- Glass (non-occluding; translucent render type ships with the model in PK) ---
    public static final Supplier<Block> ABYSSAL_SAND_GLASS = glass("abyssal_sand_glass");

    // --- Plants (bush: no collision, instant break, needs soil) ---
    public static final Supplier<Block> LUMINOUS_THISTLE = plant("luminous_thistle", false, 8);
    public static final Supplier<Block> WASTELANDS_THORN = plant("wastelands_thorn", true, 0);

    /** Register a horizontally-facing decorative block plus its item. */
    private static Supplier<Block> facing(String name, float hardness, float resistance, DecoFacingBlock.ShapeKind shape) {
        return blocked(name, () -> shape == DecoFacingBlock.ShapeKind.TOMBSTONE
            ? new TombstoneBlock(BlockBehaviour.Properties.of().strength(hardness, resistance).sound(SoundType.STONE).noOcclusion())
            : new DecoFacingBlock(BlockBehaviour.Properties.of().strength(hardness, resistance).sound(SoundType.STONE).noOcclusion(), shape));
    }

    /** Register a plain full-cube block plus its item. */
    private static Supplier<Block> solid(String name, float hardness, float resistance, SoundType sound) {
        return blocked(name, () -> new Block(
            BlockBehaviour.Properties.of().strength(hardness, resistance).sound(sound)));
    }

    /** Register a non-occluding glass-like block plus its item. */
    private static Supplier<Block> glass(String name) {
        return blocked(name, () -> new Block(
            BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS).noOcclusion()));
    }

    /** Register a decorative plant block plus its item. */
    private static Supplier<Block> plant(String name, boolean thorn, int light) {
        return blocked(name, () -> new DecoPlantBlock(
            BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)
                .lightLevel(state -> light), thorn));
    }

    /** Register a block and its default {@link BlockItem} under the same id. */
    private static Supplier<Block> blocked(String name, Supplier<Block> factory) {
        Supplier<Block> block = BLOCKS.register(name, factory);
        ChatFormatting color = switch (name) {
            case "block_of_abyssalnite" -> ChatFormatting.DARK_AQUA;
            case "block_of_refined_coralium", "block_of_ethaxium" -> ChatFormatting.AQUA;
            case "block_of_dreadium" -> ChatFormatting.DARK_RED;
            default -> null;
        };
        ITEMS.register(name, () -> color == null
            ? new BlockItem(block.get(), new Item.Properties())
            : new ColoredBlockItem(block.get(), new Item.Properties(), color));
        return block;
    }
}

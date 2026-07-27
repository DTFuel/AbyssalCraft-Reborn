package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.level.ItemLike;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
*///?}

/**
 * Compat: client item-colour (tint) handler registration (loader axis).
 *
 * <p><b>Client-only</b> -- attached through {@link SideExecutor#runWhenClient} so it never class-loads
 * on a dedicated server. Forge and NeoForge both fire {@code RegisterColorHandlersEvent.Item} with an
 * identical {@code register(ItemColor, ItemLike...)} signature (verified by javap on both merged jars);
 * only the event package differs, so that is the sole forked import. Used by PB-1 to tint the grayscale
 * elemental crystal textures per element.
 */
public final class ClientColorCompat {

    private ClientColorCompat() {}

    private record ItemEntry(java.util.function.IntSupplier rgb, List<Supplier<? extends ItemLike>> items) {}
    private record BlockEntry(java.util.function.IntSupplier rgb, List<Supplier<? extends Block>> blocks) {}
    private record DynamicBlockEntry(BlockTint tint, List<Supplier<? extends Block>> blocks) {}

    private static final List<ItemEntry> ITEM_ENTRIES = new ArrayList<>();
    private static final List<BlockEntry> BLOCK_ENTRIES = new ArrayList<>();
    private static final List<DynamicBlockEntry> DYNAMIC_BLOCK_ENTRIES = new ArrayList<>();

    @FunctionalInterface
    public interface BlockTint {
        int color(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex);
    }

    /** Queue a tint colour (RGB) for one or more items. Call (client-side) before {@link #attach}. */
    @SafeVarargs
    public static void queue(int rgb, Supplier<? extends ItemLike>... items) {
        queueDynamic(() -> rgb, items);
    }

    @SafeVarargs
    public static void queueDynamic(java.util.function.IntSupplier rgb, Supplier<? extends ItemLike>... items) {
        ITEM_ENTRIES.add(new ItemEntry(rgb, List.of(items)));
    }

    @SafeVarargs
    public static void queueBlocks(int rgb, Supplier<? extends Block>... blocks) {
        queueDynamicBlocks(() -> rgb, blocks);
    }

    @SafeVarargs
    public static void queueDynamicBlocks(java.util.function.IntSupplier rgb, Supplier<? extends Block>... blocks) {
        BLOCK_ENTRIES.add(new BlockEntry(rgb, List.of(blocks)));
    }

    @SafeVarargs
    public static void queueDynamicBlocks(BlockTint tint, Supplier<? extends Block>... blocks) {
        DYNAMIC_BLOCK_ENTRIES.add(new DynamicBlockEntry(tint, List.of(blocks)));
    }

    /** Attach the colour-registration listener to the MOD bus (client side only). */
    public static void attach(IEventBus modBus) {
        modBus.addListener((RegisterColorHandlersEvent.Item event) -> {
            for (ItemEntry entry : ITEM_ENTRIES) {
                ItemLike[] items = entry.items().stream().map(Supplier::get).toArray(ItemLike[]::new);
                event.register((stack, tintIndex) -> tintIndex == 0 ? entry.rgb().getAsInt() : 0xFFFFFF, items);
            }
        });
        modBus.addListener((RegisterColorHandlersEvent.Block event) -> {
            for (BlockEntry entry : BLOCK_ENTRIES) {
                Block[] blocks = entry.blocks().stream().map(Supplier::get).toArray(Block[]::new);
                event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? entry.rgb().getAsInt() : 0xFFFFFF, blocks);
            }
            for (DynamicBlockEntry entry : DYNAMIC_BLOCK_ENTRIES) {
                Block[] blocks = entry.blocks().stream().map(Supplier::get).toArray(Block[]::new);
                event.register(entry.tint()::color, blocks);
            }
        });
    }
}

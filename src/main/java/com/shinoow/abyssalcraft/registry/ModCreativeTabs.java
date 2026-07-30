package com.shinoow.abyssalcraft.registry;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.ghoul.GhoulHeadBlocks;
import com.shinoow.abyssalcraft.content.block.ore.OreBlocks;
import com.shinoow.abyssalcraft.content.block.portal.PortalBlocks;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.item.armor.ArmorItems;
import com.shinoow.abyssalcraft.content.item.bag.CrystalBagItems;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.energy.EnergyItems;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.item.material.MachineContentItems;
import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.content.item.portal.PortalItems;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItems;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;
import com.shinoow.abyssalcraft.content.item.transfer.TransferContent;
import com.shinoow.abyssalcraft.content.item.tool.ToolItems;
import com.shinoow.abyssalcraft.content.item.weapon.SoulReaperItems;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformers;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.LiquidAntimatterCompat;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;

/**
 * Creative-mode tabs (modern replacement for the 1.12.2 {@code ACTabs}).
 *
 * <p>Relay file (Gate B integration). Recreates the faithful 1.12.2 category structure for the M1
 * content: Blocks, Items, Tools, Combat, Food, Decorations and Crystals (the legacy Spells tab is
 * deferred with its system). Each tab pulls its contents lazily from the owning content module -- via
 * that module's {@link ModRegistrar#entries()} for blocks, or its public {@code ALL}/field lists for
 * items -- through the vanilla, fork-free {@code CreativeModeTab.Builder.displayItems} hook (proven
 * loader-identical by PB-1). Parallel content tasks never edit this file; the registrar is attached to
 * the MOD bus through {@link ModRegistries#ALL}.
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    /** DeferredRegister wrapper for {@code minecraft:creative_mode_tab} in the AbyssalCraft namespace. */
    public static final ModRegistrar<CreativeModeTab> TABS =
        ModRegistrar.of(Registries.CREATIVE_MODE_TAB, AbyssalCraft.MODID);

    // Food / miscellaneous split -- MiscItems exposes items as individual public fields, not lists.
    private static final List<Supplier<Item>> FOOD = List.of(
        MiscItems.CORALIUM_PLAGUED_FLESH, MiscItems.ANTI_BEEF, MiscItems.ANTI_CHICKEN, MiscItems.ANTI_PORK,
        MiscItems.ROTTEN_ANTI_FLESH, MiscItems.ANTI_SPIDER_EYE, MiscItems.ANTI_PLAGUED_FLESH, MiscItems.GENERIC_MEAT,
        MiscItems.COOKED_GENERIC_MEAT, MiscItems.GHOUL_FLESH, MiscItems.ABYSSAL_GHOUL_FLESH, MiscItems.DREADED_GHOUL_FLESH,
        MiscItems.OMOTHOL_GHOUL_FLESH, MiscItems.SHADOW_GHOUL_FLESH, MiscItems.ANTI_GHOUL_FLESH,
        MiscItems.CORALIUM_ANTIDOTE, MiscItems.DREAD_ANTIDOTE);

    private static final List<Supplier<Item>> MISC = List.of(
        MiscItems.OBLIVION_CATALYST, MiscItems.ANTI_BONE, MiscItems.POWERSTONE_TRACKER,
        MiscItems.EYE_OF_THE_ABYSS, MiscItems.ESSENCE_OF_THE_GATEKEEPER,
        MiscItems.TOKEN_OF_JZAHAR, MiscItems.SPIRIT_TABLET_SHARD_0, MiscItems.SPIRIT_TABLET_SHARD_1,
        MiscItems.SPIRIT_TABLET_SHARD_2, MiscItems.SPIRIT_TABLET_SHARD_3,
        MiscItems.SKIN_OF_THE_ABYSSAL_WASTELAND, MiscItems.SKIN_OF_THE_DREADLANDS, MiscItems.SKIN_OF_OMOTHOL);

    /** Blocks: building families (PB-3/8) + ores (PB-4) + machines (1.12.2 {@code tabBlock}, darkstone icon). */
    public static final Supplier<CreativeModeTab> BLOCKS = TABS.register("blocks", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.blocks"))
            .icon(() -> new ItemStack(BaseBlocks.DARKSTONE.get()))
            .displayItems((params, output) -> {
                acceptAll(output, BaseBlocks.BLOCKS.entries());
                acceptAll(output, OreBlocks.BLOCKS.entries());
                acceptAll(output, Materializers.BLOCKS.entries());
                acceptAll(output, Crystallizers.BLOCKS.entries());
                acceptAll(output, Transmutators.BLOCKS.entries());
                acceptAll(output, StateTransformers.BLOCKS.entries());
                acceptAll(output, RendingPedestals.BLOCKS.entries());
                acceptAll(output, EnergyBlocks.BLOCKS.entries());
                acceptAll(output, RitualBlocks.BLOCKS.entries());
                acceptAll(output, PortalBlocks.BLOCKS.entries());
                acceptAll(output, TransferContent.BLOCKS.entries());
                acceptAll(output, GhoulHeadBlocks.BLOCKS.entries());
            })
            .build());

    /** Items: crafting materials (PB-1) + miscellaneous item shells (PB-2) (1.12.2 {@code tabItems}). */
    public static final Supplier<CreativeModeTab> ITEMS = TABS.register("items", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.items"))
            .icon(() -> new ItemStack(MaterialItems.BASICS.get(0).get()))
            .displayItems((params, output) -> {
                acceptAll(output, MaterialItems.BASICS);
                acceptAll(output, CrystalBagItems.ALL);
                output.accept(LiquidCoraliumCompat.BUCKET.get());
                output.accept(LiquidAntimatterCompat.BUCKET.get());
                acceptAll(output, MISC);
                acceptAll(output, BookItems.ALL);
                acceptAll(output, PortalItems.ALL);
                output.accept(PortalItems.DREAD_PLAGUED_GATEWAY_KEY.get());
                acceptAll(output, RitualItems.ALL);
                acceptAll(output, ScrollItems.ALL);
                acceptAll(output, TabletItems.ALL);
                acceptAll(output, EnergyItems.CHARMS);
                output.accept(TransferContent.SPIRIT_TABLET.get());
                acceptAll(output, LegacyEntities.ITEMS.entries());
                acceptAll(output, DemonEntities.ITEMS.entries());
            })
            .build());

    /** Tools: pickaxes / axes / shovels / hoes (1.12.2 {@code tabTools}, abyssalnite axe icon). */
    public static final Supplier<CreativeModeTab> TOOLS = TABS.register("tools", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.tools"))
            .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(ACRef.id("abyssalnite_axe"))))
            .displayItems((params, output) -> {
                ToolItems.ALL.forEach(supplier -> {
                    Item tool = supplier.get();
                    if (!isSword(tool)) {
                        output.accept(tool);
                    }
                });
                acceptAll(output, MachineContentItems.ALL);
            })
            .build());

    /** Combat: swords (PB-6) + armor (PB-7) (1.12.2 {@code tabCombat}, abyssalnite sword icon). */
    public static final Supplier<CreativeModeTab> COMBAT = TABS.register("combat", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.combat"))
            .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(ACRef.id("abyssalnite_sword"))))
            .displayItems((params, output) -> {
                ToolItems.ALL.forEach(supplier -> {
                    Item tool = supplier.get();
                    if (isSword(tool)) {
                        output.accept(tool);
                    }
                });
                output.accept(SoulReaperItems.SOUL_REAPER_BLADE.get());
                acceptAll(output, ArmorItems.ALL);
            })
            .build());

    /** Food (1.12.2 {@code tabFood}, abyssal ghoul flesh icon). */
    public static final Supplier<CreativeModeTab> FOOD_TAB = TABS.register("food", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.food"))
            .icon(() -> new ItemStack(MiscItems.ABYSSAL_GHOUL_FLESH.get()))
            .displayItems((params, output) -> acceptAll(output, FOOD))
            .build());

    /** Decorations: statues / mural / tombstones / ingot blocks / ground / sand / glass / plants (1.12.2 {@code tabDecoration}). */
    public static final Supplier<CreativeModeTab> DECORATIONS = TABS.register("decorations", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.decorations"))
            .icon(() -> new ItemStack(DecoBlocks.MURAL.get()))
            .displayItems((params, output) -> acceptAll(output, DecoBlocks.BLOCKS.entries()))
            .build());

    /** Crystals: the 26-element crystal / shard / fragment set (1.12.2 {@code tabCrystals}). */
    public static final Supplier<CreativeModeTab> CRYSTALS = TABS.register("crystals", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyssalcraft.crystals"))
            .icon(() -> new ItemStack(MaterialItems.CRYSTALS.get(13).get()))
            .displayItems((params, output) -> {
                acceptAll(output, MaterialItems.CRYSTALS);
                acceptAll(output, MaterialItems.CRYSTAL_SHARDS);
                acceptAll(output, MaterialItems.CRYSTAL_FRAGMENTS);
                acceptAll(output, CrystalClusterBlocks.CLUSTERS);
                acceptAll(output, MaterialItems.MACHINE_COMPAT_CRYSTALS);
                acceptAll(output, MaterialItems.MACHINE_COMPAT_SHARDS);
                acceptAll(output, CrystalClusterBlocks.MACHINE_COMPAT_CLUSTERS);
            })
            .build());

    /** True if the item's registry id ends in {@code _sword} (splits the tool tier between tools/combat). */
    private static boolean isSword(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath().endsWith("_sword");
    }

    /** Add every supplier's element to the creative output (blocks are {@link ItemLike} via their BlockItem). */
    private static void acceptAll(CreativeModeTab.Output output, List<? extends Supplier<? extends ItemLike>> suppliers) {
        for (Supplier<? extends ItemLike> supplier : suppliers) {
            ItemLike element = supplier.get();
            if (element.asItem() != Items.AIR) {
                output.accept(element);
            }
        }
    }
}

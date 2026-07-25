package com.shinoow.abyssalcraft.content.block.energy;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.system.energy.DeityType;
import com.shinoow.abyssalcraft.system.energy.EnergyBlockKind;
import com.shinoow.abyssalcraft.system.energy.EnergyTier;

/**
 * Energy block content (owned by content/block/energy): the PE source that fills the energy items
 * (CR-58), unblocking the energy branch. The pilot deity statue charges nearby players' energy items on
 * tick (faithful 1.12.2 worship). The rest of the network -- pedestal / collector / container / relay
 * plus amplifiers / multiblock -- is the PS-5b follow-up. Registrars attach to the MOD bus through
 * {@link com.shinoow.abyssalcraft.registry.ModRegistries#ALL}.
 */
public final class EnergyBlocks {

    private EnergyBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    /** Pilot deity statue -- the PE source. Stores its deity for the deferred deity-filtered network. */
    public static final Supplier<Block> DEITY_STATUE = BLOCKS.register("deity_statue", () ->
        new DeityStatueBlock(BlockBehaviour.Properties.of().strength(3.5F), DeityType.CTHULHU));

    public static final Supplier<BlockItem> DEITY_STATUE_ITEM = ITEMS.register("deity_statue", () ->
        new BlockItem(DEITY_STATUE.get(), new Item.Properties()));

    public static final List<Supplier<Block>> DEITY_STATUES = Stream.of(DeityType.values())
        .map(deity -> registerBlock(deityStatueId(deity), () -> new DeityStatueBlock(
            BlockBehaviour.Properties.of().strength(6.0F, 12.0F).noOcclusion(), deity)))
        .toList();

    public static final Supplier<BlockEntityType<DeityStatueBlockEntity>> DEITY_STATUE_BE =
        BLOCK_ENTITIES.register("deity_statue", () ->
            BlockEntityType.Builder.<DeityStatueBlockEntity>of(
                DeityStatueBlockEntity::new, Stream.concat(Stream.of(DEITY_STATUE.get()),
                    DEITY_STATUES.stream().map(Supplier::get)).toArray(Block[]::new)).build(null));

    public static final List<Supplier<Block>> ENERGY_COLLECTORS = Stream.of(EnergyTier.values())
        .map(tier -> registerEnergyBlock(EnergyBlockKind.COLLECTOR.id(tier),
            () -> new EnergyCollectorBlock(BlockBehaviour.Properties.of().strength(3.0F, 12.0F), tier)))
        .toList();

    public static final Supplier<BlockEntityType<EnergyCollectorBlockEntity>> ENERGY_COLLECTOR_BE =
        BLOCK_ENTITIES.register("energy_collector", () ->
            BlockEntityType.Builder.<EnergyCollectorBlockEntity>of(
                EnergyCollectorBlockEntity::new,
                ENERGY_COLLECTORS.stream().map(Supplier::get).toArray(Block[]::new)).build(null));

    public static final List<Supplier<Block>> ENERGY_CONTAINERS = Stream.of(EnergyTier.values())
        .map(tier -> registerEnergyBlock(EnergyBlockKind.CONTAINER.id(tier),
            () -> new EnergyContainerBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F), tier)))
        .toList();

    public static final Supplier<BlockEntityType<EnergyContainerBlockEntity>> ENERGY_CONTAINER_BE =
        BLOCK_ENTITIES.register("energy_container", () ->
            BlockEntityType.Builder.<EnergyContainerBlockEntity>of(
                EnergyContainerBlockEntity::new,
                ENERGY_CONTAINERS.stream().map(Supplier::get).toArray(Block[]::new)).build(null));

    public static final List<Supplier<Block>> ENERGY_PEDESTALS = Stream.of(EnergyTier.values())
        .map(tier -> registerEnergyBlock(EnergyBlockKind.PEDESTAL.id(tier),
            () -> new EnergyPedestalBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F), tier)))
        .toList();

    public static final Supplier<BlockEntityType<EnergyPedestalBlockEntity>> ENERGY_PEDESTAL_BE =
        BLOCK_ENTITIES.register("energy_pedestal", () ->
            BlockEntityType.Builder.<EnergyPedestalBlockEntity>of(
                EnergyPedestalBlockEntity::new,
                ENERGY_PEDESTALS.stream().map(Supplier::get).toArray(Block[]::new)).build(null));

    public static final List<Supplier<Block>> ENERGY_RELAYS = Stream.of(EnergyTier.values())
        .map(tier -> registerEnergyBlock(EnergyBlockKind.RELAY.id(tier),
            () -> new EnergyRelayBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F), tier)))
        .toList();

    public static final Supplier<BlockEntityType<EnergyRelayBlockEntity>> ENERGY_RELAY_BE =
        BLOCK_ENTITIES.register("energy_relay", () ->
            BlockEntityType.Builder.<EnergyRelayBlockEntity>of(
                EnergyRelayBlockEntity::new,
                ENERGY_RELAYS.stream().map(Supplier::get).toArray(Block[]::new)).build(null));

    public static final Supplier<Block> ENERGY_DEPOSITIONER = registerEnergyBlock("energydepositioner", () ->
        new EnergyDepositionerBlock(BlockBehaviour.Properties.of().strength(6.0F, 12.0F)));

    public static final Supplier<BlockEntityType<EnergyDepositionerBlockEntity>> ENERGY_DEPOSITIONER_BE =
        BLOCK_ENTITIES.register("energy_depositioner", () ->
            BlockEntityType.Builder.<EnergyDepositionerBlockEntity>of(
                EnergyDepositionerBlockEntity::new, ENERGY_DEPOSITIONER.get()).build(null));

    public static final Supplier<Block> IDOL_OF_FADING = registerEnergyBlock("idol_of_fading", () ->
        new IdolOfFadingBlock(BlockBehaviour.Properties.of().strength(1.0F, 18.0F)
            .lightLevel(state -> 8).noOcclusion()));

    public static final Supplier<BlockEntityType<IdolOfFadingBlockEntity>> IDOL_OF_FADING_BE =
        BLOCK_ENTITIES.register("idol_of_fading", () ->
            BlockEntityType.Builder.<IdolOfFadingBlockEntity>of(
                IdolOfFadingBlockEntity::new, IDOL_OF_FADING.get()).build(null));

    public static final Supplier<Block> MONOLITH_PILLAR = registerBlock("monolith_pillar", () ->
        new MonolithPillarBlock(BlockBehaviour.Properties.of().strength(6.0F, 24.0F).noOcclusion()));

    public static final Supplier<Block> PLACE_OF_POWER_BASE = BLOCKS.register("multi_block", () ->
        new PlaceOfPowerBaseBlock(BlockBehaviour.Properties.of().strength(6.0F, 24.0F)));

    public static final Supplier<BlockEntityType<PlaceOfPowerBaseBlockEntity>> PLACE_OF_POWER_BASE_BE =
        BLOCK_ENTITIES.register("multi_block", () ->
            BlockEntityType.Builder.<PlaceOfPowerBaseBlockEntity>of(
                PlaceOfPowerBaseBlockEntity::new, PLACE_OF_POWER_BASE.get()).build(null));

    private static Supplier<Block> registerBlock(String name, Supplier<? extends Block> block) {
        Supplier<Block> registered = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private static Supplier<Block> registerEnergyBlock(String name, Supplier<? extends Block> block) {
        Supplier<Block> registered = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new EnergyBlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private static String deityStatueId(DeityType deity) {
        return switch (deity) {
            case CTHULHU -> "cthulhu_statue";
            case HASTUR -> "hastur_statue";
            case JZAHAR -> "jzahar_statue";
            case AZATHOTH -> "azathoth_statue";
            case NYARLATHOTEP -> "nyarlathotep_statue";
            case SHUBNIGGURATH -> "shub_niggurath_statue";
            case YOGSOTHOTH -> "yog_sothoth_statue";
        };
    }
}

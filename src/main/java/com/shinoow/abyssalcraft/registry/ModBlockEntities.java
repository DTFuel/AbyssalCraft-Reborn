package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.blockentity.base.DirectionalBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.InventoryBlockEntity;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * General block-entity type registry (owned by PC-1, Stage C1).
 *
 * <p>Aggregation point for the reusable framework BE bases ({@link DirectionalBlockEntity} /
 * {@link InventoryBlockEntity}) and future non-machine block entities. The three pilot machines keep
 * their own per-module registrars ({@code MachineBlockEntities} + crystallizer/materializer/
 * transmutator); concrete BEs supply their own blocks and register their own subtypes over these
 * bases. Attached to the MOD bus via {@link ModRegistries#ALL}.
 *
 * <p>The two example types below are block-less smoke tests proving the framework bases register on
 * both loaders (mirroring the PP-1 pattern -- {@code build(null)} = no datafixer type, valid blocks
 * added when a concrete BE claims the base).
 */
public final class ModBlockEntities {

    private ModBlockEntities() {}

    /** {@code minecraft:block_entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    /** Example directional BE type over {@link DirectionalBlockEntity}. */
    public static final Supplier<BlockEntityType<DirectionalBlockEntity>> DIRECTIONAL = BLOCK_ENTITIES.register("directional", () ->
        BlockEntityType.Builder.<DirectionalBlockEntity>of(
            (pos, state) -> new DirectionalBlockEntity(ModBlockEntities.DIRECTIONAL.get(), pos, state)).build(null));

    /** Example single-slot inventory BE type over {@link InventoryBlockEntity}. */
    public static final Supplier<BlockEntityType<InventoryBlockEntity>> INVENTORY = BLOCK_ENTITIES.register("inventory", () ->
        BlockEntityType.Builder.<InventoryBlockEntity>of(
            (pos, state) -> new InventoryBlockEntity(ModBlockEntities.INVENTORY.get(), pos, state, 1)).build(null));
}

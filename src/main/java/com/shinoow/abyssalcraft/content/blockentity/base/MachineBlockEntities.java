package com.shinoow.abyssalcraft.content.blockentity.base;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/**
 * Machine block entity types (owned by PP-1). Registers one example type over
 * {@link MachineBlockEntity} to prove the BE-registration path on both loaders. The type has no valid
 * blocks yet -- P2 machines supply their machine blocks (or register their own subtypes); a
 * block-less type still registers cleanly.
 */
public final class MachineBlockEntities {

    private MachineBlockEntities() {}

    /** {@code minecraft:block_entity_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    /** Example machine BE type ({@code build(null)} = no datafixer type; valid blocks added in P2). */
    public static final Supplier<BlockEntityType<MachineBlockEntity>> MACHINE = BLOCK_ENTITIES.register("machine", () ->
        BlockEntityType.Builder.<MachineBlockEntity>of(
            (pos, state) -> new MachineBlockEntity(MachineBlockEntities.MACHINE.get(), pos, state)).build(null));
}

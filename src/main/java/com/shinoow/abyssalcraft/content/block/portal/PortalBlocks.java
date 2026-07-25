package com.shinoow.abyssalcraft.content.block.portal;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ModRegistrar;

/** Normal and unchained Portal Anchor content. */
public final class PortalBlocks {

    private PortalBlocks() {}

    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);
    public static final ModRegistrar<BlockEntityType<?>> BLOCK_ENTITIES =
        ModRegistrar.of(Registries.BLOCK_ENTITY_TYPE, AbyssalCraft.MODID);

    public static final Supplier<PortalAnchorBlock> PORTAL_ANCHOR = anchor("portal_anchor", false);
    public static final Supplier<PortalAnchorBlock> UNCHAINED_PORTAL_ANCHOR =
        anchor("unchained_portal_anchor", true);

    public static final Supplier<BlockItem> PORTAL_ANCHOR_ITEM = item("portal_anchor", PORTAL_ANCHOR);
    public static final Supplier<BlockItem> UNCHAINED_PORTAL_ANCHOR_ITEM =
        item("unchained_portal_anchor", UNCHAINED_PORTAL_ANCHOR);

    public static final Supplier<BlockEntityType<PortalAnchorBlockEntity>> PORTAL_ANCHOR_BE =
        BLOCK_ENTITIES.register("portal_anchor", () ->
            BlockEntityType.Builder.of(PortalAnchorBlockEntity::new,
                PORTAL_ANCHOR.get(), UNCHAINED_PORTAL_ANCHOR.get()).build(null));

    private static Supplier<PortalAnchorBlock> anchor(String id, boolean unchained) {
        return BLOCKS.register(id, () -> new PortalAnchorBlock(BlockBehaviour.Properties.of()
            .strength(10.0F, 24.0F).sound(SoundType.STONE).noOcclusion(), unchained));
    }

    private static Supplier<BlockItem> item(String id, Supplier<? extends Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
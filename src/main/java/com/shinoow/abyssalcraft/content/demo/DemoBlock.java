package com.shinoow.abyssalcraft.content.demo;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Minimal demo block (owned by PA-5).
 *
 * <p>The M0 vertical-slice "hero": a plain, placeable block with no custom behaviour. Its sole job is
 * to prove the pipeline end to end -- a business-package class (outside {@code platform/}) that
 * compiles on both loader nodes, is registered through
 * {@link com.shinoow.abyssalcraft.platform.ModRegistrar}, carries hand-written blockstate/model/lang
 * assets, and shows up placeable in the creative menu. Uses only vanilla API, so it needs no
 * {@code //?} loader/version fork.
 */
public class DemoBlock extends Block {

    public DemoBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

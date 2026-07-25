package com.shinoow.abyssalcraft.data.gen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.DataGenCompat;
import com.shinoow.abyssalcraft.platform.ItemModelGen;

/**
 * Item-model datagen (owned by PK-3). Iterates every registered {@code abyssalcraft} {@link Item} and
 * emits a missing item model so items stop rendering as the missing-model marker:
 * <ul>
 *   <li>{@link SpawnEggItem} &rarr; the vanilla {@code item/template_spawn_egg} (tinted by the egg's colours);</li>
 *   <li>{@link BlockItem} &rarr; skipped (its inventory model is emitted by the block datagen, parenting the block model);</li>
 *   <li>everything else &rarr; a flat {@code item/generated} model with {@code layer0 = abyssalcraft:item/<id>}.</li>
 * </ul>
 *
 * <p>Items whose model is already shipped (hand-migrated under {@code models/item/} in resources) are
 * skipped via the inherited {@code existingFileHelper}, so this never clobbers a bespoke model. Textures
 * that are still absent will show the missing-texture marker (a separate PK-1 concern), but the model
 * itself now loads.
 */
public final class ModelItemData extends ItemModelGen {

    public ModelItemData(DataGenCompat.Gen gen) {
        super(gen);
    }

    @Override
    protected void generate() {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!AbyssalCraft.MODID.equals(id.getNamespace())) {
                continue;
            }
            // Do not clobber a hand-migrated model already present in resources.
            if (existingFileHelper.exists(id, PackType.CLIENT_RESOURCES, ".json", "models/item")) {
                continue;
            }
            String path = id.getPath();
            if (item instanceof SpawnEggItem) {
                withExistingParent(path, mcLoc("item/template_spawn_egg"));
            } else if (item instanceof BlockItem) {
                // Block-item inventory model comes from the block datagen; skip here.
                continue;
            } else if (existingFileHelper.exists(modLoc("item/" + path), PackType.CLIENT_RESOURCES, ".png", "textures")) {
                // Flat generated model, but only when the icon texture is actually present -- a generated
                // model with a missing texture fails datagen validation. Untextured items (PK-1 remainder)
                // are skipped and keep showing the missing-model marker until their texture is migrated.
                basicItem(item);
            }
        }
    }
}

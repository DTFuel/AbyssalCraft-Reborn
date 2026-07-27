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
 *   <li>{@link BlockItem} &rarr; owned by a same-id registered block and its block datagen;</li>
 *   <li>everything else &rarr; a flat {@code item/generated} model with {@code layer0 = abyssalcraft:item/<id>}.</li>
 * </ul>
 *
 * <p>Items whose model is already shipped (hand-migrated under {@code models/item/} in resources) are
 * skipped via the inherited {@code existingFileHelper}, so this never clobbers a bespoke model. Every
 * registered item must resolve to one of these owners; an untextured non-block item fails runData.
 */
public final class ModelItemData extends ItemModelGen {

    private int auditedItems;
    private int ownedModels;
    private int generatedModels;
    private int blockItemOwners;

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
            auditedItems++;
            // Do not clobber a hand-migrated model already present in resources.
            if (existingFileHelper.exists(id, PackType.CLIENT_RESOURCES, ".json", "models/item")) {
                ownedModels++;
                continue;
            }
            String path = id.getPath();
            if (item instanceof SpawnEggItem) {
                withExistingParent(path, mcLoc("item/template_spawn_egg"));
                generatedModels++;
            } else if (item instanceof BlockItem) {
                if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                    throw new IllegalStateException("RR-ASSET-ITEM block item has no same-id block owner " + id);
                }
                blockItemOwners++;
                continue;
            } else if (existingFileHelper.exists(modLoc("item/" + path), PackType.CLIENT_RESOURCES, ".png", "textures")) {
                basicItem(item);
                generatedModels++;
            } else {
                throw new IllegalStateException("RR-ASSET-ITEM missing model owner and texture for registered item " + id);
            }
        }
        if (auditedItems != ownedModels + generatedModels + blockItemOwners) {
            throw new IllegalStateException("RR-ASSET-ITEM owner accounting mismatch");
        }
        System.out.println("RR_ASSET_ITEM_AUDIT_OK items=" + auditedItems + " existing=" + ownedModels
            + " generated=" + generatedModels + " blockOwners=" + blockItemOwners);
    }
}

package com.shinoow.abyssalcraft.platform;

//? if forge {
import net.minecraftforge.client.model.generators.ItemModelProvider;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;

/**
 * Compat: item-model datagen facade (loader axis), the item-model peer of {@link BlockModelGen}.
 *
 * <p>Only the {@code ItemModelProvider} package forks between Forge and NeoForge; its helper surface
 * ({@code basicItem} / {@code withExistingParent} / {@code singleTexture}) and the inherited
 * {@code existingFileHelper} are identical, so business providers ({@code data/gen/ModelItemData})
 * extend this and stay free of {@code //?}. Item textures resolve to {@code abyssalcraft:item/<id>}.
 */
public abstract class ItemModelGen extends ItemModelProvider {

    protected ItemModelGen(DataGenCompat.Gen gen) {
        super(gen.packOutput, AbyssalCraft.MODID, gen.existingFiles);
    }

    @Override
    protected void registerModels() {
        generate();
    }

    /** Business hook: define item models here (typically iterate the item registry). */
    protected abstract void generate();
}

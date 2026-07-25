package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.shinoow.abyssalcraft.content.recipe.materialization.CountedIngredient;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
//? if forge {
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.ShapedRecipe;
//?} else {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
*///?}

public final class MaterializationRecipeSerializer implements RecipeSerializer<MaterializationRecipe> {

    //? if >=1.21 {
    /*private static final Codec<CountedIngredient> INPUT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
        Codec.INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
    ).apply(instance, CountedIngredient::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> INPUT_STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
        ByteBufCodecs.VAR_INT, CountedIngredient::count,
        CountedIngredient::new);

    private final MapCodec<MaterializationRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
        INPUT_CODEC.listOf().fieldOf("inputs").forGetter(MaterializationRecipe::inputs),
        ItemStack.CODEC.fieldOf("result").forGetter(MaterializationRecipe::result)
    ).apply(instance, MaterializationRecipe::new));

    private final StreamCodec<RegistryFriendlyByteBuf, MaterializationRecipe> streamCodec = StreamCodec.composite(
        INPUT_STREAM_CODEC.apply(ByteBufCodecs.list(5)), MaterializationRecipe::inputs,
        ItemStack.STREAM_CODEC, MaterializationRecipe::result,
        MaterializationRecipe::new);
    *///?}

    //? if forge {
    @Override
    public MaterializationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        List<CountedIngredient> inputs = new ArrayList<>();
        if (json.has("inputs")) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "inputs");
            for (JsonElement element : array) {
                JsonObject entry = GsonHelper.convertToJsonObject(element, "materialization input");
                inputs.add(new CountedIngredient(
                    Ingredient.fromJson(entry.get("ingredient")), GsonHelper.getAsInt(entry, "count", 1)));
            }
        } else {
            inputs.add(new CountedIngredient(Ingredient.fromJson(json.get("input")), 1));
        }
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        MaterializationRecipe recipe = new MaterializationRecipe(inputs, result);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public MaterializationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 1 || size > 5) {
            throw new IllegalArgumentException("Materialization recipe network input count must be between one and five");
        }
        List<CountedIngredient> inputs = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            inputs.add(new CountedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
        }
        MaterializationRecipe recipe = new MaterializationRecipe(inputs, buffer.readItem());
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, MaterializationRecipe recipe) {
        buffer.writeVarInt(recipe.inputs().size());
        for (CountedIngredient input : recipe.inputs()) {
            input.ingredient().toNetwork(buffer);
            buffer.writeVarInt(input.count());
        }
        buffer.writeItem(recipe.result());
    }
    //?} else {
    /*@Override
    public MapCodec<MaterializationRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MaterializationRecipe> streamCodec() {
        return streamCodec;
    }
    *///?}
}
package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
//? if forge {
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

public final class CrystallizationRecipeSerializer implements RecipeSerializer<CrystallizationRecipe> {

    //? if >=1.21 {
    /*private final MapCodec<CrystallizationRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.fieldOf("input").forGetter(CrystallizationRecipe::input),
        ItemStack.CODEC.fieldOf("result").forGetter(CrystallizationRecipe::result),
        ItemStack.CODEC.optionalFieldOf("secondary_result", ItemStack.EMPTY).forGetter(CrystallizationRecipe::secondaryResult),
        Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(CrystallizationRecipe::experience),
        Codec.INT.optionalFieldOf("time", 200).forGetter(CrystallizationRecipe::time)
    ).apply(instance, CrystallizationRecipe::new));

    private final StreamCodec<RegistryFriendlyByteBuf, CrystallizationRecipe> streamCodec = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, CrystallizationRecipe::input,
        ItemStack.STREAM_CODEC, CrystallizationRecipe::result,
        ItemStack.OPTIONAL_STREAM_CODEC, CrystallizationRecipe::secondaryResult,
        ByteBufCodecs.FLOAT, CrystallizationRecipe::experience,
        ByteBufCodecs.VAR_INT, CrystallizationRecipe::time,
        CrystallizationRecipe::new);
    *///?}

    //? if forge {
    @Override
    public CrystallizationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(json.get("input"));
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        ItemStack secondary = json.has("secondary_result")
            ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "secondary_result"))
            : ItemStack.EMPTY;
        float experience = GsonHelper.getAsFloat(json, "experience", 0F);
        int time = GsonHelper.getAsInt(json, "time", 200);
        CrystallizationRecipe recipe = new CrystallizationRecipe(input, result, secondary, experience, time);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public CrystallizationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        CrystallizationRecipe recipe = new CrystallizationRecipe(
            Ingredient.fromNetwork(buffer), buffer.readItem(), buffer.readItem(), buffer.readFloat(), buffer.readVarInt());
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CrystallizationRecipe recipe) {
        recipe.input().toNetwork(buffer);
        buffer.writeItem(recipe.result());
        buffer.writeItem(recipe.secondaryResult());
        buffer.writeFloat(recipe.experience());
        buffer.writeVarInt(recipe.time());
    }
    //?} else {
    /*@Override
    public MapCodec<CrystallizationRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CrystallizationRecipe> streamCodec() {
        return streamCodec;
    }
    *///?}
}
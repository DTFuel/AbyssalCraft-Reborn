package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;
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

public final class TransmutationRecipeSerializer implements RecipeSerializer<TransmutationRecipe> {

    //? if >=1.21 {
    /*private final MapCodec<TransmutationRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.fieldOf("input").forGetter(TransmutationRecipe::input),
        ItemStack.CODEC.fieldOf("result").forGetter(TransmutationRecipe::result),
        Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(TransmutationRecipe::experience),
        Codec.INT.optionalFieldOf("time", 200).forGetter(TransmutationRecipe::time)
    ).apply(instance, TransmutationRecipe::new));

    private final StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> streamCodec = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, TransmutationRecipe::input,
        ItemStack.STREAM_CODEC, TransmutationRecipe::result,
        ByteBufCodecs.FLOAT, TransmutationRecipe::experience,
        ByteBufCodecs.VAR_INT, TransmutationRecipe::time,
        TransmutationRecipe::new);
    *///?}

    //? if forge {
    @Override
    public TransmutationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(json.get("input"));
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        float experience = GsonHelper.getAsFloat(json, "experience", 0F);
        int time = GsonHelper.getAsInt(json, "time", 200);
        TransmutationRecipe recipe = new TransmutationRecipe(input, result, experience, time);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public TransmutationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        TransmutationRecipe recipe = new TransmutationRecipe(
            Ingredient.fromNetwork(buffer), buffer.readItem(), buffer.readFloat(), buffer.readVarInt());
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, TransmutationRecipe recipe) {
        recipe.input().toNetwork(buffer);
        buffer.writeItem(recipe.result());
        buffer.writeFloat(recipe.experience());
        buffer.writeVarInt(recipe.time());
    }
    //?} else {
    /*@Override
    public MapCodec<TransmutationRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> streamCodec() {
        return streamCodec;
    }
    *///?}
}
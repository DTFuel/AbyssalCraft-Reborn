package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.shinoow.abyssalcraft.content.recipe.anvil.AnvilForgingRecipe;
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

/**
 * Forked {@link RecipeSerializer} for {@link AnvilForgingRecipe} -- {@code input1 + input2 -> result}
 * with an int {@code price} and a {@code forging_type} string. Absorbs the 1.21 serializer rework
 * ({@code fromJson}/{@code fromNetwork}/{@code toNetwork} -&gt; {@code MapCodec} + {@code StreamCodec}).
 * JSON: {@code {"input1":<ing>,"input2":<ing>,"result":<item>,"price":<int>,"forging_type":<str>}}.
 */
public final class AnvilForgingRecipeSerializer implements RecipeSerializer<AnvilForgingRecipe> {

    //? if >=1.21 {
    /*private final MapCodec<AnvilForgingRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.fieldOf("input1").forGetter(AnvilForgingRecipe::input1),
        Ingredient.CODEC.fieldOf("input2").forGetter(AnvilForgingRecipe::input2),
        ItemStack.CODEC.fieldOf("result").forGetter(AnvilForgingRecipe::result),
        Codec.INT.optionalFieldOf("price", 1).forGetter(AnvilForgingRecipe::price),
        Codec.STRING.optionalFieldOf("forging_type", "default").forGetter(AnvilForgingRecipe::forgingType)
    ).apply(instance, AnvilForgingRecipe::new));

    private final StreamCodec<RegistryFriendlyByteBuf, AnvilForgingRecipe> streamCodec = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, AnvilForgingRecipe::input1,
        Ingredient.CONTENTS_STREAM_CODEC, AnvilForgingRecipe::input2,
        ItemStack.STREAM_CODEC, AnvilForgingRecipe::result,
        ByteBufCodecs.VAR_INT, AnvilForgingRecipe::price,
        ByteBufCodecs.STRING_UTF8, AnvilForgingRecipe::forgingType,
        AnvilForgingRecipe::new);
    *///?}

    //? if forge {
    @Override
    public AnvilForgingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input1 = Ingredient.fromJson(json.get("input1"));
        Ingredient input2 = Ingredient.fromJson(json.get("input2"));
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        int price = GsonHelper.getAsInt(json, "price", 1);
        String forgingType = GsonHelper.getAsString(json, "forging_type", "default");
        AnvilForgingRecipe recipe = new AnvilForgingRecipe(input1, input2, result, price, forgingType);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public AnvilForgingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input1 = Ingredient.fromNetwork(buffer);
        Ingredient input2 = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        int price = buffer.readVarInt();
        String forgingType = buffer.readUtf();
        AnvilForgingRecipe recipe = new AnvilForgingRecipe(input1, input2, result, price, forgingType);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AnvilForgingRecipe recipe) {
        recipe.input1().toNetwork(buffer);
        recipe.input2().toNetwork(buffer);
        buffer.writeItem(recipe.result());
        buffer.writeVarInt(recipe.price());
        buffer.writeUtf(recipe.forgingType());
    }
    //?} else {
    /*@Override
    public MapCodec<AnvilForgingRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AnvilForgingRecipe> streamCodec() {
        return streamCodec;
    }
    *///?}
}

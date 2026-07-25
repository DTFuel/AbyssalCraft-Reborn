package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
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
 * Forked {@link RecipeSerializer} for {@link RendingRecipe} -- {@code name + max_energy + entity ->
 * result} (with an optional {@code dimension}). Absorbs the 1.21 serializer rework
 * ({@code fromJson}/{@code fromNetwork}/{@code toNetwork} -&gt; {@code MapCodec} + {@code StreamCodec}).
 * JSON: {@code {"name":<str>,"max_energy":<int>,"result":<item>,"entity":<str>,"dimension":<int>}}.
 */
public final class RendingRecipeSerializer implements RecipeSerializer<RendingRecipe> {

    //? if >=1.21 {
    /*private final MapCodec<RendingRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(RendingRecipe::energyName),
        Codec.INT.fieldOf("max_energy").forGetter(RendingRecipe::maxEnergy),
        ItemStack.CODEC.fieldOf("result").forGetter(RendingRecipe::result),
        Codec.STRING.fieldOf("entity").forGetter(RendingRecipe::entity),
        Codec.INT.optionalFieldOf("dimension", 0).forGetter(RendingRecipe::dimension)
    ).apply(instance, RendingRecipe::new));

    private final StreamCodec<RegistryFriendlyByteBuf, RendingRecipe> streamCodec = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RendingRecipe::energyName,
        ByteBufCodecs.VAR_INT, RendingRecipe::maxEnergy,
        ItemStack.STREAM_CODEC, RendingRecipe::result,
        ByteBufCodecs.STRING_UTF8, RendingRecipe::entity,
        ByteBufCodecs.VAR_INT, RendingRecipe::dimension,
        RendingRecipe::new);
    *///?}

    //? if forge {
    @Override
    public RendingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String name = GsonHelper.getAsString(json, "name");
        int maxEnergy = GsonHelper.getAsInt(json, "max_energy");
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        String entity = GsonHelper.getAsString(json, "entity");
        int dimension = GsonHelper.getAsInt(json, "dimension", 0);
        RendingRecipe recipe = new RendingRecipe(name, maxEnergy, result, entity, dimension);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public RendingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String name = buffer.readUtf();
        int maxEnergy = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        String entity = buffer.readUtf();
        int dimension = buffer.readVarInt();
        RendingRecipe recipe = new RendingRecipe(name, maxEnergy, result, entity, dimension);
        recipe.assignId(recipeId);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RendingRecipe recipe) {
        buffer.writeUtf(recipe.energyName());
        buffer.writeVarInt(recipe.maxEnergy());
        buffer.writeItem(recipe.result());
        buffer.writeUtf(recipe.entity());
        buffer.writeVarInt(recipe.dimension());
    }
    //?} else {
    /*@Override
    public MapCodec<RendingRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RendingRecipe> streamCodec() {
        return streamCodec;
    }
    *///?}
}

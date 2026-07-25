package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
 * Compat: a {@link RecipeSerializer} for the fixed "processing" shape (input {@link Ingredient} +
 * result {@link ItemStack} + time), absorbing the 1.21 serializer rework
 * ({@code fromJson}/{@code fromNetwork}/{@code toNetwork} -&gt; {@code MapCodec codec()} +
 * {@code StreamCodec streamCodec()}). JSON shape: {@code {"input":<ingredient>,"result":<item>,"time":<int>}}.
 *
 * <p>Callers pass a {@link Factory} that builds their concrete {@link RecipeCompat} subclass, so both
 * the recipe class and its registrar stay free of loader/version forks.
 */
public final class RecipeSerializerCompat {

    private RecipeSerializerCompat() {}

    /** Builds a processing-shape {@link RecipeCompat} from its three parts. */
    @FunctionalInterface
    public interface Factory<T extends RecipeCompat> {
        T create(Ingredient input, ItemStack result, int time);
    }

    public static <T extends RecipeCompat> RecipeSerializer<T> processing(Factory<T> factory) {
        return new Impl<>(factory);
    }

    private static final class Impl<T extends RecipeCompat> implements RecipeSerializer<T> {

        private final Factory<T> factory;

        //? if >=1.21 {
        /*private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
        *///?}

        Impl(Factory<T> factory) {
            this.factory = factory;
            //? if >=1.21 {
            /*this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.INT.fieldOf("time").forGetter(recipe -> recipe.time)
            ).apply(instance, factory::create));
            this.streamCodec = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.input,
                ItemStack.STREAM_CODEC, recipe -> recipe.result,
                ByteBufCodecs.VAR_INT, recipe -> recipe.time,
                factory::create);
            *///?}
        }

        //? if forge {
        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "time", 200);
            T recipe = factory.create(input, result, time);
            recipe.assignId(recipeId);
            return recipe;
        }

        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readVarInt();
            T recipe = factory.create(input, result, time);
            recipe.assignId(recipeId);
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.time);
        }
        //?} else {
        /*@Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
        *///?}
    }
}

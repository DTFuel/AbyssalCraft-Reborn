package com.shinoow.abyssalcraft.platform;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
//? if >=1.21 {
/*import com.mojang.serialization.MapCodec;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

//? if forge {
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
//?} else {
/*import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.config.ContentConfigMatrix;

/** Loader registry bridge for the server-side legacy vanilla chest loot injection. */
public final class ContentLootCompat {

    //? if forge {
    public static final ModRegistrar<Codec<? extends IGlobalLootModifier>> MODIFIERS = registrar();
    public static final Supplier<Codec<LegacyChestLootModifier>> LEGACY_CHESTS =
        MODIFIERS.register("legacy_chest_content", () -> LegacyChestLootModifier.CODEC);
    //?} else {
    /*public static final ModRegistrar<MapCodec<? extends IGlobalLootModifier>> MODIFIERS = registrar();
    public static final Supplier<MapCodec<LegacyChestLootModifier>> LEGACY_CHESTS =
        MODIFIERS.register("legacy_chest_content", () -> LegacyChestLootModifier.CODEC);
    *///?}

    private ContentLootCompat() {}

    //? if forge {
    private static ModRegistrar<Codec<? extends IGlobalLootModifier>> registrar() {
        return ModRegistrar.of(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, AbyssalCraft.MODID);
    }
    //?} else {
    /*private static ModRegistrar<MapCodec<? extends IGlobalLootModifier>> registrar() {
        return ModRegistrar.of(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, AbyssalCraft.MODID);
    }
    *///?}

    public static final class LegacyChestLootModifier extends LootModifier {

        //? if forge {
        static final Codec<LegacyChestLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, LegacyChestLootModifier::new));
        //?} else {
        /*static final MapCodec<LegacyChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, LegacyChestLootModifier::new));
        *///?}
        private static final ResourceLocation[] ITEMS = {
            ACRef.id("abyssalnite_ingot"), ACRef.id("coralium_gem"), ACRef.id("shadow_fragment")
        };

        private LegacyChestLootModifier(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                      LootContext context) {
            if (!ContentConfigMatrix.lootTableContent() || context.getRandom().nextInt(4) != 0) {
                return generatedLoot;
            }
            ResourceLocation id = ITEMS[context.getRandom().nextInt(ITEMS.length)];
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(item ->
                generatedLoot.add(new ItemStack(item, 1 + context.getRandom().nextInt(3))));
            return generatedLoot;
        }

        @Override
        //? if forge {
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
        //?} else {
        /*public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
        *///?}
    }
}
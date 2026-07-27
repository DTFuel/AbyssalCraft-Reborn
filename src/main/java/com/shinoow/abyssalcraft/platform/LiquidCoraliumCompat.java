package com.shinoow.abyssalcraft.platform;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
//? if forge {
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
*///?}

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.block.world.LiquidCoraliumBlock;

public final class LiquidCoraliumCompat {

    private static final ResourceLocation STILL = ACRef.id("block/liquid_coralium_still");
    private static final ResourceLocation FLOWING_TEXTURE = ACRef.id("block/liquid_coralium_flow");

    public static final ModRegistrar<FluidType> FLUID_TYPES = createFluidTypeRegistrar();
    public static final ModRegistrar<Fluid> FLUIDS = ModRegistrar.of(Registries.FLUID, AbyssalCraft.MODID);
    public static final ModRegistrar<Block> BLOCKS = ModRegistrar.of(Registries.BLOCK, AbyssalCraft.MODID);
    public static final ModRegistrar<Item> ITEMS = ModRegistrar.of(Registries.ITEM, AbyssalCraft.MODID);

    public static final Supplier<FluidType> TYPE = FLUID_TYPES.register("liquid_coralium",
        LiquidCoraliumCompat::createType);
    public static final Supplier<Fluid> SOURCE = FLUIDS.register("liquid_coralium", () -> createFluid(true));
    public static final Supplier<Fluid> FLOWING = FLUIDS.register("flowing_liquid_coralium", () -> createFluid(false));
    public static final Supplier<LiquidBlock> BLOCK = BLOCKS.register("liquid_coralium", () ->
        new LiquidCoraliumBlock((FlowingFluid) SOURCE.get(), BlockBehaviour.Properties.of().replaceable()
            .noCollission().strength(100.0F).noLootTable().liquid().lightLevel(state -> 15)));
    public static final Supplier<Item> BUCKET = ITEMS.register("liquid_coralium_bucket", () ->
        new BucketItem(SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private LiquidCoraliumCompat() {}

    private static ModRegistrar<FluidType> createFluidTypeRegistrar() {
        //? if forge {
        return ModRegistrar.of(ForgeRegistries.Keys.FLUID_TYPES, AbyssalCraft.MODID);
        //?} else {
        /*return ModRegistrar.of(NeoForgeRegistries.Keys.FLUID_TYPES, AbyssalCraft.MODID);
        *///?}
    }

    private static FluidType createType() {
        return new FluidType(FluidType.Properties.create()
            .descriptionId("fluid.abyssalcraft.liquid_coralium")
            .canConvertToSource(true).canSwim(true).canDrown(true)
            .lightLevel(15).density(1000).viscosity(1000)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override public ResourceLocation getStillTexture() { return STILL; }
                    @Override public ResourceLocation getFlowingTexture() { return FLOWING_TEXTURE; }
                });
            }
        };
    }

    private static Fluid createFluid(boolean source) {
        //? if forge {
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(TYPE, SOURCE, FLOWING)
            .block(BLOCK).bucket(BUCKET).slopeFindDistance(4).levelDecreasePerBlock(1)
            .explosionResistance(500.0F).tickRate(5);
        return source ? new ForgeFlowingFluid.Source(properties) : new ForgeFlowingFluid.Flowing(properties);
        //?} else {
        /*BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING)
            .block(BLOCK).bucket(BUCKET).slopeFindDistance(4).levelDecreasePerBlock(1)
            .explosionResistance(500.0F).tickRate(5);
        return source ? new BaseFlowingFluid.Source(properties) : new BaseFlowingFluid.Flowing(properties);
        *///?}
    }

    public static void attach(IEventBus modBus) {
        //? if >=1.21 {
        /*modBus.addListener((RegisterCapabilitiesEvent event) ->
            event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, ignored) -> new FluidBucketWrapper(stack), BUCKET.get()));
        *///?}
    }
}
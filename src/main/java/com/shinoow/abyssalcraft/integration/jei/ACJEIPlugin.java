package com.shinoow.abyssalcraft.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.content.recipe.anvil.AnvilForgingRecipe;
import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

/**
 * JEI plugin (owned by PP-5) -- pilot integration for the three processing machines.
 *
 * <p>Auto-discovered by JEI via {@link JeiPlugin} (never listed in mods.toml), so when JEI is absent
 * this class is simply never loaded and the mod carries no runtime dependency on it -- nothing outside
 * this package references it. Registers one recipe category + catalyst per machine (crystallizer /
 * materializer / transmutator). The JEI API used here has identical signatures on
 * the Forge-1.20.1 (JEI 15) and NeoForge-1.21.1 (JEI 19) artifacts (verified by javap), so this
 * integration is loader-fork-free; recipe enumeration is absorbed by {@link DataRecipeCompat}.
 */
@JeiPlugin
public class ACJEIPlugin implements IModPlugin {

    /** One machine's JEI wiring: JEI category type, the MC recipe type to enumerate, icon, title key. */
    private static final RecipeType<CrystallizationRecipe> CRYSTALLIZATION_JEI =
        RecipeType.create(AbyssalCraft.MODID, "crystallization", CrystallizationRecipe.class);
    private static final RecipeType<MaterializationRecipe> MATERIALIZATION_JEI =
        RecipeType.create(AbyssalCraft.MODID, "materialization", MaterializationRecipe.class);
    private static final RecipeType<TransmutationRecipe> TRANSMUTATION_JEI =
        RecipeType.create(AbyssalCraft.MODID, "transmutation", TransmutationRecipe.class);

    /** Anvil forging (PJ-1): a distinct recipe class ({@link AnvilForgingRecipe}), so its own JEI type + category. */
    private static final RecipeType<AnvilForgingRecipe> ANVIL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "anvil_forging", AnvilForgingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ACRef.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        IGuiHelper gui = reg.getJeiHelpers().getGuiHelper();
        reg.addRecipeCategories(new CrystallizationCategory(gui, CRYSTALLIZATION_JEI,
            Component.translatable("container.abyssalcraft.crystallizer"), new ItemStack(Crystallizers.CRYSTALLIZER.get())));
        reg.addRecipeCategories(new MaterializationCategory(gui, MATERIALIZATION_JEI,
            Component.translatable("container.abyssalcraft.materializer"), new ItemStack(Materializers.MATERIALIZER.get())));
        reg.addRecipeCategories(new TransmutationCategory(gui, TRANSMUTATION_JEI,
            Component.translatable("container.abyssalcraft.transmutator"), new ItemStack(Transmutators.TRANSMUTATOR.get())));
        reg.addRecipeCategories(new AnvilForgingCategory(gui, ANVIL_JEI));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return; // recipes are synced from the server; nothing to show until a world is joined
        }
        reg.addRecipes(CRYSTALLIZATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.CRYSTALLIZATION.get()));
        reg.addRecipes(MATERIALIZATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.MATERIALIZATION.get()));
        reg.addRecipes(TRANSMUTATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.TRANSMUTATION.get()));
        reg.addRecipes(ANVIL_JEI, DataRecipeCompat.allOfType(level, ModRecipes.ANVIL_FORGING.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Crystallizers.CRYSTALLIZER.get()), CRYSTALLIZATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(Materializers.MATERIALIZER.get()), MATERIALIZATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(Transmutators.TRANSMUTATOR.get()), TRANSMUTATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(net.minecraft.world.item.Items.ANVIL), ANVIL_JEI);
    }
}

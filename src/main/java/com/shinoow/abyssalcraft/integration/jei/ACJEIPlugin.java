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
import mezz.jei.api.registration.IRecipeTransferRegistration;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.content.machine.crystallizer.CrystallizerMenu;
import com.shinoow.abyssalcraft.content.machine.materializer.Materializers;
import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestals;
import com.shinoow.abyssalcraft.content.machine.transmutator.TransmutatorMenu;
import com.shinoow.abyssalcraft.content.machine.transmutator.Transmutators;
import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.recipe.anvil.AnvilForgingRecipe;
import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;

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
    private static final RecipeType<FuelRecipe> CRYSTALLIZER_FUEL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "crystallizer_fuel", FuelRecipe.class);
    private static final RecipeType<FuelRecipe> TRANSMUTATOR_FUEL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "transmutator_fuel", FuelRecipe.class);
    private static final RecipeType<RendingRecipe> RENDING_JEI =
        RecipeType.create(AbyssalCraft.MODID, "rending", RendingRecipe.class);
    private static final RecipeType<RitualManifest> INFUSION_RITUAL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "infusion_ritual", RitualManifest.class);
    private static final RecipeType<RitualManifest> RITUAL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "ritual", RitualManifest.class);
    private static final RecipeType<RitualManifest> CREATION_RITUAL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "creation_ritual", RitualManifest.class);
    private static final RecipeType<RitualManifest> TRANSFORMATION_RITUAL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "transformation_ritual", RitualManifest.class);
    private static final RecipeType<SpellManifest> SPELL_JEI =
        RecipeType.create(AbyssalCraft.MODID, "spell", SpellManifest.class);

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
        reg.addRecipeCategories(new CrystallizerFuelCategory(gui, CRYSTALLIZER_FUEL_JEI));
        reg.addRecipeCategories(new TransmutatorFuelCategory(gui, TRANSMUTATOR_FUEL_JEI));
        reg.addRecipeCategories(new RendingCategory(gui, RENDING_JEI));
        reg.addRecipeCategories(new InfusionRitualCategory(gui, INFUSION_RITUAL_JEI));
        reg.addRecipeCategories(new RitualCategory(gui, RITUAL_JEI));
        reg.addRecipeCategories(new CreationRitualCategory(gui, CREATION_RITUAL_JEI));
        reg.addRecipeCategories(new TransformationRitualCategory(gui, TRANSFORMATION_RITUAL_JEI));
        reg.addRecipeCategories(new SpellCategory(gui, SPELL_JEI));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        requireAudit(JEIAuditGate.runFullAudit());
        reg.addRecipes(CRYSTALLIZER_FUEL_JEI, CrystallizerFuelCategory.getAllFuels());
        reg.addRecipes(TRANSMUTATOR_FUEL_JEI, TransmutatorFuelCategory.getAllFuels());
        reg.addRecipes(INFUSION_RITUAL_JEI, InfusionRitualCategory.getInfusionRituals());
        reg.addRecipes(RITUAL_JEI, RitualCategory.getRituals());
        reg.addRecipes(CREATION_RITUAL_JEI, CreationRitualCategory.getCreationRituals());
        reg.addRecipes(TRANSFORMATION_RITUAL_JEI, TransformationRitualCategory.getTransformationRituals());
        reg.addRecipes(SPELL_JEI, SpellCategory.getValuableSpells());
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return; // recipes are synced from the server; nothing to show until a world is joined
        }
        requireAudit(JEIAuditGate.runFullAudit(level));
        reg.addRecipes(CRYSTALLIZATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.CRYSTALLIZATION.get()));
        reg.addRecipes(MATERIALIZATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.MATERIALIZATION.get()));
        reg.addRecipes(TRANSMUTATION_JEI, DataRecipeCompat.allOfType(level, ModRecipes.TRANSMUTATION.get()));
        reg.addRecipes(ANVIL_JEI, DataRecipeCompat.allOfType(level, ModRecipes.ANVIL_FORGING.get()));
        reg.addRecipes(RENDING_JEI, DataRecipeCompat.allOfType(level, ModRecipes.RENDING.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Crystallizers.CRYSTALLIZER.get()), CRYSTALLIZATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(Materializers.MATERIALIZER.get()), MATERIALIZATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(Transmutators.TRANSMUTATOR.get()), TRANSMUTATION_JEI);
        reg.addRecipeCatalyst(new ItemStack(net.minecraft.world.item.Items.ANVIL), ANVIL_JEI);
        reg.addRecipeCatalyst(new ItemStack(Crystallizers.CRYSTALLIZER.get()), CRYSTALLIZER_FUEL_JEI);
        reg.addRecipeCatalyst(new ItemStack(Transmutators.TRANSMUTATOR.get()), TRANSMUTATOR_FUEL_JEI);
        reg.addRecipeCatalyst(new ItemStack(RendingPedestals.RENDING_PEDESTAL_ITEM.get()), RENDING_JEI);
        reg.addRecipeCatalyst(new ItemStack(RitualItems.STAFF_OF_RENDING.get()), RENDING_JEI);
        reg.addRecipeCatalyst(new ItemStack(RitualBlocks.RITUAL_ALTAR_ITEM.get()),
            INFUSION_RITUAL_JEI, RITUAL_JEI, CREATION_RITUAL_JEI, TRANSFORMATION_RITUAL_JEI);
        for (var book : BookItems.ALL) {
            reg.addRecipeCatalyst(new ItemStack(book.get()), SPELL_JEI);
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration reg) {
        JEIAutomationCatalog.TransferDecision crystallization = transfer("crystallization");
        reg.addRecipeTransferHandler(CrystallizerMenu.class, Crystallizers.CRYSTALLIZER_MENU.get(),
            CRYSTALLIZATION_JEI, crystallization.recipeSlotStart(), crystallization.recipeSlotCount(),
            crystallization.playerSlotStart(), crystallization.playerSlotCount());
        JEIAutomationCatalog.TransferDecision transmutation = transfer("transmutation");
        reg.addRecipeTransferHandler(TransmutatorMenu.class, Transmutators.TRANSMUTATOR_MENU.get(),
            TRANSMUTATION_JEI, transmutation.recipeSlotStart(), transmutation.recipeSlotCount(),
            transmutation.playerSlotStart(), transmutation.playerSlotCount());
    }

    private static JEIAutomationCatalog.TransferDecision transfer(String category) {
        JEIAutomationCatalog.TransferDecision decision = JEIAutomationCatalog.transfers().get(category);
        if (decision == null || decision.status() != JEIAutomationCatalog.Status.SUPPORTED) {
            throw new IllegalStateException("JEI transfer is not supported for " + category);
        }
        return decision;
    }

    private static void requireAudit(JEIAuditGate.AuditResult audit) {
        if (!audit.passed()) {
            throw new IllegalStateException("JEI audit failed: " + String.join("; ", audit.errors()));
        }
    }
}

package com.shinoow.abyssalcraft.integration.jei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.spell.SpellIngredient;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;

/**
 * JEI category for valuable Necronomicon spells (RR-JEI-AUTO / TP.5b / T8.1b).
 * Shows spell inscription reagents, PE cost, and scroll type for selected useful spells.
 *
 * <p>Recipe data comes from {@link SpellManifestCatalog}, the immutable authoritative source.
 * Only displays spells confirmed as valuable by design review (combat, utility, teleport).
 */
public final class SpellCategory implements IRecipeCategory<SpellManifest> {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 80;

    private final RecipeType<SpellManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;

    public SpellCategory(IGuiHelper gui, RecipeType<SpellManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.spell");
        // Use necronomicon as icon
        this.icon = gui.createDrawable(ACRef.id("textures/item/necronomicon.png"), 0, 0, 16, 16);
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<SpellManifest> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpellManifest spell, IFocusGroup focuses) {
        // Display reagents in a row (up to 5 slots)
        List<SpellIngredient> reagents = spell.reagentLayout();
        for (int i = 0; i < reagents.size() && i < 5; i++) {
            SpellIngredient reagent = reagents.get(i);
            if (!reagent.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, 2 + i * 18, 40)
                    .setBackground(slot, -1, -1)
                    .addItemStack(reagent.example());
            }
        }

        // Display scroll result on right
        String scrollItemId = switch (spell.scrollType()) {
            case NONE, UNIQUE -> null;
            case BASIC -> "abyssalcraft:basic_scroll";
            case LESSER -> "abyssalcraft:lesser_scroll";
            case MODERATE -> "abyssalcraft:moderate_scroll";
            case GREATER -> "abyssalcraft:greater_scroll";
        };
        if (scrollItemId != null) {
            ItemStack scrollStack = new ItemStack(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.parse(scrollItemId)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 40).setBackground(slot, -1, -1)
                .addItemStack(scrollStack);
        }
    }

    @Override
    public void draw(SpellManifest spell, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Display spell name
        Component nameText = Component.translatable("spell.abyssalcraft." + spell.id());
        graphics.drawString(Minecraft.getInstance().font, nameText, 2, 2, 0x404040, false);

        // Display PE cost
        Component peText = Component.translatable("jei.abyssalcraft.spell_energy",
            (int)spell.requiredEnergy());
        graphics.drawString(Minecraft.getInstance().font, peText, 2, 14, 0x808080, false);

        // Display target type
        String targetKey = "jei.abyssalcraft.spell_target." + spell.targetType().name().toLowerCase();
        Component targetText = Component.translatable(targetKey);
        graphics.drawString(Minecraft.getInstance().font, targetText, 2, 26, 0x606060, false);

        // Display scroll type
        Component scrollText = Component.translatable("jei.abyssalcraft.scroll_type",
            spell.scrollType().name().toLowerCase());
        graphics.drawString(Minecraft.getInstance().font, scrollText, 2, 68, 0x606060, false);
    }

    /** Get every spell from the authoritative manifest catalog. */
    public static List<SpellManifest> getValuableSpells() {
        return SpellManifestCatalog.entries();
    }
}

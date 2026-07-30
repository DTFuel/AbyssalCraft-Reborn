package com.shinoow.abyssalcraft.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;

import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import net.minecraft.world.item.ItemStack;

final class RitualJeiLayout {

    static final int WIDTH = 166;
    static final int HEIGHT = 178;
    static final int CENTER_X = 72;
    static final int CENTER_Y = 41;
    static final int OUTPUT_X = 72;
    static final int OUTPUT_Y = 114;
    static final int TRANSFORMATION_INPUT_X = 20;
    static final int TRANSFORMATION_INPUT_Y = 114;
    static final int BOOK_X = 14;
    static final int BOOK_Y = 108;
    static final int TRANSFORMATION_BOOK_Y = 87;
    static final int FOOTER_Y = 142;
    static final int FOOTER_SECOND_Y = 154;
    static final int FOOTER_THIRD_Y = 166;
    private static final int SLOT_SIZE = 16;
    private static final int[][] PEDESTALS = {
        {72, 5}, {98, 15}, {108, 41}, {98, 67},
        {72, 78}, {46, 67}, {36, 41}, {46, 15}
    };

    private RitualJeiLayout() {}

    static void addOfferings(IRecipeLayoutBuilder builder, List<RitualIngredient> offerings,
                             IDrawable slot) {
        for (int index = 0; index < offerings.size() && index < PEDESTALS.length; index++) {
            RitualIngredient ingredient = offerings.get(index);
            if (ingredient.isEmpty()) continue;
            int[] position = PEDESTALS[index];
            builder.addSlot(RecipeIngredientRole.INPUT, position[0], position[1])
                .setBackground(slot, -1, -1).addItemStack(ingredient.example());
        }
    }

    static void addBook(IRecipeLayoutBuilder builder, int bookType, IDrawable slot) {
        addBook(builder, bookType, slot, BOOK_Y);
    }

    static void addTransformationBook(IRecipeLayoutBuilder builder, int bookType, IDrawable slot) {
        addBook(builder, bookType, slot, TRANSFORMATION_BOOK_Y);
    }

    private static void addBook(IRecipeLayoutBuilder builder, int bookType, IDrawable slot, int y) {
        int index = Math.max(0, Math.min(BookItems.ALL.size() - 1, bookType));
        builder.addSlot(RecipeIngredientRole.CATALYST, BOOK_X, y)
            .setBackground(slot, -1, -1).addItemStack(new ItemStack(BookItems.ALL.get(index).get()));
    }

    static List<String> audit() {
        List<String> errors = new ArrayList<>();
        List<SlotRect> slots = new ArrayList<>();
        for (int index = 0; index < PEDESTALS.length; index++) {
            slots.add(new SlotRect("pedestal_" + index, PEDESTALS[index][0], PEDESTALS[index][1]));
        }
        slots.add(new SlotRect("center", CENTER_X, CENTER_Y));
        slots.add(new SlotRect("output", OUTPUT_X, OUTPUT_Y));
        slots.add(new SlotRect("transformation_input", TRANSFORMATION_INPUT_X, TRANSFORMATION_INPUT_Y));
        slots.add(new SlotRect("book", BOOK_X, TRANSFORMATION_BOOK_Y));
        for (SlotRect slot : slots) {
            if (slot.x < 0 || slot.y < 0 || slot.x + SLOT_SIZE > WIDTH
                || slot.y + SLOT_SIZE > 140) {
                errors.add("ritual JEI slot out of bounds: " + slot.name);
            }
        }
        for (int left = 0; left < slots.size(); left++) {
            for (int right = left + 1; right < slots.size(); right++) {
                if (slots.get(left).intersects(slots.get(right))) {
                    errors.add("ritual JEI slots overlap: " + slots.get(left).name
                        + "/" + slots.get(right).name);
                }
            }
        }
        return List.copyOf(errors);
    }

    private record SlotRect(String name, int x, int y) {
        private boolean intersects(SlotRect other) {
            return x < other.x + SLOT_SIZE && x + SLOT_SIZE > other.x
                && y < other.y + SLOT_SIZE && y + SLOT_SIZE > other.y;
        }
    }
}
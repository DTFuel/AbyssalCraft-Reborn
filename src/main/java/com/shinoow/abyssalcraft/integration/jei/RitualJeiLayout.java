package com.shinoow.abyssalcraft.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;

import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;

final class RitualJeiLayout {

    static final int WIDTH = 176;
    static final int HEIGHT = 128;
    static final int CENTER_X = 66;
    static final int CENTER_Y = 62;
    static final int OUTPUT_X = 148;
    static final int OUTPUT_Y = 66;
    static final int FOOTER_Y = 116;
    private static final int SLOT_SIZE = 16;
    private static final int[][] PEDESTALS = {
        {34, 48}, {66, 38}, {98, 48}, {108, 66},
        {98, 84}, {66, 94}, {34, 84}, {24, 66}
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

    static List<String> audit() {
        List<String> errors = new ArrayList<>();
        List<SlotRect> slots = new ArrayList<>();
        for (int index = 0; index < PEDESTALS.length; index++) {
            slots.add(new SlotRect("pedestal_" + index, PEDESTALS[index][0], PEDESTALS[index][1]));
        }
        slots.add(new SlotRect("center", CENTER_X, CENTER_Y));
        slots.add(new SlotRect("output", OUTPUT_X, OUTPUT_Y));
        for (SlotRect slot : slots) {
            if (slot.x < 0 || slot.y < 36 || slot.x + SLOT_SIZE > WIDTH
                || slot.y + SLOT_SIZE > FOOTER_Y) {
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
package com.shinoow.abyssalcraft.integration.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;

import com.shinoow.abyssalcraft.platform.ACRef;

final class LegacyJeiBackgrounds {

    private LegacyJeiBackgrounds() {}

    static IDrawable crystallization(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/crystallizer_nei.png"),
            55, 16, 116, 54);
    }

    static IDrawable crystallizerFuel(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/crystallizer_nei.png"),
            55, 38, 18, 32);
    }

    static IDrawable materialization(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/materializer_nei.png"),
            32, 47, 116, 72);
    }

    static IDrawable transmutation(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/transmutator_nei.png"),
            55, 16, 82, 54);
    }

    static IDrawable transmutatorFuel(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/transmutator_nei.png"),
            55, 38, 18, 32);
    }

    static IDrawable ritual(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/ritual_nei.png"),
            5, 11, 166, 140);
    }

    static IDrawable transformationRitual(IGuiHelper gui) {
        return gui.createDrawable(ACRef.id("textures/gui/container/transformation_ritual_jei.png"),
            5, 11, 166, 140);
    }
}